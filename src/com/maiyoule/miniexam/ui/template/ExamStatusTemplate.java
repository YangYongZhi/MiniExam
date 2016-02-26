package com.maiyoule.miniexam.ui.template;

import java.util.List;

import com.maiyoule.miniexam.utils.ExamlayoutUtil;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class ExamStatusTemplate implements TemplateMethodModelEx {

	public Object exec(List args) throws TemplateModelException {
		Object userstatus = args.get(0);
		Object layoutstatus = args.get(1);

		return ExamlayoutUtil.status(Integer.parseInt(layoutstatus + ""),
				Integer.parseInt("" + userstatus));
	}

}
