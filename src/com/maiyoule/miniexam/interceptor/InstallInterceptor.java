package com.maiyoule.miniexam.interceptor;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.maiyoule.miniexam.GUIConstants;

public class InstallInterceptor implements Interceptor {

	public void intercept(ActionInvocation ai) {

		Controller c=ai.getController();
		HttpServletRequest request=c.getRequest();

		String basePath = request.getScheme()+"://"+request.getServerName();
		if(request.getServerPort()!=80){
			basePath+=":"+request.getServerPort();
		}
		String cpath=request.getContextPath();
		if(cpath!=null&&!cpath.equals("")){
			basePath += cpath;
		}

		request.setAttribute("basePath", basePath);

		// 检查是否已安装
		if (!GUIConstants.isInstall()) {
			// 重定向到安装界面
			Controller con=ai.getController();
			//检查是否已安装
			con.redirect("/setup");
		}else{
			ai.invoke();
		}
	}

}
