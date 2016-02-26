package com.maiyoule.miniexam.ui.template;

import java.util.List;

import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.entity.AreaItem;
import com.maiyoule.miniexam.model.AreaModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class AreaTemplate implements TemplateMethodModelEx {

	public Object exec(List arg0) throws TemplateModelException {
		int id=Integer.parseInt(arg0.get(0).toString());
		if(id<0){
			return null;
		}
		AreaItem area=CacheKit.get("areacache", id);
		if(area==null){
			area=new AreaItem();
			AreaModel model=AreaModel.dao.findById(id);
			if(model!=null){
				area.setId(model.getInt("id"));
				area.setName(model.getStr("name"));
				area.setParent(model.getInt("parent"));
				area.setLevel(model.getStr("level"));
				area.setLeaf(model.getStr("isleaf").equals("1"));
			}
			CacheKit.put("areacache", id, area);
		}
		return area.getName();
	}

}
