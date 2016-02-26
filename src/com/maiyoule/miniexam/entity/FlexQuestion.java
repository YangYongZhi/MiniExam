package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class FlexQuestion implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3569717913224411183L;
	private int id;
	private String type;
	private String value;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
