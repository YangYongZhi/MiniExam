package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class ExamlayoutStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -193659899351048187L;
	
	private int id;
	private int layoutstatus;
	private int userstatus;
	
	public ExamlayoutStatus(int id,int layoutstatus,int userstatus){
		this.id=id;
		this.layoutstatus=layoutstatus;
		this.userstatus=userstatus;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLayoutstatus() {
		return layoutstatus;
	}
	public void setLayoutstatus(int layoutstatus) {
		this.layoutstatus = layoutstatus;
	}
	public int getUserstatus() {
		return userstatus;
	}
	public void setUserstatus(int userstatus) {
		this.userstatus = userstatus;
	}
	
	
	
}
