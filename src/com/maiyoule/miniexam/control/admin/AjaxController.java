package com.maiyoule.miniexam.control.admin;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.maiyoule.miniexam.entity.AreaItem;
import com.maiyoule.miniexam.entity.BankInfo;
import com.maiyoule.miniexam.model.AreaModel;
import com.maiyoule.miniexam.model.BanksModel;

public class AjaxController extends C {
	/**
	 * 地区查询
	 * @return
	 */
	public String area() {
		int parent = this.getParaToInt("parent", 0);

		List<AreaItem> item = AreaModel.dao.findByParentId(parent);
		
		return this.ajaxMessage(item, "成功", true);
	}
	
	/**
	 * 根据城市和地区查询金融机构
	 * @return
	 */
	public String bank(){
		int city=this.getParaToInt("city",0);
		int country=this.getParaToInt("conutry", 0);
		String parent=this.getPara("orgtype",null);
		List<BankInfo> banks=BanksModel.dao.findByArea(city, country,parent);
		return this.ajaxMessage(banks, "成功", true);
	}
	
	
	/**
	 * 压缩数据库
	 * @return
	 */
	public String dbVACUUM(){
		Db.update("VACUUM");
		return this.ajaxMessage("压缩成功", "成功", true);
	}
}
