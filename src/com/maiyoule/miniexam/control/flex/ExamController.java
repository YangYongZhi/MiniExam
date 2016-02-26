package com.maiyoule.miniexam.control.flex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.entity.FlexAnswerItem;
import com.maiyoule.miniexam.entity.FlexQuestion;
import com.maiyoule.miniexam.entity.FlexScore;
import com.maiyoule.miniexam.entity.FlexSubmit;
import com.maiyoule.miniexam.entity.GenPaper;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.PapersModel;
import com.maiyoule.miniexam.services.PaperService;
import com.maiyoule.miniexam.utils.ExamResultCompare;
import com.maiyoule.miniexam.utils.ExamlayoutUtil;
import com.maiyoule.miniexam.utils.StringHelper;

public class ExamController extends C {
    private static final String CK = "exams";

    /**
     * 临时保存下一次间隔秒数范围.
     */
    private static final int CACHE_SAVE_MIN_SECOND = 10;
    private static final int CACHE_SAVE_MAX_SECOND = 59;

    /**
     * 拉取试卷
     * 
     * @return
     */
    public String pull() {
        int paperid = this.getParaToInt("paperid", 0);
        if (paperid < 1) {
            return this.ajaxMessage("请选择正确的试卷", "试卷");
        }

        int uid = this.getParaToInt("uid", 0);
        if (uid < 1) {
            return this.ajaxMessage("请登录", "未登录");
        }
        int layoutid = this.getParaToInt("lid", 0);

        ExamsModel exam = ExamsModel.dao.findById(layoutid);
        if (exam == null) {
            return this.ajaxMessage("您当前无法拉取试卷，请联系管理员", "试卷");
        }

        int layouts_status = exam.getInt("layouts_status");
        if (layouts_status == ExamlayoutUtil.LAYOUT_EMPTY
                || layouts_status == ExamlayoutUtil.LAYOUT_FLISH) {

        } else {
            return this.ajaxMessage("您当前无法拉取试卷，请联系管理员", "试卷");
        }

        GenPaper genpaper = CacheKit.get(CK, "paper_" + layoutid);
        if (genpaper == null) {
            PaperService paperService = new PaperService();
            genpaper = paperService.generatePaper(paperid);
            if (genpaper != null) {
                //试卷的正确答案，保存在缓存中，用于试卷判断
                List<FlexQuestion> questions = genpaper.getAn();
                CacheKit.put(CK, "paperanswer_" + layoutid, questions);
                //将试卷正确答案清空后，将试卷发放给用户
                genpaper.setAn(null);
                CacheKit.put(CK, "paper_" + layoutid, genpaper);
            }
        }
        if (genpaper == null) {
            return this.ajaxMessage("没有找到您的考试试卷，请联系管理员", "试卷");
        }
        String existsanswer = CacheKit.get(CK, "useranswer_" + layoutid);
        genpaper.setExistsanswer(existsanswer);

        exam.set("layouts_status", ExamlayoutUtil.LAYOUT_DOING);
        if (exam.update()) {
            return this.ajaxMessage(genpaper, "成功", true);
        } else {
            return this.ajaxMessage("拉取试卷失败", "试卷");
        }

    }

    /**
     * 考题缓存
     * 
     * @return
     */
    public String cacheSave() {
        int id = this.getParaToInt("lid", 0);
        if (id < 1) {
            return this.ajaxMessage("保存失败", "失败");
        }

        ExamsModel exam = ExamsModel.dao.findById(id);
        if (exam == null) {
            return this.ajaxMessage("保存失败", "失败");
        }
        String answer = this.getPara("result", null);
        if (answer != null) {
            CacheKit.put(CK, "useranswer_" + id, answer);
        }
        // 下次提交时间
        int nx = (int) Math.round(Math.random() * (CACHE_SAVE_MAX_SECOND - CACHE_SAVE_MIN_SECOND)
                + CACHE_SAVE_MIN_SECOND);
        //        int nx = (int) ((RandomUtils.nextDouble(new Random()) * 1 * 60));// 随机保存
        // 当前unix时间戳
        long nxtime = (nx * 1000) + System.currentTimeMillis();
        exam.set("next_time", nxtime);

        if (exam.update()) {
            return this.ajaxMessage(nx, "保存成功", true);
        } else {
            return this.ajaxMessage("保存失败", "失败");
        }

    }

    /**
     * 提交试卷
     * 
     * @return
     */
    public String submit() {
        int uid = this.getParaToInt("uid", 0);
        if (uid < 1) {
            return this.ajaxMessage("请登录后操作", "登录");
        }
        int layoutid = this.getParaToInt("layoutid", 0);
        if (layoutid < 1) {
            return this.ajaxMessage("请登录后操作", "登录");
        }
        ExamsModel exam = ExamsModel.dao.findById(layoutid);

        if (exam == null) {
            return this.ajaxMessage("当前没有您的考试安排或者考试还未开始，请留意您的考试时间，若有问题请联系管理员。", "考试");
        }
        int layoutstatus = exam.getInt("layouts_status");
        int userstatus = exam.getInt("user_status");
        if (userstatus == ExamlayoutUtil.USER_OFFLINE) {
            return this.ajaxMessage("您的帐号未登录或已被下线，请刷新后重新登录考试。", "考试");
        }
        if (layoutstatus == ExamlayoutUtil.LAYOUT_CLOSE) {
            return this.ajaxMessage("您的考试已经关闭，无法进行此场考试", "考试");
        }
        if (layoutstatus == ExamlayoutUtil.LAYOUT_FLISH) {
            return this.ajaxMessage("您已经完成了此场考试，不能重复提交", "考试");
        }
        //获取用户的试卷
        GenPaper genPaper = CacheKit.get(CK, "paper_" + layoutid);
        if (genPaper == null) {
            return this.ajaxMessage("无效的考试信息", "考试");
        }
        //试卷的正确答案
        List<FlexQuestion> paperanswer = CacheKit.get(CK, "paperanswer_" + layoutid);

        String result = this.getPara("result", null);
        if (StringHelper.isNullOrEmpty(result)) {
            return this.ajaxMessage("请提交正确的试卷", "试卷");
        }

        FlexSubmit fsubmit = JSON.parseObject(result, FlexSubmit.class);

        //z试卷
        PapersModel paper = PapersModel.dao.findById(genPaper.getId());

        ExamResultCompare erc = new ExamResultCompare();
        int score = erc.genResult(fsubmit.getAnswer(), paperanswer, paper.getInt("single_score"),
                paper.getInt("muti_score"), paper.getInt("juge_score"));

        Map<Integer, Short> resultlist = erc.getLastResultList();
        String label = "不合格";
        if (score >= paper.getInt("min_score")) {
            label = "合格";
        }

        FlexScore fs = new FlexScore();
        fs.setScore(score);
        fs.setLabel(label);

        exam.set("score", score);
        exam.set("layouts_status", ExamlayoutUtil.LAYOUT_FLISH);
        exam.set("user_status", ExamlayoutUtil.USER_OFFLINE);
        exam.set("exit_time", System.currentTimeMillis());

        exam.update();

        //保存作答情况
        List<String> answersqlbath = new ArrayList<String>();
        List<FlexAnswerItem> useranswers = fsubmit.getAnswer();
        int examlayout = exam.getInt("layout_id");
        for (FlexAnswerItem fai : useranswers) {
            StringBuffer sb = new StringBuffer();
            sb.append("insert into answers(exam_id,layout_id,question_id,status,label) values(");
            sb.append(layoutid);
            sb.append(",");
            sb.append(examlayout);
            sb.append(",");
            sb.append(fai.getId());
            sb.append(",");
            sb.append(resultlist.get(fai.getId()));
            sb.append(",");
            sb.append("'");
            sb.append(fai.getValue());//用户的回答
            sb.append("')");
            answersqlbath.add(sb.toString());

            if (answersqlbath.size() > 30) {
                Db.batch(answersqlbath, answersqlbath.size());
                answersqlbath.clear();
            }
        }
        if (answersqlbath.size() > 0) {
            Db.batch(answersqlbath, answersqlbath.size());
            answersqlbath.clear();
        }

        //清理缓存
        CacheKit.remove(CK, "paper_" + layoutid);
        CacheKit.remove(CK, "paperanswer_" + layoutid);
        CacheKit.remove(CK, "useranswer_" + layoutid);

        return this.ajaxMessage(fs, "提交成功", true);
    }
}
