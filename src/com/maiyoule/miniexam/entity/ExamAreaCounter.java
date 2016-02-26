package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class ExamAreaCounter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3964753949800753197L;
	private int count;
	private int id;
	private int parentId;
	private String name;
	
	private boolean hasChild;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isHasChild() {
		return hasChild;
	}

	public void setHasChild(boolean hasChild) {
		this.hasChild = hasChild;
	}

	
	
}
