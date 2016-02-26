package com.maiyoule.miniexam.control.admin;

import com.jfinal.core.Controller;
import com.maiyoule.miniexam.utils.Ajax;

public class C extends Controller {
	/**
	 * 显示错误信息
	 * @param message
	 * @return
	 */
	public String error(String message){
		this.setAttr("message", message);
		this.render("/contral/common/error.html");
		return null;
	}
	/**
	 * 成功消息
	 * @param jump
	 * @return
	 */
	public String success(String jump){
		this.setAttr("jump", jump);
		this.render("/contral/common/success.html");
		return null;
	}
	
	public String ajaxMessage(String data,String info,boolean status){
		this.renderText(Ajax.message(data, info,status), "text/html;charset=utf-8");
		return null;
	}
	public String ajaxMessage(String data,String info){
		this.renderText(Ajax.message(data, info), "text/html;charset=utf-8");
		return null;
	}
	public String ajaxMessage(String data){
		this.renderText(Ajax.message(data), "text/html;charset=utf-8");
		return null;
	}
	public String ajaxMessage(Object obj,String info,boolean status){
		this.renderText(Ajax.message(obj, info, status), "text/html;charset=utf-8");
		return null;
	}
}
