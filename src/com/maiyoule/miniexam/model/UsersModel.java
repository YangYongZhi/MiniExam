package com.maiyoule.miniexam.model;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.entity.UserInfo;
public class UsersModel extends Model<UsersModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5272359206098470459L;
	public static UsersModel dao=new UsersModel();
	
	/**
	 * 根据身份证查找用户
	 * @param cardno
	 * @return
	 */
	public UsersModel findByCardNo(String cardno){
		return this.findFirst("select * from users where cardno=?",cardno);
	}
	
	public UserInfo getByCardNo(String cardno){
		
		UserInfo uinfo=CacheKit.get("userinfos", "userinfo_"+cardno);
		if(uinfo!=null){
			return uinfo;
		}
		UsersModel userm=this.findByCardNo(cardno);
		if(userm==null){
			return null;
		}
		uinfo=new UserInfo();
		uinfo.setName(userm.getStr("name"));
		uinfo.setBankName(userm.getStr("bank_name"));
		uinfo.setBankNo(userm.getStr("bankno"));
		uinfo.setCardno(userm.getStr("cardno"));
		uinfo.setCardType(userm.getStr("card_type"));
		uinfo.setLastExamTime(userm.getStr("last_exam_time"));
		uinfo.setDepartment(userm.getStr("department"));
		uinfo.setId(userm.getInt("id"));
		uinfo.setStatus(userm.getInt("status"));
		uinfo.setTelphone(userm.getStr("telphone"));
		CacheKit.put("userinfos",  "userinfo_"+cardno, uinfo);
		//cache.set("userinfo_"+cardno, uinfo);
		return uinfo;
	}
	
	
}
