package com.maiyoule.miniexam.ui.template;

import java.util.List;

import com.maiyoule.miniexam.entity.BankInfo;
import com.maiyoule.miniexam.model.BanksModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class BankTemplate implements TemplateMethodModelEx {

	public Object exec(List args) throws TemplateModelException {
		Object no=args.get(0);
		if(no==null){
			return null;
		}
		BankInfo bankinfo=BanksModel.dao.getByNo(no.toString());
		
		if(bankinfo == null) {
			return "";
		}
		return bankinfo.getName();
	}

}
