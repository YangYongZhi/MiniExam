package com.maiyoule.miniexam.entity;

import java.io.Serializable;
import java.util.List;

public class ExamInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8169334439279796168L;

	/**
	 * 考试序号
	 */
	private int id;
	
	/**
	 * 考试注意事项
	 */
	private String notice;
	
	private ExamUserInfo user;

	private ExamConfig examconfig;

	private List<ExamPaper> exampaper;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public ExamUserInfo getUser() {
		return user;
	}

	public void setUser(ExamUserInfo user) {
		this.user = user;
	}

	public ExamConfig getExamconfig() {
		return examconfig;
	}

	public void setExamconfig(ExamConfig examconfig) {
		this.examconfig = examconfig;
	}

	public List<ExamPaper> getExampaper() {
		return exampaper;
	}

	public void setExampaper(List<ExamPaper> exampaper) {
		this.exampaper = exampaper;
	}
}
