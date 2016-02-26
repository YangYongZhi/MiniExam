package com.maiyoule.miniexam.entity;

import java.io.Serializable;
import java.util.List;

public class BankSelectBoxItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5923597437580314519L;

	private String v;
	private String n;
	private List<BankSelectBoxItem> s;
	public String getV() {
		return v;
	}
	public void setV(String v) {
		this.v = v;
	}
	public String getN() {
		return n;
	}
	public void setN(String n) {
		this.n = n;
	}
	public List<BankSelectBoxItem> getS() {
		return s;
	}
	public void setS(List<BankSelectBoxItem> s) {
		this.s = s;
	}
	
	
}
