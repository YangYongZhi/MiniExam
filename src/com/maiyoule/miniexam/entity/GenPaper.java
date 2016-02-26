package com.maiyoule.miniexam.entity;

import java.io.Serializable;
import java.util.List;

public class GenPaper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1637854739864557068L;
	
	/**
	 * 试卷序号
	 */
	private int id;
	/**
	 * 试卷名称
	 */
	private String name;
	
	/**
	 * 试卷分数
	 */
	private GenPaperScores scores;
	
	/**
	 * 试卷考题
	 */
	private GenPaperQuestion question;
	
	/**
	 * 正确答案
	 */
	private List<FlexQuestion> an;
	
	/**
	 * 已经作答的答案信息
	 */
	private String existsanswer;
	
	public String getExistsanswer() {
		return existsanswer;
	}

	public void setExistsanswer(String existsanswer) {
		this.existsanswer = existsanswer;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GenPaperScores getScores() {
		return scores;
	}

	public void setScores(GenPaperScores scores) {
		this.scores = scores;
	}

	public GenPaperQuestion getQuestion() {
		return question;
	}

	public void setQuestion(GenPaperQuestion question) {
		this.question = question;
	}

	public List<FlexQuestion> getAn() {
		return an;
	}

	public void setAn(List<FlexQuestion> an) {
		this.an = an;
	}

	
	
}
