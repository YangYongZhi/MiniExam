package com.maiyoule.miniexam.entity;

public class UserInfo implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6492426030553981457L;
	private int id;
	private String cardno;
	private String name;
	private String cardType;
	private String lastExamTime;
	private String bankNo;
	private String bankName;
	private String telphone;
	private String department;
	private int status;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getLastExamTime() {
		return lastExamTime;
	}
	public void setLastExamTime(String lastExamTime) {
		this.lastExamTime = lastExamTime;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getTelphone() {
		return telphone;
	}
	public void setTelphone(String telphone) {
		this.telphone = telphone;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
	
	
}
