package com.maiyoule.miniexam.db;

import javax.sql.DataSource;

import com.jfinal.plugin.c3p0.C3p0Plugin;

public class InstallDbSource {
	
	private static InstallDbSource s=null;
	
	public static InstallDbSource getInstance(){
		if(s==null){
			s=new InstallDbSource();
		}
		return s;
	}
	

	private C3p0Plugin dbplugin;
	public void setPlugin(C3p0Plugin dbplugin){
		this.dbplugin=dbplugin;
	}
	
	public DataSource getSource(){
		if(this.dbplugin!=null){
			return this.dbplugin.getDataSource();
		}
		return null;
	}
}
