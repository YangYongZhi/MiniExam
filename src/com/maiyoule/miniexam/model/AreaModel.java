package com.maiyoule.miniexam.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.entity.AreaItem;

public class AreaModel extends Model<AreaModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1357160523993807100L;
	public static AreaModel dao=new AreaModel();
	
	
	public List<AreaItem> findByParentId(int parentId){
		
		List<AreaItem> item = CacheKit.get("areacache", "child_"+parentId);//new 
		if(item!=null){
			return item;
		}
		item=new ArrayList<AreaItem>();
		List<AreaModel> areas= this.find("select * from areas where parent=? and status=1",parentId);
		if (areas != null) {
			for (AreaModel m : areas) {
				item.add(new AreaItem(m.getInt("id"), m.getInt("parent"), m
						.getStr("name"),m.getStr("isleaf").equals("0"),m.getStr("level")));
			}
		}
		CacheKit.put("areacache", "child_"+parentId, item);
		return item;
	}
	/**
	 * 获取全部
	 * @return
	 */
	public List<AreaItem> findAll(){
		List<AreaItem> item = CacheKit.get("areacache", "all");//new 
		if(item!=null){
			return item;
		}
		item=new ArrayList<AreaItem>();
		List<AreaModel> areas= this.find("select * from areas where status=1");
		if (areas != null) {
			for (AreaModel m : areas) {
				item.add(new AreaItem(m.getInt("id"), m.getInt("parent"), m
						.getStr("name"),m.getStr("isleaf").equals("0"),m.getStr("level")));
			}
		}
		CacheKit.put("areacache", "all", item);
		return item;
	}
	
	
	
}
