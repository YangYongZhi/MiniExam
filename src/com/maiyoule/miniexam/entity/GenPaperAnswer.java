package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class GenPaperAnswer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2438153901727791293L;
	private String label;
	private String value;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
