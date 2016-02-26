package com.maiyoule.miniexam.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

public class ConfigModel extends Model<ConfigModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2917021381530276995L;
	public static ConfigModel dao=new ConfigModel();
	
	
	public String findByKey(String key){
		List<ConfigModel> cfg=ConfigModel.dao.findByCache("config", key, "select * from config where id='"+key+"' limit 1");
		if(cfg==null){
			return null;
		}
		ConfigModel configModel =cfg.get(0);
		return configModel.getStr("value");
	}
}
