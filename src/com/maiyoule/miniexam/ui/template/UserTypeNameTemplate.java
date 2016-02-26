package com.maiyoule.miniexam.ui.template;

import java.util.List;

import com.maiyoule.miniexam.entity.UserType;
import com.maiyoule.miniexam.model.UserTypeModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class UserTypeNameTemplate implements TemplateMethodModelEx {

	public Object exec(List args) throws TemplateModelException {
		int id=Integer.parseInt(args.get(0).toString());
		UserType utype=UserTypeModel.dao.getById(id);
		if(utype!=null){
			return utype.getName();
		}else{
			return "";
		}
	}

	

	
}
