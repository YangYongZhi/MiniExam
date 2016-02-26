package com.maiyoule.miniexam.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.utils.StringHelper;

public class UsersUtypesModel extends Model<UsersUtypesModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5753643554489570761L;
	public static UsersUtypesModel dao=new UsersUtypesModel();
	
	
	public void delByCardno(String cardno){
		Db.update("delete from users_utypes where cardno=?",cardno);
	}
	/**
	 * 根据身份证查找数据集
	 * @param cardno
	 * @return
	 */
	public Map<String,UsersUtypesModel> findKVConllentsByCardno(String cardno){
		List<UsersUtypesModel> utypemodels=this.find("select id,type_id,bank_no from users_utypes where cardno=?",cardno);
		if(utypemodels==null||utypemodels.size()<1){
			return null;
		}
		Map<String,UsersUtypesModel> map=new HashMap<String,UsersUtypesModel>();
		for(UsersUtypesModel m:utypemodels){
			map.put(m.getStr("bank_no")+"_"+m.getInt("type_id"), m);
		}
		return map;
	}
	
	
/**
 * 根据条件查找人数
 * @param city
 * @param country
 * @param utype
 * @param orgtype
 * @param ubanknos
 * @param cardno
 * @return
 */
	public int getCountNormal(int city,int country,int utype,String orgtype,String[] ubanknos,String cardno){
		String path="";
		if(!StringHelper.isNullOrEmpty(orgtype)){
			path+=orgtype;
		}
		if(ubanknos!=null&&ubanknos.length>0){
			path+=GUIConstants.STRING_SPLITE+StringUtils.join(ubanknos,GUIConstants.STRING_SPLITE);
		}
		String sql="select count(id) as al from users_utypes where city=%d and country=%d";
		if(utype>0){
			sql+=" and type_id="+utype;
		}
		if(!StringHelper.isNullOrEmpty(path)){
			sql+=" and bank_path like '%"+path+"%'";
		}
		if(!StringHelper.isNullOrEmpty(cardno)){
			sql+=" and cardno='"+cardno+"'";
		}
		
		UsersUtypesModel ut=this.findFirst(sql);
		if(ut==null){
			return 0;
		}
		return ut.getInt("al");
		
	}
	
	public Page<UsersUtypesModel> getPage(int city,int country,int utype,String orgtype,String[] ubanknos,String cardno,int page){
		String path="";
		if(!StringHelper.isNullOrEmpty(orgtype)){
			path+=orgtype;
		}
		if(ubanknos!=null&&ubanknos.length>0){
			path+=GUIConstants.STRING_SPLITE+StringUtils.join(ubanknos,GUIConstants.STRING_SPLITE);
		}
		String sql="from users_utypes where 1=1 ";
		if(city>0){
			sql+=" and city="+city;
		}
		if(country>0){
			sql+=" and country="+country;
		}
		if(utype>0){
			sql+=" and type_id="+utype;
		}
		if(!StringHelper.isNullOrEmpty(path)){
			sql+=" and bank_path like '%"+path+"%'";
		}
		if(!StringHelper.isNullOrEmpty(cardno)){
			sql+=" and cardno='"+cardno+"'";
		}
		
		return this.paginate(page, GUIConstants.PAGE_SIZE, "select *", sql);
		
	}
}
