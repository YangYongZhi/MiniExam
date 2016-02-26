package com.maiyoule.miniexam.ui.template;

import java.util.List;

import com.maiyoule.miniexam.model.LibraryModel;
import com.maiyoule.miniexam.model.QuestionModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class AnswerItemTemplate implements TemplateMethodModelEx {

	public Object exec(List args) throws TemplateModelException {
		//int id=Integer.parseInt(args.get(0).toString());
		//int examid=Integer.parseInt(args.get(1).toString());
		//int layoutid=Integer.parseInt(args.get(2).toString());
		int questionid=Integer.parseInt(args.get(3).toString());
		int status=Integer.parseInt(args.get(4).toString());
		String label=args.get(5).toString();
		
		
		QuestionModel qm=QuestionModel.dao.findCById(questionid);
		if(qm==null){
			return "";
		}
		LibraryModel lib=LibraryModel.dao.findCById(qm.getInt("libid"));
		
		StringBuffer sb=new StringBuffer();
		sb.append("<tr><td>");
		
		//标题
		sb.append(qm.getStr("title"));
		sb.append("</td>");

		//题库
		sb.append("<td align=\"center\">");
		if(lib!=null){
			sb.append(lib.getStr("name"));
		}
		sb.append("</td>");
		
		sb.append("<td align=\"center\">");
		String qtype=qm.getStr("type");
		if(qtype.equals(QuestionModel.TYPE_SINGLE)){
			sb.append("单选");
		}else if(qtype.equals(QuestionModel.TYPE_MUTI)){
			sb.append("多选");
		}else if(qtype.equals(QuestionModel.TYPE_JUDGE)){
			sb.append("判断");
		}
		sb.append("</td>");
		//回答
		sb.append("<td align=\"center\">");
		sb.append(label);
		sb.append("</td>");
		//是否正确
		sb.append("<td align=\"center\">");
		if(status==0){
			sb.append("<label class=\"red\">错误</label>");
		}else{
			sb.append("<label class=\"green\">正确</label>");
		}
		sb.append("</td>");
		
		return sb.toString();
		
	}

}
