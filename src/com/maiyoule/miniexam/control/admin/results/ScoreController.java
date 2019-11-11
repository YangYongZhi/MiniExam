package com.maiyoule.miniexam.control.admin.results;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.util.Base64;

import com.jfinal.plugin.activerecord.Page;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.model.AnswersModel;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.LayoutsModel;
import com.maiyoule.miniexam.model.QuestionModel;
import com.maiyoule.miniexam.utils.ExamlayoutUtil;
import com.maiyoule.miniexam.utils.StringHelper;

/**
 * 考试成绩
 * 
 * @author shengli
 *
 */
public class ScoreController extends C {

	public void index() {
		int layoutid = this.getParaToInt("layoutid", 0);
		String cardno = this.getPara("cardno", "");

		// 获取考试已经完成的安排
		List<LayoutsModel> layouts = LayoutsModel.dao.find("select * from layouts where start_time<"
		        + System.currentTimeMillis());

		this.setAttr("layouts", layouts);

		int pageNumber = this.getParaToInt("page", 1);

		Page<ExamsModel> lists = ExamsModel.dao.paginate(pageNumber, GUIConstants.PAGE_SIZE,
		        "select id,cardno,bankno,utype,layout_id,score",
		        "from exams where start_time<" + System.currentTimeMillis() + " and layouts_status="
		                + ExamlayoutUtil.LAYOUT_FLISH
		                + (StringHelper.isNullOrEmpty(cardno) ? "" : " and cardno = " + cardno)
		                + (layoutid == 0 ? " and layout_id != 0 " : " and layout_id = " + layoutid));

		this.setAttr("lists", lists);
		this.setAttr("layoutid", layoutid);
		this.setAttr("cardno", cardno);

		String history = "page=" + pageNumber + "&layoutid=" + layoutid + "&cardno=" + cardno;
		history = new String(Base64.encode(history.getBytes()));
		this.setAttr("history", history);
		this.setAttr("queryString", "layoutid=" + layoutid + "&cardno=" + cardno);
		this.render("/contral/results/index.html");
	}

	/**
	 * 编辑成绩
	 */
	public String edit() {
		int id = this.getParaToInt("id", 0);
		if (id < 1) {
			return this.error("无效请求");
		}
		ExamsModel model = ExamsModel.dao.findById(id);
		if (model == null) {
			return this.error("无效请求");
		}
		this.setAttr("exam", model);
		String history = this.getPara("history", "");
		this.setAttr("history", history);
		this.render("/contral/results/edit.html");
		return null;
	}

	public String update() {
		int score = this.getParaToInt("score", -1);
		if (score < 0) {
			return this.ajaxMessage("请输入成绩", "成绩");
		}

		int id = this.getParaToInt("id", 0);
		if (id < 1) {
			return this.ajaxMessage("无效请求", "请求");
		}

		ExamsModel model = ExamsModel.dao.findById(id);
		if (model == null) {
			return this.ajaxMessage("无效请求", "请求");
		}

		model.set("score", score);
		if (model.update()) {
			String history = this.getPara("history", "");
			if (!StringHelper.isNullOrEmpty(history)) {
				history = new String(Base64.decode(history.getBytes()));
			}
			return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral/results/score?"
			        + history, "成功", true);

		} else {
			return this.ajaxMessage("修改失败，请稍后再试", "失败");
		}
	}

	/**
	 * According to answer details to re calculate the score.
	 * 
	 * @return
	 */
	public String answer() {
		// judge();
		calculateScore();

		return this.ajaxMessage("data", "提交成功", true);
	}

	private void judge() {

		List<AnswersModel> am = AnswersModel.dao.find("select * from answers");

		for (Iterator<AnswersModel> iterator = am.iterator(); iterator.hasNext();) {
			AnswersModel a = iterator.next();
			QuestionModel q = QuestionModel.dao.findById(a.get("question_id"));

			if (q == null) {
				a.set("status", 0);
				a.save();
				continue;
			}

			/*
			 * 计算单选题得分
			 */
			if (q.get("type").equals(QuestionModel.TYPE_SINGLE)) {
				if (q.get("answer").equals(a.get("label"))) {
					// 答案正确
					// this.score += this.scoresingle;
					a.set("status", 1);
				} else {
					a.set("status", 0);
				}
			} else if (q.get("type").equals(QuestionModel.TYPE_MUTI)) {
				/*
				 * 计算多选题得分
				 */
				String[] useranswers = StringUtils.split(a.getStr("label"), ";");
				String[] trueanswers = StringUtils.split(q.getStr("answer"), GUIConstants.STRING_SPLITE);
				boolean isanswertrue = true;
				if (useranswers.length < trueanswers.length) {
					isanswertrue = false;
				} else {
					for (String tmpuseranswer : useranswers) {
						if (!ArrayUtils.contains(trueanswers, tmpuseranswer)) {
							isanswertrue = false;
							break;
						}
					}
				}
				if (isanswertrue) {
					a.set("status", 1);
				} else {
					a.set("status", 0);
				}
			} else if (q.get("type").equals(QuestionModel.TYPE_JUDGE)) {
				/*
				 * 计算判断题得分
				 */
				String userAnswer = a.getStr("label"); // 为空表示未作答，直接不计分
				if (StringHelper.isNullOrEmpty(userAnswer)) {
					// this.resultlist.put(fq.getId(), (short) 0);
				} else {
					String checkstr = userAnswer.equals("yes") ? "1" : "0";
					if (q.getStr("answer").equals(checkstr)) {
						a.set("status", 1);
					} else {
						a.set("status", 0);
					}
				}
			}

			System.out.println("calculating answer :" + a.get("id"));
			a.update();

		}

	}

	private void calculateScore() {
		List<ExamsModel> exams = ExamsModel.dao.find("select * from exams " + "where id = 20859");

		for (Iterator<ExamsModel> iterator = exams.iterator(); iterator.hasNext();) {
			ExamsModel exam = iterator.next();

			List<AnswersModel> answers = AnswersModel.dao.find("select * from answers where exam_id = "
			        + exam.getInt("id"));

			if (answers == null || answers.size() <= 0) {
				System.out.println("The anserw of this exam is null:" + exam.get("id"));
				continue;
			}

			int score = 0;
			for (Iterator<AnswersModel> iterator2 = answers.iterator(); iterator2.hasNext();) {
				AnswersModel a = iterator2.next();
				if (a.getInt("status") != null && a.getInt("status") == 1) {
					QuestionModel q = QuestionModel.dao.findById(a.get("question_id"));
					if (q.get("type").equals(QuestionModel.TYPE_SINGLE)) {
						score += 2;
					} else if (q.get("type").equals(QuestionModel.TYPE_MUTI)) {
						score += 2;
					} else if (q.get("type").equals(QuestionModel.TYPE_JUDGE)) {
						score += 1;
					}
				}
			}

			exam.set("score", score);
			System.out.println("calculating exam :" + exam.get("id"));
			exam.update();
		}
	}

	public String _answer() {
		int id = this.getParaToInt("id", 0);
		if (id < 1) {
			return this.error("无效请求");
		}

		int pageNum = this.getParaToInt("page", 1);

		Page<AnswersModel> answers = AnswersModel.dao.paginate(pageNum, GUIConstants.PAGE_SIZE, "select *",
		        "from answers where exam_id=" + id);

		// Fill the null status.
		List<AnswersModel> answersData = answers.getList();
		for (Iterator<AnswersModel> iterator = answersData.iterator(); iterator.hasNext();) {
			AnswersModel answersModel = iterator.next();
			if (answersModel.get("status") == null) {
				// If there is a null status of this answer, we can consider as
				// wrong.
				answersModel.set("status", 0);
			}
		}

		this.setAttr("lists", answers);

		this.setAttr("queryString", "id=" + id);

		this.render("/contral/results/answers.html");
		return null;
	}
}
