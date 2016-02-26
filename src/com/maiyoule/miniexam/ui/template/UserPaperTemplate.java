package com.maiyoule.miniexam.ui.template;

import java.util.List;

import com.maiyoule.miniexam.entity.Paper;
import com.maiyoule.miniexam.model.PapersModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class UserPaperTemplate implements TemplateMethodModelEx {

	public Object exec(List args) throws TemplateModelException {
		Object utype=args.get(0);
		if(utype==null){
			return null;
		}
		
		Paper paper=PapersModel.dao.findByUtype(Integer.parseInt(utype+""));
		if(paper==null){
			return null;
		}
		return paper.getName();
	}

}
