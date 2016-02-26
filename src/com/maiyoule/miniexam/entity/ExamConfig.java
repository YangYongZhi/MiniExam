package com.maiyoule.miniexam.entity;

import java.io.Serializable;

public class ExamConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3940873405618188486L;
	private int limit_submit=300;
	private int exam_time=1800;
	private int read_time=10;
	private int end_tips_time=90;
	private int active_time_out=300;
	public int getLimit_submit() {
		return limit_submit;
	}
	public void setLimit_submit(int limit_submit) {
		this.limit_submit = limit_submit;
	}
	public int getExam_time() {
		return exam_time;
	}
	public void setExam_time(int exam_time) {
		this.exam_time = exam_time;
	}
	public int getRead_time() {
		return read_time;
	}
	public void setRead_time(int read_time) {
		this.read_time = read_time;
	}
	public int getEnd_tips_time() {
		return end_tips_time;
	}
	public void setEnd_tips_time(int end_tips_time) {
		this.end_tips_time = end_tips_time;
	}
	public int getActive_time_out() {
		return active_time_out;
	}
	public void setActive_time_out(int active_time_out) {
		this.active_time_out = active_time_out;
	}
}
