package com.maiyoule.miniexam.services;

import com.googlecode.asyn4j.core.handler.CacheAsynWorkHandler;
import com.googlecode.asyn4j.core.handler.DefaultErrorAsynWorkHandler;
import com.googlecode.asyn4j.core.handler.FileAsynServiceHandler;
import com.googlecode.asyn4j.service.AsynService;
import com.googlecode.asyn4j.service.AsynServiceImpl;

public class AyncWork {
	private static AyncWork _work;
	public static AyncWork getInstance(){
		if(_work==null){
			_work=new AyncWork();
		}
		return _work;
	}
	
	
	private AsynService anycService =null;
	public AyncWork(){
		anycService = AsynServiceImpl.getService(300, 3000L, 100, 100,1000);
		//异步工作缓冲处理器
        anycService.setWorkQueueFullHandler(new CacheAsynWorkHandler(100));
        //服务启动和关闭处理器
       // anycService.setServiceHandler(new FileAsynServiceHandler());
        //异步工作执行异常处理器
        //anycService.setErrorAsynWorkHandler(new DefaultErrorAsynWorkHandler());
        
	}
	
	public void start(){
		if(anycService!=null){
			anycService.init();
		}
		
	}
	
	public void stop(){
		if(anycService!=null){
			anycService.close();
		}
	}

	public AsynService getAnycService() {
		return anycService;
	}

	public void setAnycService(AsynService anycService) {
		this.anycService = anycService;
	}
	
	
	
}
