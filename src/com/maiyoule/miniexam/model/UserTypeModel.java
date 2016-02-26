package com.maiyoule.miniexam.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.entity.UserType;

public class UserTypeModel extends Model<UserTypeModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2528136293776769957L;
	public static UserTypeModel dao=new UserTypeModel();
	
	/**
	 * 获取全部用户类型
	 * @return
	 */
	public List<UserType> getAll(){
		List<UserType> types=CacheKit.get("utypecache", "all");
		if(types!=null){
			return types;
		}
		types=new ArrayList<UserType>();
		List<UserTypeModel> lists=this.find("select * from user_type order by id asc");
		for(UserTypeModel utm:lists){
			UserType ut=new UserType();
			ut.setId(utm.getInt("id"));
			ut.setName(utm.getStr("name"));
			types.add(ut);
		}
		CacheKit.put("utypecache", "all", types);
		return types;
	}
	
	public UserType findByName(String name){
		//MCache cache=MCache.getInstance();
		UserType utype =CacheKit.get("utypecache", name);//cache.get(name);
		if(utype!=null){
			return utype;
		}
		
		
		UserTypeModel ut=this.findFirst("select * from user_type where name=?",name);
		if(ut==null){
			return null;
		}
		utype=new UserType();
		utype.setId(ut.getInt("id"));
		utype.setName(ut.getStr("name"));
		//cache.set(name, utype);
		CacheKit.put("utypecache", name, utype);
		return utype;
	}
	
	public UserType getById(int id){
		//MCache cache=MCache.getInstance();

		UserType utype =CacheKit.get("utypecache", "usertype_"+id);//cache.get("usertype_"+id);
		if(utype!=null){
			return utype;
		}
		UserTypeModel ut=this.findById(id);
		if(ut==null){
			return null;
		}
		utype=new UserType();
		utype.setId(ut.getInt("id"));
		utype.setName(ut.getStr("name"));
		CacheKit.put("utypecache",  "usertype_"+id, utype);
		//cache.set("usertype_"+id, utype);
		return utype;
	}
	
}
