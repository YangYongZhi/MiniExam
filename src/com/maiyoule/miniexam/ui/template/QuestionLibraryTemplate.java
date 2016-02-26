package com.maiyoule.miniexam.ui.template;

import java.util.List;

import com.maiyoule.miniexam.model.LibraryModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class QuestionLibraryTemplate implements TemplateMethodModelEx {

	public Object exec(List args) throws TemplateModelException {
		Object idstr=args.get(0);
		if(idstr==null){
			return "-";
		}
		
		int id=Integer.parseInt(idstr+"");
		List<LibraryModel> library=LibraryModel.dao.findByCache("questions", "library_"+id, "select * from library where id="+id);
		if(library==null){
			return "-";
		}
		LibraryModel lib=library.get(0);
		return lib.getStr("name");
	}

}
