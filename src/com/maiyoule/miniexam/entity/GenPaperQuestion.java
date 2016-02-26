package com.maiyoule.miniexam.entity;

import java.io.Serializable;
import java.util.List;

public class GenPaperQuestion implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8196869933476785181L;
	private List<GenPaperItem> radio;
	private List<GenPaperItem> multi;
	private List<GenPaperItem> judge;

	public List<GenPaperItem> getRadio() {
		return radio;
	}

	public void setRadio(List<GenPaperItem> radio) {
		this.radio = radio;
	}

	public List<GenPaperItem> getMulti() {
		return multi;
	}

	public void setMulti(List<GenPaperItem> multi) {
		this.multi = multi;
	}

	public List<GenPaperItem> getJudge() {
		return judge;
	}

	public void setJudge(List<GenPaperItem> judge) {
		this.judge = judge;
	}
}
