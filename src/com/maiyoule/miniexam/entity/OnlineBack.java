package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class OnlineBack implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5788203177332298360L;
	private int layoutstatus;
	private int userstatus;
	private int layoutid;
	
	public OnlineBack(){
	}
	public OnlineBack(int layoutstatus,int userstatus,int layoutid){
		this.layoutstatus=layoutstatus;
		this.userstatus=userstatus;
		this.layoutid=layoutid;
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
	public int getLayoutid() {
		return layoutid;
	}
	public void setLayoutid(int layoutid) {
		this.layoutid = layoutid;
	}
}
