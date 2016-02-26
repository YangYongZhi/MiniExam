package com.maiyoule.miniexam.entity;

import java.io.Serializable;
import java.util.List;

public class GenPaperItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5093704732975538651L;
	private int id;
	private String title;
	/**
	 * 答案中最长的答案的长度，用于打印进行分割处理
	 */
	private int answermaxlength=0;
	private List<GenPaperAnswer> answer;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<GenPaperAnswer> getAnswer() {
		return answer;
	}
	public void setAnswer(List<GenPaperAnswer> answer) {
		this.answer = answer;
	}
	public int getAnswermaxlength() {
		return answermaxlength;
	}
	public void setAnswermaxlength(int answermaxlength) {
		this.answermaxlength = answermaxlength;
	}
	
	
}
