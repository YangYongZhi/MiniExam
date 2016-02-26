package com.maiyoule.miniexam.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

public class PaperScaleModel extends Model<PaperScaleModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5680266826549625284L;
	public static PaperScaleModel dao = new PaperScaleModel();
	
	
	/**
	 * 获取试卷比例设置
	 * @param paperId
	 * @return
	 */
	public List<PaperScaleModel> getScaleByPaperId(int paperId){
		return this.find("select * from papers_scale where paper_id=?",paperId);
	}
}
