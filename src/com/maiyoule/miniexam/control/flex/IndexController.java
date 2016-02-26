package com.maiyoule.miniexam.control.flex;

import com.jfinal.core.Controller;
import com.maiyoule.miniexam.GUIConstants;

public class IndexController extends Controller {
	
	public void index(){
		this.setAttr("LogServer", this.getRequest().getAttribute(GUIConstants.BASEPATH));
		this.render("/flex/index.html");
	}	
}
