package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class ExamUserInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -290335868293836165L;
	/**
	 * 
	 */
	private String name;
	private String id;
	private String group;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
	/**
	 * 用户序列号
	 */
	private int uid;
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}
	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}
	
}
