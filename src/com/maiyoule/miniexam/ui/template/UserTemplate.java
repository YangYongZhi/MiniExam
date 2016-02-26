package com.maiyoule.miniexam.ui.template;

import java.util.List;

import com.maiyoule.miniexam.entity.UserInfo;
import com.maiyoule.miniexam.model.UsersModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class UserTemplate implements TemplateMethodModelEx {

	public Object exec(List args) throws TemplateModelException {
		Object cardno=args.get(0);
		if(cardno==null){
			return null;
		}
		//查询
		UserInfo info=UsersModel.dao.getByCardNo(cardno.toString());
		if(info==null){
			return null;
		}
		return info.getName();
	}

}
