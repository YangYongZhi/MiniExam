package com.maiyoule.miniexam.ui;

import com.jfinal.core.Controller;

public class QuestionPage implements IPage {

	private int libraryId=0;
	private String questionType="";
	private int questionStatus=-1;
	private int page=1;
	
	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}

	public String getQuestionType() {
		return questionType;
	}

	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}

	public int getQuestionStatus() {
		return questionStatus;
	}

	public void setQuestionStatus(int questionStatus) {
		this.questionStatus = questionStatus;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void run(Controller c) {
		this.libraryId=c.getParaToInt("libraryId",0);
		this.questionType=c.getPara("questionType", "");
		this.questionStatus=c.getParaToInt("questionStatus", -1);
		this.page=c.getParaToInt("page", 1);
	}

	public String toQueryString() {
		StringBuffer sb=new StringBuffer();
		if(this.libraryId>0){
			sb.append("&");
			sb.append("libraryId="+this.libraryId);
		}
		if(this.questionType!=null){
			sb.append("&");
			sb.append("questionType="+this.questionType);
		}
		if(this.questionStatus>=0){
			sb.append("&");
			sb.append("questionStatus="+this.questionStatus);
		}
		
		
		return sb.toString();
	}

}
