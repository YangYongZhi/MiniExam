package com.maiyoule.miniexam.ui.template;

import java.util.List;

import com.maiyoule.miniexam.entity.Paper;
import com.maiyoule.miniexam.model.PapersModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class ScoreStatusTemplate implements TemplateMethodModelEx {

	public Object exec(List args) throws TemplateModelException {
		
		int score=Integer.parseInt(args.get(0).toString());
		int utype=Integer.parseInt(args.get(1).toString());
		Paper paper=PapersModel.dao.findByUtype(utype);
		if(paper==null){
			return score;
		}
		if(paper.getMinScore()>score){
			return String.format("%d (<label class=\"red\">不及格</label>)", score);
		}else{
			return String.format("%d (<label class=\"green\">及格</label>)", score);
		}
		
	}

}
