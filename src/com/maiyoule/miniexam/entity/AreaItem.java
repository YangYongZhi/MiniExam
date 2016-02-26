package com.maiyoule.miniexam.entity;

public class AreaItem implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3097469567537880883L;
	private int id;
	private int parent;
	private String name;
	private boolean isLeaf;
	private String level;
	
	public AreaItem(){
		
	}
	public AreaItem(int id,int parent,String name){
		this.id=id;
		this.parent=parent;
		this.name=name;
	}
	public AreaItem(int id,int parent,String name,boolean isParent){
		this.id=id;
		this.parent=parent;
		this.name=name;
		this.isLeaf=!isParent;
	}
	public AreaItem(int id,int parent,String name,boolean isParent,String level){
		this.id=id;
		this.parent=parent;
		this.name=name;
		this.level=level;
		this.isLeaf=!isParent;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getParent() {
		return parent;
	}
	public void setParent(int parent) {
		this.parent = parent;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public boolean isLeaf() {
		return isLeaf;
	}
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	
	
}
