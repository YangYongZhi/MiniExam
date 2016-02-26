package com.maiyoule.miniexam.entity;

public class OrgTypeItem implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2622807547102502758L;
	private int id;
	private String no;
	private String name;
	private String parentNo;
	
	public OrgTypeItem(){
		
	}
	public OrgTypeItem(int id,String no,String name,String parentNo){
		this.id=id;
		this.no=no;
		this.name=name;
		this.parentNo=parentNo;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
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
	public String getParentNo() {
		return parentNo;
	}
	public void setParentNo(String parentNo) {
		this.parentNo = parentNo;
	}
	
	
}
