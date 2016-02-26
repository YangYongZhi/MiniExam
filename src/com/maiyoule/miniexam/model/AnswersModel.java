package com.maiyoule.miniexam.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.maiyoule.miniexam.utils.StringHelper;

public class AnswersModel extends Model<AnswersModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5937858087525024256L;
	
	public static AnswersModel dao=new AnswersModel();
	
	
	/**
	 * 根据试题ID删除回答记录
	 * @param id
	 */
	public void delByQuestionId(int id){
		Db.update("delete from answers where question_id=?",id);
	}
	
	public void delByQuestionIds(String[] ids){
		if(ids==null||ids.length<1){
			return;
		}
		String idstr=StringHelper.join(ids, ",");
		Db.update("delete from answers where question_id in("+idstr+")");
	}
	
	public void delByLayoutId(int id){
		Db.update("delete from answers where layout_id=?",id);
	}
	
	public void delByExamId(int id){
		Db.update("delete from answers where exam_id=?",id);
	}
}
