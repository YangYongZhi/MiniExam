package com.maiyoule.miniexam.entity;

import java.io.Serializable;
import java.util.List;

public class FlexSubmit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4434171351079376026L;

	
	private int examid;
	private String examname;

	private List<FlexAnswerItem> answer;
	public int getExamid() {
		return examid;
	}
	public void setExamid(int examid) {
		this.examid = examid;
	}
	public String getExamname() {
		return examname;
	}
	public void setExamname(String examname) {
		this.examname = examname;
	}
	public List<FlexAnswerItem> getAnswer() {
		return answer;
	}
	public void setAnswer(List<FlexAnswerItem> answer) {
		this.answer = answer;
	}
	
	
}
