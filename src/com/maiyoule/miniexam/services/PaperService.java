package com.maiyoule.miniexam.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.entity.FlexQuestion;
import com.maiyoule.miniexam.entity.GenPaper;
import com.maiyoule.miniexam.entity.GenPaperAnswer;
import com.maiyoule.miniexam.entity.GenPaperItem;
import com.maiyoule.miniexam.entity.GenPaperQuestion;
import com.maiyoule.miniexam.entity.GenPaperScores;
import com.maiyoule.miniexam.model.PaperScaleModel;
import com.maiyoule.miniexam.model.PapersModel;
import com.maiyoule.miniexam.model.QuestionModel;

public class PaperService {

	/**
	 * 生成考试试卷
	 * 
	 * @param paperId
	 * @return
	 */
	public GenPaper generatePaper(int paperId) {
		PapersModel paper = PapersModel.dao.findById(paperId);
		if(paper==null){
			return null;
		}
		GenPaperScores scores = new GenPaperScores();
		scores.setJudge(paper.getInt("juge_score"));
		scores.setMulti(paper.getInt("muti_score"));
		scores.setRadio(paper.getInt("single_score"));

		scores.setPass(paper.getInt("min_score"));
		scores.setTotal(paper.getInt("score"));

		// 查询题库比例
		List<PaperScaleModel> scales = PaperScaleModel.dao
				.getScaleByPaperId(paperId);

		List<GenPaperItem> psingles = new ArrayList<GenPaperItem>();
		List<GenPaperItem> pmutis = new ArrayList<GenPaperItem>();
		List<GenPaperItem> pjudges = new ArrayList<GenPaperItem>();

		// 正确答案
		List<FlexQuestion> trueanswer = new ArrayList<FlexQuestion>();

		int singcount = paper.getInt("single_count");
		int muticount = paper.getInt("muti_count");
		int jugecount = paper.getInt("juge_count");
		for (PaperScaleModel scale : scales) {
			// 计算单选实际筛选的数量
			int scalesingle = scale.getInt("scale_single");
			int scalemuti = scale.getInt("scale_muti");
			int scalejuge = scale.getInt("scale_juge");
			int singlemax = (int) Math.round(singcount*(scalesingle / 100.0));
			int multimax = (int)Math.round(muticount * (scalemuti / 100.0));
			int judgemax = (int)Math.round(jugecount * (scalejuge / 100.0));

			// 随机查找
			// 单选题
			List<QuestionModel> singles = QuestionModel.dao.findRandomByLirary(
					scale.getInt("library_id"), QuestionModel.TYPE_SINGLE,
					singlemax);

			for (QuestionModel single : singles) {
				GenPaperItem tgpi = new GenPaperItem();
				tgpi.setId(single.getInt("id"));
				tgpi.setTitle(single.getStr("title"));

				// 正确答案
				FlexQuestion truefq = new FlexQuestion();
				truefq.setId(single.getInt("id"));
				truefq.setType(single.getStr("type"));
				truefq.setValue(single.getStr("answer"));
				trueanswer.add(truefq);

				List<GenPaperAnswer> answerlist = new ArrayList<GenPaperAnswer>();
				// 设置答案选项
				String answerstr = single.getStr("answer_list");
				if (answerstr != null && !answerstr.equals("")) {
					String[] answers = answerstr
							.split(GUIConstants.STRING_SPLITE);
					for (int i = 0; i < answers.length; i++) {
						GenPaperAnswer gpa = new GenPaperAnswer();
						gpa.setLabel(answers[i]);
						if (tgpi.getAnswermaxlength() < gpa.getLabel().length()) {
							tgpi.setAnswermaxlength(gpa.getLabel().length());
						}
						gpa.setValue("" + ((char) (i + 65)));
						answerlist.add(gpa);
					}
				}

				tgpi.setAnswer(answerlist);
				psingles.add(tgpi);
			}

			// 多选题
			List<QuestionModel> mutis = QuestionModel.dao.findRandomByLirary(
					scale.getInt("library_id"), QuestionModel.TYPE_MUTI,
					multimax);

			for (QuestionModel muti : mutis) {
				GenPaperItem tgpi = new GenPaperItem();
				tgpi.setId(muti.getInt("id"));
				tgpi.setTitle(muti.getStr("title"));

				// 正确答案
				FlexQuestion truefq = new FlexQuestion();
				truefq.setId(muti.getInt("id"));
				truefq.setType(muti.getStr("type"));
				truefq.setValue(muti.getStr("answer"));
				trueanswer.add(truefq);

				List<GenPaperAnswer> answerlist = new ArrayList<GenPaperAnswer>();

				// 设置答案选项
				String answerstr = muti.getStr("answer_list");
				if (answerstr != null && !answerstr.equals("")) {
					String[] answers = answerstr
							.split(GUIConstants.STRING_SPLITE);

					for (int i = 0; i < answers.length; i++) {
						GenPaperAnswer gpa = new GenPaperAnswer();
						gpa.setLabel(answers[i]);
						if (tgpi.getAnswermaxlength() < gpa.getLabel().length()) {
							tgpi.setAnswermaxlength(gpa.getLabel().length());
						}
						gpa.setValue("" + ((char) (i + 65)));
						answerlist.add(gpa);
					}
				}
				tgpi.setAnswer(answerlist);
				pmutis.add(tgpi);
			}

			// 判断题
			List<QuestionModel> judges = QuestionModel.dao.findRandomByLirary(
					scale.getInt("library_id"), QuestionModel.TYPE_JUDGE,
					judgemax);

			for (QuestionModel judge : judges) {
				GenPaperItem tgpi = new GenPaperItem();
				tgpi.setId(judge.getInt("id"));
				tgpi.setTitle(judge.getStr("title"));

				// 正确答案
				FlexQuestion truefq = new FlexQuestion();
				truefq.setId(judge.getInt("id"));
				truefq.setType(judge.getStr("type"));
				truefq.setValue(judge.getStr("answer"));
				trueanswer.add(truefq);

				List<GenPaperAnswer> answerlist = new ArrayList<GenPaperAnswer>();

				GenPaperAnswer truegpa = new GenPaperAnswer();
				truegpa.setLabel("正确");
				truegpa.setValue("yes");
				answerlist.add(truegpa);

				GenPaperAnswer errorgpa = new GenPaperAnswer();
				errorgpa.setLabel("错误");
				errorgpa.setValue("no");

				tgpi.setAnswermaxlength(4);
				answerlist.add(errorgpa);

				tgpi.setAnswer(answerlist);
				pjudges.add(tgpi);
			}

		}

		GenPaperQuestion question = new GenPaperQuestion();
		question.setRadio(psingles);
		question.setMulti(pmutis);
		question.setJudge(pjudges);

		GenPaper genPaper = new GenPaper();

		// 设置试卷
		Calendar cale = Calendar.getInstance();
		genPaper.setName(cale.get(Calendar.YEAR) + "年" + paper.getStr("name"));
		genPaper.setId(paper.getInt("id"));
		genPaper.setScores(scores);
		genPaper.setQuestion(question);
		genPaper.setAn(trueanswer);

		return genPaper;
	}
}
