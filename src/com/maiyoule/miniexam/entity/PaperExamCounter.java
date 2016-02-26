package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class PaperExamCounter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8212189017356349449L;
	private int paperId;
	private String paperName;
	@SuppressWarnings("unused")
	private int total;
	// 完成考试
	private int flish;
	// 未考试
	private int emptycount;
	// 转移考试
	private int move;
	// 关闭考试
	private int close;
	// 正在考试
	private int doing;
	public int getPaperId() {
		return paperId;
	}
	public void setPaperId(int paperId) {
		this.paperId = paperId;
	}
	public String getPaperName() {
		return paperName;
	}
	public void setPaperName(String paperName) {
		this.paperName = paperName;
	}
	public int getTotal() {
		//
		return this.close+this.doing+this.emptycount+this.flish+this.move;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public int getFlish() {
		return flish;
	}
	public void setFlish(int flish) {
		this.flish = flish;
	}
	public int getEmptycount() {
		return emptycount;
	}
	public void setEmptycount(int emptycount) {
		this.emptycount = emptycount;
	}
	public int getMove() {
		return move;
	}
	public void setMove(int move) {
		this.move = move;
	}
	public int getClose() {
		return close;
	}
	public void setClose(int close) {
		this.close = close;
	}
	public int getDoing() {
		return doing;
	}
	public void setDoing(int doing) {
		this.doing = doing;
	}
	
	
	
	
	
}
