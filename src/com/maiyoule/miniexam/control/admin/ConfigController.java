package com.maiyoule.miniexam.control.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.model.AnswersModel;
import com.maiyoule.miniexam.model.ConfigModel;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.PapersModel;
import com.maiyoule.miniexam.model.QuestionModel;
import com.maiyoule.miniexam.utils.StringHelper;

public class ConfigController extends C {
	private Logger log = Logger.getLogger(ConfigController.class);

	private static final Map<Integer, PapersModel> PARPERS_CACHE = new HashMap<Integer, PapersModel>();

	private static AtomicBoolean recalculating = new AtomicBoolean(false);

	public void index() {
		List<ConfigModel> configs = ConfigModel.dao.find("select * from config");
		if (configs != null) {

			for (ConfigModel cfg : configs) {
				String id = cfg.getStr("id");
				String value = cfg.get("value");
				if (id.equalsIgnoreCase("examNotice")) {
					value = value.replaceAll("<br>", "");
				}
				this.setAttr(id, value);
			}

		}
		this.render("/contral/config/index.html");
	}

	public String update() {
		Map<String, String[]> kvs = this.getParaMap();

		Set<String> keys = kvs.keySet();

		for (String key : keys) {
			String[] values = kvs.get(key);
			String val = values[0];
			// System.out.println(val);

			if (key.equalsIgnoreCase("examNotice")) {
				val = val.replaceAll("\n", "<br>\n");
			}

			ConfigModel cfg = ConfigModel.dao.findById(key);
			if (cfg == null) {
				cfg = new ConfigModel();
				cfg.set("id", key);
				cfg.set("value", val);
				cfg.save();
			} else {
				cfg.set("id", key);
				cfg.set("value", val);
				cfg.update();
			}
		}
		try {
			CacheKit.removeAll("config");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.ajaxMessage("设置成功", "成功", true);
	}

	/**
	 * 恢复数据库，清空考试数据初始页面.
	 */
	public void recoverdb() {
		this.render("/contral/config/recoverdb.html");
	}

	/**
	 * 恢复考试相关数据库及缓存，清空考试数据
	 * 
	 * @return
	 */
	public String dorecoverdb() {
		List<String> batchSql = new ArrayList<String>();
		batchSql.add("DELETE FROM ANSWERS");
		batchSql.add("UPDATE SQLITE_SEQUENCE SET SEQ = 0 WHERE NAME = 'answers' OR NAME = 'ANSWERS'");
		batchSql.add("DELETE FROM EXAMS");
		batchSql.add("UPDATE SQLITE_SEQUENCE SET SEQ = 0 WHERE NAME = 'exams' OR NAME = 'EXAMS'");
		batchSql.add("DELETE FROM LAYOUTS");
		batchSql.add("UPDATE SQLITE_SEQUENCE SET SEQ = 0 WHERE NAME = 'layouts' OR NAME = 'LAYOUTS'");
		try {
			Db.batch(batchSql, batchSql.size());
		} catch (Exception e) {
			return this.ajaxMessage("清空考试数据失败，请稍后再试！", "失败");
		}
		try {
			CacheKit.removeAll("areacache");
			CacheKit.removeAll("bankcache");
			CacheKit.removeAll("utypecache");
			CacheKit.removeAll("orgtypes");
			CacheKit.removeAll("areacount");
			CacheKit.removeAll("userinfos");
			CacheKit.removeAll("layouts");
			CacheKit.removeAll("userpaper");
			CacheKit.removeAll("exams");
			CacheKit.removeAll("config");
			CacheKit.removeAll("answer");
			CacheKit.removeAll("questions");
		} catch (Exception e) {
			return this.ajaxMessage("清空考试数据缓存失败，请稍后再试！", "失败");
		}
		return this.ajaxMessage("清空考试数据成功！", "成功", true);
	}

	/**
	 * According to answer details to re calculate the score.
	 * 
	 * you can clear the result calculated before:
	 * 
	 * update answers set status = null; <br>
	 * update exams set score = 0 ;
	 * 
	 * 
	 * @return
	 */
	public String recalculate() {
		if (recalculating.get()) {
			return this.ajaxMessage("上一次的重算还未结束，请稍后重试", "重新计算成绩", true);
		} else {
			try {
				recalculating.set(true);
				judge();
				calculateScore();
				return this.ajaxMessage("重新计算成绩完成，请到成绩查看页面查看详情", "重新计算成绩成功", true);
			} catch (Exception e) {
				return this.ajaxMessage("重新计算成绩结束，" + e.getMessage(), "结束");
			} finally {
				recalculating.set(false);
			}

		}
	}

	/**
	 * 
	 */
	private void judge() {
		List<AnswersModel> am = AnswersModel.dao.find("select * from answers");
		if (am == null) {
			log.warn("answern list is empty");
			throw new RuntimeException("answern list is empty");
		}

		if (log.isInfoEnabled()) {
			log.info("Recalculating answerw is ready, there are " + am.size() + " answers need to be recalculate.");
		}

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

			a.update();
			if (log.isInfoEnabled()) {
				log.info("calculating answer successfully:" + a.get("id"));
			}

		}

	}

	private PapersModel getPaper(int utype) {
		if (PARPERS_CACHE == null || PARPERS_CACHE.isEmpty()) {
			List<PapersModel> pList = PapersModel.dao.find("select * from papers");
			for (Iterator<PapersModel> iterator = pList.iterator(); iterator.hasNext();) {
				PapersModel p = iterator.next();
				PARPERS_CACHE.put(p.getInt("id"), p);
			}
		}

		if (utype == 7 || utype == 8) {
			return PARPERS_CACHE.get(Integer.valueOf(GUIConstants.DATA_PAPER));
		} else {
			return PARPERS_CACHE.get(Integer.valueOf(GUIConstants.NON_DATA_PAPER));
		}

	}

	/**
	 * 
	 */
	private void calculateScore() {
		List<ExamsModel> exams = ExamsModel.dao.find("select * from exams"
		// + "where id = 20859"
		        );
		if (exams == null) {
			log.warn("exams is empty");
			throw new RuntimeException("exams is empty");
		}

		if (log.isInfoEnabled()) {
			log.info("Recalculating exams is ready, there are " + exams.size() + " exams need to be recalculate.");
		}

		for (Iterator<ExamsModel> iterator = exams.iterator(); iterator.hasNext();) {
			ExamsModel exam = iterator.next();
			PapersModel p = getPaper(exam.getInt("utype"));
			if (p == null) {
				if (log.isInfoEnabled()) {
					log.info("The paper of this exam is null, so ignore this exam:" + exam.get("id"));
				}
				continue;
			}

			List<AnswersModel> answers = AnswersModel.dao.find("select * from answers where exam_id = "
			        + exam.getInt("id"));

			if (answers == null || answers.size() <= 0) {
				if (log.isInfoEnabled()) {
					log.info("The anserw of this exam is null, so ignore this exam:" + exam.get("id"));
				}
				continue;
			}

			int score = 0;
			for (Iterator<AnswersModel> iterator2 = answers.iterator(); iterator2.hasNext();) {
				AnswersModel a = iterator2.next();
				if (a.getInt("status") != null && a.getInt("status") == 1) {
					QuestionModel q = QuestionModel.dao.findById(a.get("question_id"));
					// LibraryModel.dao.find("select * from library where id = "
					// + q.get("libid"));

					if (q.get("type").equals(QuestionModel.TYPE_SINGLE)) {
						score += p.getInt("single_score");
					} else if (q.get("type").equals(QuestionModel.TYPE_MUTI)) {
						score += p.getInt("muti_score");
					} else if (q.get("type").equals(QuestionModel.TYPE_JUDGE)) {
						score += p.getInt("juge_score");
					}
				}
			}

			exam.set("score", score);
			exam.update();
			if (log.isInfoEnabled()) {
				log.info("recalculating exam successfully:" + exam.get("id"));
			}

		}
	}
}
