package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class Paper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9055803673873049422L;

	
	private int id;
	private String name;
	private int status;
	private int score;
	private int minScore;
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getMinScore() {
		return minScore;
	}
	public void setMinScore(int minScore) {
		this.minScore = minScore;
	}
	
	
}
