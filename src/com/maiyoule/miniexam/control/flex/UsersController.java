package com.maiyoule.miniexam.control.flex;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.entity.BankInfo;
import com.maiyoule.miniexam.entity.ExamConfig;
import com.maiyoule.miniexam.entity.ExamInfo;
import com.maiyoule.miniexam.entity.ExamPaper;
import com.maiyoule.miniexam.entity.ExamUserInfo;
import com.maiyoule.miniexam.entity.Paper;
import com.maiyoule.miniexam.model.BanksModel;
import com.maiyoule.miniexam.model.ConfigModel;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.PapersModel;
import com.maiyoule.miniexam.model.UsersModel;
import com.maiyoule.miniexam.utils.ExamlayoutUtil;
import com.maiyoule.miniexam.utils.StringHelper;
import com.maiyoule.miniexam.utils.TimeConvert;

public class UsersController extends C {

	public String login() {
		String cardno = this.getPara("username", null);
		if (StringHelper.isNullOrEmpty(cardno)) {
			return this.ajaxMessage("请输入正确的准考证号", "准考证");
		}
		String uname = this.getPara("password", null);
		if (StringHelper.isNullOrEmpty(uname)) {
			return this.ajaxMessage("请输入正确的姓名", "姓名");
		}
		UsersModel userModel = UsersModel.dao.findByCardNo(cardno);
		if (userModel == null) {
			return this.ajaxMessage("不能找到用户", "信息");
		}
		if (!(userModel.getStr("name").replaceAll(" ", "")).equals(uname.replaceAll(" ", ""))) {
			return this.ajaxMessage("用户姓名不匹配", "信息");
		}
		// 获取考试信息
		long nowTime = System.currentTimeMillis();
		List<ExamsModel> exams = ExamsModel.dao.find(
		        "select * from exams where cardno=? and start_time<=? and end_time>?", cardno, nowTime, nowTime);
		if (exams == null || exams.size() == 0) {
			return this.ajaxMessage("当前没有您的考试安排或者考试还未开始，请留意您的考试时间，若有问题请联系管理员。", "没有考试");
		}

		long endTime = exams.get(0).getLong("end_time");

		long tmp = (endTime - nowTime) / 1000;
		int interval = (int) tmp;

		ExamsModel userlayout = null;
		for (int i = 0; i < exams.size(); i++) {
			ExamsModel ex = exams.get(i);
			int userstatus = ex.getInt("user_status");
			int layoutstatus = ex.getInt("layouts_status");
			if (userstatus == ExamlayoutUtil.USER_ONLINE && layoutstatus != ExamlayoutUtil.LAYOUT_MOVE) {
				return this.ajaxMessage("当前帐号已登录，若有问题请联系管理员。", "已登录");
			}
			if (userstatus == ExamlayoutUtil.USER_ERROR && layoutstatus != ExamlayoutUtil.LAYOUT_MOVE) {
				return this.ajaxMessage("当前帐号处于异常状态，请联系管理员", "异常");
			}
			if (layoutstatus == ExamlayoutUtil.LAYOUT_FLISH) {
				continue;
			}

			if (layoutstatus == ExamlayoutUtil.LAYOUT_CLOSE) {
				if (i >= exams.size()) {
					return this.ajaxMessage("您的考试已经关闭，无法进行此场考试", "已关闭");
				}
				continue;
			}
			if (layoutstatus == ExamlayoutUtil.LAYOUT_MOVE) {
				ex.set("layouts_status", ExamlayoutUtil.LAYOUT_EMPTY);
				userlayout = ex;
				break;
			}
			// 没有进行考试，则开始该场考试
			if (layoutstatus == ExamlayoutUtil.LAYOUT_EMPTY) {
				userlayout = ex;
				break;
			}

		}
		if (userlayout == null) {
			return this.ajaxMessage("当前没有您的考试安排或者考试还未开始，请留意您的考试时间，若有问题请联系管理员。", "没有考试");
		}
		UsersModel user = UsersModel.dao.findByCardNo(userlayout.getStr("cardno"));
		BankInfo banks = BanksModel.dao.getByNo(userlayout.getStr("bankno"));

		// 获取试卷
		Paper paper = PapersModel.dao.findPaperByCardno(cardno);
		if (paper == null) {
			return this.ajaxMessage("没有找到相关的考试试题，请联系管理员", "试卷");
		}

		// 组装考试信息
		ExamInfo examinfo = new ExamInfo();
		examinfo.setId(userlayout.getInt("id"));

		// 查找设置信息
		ExamConfig ecfg = new ExamConfig();
		// 考试未操作时 时长（分）
		ecfg.setActive_time_out(TimeConvert.MinuteToSecond(Integer.parseInt(ConfigModel.dao
		        .findByKey("examAutoEndSubmitTime"))));
		// 考试结束前提示（分）
		ecfg.setEnd_tips_time(TimeConvert.MinuteToSecond(Integer.parseInt(ConfigModel.dao.findByKey("examEndTipsTime"))));
		// 考试时长（分）
		int inter = TimeConvert.MinuteToSecond(Integer.parseInt(ConfigModel.dao.findByKey("examTime")));
		ecfg.setExam_time(inter < interval ? inter : interval);
		// 限制提交时间（分）
		ecfg.setLimit_submit(TimeConvert.MinuteToSecond(Integer.parseInt(ConfigModel.dao.findByKey("examSubmitTime"))));
		// 注意事项强制读取时间（秒）
		ecfg.setRead_time(Integer.parseInt(ConfigModel.dao.findByKey("examReadNotice")));
		examinfo.setExamconfig(ecfg);
		// 设置注意事项
		examinfo.setNotice(ConfigModel.dao.findByKey("examNotice").replaceAll("\n", ""));

		// 设置用户信息
		ExamUserInfo euf = new ExamUserInfo();
		euf.setId(user.getStr("cardno"));
		euf.setName(user.getStr("name"));
		euf.setUid(user.getInt("id"));
		// 从db2导入的用户数据很可能没有机构信息，但是需要判断，让其仍能考试
		if (banks != null && StringUtils.isNotBlank(banks.getName())) {
			euf.setGroup(banks.getName());
		} else {
			euf.setGroup(StringUtils.EMPTY);
		}
		examinfo.setUser(euf);

		Calendar now = Calendar.getInstance();
		List<ExamPaper> epapers = new ArrayList<ExamPaper>();

		ExamPaper ep = new ExamPaper();
		ep.setId(paper.getId());
		ep.setName(now.get(Calendar.YEAR) + "年" + paper.getName());
		ep.setStatus(true);
		epapers.add(ep);

		examinfo.setExampaper(epapers);

		userlayout.set("enter_time", System.currentTimeMillis());
		userlayout.set("user_status", ExamlayoutUtil.USER_ONLINE);
		if (userlayout.update()) {

			return this.ajaxMessage(examinfo, "成功", true);
		} else {
			return this.ajaxMessage("登录失败", "失败");
		}
	}

	/**
	 * 退出
	 * 
	 * @return
	 */
	public String logout() {

		int id = this.getParaToInt("id", 0);
		if (id < 1) {
			return this.ajaxMessage("无效操作", "操作");
		}

		ExamsModel exam = ExamsModel.dao.findById(id);
		if (exam == null) {
			return this.ajaxMessage("当前没有您的考试安排或者考试还未开始，请留意您的考试时间，若有问题请联系管理员。", "没有考试");
		}
		exam.set("exit_time", System.currentTimeMillis());
		exam.set("user_status", ExamlayoutUtil.USER_OFFLINE);
		if (exam.update()) {
			return this.ajaxMessage("退出成功", "成功", true);
		} else {
			return this.ajaxMessage("退出失败", "失败");
		}
	}
}
