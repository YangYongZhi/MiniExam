package com.maiyoule.miniexam.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

public class LayoutsModel extends Model<LayoutsModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8009376404066632934L;
	public static LayoutsModel dao = new LayoutsModel();
	
	
	public void updateCount(int id){
		int count=ExamsModel.dao.getCountByLayoutId(id);
		
		LayoutsModel f=this.findById(id);
		f.set("count", count);
		f.update();
		
	}
	
	/**
	 * 从缓存中读取
	 * @param id
	 * @return
	 */
	public LayoutsModel fCacheById(int id){
		List<LayoutsModel> layouts=this.findByCache("layouts", "layout_"+id, "select * from layouts where id="+id);
		if(layouts==null){
			return null;
		}
		return layouts.get(0);
	}
	
}
