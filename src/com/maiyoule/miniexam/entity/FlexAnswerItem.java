package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class FlexAnswerItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7476997659312997791L;
	private String value;
	private int id;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
