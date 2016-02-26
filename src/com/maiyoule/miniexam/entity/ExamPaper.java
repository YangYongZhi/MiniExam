package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class ExamPaper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3396988769583646835L;
	private int id;
	private String name;
	
	private boolean status;
	
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
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
}
