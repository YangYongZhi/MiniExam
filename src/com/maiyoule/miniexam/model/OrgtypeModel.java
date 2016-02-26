package com.maiyoule.miniexam.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.entity.OrgTypeItem;

public class OrgtypeModel extends Model<OrgtypeModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8960110645336259753L;
	public static OrgtypeModel dao=new OrgtypeModel();
	
	public List<OrgTypeItem> getAll(){
		
		 List<OrgTypeItem> items=CacheKit.get("orgtypes","all");
		 if(items!=null){
			 return items;
		 }
		 items=new ArrayList<OrgTypeItem>();
		 List<OrgtypeModel> ots=this.find("select * from orgtype");
		 for(OrgtypeModel m:ots){
			 items.add(new OrgTypeItem(m.getInt("id"),m.getStr("no"),m.getStr("name"),m.getStr("parent_no")));
		 }
		 CacheKit.put("orgtypes", "all", items);
		 return items;
	}
}
