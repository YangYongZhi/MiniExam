package com.maiyoule.miniexam.control.admin.exam;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.entity.ExamlayoutStatus;
import com.maiyoule.miniexam.entity.OnlineBack;
import com.maiyoule.miniexam.entity.PaperExamCounter;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.LayoutsModel;
import com.maiyoule.miniexam.model.PapersModel;
import com.maiyoule.miniexam.model.PapersUserTypeModel;
import com.maiyoule.miniexam.services.LoadUserService;
import com.maiyoule.miniexam.utils.ExamlayoutUtil;
import com.maiyoule.miniexam.utils.StringHelper;

public class OnlineController extends C {

    private static final Logger LOGGER = Logger.getLogger(LoadUserService.class);

    /**
     * 在线状态
     */
    public void index() {
        StringBuffer queryString = new StringBuffer();
        int status = this.getParaToInt("status", -1);
        this.setAttr("status", status);
        queryString.append("status=" + status);
        //考试状态
        int layoutstatus = this.getParaToInt("layoutstatus", -1);
        this.setAttr("layoutstatus", layoutstatus);
        queryString.append("&layoutstatus=" + layoutstatus);

        //修改BUG8，layout_id取请求里的examtime参数
        int layout_id = this.getParaToInt("layout_id", 0);
        this.setAttr("layout_id", layout_id);
        queryString.append("&layout_id=" + layout_id);

        String cardno = this.getPara("cardno", "");
        this.setAttr("cardno", cardno);
        queryString.append("&cardno=" + cardno);

        this.setAttr("queryString", queryString.toString());

        int pageNum = this.getParaToInt("page", 1);
        if (pageNum < 1) {
            pageNum = 1;
        }

        //获取考试安排
        List<LayoutsModel> layouts = LayoutsModel.dao.find("select * from layouts where end_time>="
                + System.currentTimeMillis() + " order by start_time desc");
        this.setAttr("layouts", layouts);
        if (GUIConstants.APP_MODEL == 1) {
            //如果为单机版，默认转化时间类型为GMT，需要在用时间戳转化时间类型时加8小时
            for (Iterator<LayoutsModel> iterator = layouts.iterator(); iterator.hasNext();) {
                LayoutsModel layoutsModel = iterator.next();
                LOGGER.debug(layoutsModel.getLong("start_time"));
                layoutsModel.set("start_time", layoutsModel.getLong("start_time")
                        + DateUtils.MILLIS_PER_HOUR * 8);
                layoutsModel.set("end_time", layoutsModel.getLong("end_time")
                        + DateUtils.MILLIS_PER_HOUR * 8);
                LOGGER.debug(new Date(layoutsModel.getLong("start_time")));
            }
        } else {
            //如果为服务器版本，默认转化时间类型为CST，不用处理
            // ignore
        }

        //查找用户
        StringBuffer condition = new StringBuffer();
        if (status > 0) {
            if (condition.length() > 0) {
                condition.append(" and ");
            }
            condition.append("user_status=" + status);
        }
        if (layoutstatus > 0) {
            if (condition.length() > 0) {
                condition.append(" and ");
            }
            condition.append("layouts_status=" + layoutstatus);
        }
        if (layout_id > 0) {
            if (condition.length() > 0) {
                condition.append(" and ");
            }
            condition.append("layout_id=" + layout_id);
        } else {
            if (condition.length() > 0) {
                condition.append(" and ");
            }
            condition.append("layout_id<>0");
        }

        if (!StringHelper.isNullOrEmpty(cardno)) {
            if (condition.length() > 0) {
                condition.append(" and ");
            }
            condition.append("cardno='" + cardno + "'");
        }
        if (condition.length() > 0) {
            condition.append(" and ");
        }
        condition.append("end_time>" + System.currentTimeMillis());

        Page<ExamsModel> lists = ExamsModel.dao.paginate(pageNum, GUIConstants.PAGE_SIZE,
                "select *",
                "from exams" + (condition.length() > 0 ? " where " + condition.toString() : "")
                        + " order by id desc");

        this.setAttr("lists", lists);

        this.render("/contral/online/index.html");
    }

    /**
     * 查询状态
     */
    public String query() {
        String[] ids = this.getParaValues("ids");
        if (ids == null || ids.length < 1) {
            return this.ajaxMessage("无数据", "请求");
        }

        long curindex = System.currentTimeMillis();
        curindex -= 300000;

        //更新异常数据
        String sql = "update exams set user_status=" + ExamlayoutUtil.USER_ERROR
                + " where layouts_status=" + ExamlayoutUtil.LAYOUT_DOING + " and end_time>"
                + System.currentTimeMillis() + " and next_time<" + curindex + " and next_time<>0";
        Db.update(sql);

        List<ExamlayoutStatus> status = ExamsModel.dao.findByIds(ids);

        return this.ajaxMessage(status, "成功", true);
    }

    /**
     * 关闭考试
     */
    public void closeAll() {

    }

    /**
     * 关闭指定考试
     */
    public String closeexam() {
        int id = this.getParaToInt("id", 0);
        if (id < 1) {
            return this.ajaxMessage("无效的操作", "操作");
        }
        ExamsModel exam = ExamsModel.dao.fCacheById(id);
        if (exam == null) {
            return this.ajaxMessage("无效的操作", "操作");
        }

        int ustatus = exam.getInt("user_status");
        exam.set("layouts_status", ExamlayoutUtil.LAYOUT_CLOSE);
        if (ustatus == 1) {
            exam.set("user_status", ExamlayoutUtil.USER_OFFLINE);
            exam.set("exit_time", System.currentTimeMillis());
        }

        if (!exam.update()) {
            return this.ajaxMessage("关闭失败", "失败");
        }

        //清除考试信息
        CacheKit.remove("exams", "exams_" + id);
        CacheKit.remove("exams", "answer_" + id);
        CacheKit.remove("exam", "moveuser_" + id);

        return this.ajaxMessage(new OnlineBack(ExamlayoutUtil.LAYOUT_CLOSE,
                ExamlayoutUtil.USER_OFFLINE, id), "成功", true);

    }

    /**
     * 下线操作
     */
    public String offline() {
        int id = this.getParaToInt("id", 0);
        if (id < 1) {
            return this.ajaxMessage("无效的操作", "操作");
        }
        ExamsModel exam = ExamsModel.dao.fCacheById(id);
        if (exam == null) {
            return this.ajaxMessage("无效的操作", "操作");
        }
        exam.set("layouts_status", ExamlayoutUtil.LAYOUT_EMPTY);
        exam.set("user_status", ExamlayoutUtil.USER_OFFLINE);
        exam.set("next_time", 0);//清除下次提交时间
        if (exam.update()) {
            //清除缓存信息
            CacheKit.remove("exams", "exams_" + id);
            CacheKit.remove("exams", "answer_" + id);
            return this.ajaxMessage(new OnlineBack(ExamlayoutUtil.LAYOUT_EMPTY,
                    ExamlayoutUtil.USER_OFFLINE, id), "成功", true);
        } else {
            return this.ajaxMessage("操作失败", "失败");
        }
    }

    /**
     * 转移
     */
    public String move() {
        int id = this.getParaToInt("id", 0);
        if (id < 1) {
            return this.ajaxMessage("无效的操作", "操作");
        }
        ExamsModel exam = ExamsModel.dao.fCacheById(id);
        if (exam == null) {
            return this.ajaxMessage("无效的操作", "操作");
        }
        int ustatus = exam.getInt("user_status");
        exam.set("layouts_status", ExamlayoutUtil.LAYOUT_MOVE);
        if (exam.update()) {
            //停止计时
            CacheKit.put("exam", "moveuser_" + id, System.currentTimeMillis());
            CacheKit.remove("exams", "exams_" + id);
            return this.ajaxMessage(new OnlineBack(ExamlayoutUtil.LAYOUT_MOVE, ustatus, id), "成功",
                    true);
        } else {
            return this.ajaxMessage("转移失败", "失败");
        }
    }

    /**
     * 在线统计
     */
    public void viewcount() {

        //获取考试安排
        List<LayoutsModel> layouts = LayoutsModel.dao.find("select * from layouts where end_time>="
                + System.currentTimeMillis() + " order by start_time desc");
        this.setAttr("layouts", layouts);

        int layoutid = this.getParaToInt("layoutid", 0);

        this.setAttr("layoutid", layoutid);

        List<PaperExamCounter> counters = new ArrayList<PaperExamCounter>();
        //获取试卷
        List<PapersUserTypeModel> utypes = PapersUserTypeModel.dao
                .find("select put.paper_id,group_concat(put.user_type_id) as utype from papers_user_type as put group by put.paper_id");
        for (PapersUserTypeModel put : utypes) {
            int paperid = put.getInt("paper_id");
            String utypeids = put.getStr("utype");
            PapersModel paper = PapersModel.dao.findById(paperid);

            PaperExamCounter c = new PaperExamCounter();
            c.setPaperId(paperid);
            c.setPaperName(paper.getStr("name"));

            //统计个数

            c.setClose(ExamsModel.dao.getCountExam(layoutid, ExamlayoutUtil.LAYOUT_CLOSE, utypeids));
            c.setDoing(ExamsModel.dao.getCountExam(layoutid, ExamlayoutUtil.LAYOUT_DOING, utypeids));
            c.setEmptycount(ExamsModel.dao.getCountExam(layoutid, ExamlayoutUtil.LAYOUT_EMPTY,
                    utypeids));
            c.setFlish(ExamsModel.dao.getCountExam(layoutid, ExamlayoutUtil.LAYOUT_FLISH, utypeids));
            c.setMove(ExamsModel.dao.getCountExam(layoutid, ExamlayoutUtil.LAYOUT_MOVE, utypeids));
            counters.add(c);
        }
        this.setAttr("counters", counters);

        this.render("/contral/online/viewcount.html");

    }

}
