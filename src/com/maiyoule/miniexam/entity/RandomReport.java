package com.maiyoule.miniexam.entity;

import java.util.List;

public class RandomReport implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4159459953608777688L;
	private int total;
	private int scale;
	private int finalcount;
	private List<RandomReportItem> list;
	
	private long sign;

	
	public long getSign() {
		return sign;
	}

	public void setSign(long sign) {
		this.sign = sign;
	}

	public int getFinalcount() {
		return finalcount;
	}

	public void setFinalcount(int finalcount) {
		this.finalcount = finalcount;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public List<RandomReportItem> getList() {
		return list;
	}

	public void setList(List<RandomReportItem> list) {
		this.list = list;
	}

}
