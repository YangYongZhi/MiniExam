package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class RandomReportItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -779660882893418441L;
	private int typeId;
	private int scale;
	private int count;

	public RandomReportItem() {

	}

	public RandomReportItem(int typeId, int scale, int count) {
		this.scale = scale;
		this.count = count;
	}


	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
