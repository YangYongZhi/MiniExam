package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class FlexScore implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -22016296189473456L;
	private int score;
	private String label;
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
