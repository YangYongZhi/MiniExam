package com.maiyoule.miniexam.entity;

public class BankInfo implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4853169452296757150L;
	private String no;
	private String name;
	private String parentno;
	private int cityNo;
	private String cityName;
	private int countryNo;
	private String conntryName;
	private String isleaf;
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParentno() {
		return parentno;
	}
	public void setParentno(String parentno) {
		this.parentno = parentno;
	}
	public int getCityNo() {
		return cityNo;
	}
	public void setCityNo(int cityNo) {
		this.cityNo = cityNo;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public int getCountryNo() {
		return countryNo;
	}
	public void setCountryNo(int countryNo) {
		this.countryNo = countryNo;
	}
	public String getConntryName() {
		return conntryName;
	}
	public void setConntryName(String conntryName) {
		this.conntryName = conntryName;
	}
	public String getIsleaf() {
		return isleaf;
	}
	public void setIsleaf(String isleaf) {
		this.isleaf = isleaf;
	}
	
	
}
