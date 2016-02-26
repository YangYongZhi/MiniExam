package com.maiyoule.miniexam.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

public class PapersUserTypeModel extends Model<PapersUserTypeModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5535291689637238025L;
	
	public static PapersUserTypeModel dao=new PapersUserTypeModel();
	
	
	public List<PapersUserTypeModel> getTypeByPaperId(int paperId){
		return this.find("select * from papers_user_type where paper_id=?",paperId);
	}
	
}
