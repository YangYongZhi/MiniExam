package com.maiyoule.miniexam.control.admin;

import com.jfinal.core.Controller;

public class MainController extends Controller {
	//首页
	public void index(){
		this.render("/contral/index.html");
	}
	
	
	public void page(){
		String p=this.getPara("p");
		this.render(String.format("/contral/%s.html", p));
	}
	
	
}
