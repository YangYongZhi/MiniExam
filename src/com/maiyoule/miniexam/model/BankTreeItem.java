package com.maiyoule.miniexam.model;

import java.io.Serializable;

public class BankTreeItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2985169392239532023L;
	private String id;
	private String pId;
	private String name;

	public BankTreeItem() {

	}

	public BankTreeItem(String id, String pId, String name) {
		this.id = id;
		if(pId==null||pId.length()<14){
			this.pId="";
		}else{

			this.pId = pId;
		}
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPId() {
		return pId;
	}

	public void setPId(String pId) {
		this.pId = pId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
