package com.maiyoule.miniexam.control.admin;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.entity.AdminUser;
import com.maiyoule.miniexam.model.AdminModel;
import com.maiyoule.miniexam.utils.Ajax;
import com.maiyoule.miniexam.utils.PropHelper;
import com.maiyoule.miniexam.utils.S;
import com.maiyoule.miniexam.utils.StringHelper;

public class LoginController extends Controller {
	public void index() {
		this.render("/contral/login.html");
	}

	/**
	 * 管理员登录
	 */
	public void dologin() {
		String uname = this.getPara("username");
		String upwd = this.getPara("password");
		if (uname == null || uname.length() < 1) {
			this.renderJson(Ajax.message("请输入正确的帐号", "帐号"));
			return;
		}

		if (upwd == null || upwd.length() < 1) {
			this.renderJson(Ajax.message("请输入正确的密码", "密码"));
			return;
		}
		String chpwd = S.m(upwd); // 获取密码的MD5值
		if (chpwd == null) {
			this.renderJson(Ajax.message("密码md5解码为空", "帐号"));
			return;
		}
		// 验证密码
		AdminModel model = new AdminModel();
		AdminModel m = model.findFirst(String.format("select * from admin where uname='%s'", uname));
		if (m == null) {
			this.renderJson(Ajax.message("不能找到用户", "帐号"));
			return;
		}
		String duname = m.getStr("uname");
		String dpwd = m.getStr("upassword");

		// 解密
		String udpwd = S.d(dpwd); // 解密得到MD5值
		if (udpwd == null) {
			this.renderJson(Ajax.message("解密数据库密码为空", "帐号"));
			return;
		}

		if (!chpwd.equals(udpwd)) {
			this.renderJson(Ajax.message("密码不匹配", "帐号"));
			return;
		}

		// 登录成功
		AdminUser user = new AdminUser();
		user.setName(duname);
		this.setSessionAttr("ADMINI", user);

		// 更新密码
		if (m.set("upassword", S.e(chpwd)).update()) {
			this.renderJson(Ajax
			        .message(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral", "成功", true));
		} else {
			this.renderJson(Ajax
			        .message(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral", "成功", true));
		}

	}

	/**
	 * 管理员登录 For test
	 */
	public void _dologin() {
		String uname = this.getPara("username");
		String upwd = this.getPara("password");
		if (uname == null || uname.length() < 1) {
			this.renderJson(Ajax.message("请输入正确的帐号", "帐号"));
			return;
		}

		if (upwd == null || upwd.length() < 1) {
			this.renderJson(Ajax.message("请输入正确的密码", "密码"));
			return;
		}
		String chpwd = S.m(upwd); // 获取密码的MD5值
		if (chpwd == null) {
			this.renderJson(Ajax.message("密码md5解码为空", "帐号"));
			return;
		}
		// 验证密码
		AdminModel model = new AdminModel();
		AdminModel m = model.findFirst(String.format("select * from admin where uname='%s'", uname));
		if (m == null) {
			this.renderJson(Ajax.message("不能找到用户", "帐号"));
			return;
		}
		String duname = m.getStr("uname");
		String dpwd = m.getStr("upassword");

		// 解密
		String udpwd = S.d(dpwd); // 解密得到MD5值
		if (udpwd == null) {
			this.renderJson(Ajax.message("解密数据库密码为空", "帐号"));
			return;
		}

		// if(!chpwd.equals(udpwd)){
		// this.renderJson(Ajax.message("密码不匹配", "帐号"));
		// return;
		// }

		// 登录成功
		AdminUser user = new AdminUser();
		user.setName(duname);
		this.setSessionAttr("ADMINI", user);

		// 更新密码
		if (m.set("upassword", S.e(chpwd)).update()) {
			this.renderJson(Ajax
			        .message(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral", "成功", true));
		} else {
			this.renderJson(Ajax
			        .message(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral", "成功", true));
		}

	}

	public void out() {
		this.setSessionAttr("ADMINI", null);
		this.redirect(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/login");
	}

	/**
	 * 同步基础数据，仅用于系统管理使用
	 */
	public void syncBaseData() {
		// 删除本地的bank
		Db.update("delete from banks");
		Db.update("update sqlite_sequence set seq=0 where name='banks'");
		String schame = PropHelper.getDb2Config(GUIConstants.WEB_ROOT, "db.schema");
		// 查询总数
		List<Integer> countobject = Db.use("db2")
		        .query(String.format("SELECT count(*) FROM %sBS_BANK", StringHelper.isNullOrEmpty(schame) ? "" : schame
		                + "."));
		int count = 0;
		if (countobject != null && countobject.size() > 0) {
			count = countobject.get(0);
		}
		List<String> sqlbatch = new ArrayList<String>();

		if (count > 0) {
			// 计算分页大小
			double usersize = 100.0;
			double maxpage = Math.ceil(count / usersize);
			for (int i = 0; i < maxpage; i++) {
				// 分页查找
				int start = (int) (i * usersize);
				int end = (int) ((i + 1) * usersize);
				String sql = String
				        .format("SELECT * FROM (select ROW_NUMBER() OVER() AS ROWNUM,NO,NAME,PARENTNO,PARENTNAME,PCBNO,PCBNAME,CITYNO,CITYNAME,COUNTRYNO,COUNTRYNAME,ORGTYPENO,ORGTYPENAME,ISLEAF from %sBS_BANK) a where ROWNUM>%d and ROWNUM<=%d",
				                StringHelper.isNullOrEmpty(schame) ? "" : schame + ".", start, end);
				List<Object[]> items = Db.use("db2").query(sql);

				sqlbatch.clear();
				for (Object[] item : items) {
					String tmpsql = String
					        .format("insert into banks(no,name,parent_no,parent_name,pcb_no,pcb_name,city_no,city_name,country_no,country_name,orgtype_no,orgtype_name,isleaf) values('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s','%s', '%s', '%s', '%s', '%s')",
					                toTrim(item[1]), toTrim(item[2]), toTrim(item[3]), toTrim(item[4]),
					                toTrim(item[5]), toTrim(item[6]), toTrim(item[7]), toTrim(item[8]),
					                toTrim(item[9]), toTrim(item[10]), toTrim(item[11]), toTrim(item[12]),
					                toTrim(item[13]));
					sqlbatch.add(tmpsql);
				}
				Db.batch(sqlbatch, sqlbatch.size());
			}
		}
		System.out.println("Bank Flish");
		// 更新地区
		Db.update("delete from areas");
		// 查询总数
		List<Integer> areacount = Db.use("db2")
		        .query(String.format("SELECT count(*) FROM %sBS_AREA", StringHelper.isNullOrEmpty(schame) ? "" : schame
		                + "."));
		count = 0;
		if (areacount != null && areacount.size() > 0) {
			count = areacount.get(0);
		}

		if (count > 0) {
			double usersize = 100.0;
			double maxpage = Math.ceil(count / usersize);
			for (int i = 0; i < maxpage; i++) {
				// 分页查找
				int start = (int) (i * usersize);
				int end = (int) ((i + 1) * usersize);
				String sql = String
				        .format("SELECT * FROM (select ROW_NUMBER() OVER() AS ROWNUM,ID,NAME,PARENT,LEVEL,ISLEAF,SHORTNAME,STATUS,FLAG from %sBS_AREA) a where ROWNUM>%d and ROWNUM<=%d",
				                StringHelper.isNullOrEmpty(schame) ? "" : schame + ".", start, end);
				List<Object[]> items = Db.use("db2").query(sql);

				sqlbatch.clear();
				for (Object[] item : items) {
					String tmpsql = String
					        .format("insert into areas(id,name,parent,level,isleaf,short_name,status,flag) values(%s, '%s', %s, '%s', '%s', '%s', '%s', '%s')",
					                toTrim(item[1]), toTrim(item[2]), toTrim(item[3]), toTrim(item[4]),
					                toTrim(item[5]), toTrim(item[6]), toTrim(item[7]), toTrim(item[8]));
					sqlbatch.add(tmpsql);
				}
				Db.batch(sqlbatch, sqlbatch.size());
			}
		}
		System.out.println("Area Flish");
		// ORGTYPE

		Db.update("delete from orgtype");
		Db.update("update sqlite_sequence set seq=0 where name='orgtype'");
		String sql = String.format("SELECT NO,NAME,PARENTNO,PARENTNAME FROM %sBS_ORGTYPE", schame + ".");

		List<Object[]> orgtypes = Db.use("db2").query(sql);

		sqlbatch.clear();
		for (Object[] item : orgtypes) {
			sqlbatch.add(String.format(
			        "insert into orgtype(no,name,parent_no,parent_name) values('%s','%s','%s','%s')", toTrim(item[0]),
			        toTrim(item[1]), toTrim(item[2]), toTrim(item[3])));
		}
		Db.batch(sqlbatch, sqlbatch.size());
		System.out.println("Orgtype Flish");
		// 更新PCB
		Db.update("delete from pcb");
		Db.update("update sqlite_sequence set seq=0 where name='pcb'");
		sql = String.format("SELECT NO,NAME,PARENTNO,PARENTNAME,PCBNO,PCBNAME FROM %sBS_PCB", schame + ".");

		List<Object[]> pbcs = Db.use("db2").query(sql);

		sqlbatch.clear();
		for (Object[] item : pbcs) {
			sqlbatch.add(String
			        .format("insert into pcb(no,name,parent_no,parent_name,pcb_no,pcb_name) values('%s','%s','%s','%s','%s','%s')",
			                toTrim(item[0]), toTrim(item[1]), toTrim(item[2]), toTrim(item[3]), toTrim(item[4]),
			                toTrim(item[5])));
		}
		Db.batch(sqlbatch, sqlbatch.size());
		System.out.println("PCB Flish");
		this.renderText("OK");
	}

	private String toTrim(Object ib) {
		if (ib == null) {
			return null;
		}
		String str = ib.toString();
		return str.trim();
	}
}
