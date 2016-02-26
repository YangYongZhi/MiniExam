package com.maiyoule.miniexam.control.admin.exam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.entity.AreaItem;
import com.maiyoule.miniexam.entity.BankSelectBoxItem;
import com.maiyoule.miniexam.entity.ExamAreaCounter;
import com.maiyoule.miniexam.entity.OrgTypeItem;
import com.maiyoule.miniexam.entity.RandomReport;
import com.maiyoule.miniexam.entity.RandomReportItem;
import com.maiyoule.miniexam.entity.UserType;
import com.maiyoule.miniexam.model.AnswersModel;
import com.maiyoule.miniexam.model.AreaModel;
import com.maiyoule.miniexam.model.BankTreeItem;
import com.maiyoule.miniexam.model.BanksModel;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.OrgtypeModel;
import com.maiyoule.miniexam.model.UserTypeModel;
import com.maiyoule.miniexam.model.UsersUtypesModel;
import com.maiyoule.miniexam.utils.StringHelper;

public class ExamController extends C {

	public void index() {
		ExamsModel count = ExamsModel.dao
				.findFirst("select count(id) as al from exams where layout_id=0");
		this.setAttr("count", count.getInt("al"));
		this.render("/contral/exam/index.html");
	}

	public String ajaxExam() {
		int parentId = this.getParaToInt("parentid", 0);
		if (parentId < 1) {
			return this.ajaxMessage("无效请求", "无效");
		}
		List<ExamAreaCounter> counters = CacheKit.get("areacount",
				"examareacounter_" + parentId);// cache.get("examareacounter_"+parentId);
		if (counters != null&&counters.size()>0) {
			return this.ajaxMessage(counters, "成功", true);
		}
		counters = new ArrayList<ExamAreaCounter>();
		List<AreaItem> areas = AreaModel.dao.findByParentId(parentId);
		for (AreaItem area : areas) {
			ExamAreaCounter eac = new ExamAreaCounter();
			eac.setId(area.getId());
			eac.setParentId(area.getParent());
			eac.setName(area.getName());
			String sql = "";
			if (area.getLevel().equals("COUNTRY")) {
				// 地区
				eac.setHasChild(false);
				sql = String
						.format("select count(id) as al from exams where country=%s and layout_id=0",
								eac.getId());
			} else {
				// 城市
				eac.setHasChild(true);
				sql = String
						.format("select count(id) as al from exams where city=%s and layout_id=0",
								eac.getId());
			}
			ExamsModel cmodel = ExamsModel.dao.findFirst(sql);
			int c = cmodel.getInt("al");
			if (c < 1) {
				continue;
			}
			eac.setCount(c);
			counters.add(eac);
		}
		CacheKit.put("areacount", "examareacounter_" + parentId, counters);
		// cache.set("examareacounter_"+parentId, counters, 300);

		return this.ajaxMessage(counters, "成功", true);
	}

	/**
	 * 指定生成
	 */
	public void normal() {
		List<OrgTypeItem> orgtypes = OrgtypeModel.dao.getAll();
		this.setAttr("orgtypes", orgtypes);
		List<UserType> utypes = UserTypeModel.dao.getAll();
		this.setAttr("utypes", utypes);
		this.render("/contral/exam/normal.html");
	}

	/**
	 * 常规生成
	 */
	public String gennormal() {

		StringBuffer queryString = new StringBuffer();
		int city = this.getParaToInt("ucity", 0);
		if (city > 0) {
			queryString.append("&ucity=" + city);
		}
		int country = this.getParaToInt("ucountry", 0);
		if (country > 0) {
			queryString.append("&ucountry=" + country);
		}
		int utype = this.getParaToInt("utype", 0);
		if (utype > 0) {
			queryString.append("&utype=" + utype);
		}
		String orgtype = this.getPara("orgtype", null);
		
		//修改BUG，不为空才添加该条件，这里加入了"null"字符，导致永远只查第一页的数据（每次只能生成10条），因为后面拼装了查询条件 like '%null%'
		if (!StringHelper.isNullOrEmpty(orgtype)) {
			queryString.append("&orgtype=" + orgtype);
		}
		String[] ubankNos = this.getParaValues("ubankNo");
		if (ubankNos != null && ubankNos.length > 0) {
			for (String no : ubankNos) {
				if (no != null && !no.equals("")) {
					queryString.append("&ubankNo=" + no);
				}
			}
		}

		String cardno = this.getPara("ucardno", null);
		if (cardno != null && !cardno.equals("")) {
			queryString.append("&ucardno=" + cardno);
		}

		long sign = this.getParaToLong("sign", System.currentTimeMillis());
		queryString.append("&sign=" + sign);

		int pageNum = this.getParaToInt("pageNum", 1);

		// 生成
		Page<UsersUtypesModel> lists = UsersUtypesModel.dao.getPage(city,
				country, utype, orgtype, ubankNos, cardno, pageNum);
		if (lists == null) {
			// 没有了
			return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral/exam/resultuser?sign=" + sign, "", true);
		}
		// 加入列表
		if (lists.getPageNumber() > lists.getTotalPage()) {
			return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral/exam/resultuser?sign=" + sign, "", true);
		}
		List<UsersUtypesModel> utypeslist = lists.getList();
		List<String> batchsql = new ArrayList<String>();
		
		//修改BUG，bank_no参数错误
		Set<String> uninserted = new HashSet<String>();
		for (UsersUtypesModel uum : utypeslist) {
			// 检查是否重复
			ExamsModel isExists = ExamsModel.dao
					.findFirst(
							"select id from exams where layout_id=0 and cardno=?",
							uum.getStr("cardno"));
			if ((isExists != null) || uninserted.contains(uum.getStr("cardno"))) {
				continue;
			}
			StringBuffer sb = new StringBuffer();
			sb.append("insert into exams(cardno,bankno,city,country,utype,sign) values(");
			sb.append("'" + uum.getStr("cardno") + "',");
			sb.append("'" + uum.getStr("bank_no") + "',");
			sb.append(uum.getInt("city"));
			sb.append(",");
			sb.append(uum.getInt("country"));
			sb.append(",");
			sb.append(uum.getInt("type_id"));
			sb.append(",'");
			sb.append(sign);
			sb.append("');");

			batchsql.add(sb.toString());
			uninserted.add(uum.getStr("cardno"));
			
			if(batchsql.size()>30){
				Db.batch(batchsql, batchsql.size());
				batchsql.clear();
				uninserted.clear();
			}
		}
		if (batchsql.size() > 0) {
			Db.batch(batchsql, batchsql.size());
			uninserted.clear();
			uninserted.clear();
		}
		// 继续下一页
		String nexturl = this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral/exam/gennormal?pageNum=" + (pageNum + 1) + queryString.toString();
		String flished = String.format("%.2f",
				(lists.getPageNumber() / (lists.getTotalPage() + 0.0)) * 100);
		return this.ajaxMessage(nexturl, flished, true);
	}

	/**
	 * 查看生成结果
	 * 
	 * @return
	 */
	public String resultuser() {
		String sign = this.getPara("sign", null);
		if (sign == null) {
			return this.error("无效请求");
		}
		this.setAttr("sign", sign);
		this.setAttr("queryString", "sign=" + sign);
		int pageNum = this.getParaToInt("page", 1);
		// 查询考试列表
		Page<ExamsModel> lists = ExamsModel.dao.paginate(pageNum,
				GUIConstants.PAGE_SIZE, "select *", "from exams where sign='"
						+ sign + "'");
		this.setAttr("lists", lists);
		this.render("/contral/exam/resultuser.html");
		return null;
	}

	/**
	 * 清理结果
	 * 
	 * @return
	 */
	public String clearResultUser() {
		String sign = this.getPara("sign", null);
		if (sign == null) {
			return this.ajaxMessage("无效请求", "请求");
		}

		ExamsModel.dao.delBySign(sign);
		return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral/exam", "撤销成功", true);
	}

	/**
	 * 删除用户
	 * 
	 * @return
	 */
	public String delExamUser() {
		int id = this.getParaToInt("id", 0);
		if (id < 1) {
			return this.ajaxMessage("无效请求", "请求");
		}
		// 删除回答
		AnswersModel.dao.delByExamId(id);

		if (ExamsModel.dao.deleteById(id)) {
			return this.ajaxMessage("删除成功", "成功", true);
		} else {
			return this.ajaxMessage("删除失败", "失败");
		}
	}
	
	/**
	 * 删除用户
	 * 
	 * @return
	 */
	public String batchDelExamUser() {
		String[] ids=this.getParaValues("ids");
		if(ids==null||ids.length<1){
			return this.ajaxMessage("请选择要移除的用户", "用户");
		}
		
		// 删除回答
		try {
			AnswersModel.dao.delByQuestionIds(ids);
			String sql="delete from exams where id in("+StringUtils.join(ids, ",")+")";
			Db.update(sql);
			return this.ajaxMessage("删除成功", "成功", true);
		} catch(Exception e) {
			return this.ajaxMessage("删除失败", "失败");			
		}
	}

	/**
	 * 查看考试用户
	 */
	public void views() {
		StringBuffer sb = new StringBuffer();
		int city = this.getParaToInt("city", 0);
		if (city > 0) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append("city=" + city);
		}
		int country = this.getParaToInt("country", 0);
		if (country > 0) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append("country=" + country);
		}

		String[] banknos = this.getParaValues("ubankNo");

		int page = this.getParaToInt("page", 1);

		String wheresql="";
		
		if (city > 0) {
			wheresql += " and uu.city = e.city and uu.city=" + city;
		}
		if (country > 0) {
			wheresql += " and uu.country = e.country and uu.country=" + country;
		}
		if(banknos!=null&&banknos.length>0){
			wheresql+=" and uu.bank_path like '%"+StringUtils.join(banknos,GUIConstants.STRING_SPLITE)+"%'";
		}
		
		
		// 分页查找
		Page<ExamsModel> lists = ExamsModel.dao.paginate(page,
				GUIConstants.PAGE_SIZE, "select tmp.* ",
				"from (select distinct e.* from users_utypes as uu, exams as e where e.layout_id=0 and uu.cardno = e.cardno AND uu.type_id = e.utype"+wheresql+" order by e.id desc) as tmp");

		this.setAttr("lists", lists);
		String[] fillbanks = new String[] { "", "", "", "", "", "", "", "", "",
				"" };

		if (banknos != null) {
			for (int i = 0; i < banknos.length; i++) {
				fillbanks[i] = banknos[i];
				if (sb.length() > 0) {
					sb.append("&");
				}
				sb.append("ubankNo=" + banknos[i]);
			}
		}
		this.setAttr("city", city);
		this.setAttr("country", country);
		this.setAttr("fillbanks", fillbanks);
		this.setAttr("queryString", sb.toString());
		// 查询路径
		String sql = "select uu.bank_path from users_utypes as uu INNER JOIN exams as e ON e.layout_id=0 and uu.cardno = e.cardno AND uu.type_id = e.utype";
		if (city > 0) {
			sql += " and uu.city=" + city;
		}
		if (country > 0) {
			sql += " and uu.country=" + country;
		}
		if(banknos!=null&&banknos.length>0){
			sql+=" and uu.bank_path like '%"+StringUtils.join(banknos,GUIConstants.STRING_SPLITE)+"%'";
		}
		
		sql += " group by uu.bank_path";
		List<ExamsModel> models = ExamsModel.dao.find(sql);
		List<String> paths = new ArrayList<String>();
		for (ExamsModel em : models) {
			paths.add(em.getStr("bank_path"));
		}
		List<BankSelectBoxItem> treebox = BanksModel.dao.findTreeBox(paths);
		this.setAttr("treebox", JSON.toJSONString(treebox));

		this.render("/contral/exam/view.html");
	}

	/**
	 * 随机生成用户
	 * 
	 * @return
	 */
	public String random() {

		// 获取全部类型
		List<UserType> utypes = UserTypeModel.dao.getAll();
		this.setAttr("utypes", utypes);

		// 机构类型
		List<OrgTypeItem> orgtypes = OrgtypeModel.dao.getAll();
		this.setAttr("orgtypes", orgtypes);

		// 全部地区

		List<AreaItem> areas = AreaModel.dao.findAll();
		this.setAttr("areasjson", JSON.toJSONString(areas));
		this.render("/contral/exam/random.html");
		return null;
	}

	/**
	 * 查询金融机构
	 * 
	 * @return
	 */
	public String querybank() {
		String[] areas = this.getParaValues("areas");
		String[] orgtype = this.getParaValues("orgtype");
		List<BankTreeItem> items = BanksModel.dao.findByAreaAndType(areas,
				orgtype);

		return this.ajaxMessage(items, "成功", true);
	}

	/**
	 * 随机生成考试人员
	 * 
	 * @return
	 */
	public String genRandom() {
		int usersacle = this.getParaToInt("usersacle", 0);
		if (usersacle < 0) {
			return this.ajaxMessage("请输入正确的抽取比例", "比例");
		}
		String[] banks = this.getParaValues("bank");
		String[] selectusertypes = this.getParaValues("utype");
		String[] usertypesacle = this.getParaValues("usertypesacle");

		StringBuffer condition = new StringBuffer();
		if (banks != null && banks.length > 0) {
			if (condition.length() > 0) {
				condition.append(" and ");
			}
			condition.append("bank_no in ('" + (StringUtils.join(banks, "','"))
					+ "')");
		}
		if (selectusertypes != null && selectusertypes.length > 0) {
			if (condition.length() > 0) {
				condition.append(" and ");
			}
			condition.append("type_id in("
					+ (StringUtils.join(selectusertypes, ",")) + ")");
		}

		RandomReport report=new RandomReport();

		// 查询总个数
		String sql = "select count(id) as al from users_utypes";
		if (condition.length() > 0) {
			sql += " where " + condition.toString();
		}
		UsersUtypesModel totalobj = UsersUtypesModel.dao.findFirst(sql);
		int count = totalobj.getInt("al");

		report.setTotal(count);
		report.setScale(usersacle);
		
		// 实际抽取的数量
		int randomcount = (int) Math.ceil(count * (usersacle / 100.0));
		int truecount=0;
		List<RandomReportItem> ritem=new ArrayList<RandomReportItem>();
		if (usertypesacle != null && usertypesacle.length > 0) {
			long sign = System.currentTimeMillis();
			String tmpconditionbank = "";
			if (banks != null && banks.length > 0) {
				tmpconditionbank = "bank_no in ('"
						+ (StringUtils.join(banks, "','")) + "')";
			}
			report.setSign(sign);
			
			List<String> batchSql=new ArrayList<String>();
			for (int i = 0; i < usertypesacle.length; i++) {
				int randomMax = (int) Math.ceil(randomcount
						* (Integer.parseInt(usertypesacle[i]) / 100.0));
				StringBuffer tmpcondition = new StringBuffer(tmpconditionbank);
				if (tmpcondition.length() > 0) {
					tmpcondition.append(" and ");
				}
				tmpcondition.append("type_id=" + selectusertypes[i]);
				tmpcondition.append(" order by random() limit " + randomMax);
				String tmpsql = "select * from users_utypes";
				if (tmpcondition.length() > 0) {
					tmpsql += " where " + tmpcondition.toString();
				}

				List<UsersUtypesModel> users = UsersUtypesModel.dao
						.find(tmpsql);
				RandomReportItem rchilditem=new RandomReportItem();
				rchilditem.setCount(users.size());
				rchilditem.setScale(Integer.parseInt(usertypesacle[i]));
				rchilditem.setTypeId(Integer.parseInt(selectusertypes[i]));
				
				ritem.add(rchilditem);
				
				batchSql.clear();
				Set<String> uninserted = new HashSet<String>();
				//保存
				for (UsersUtypesModel uum : users) {
					// 检查是否重复
					ExamsModel isExists = ExamsModel.dao
							.findFirst(
									"select id from exams where layout_id=0 and cardno=?",
									uum.getStr("cardno"));
					if ((isExists != null) || uninserted.contains(uum.getStr("cardno"))) {
						continue;
					}
					StringBuffer sb = new StringBuffer();
					sb.append("insert into exams(cardno,bankno,city,country,utype,sign) values(");
					sb.append("'" + uum.getStr("cardno") + "',");
					sb.append("'" + uum.getStr("bank_no") + "',");
					sb.append(uum.getInt("city"));
					sb.append(",");
					sb.append(uum.getInt("country"));
					sb.append(",");
					sb.append(uum.getInt("type_id"));
					sb.append(",'");
					sb.append(sign);
					sb.append("');");
					batchSql.add(sb.toString());
					uninserted.add(uum.getStr("cardno"));
					
					if(batchSql.size()>50){
						Db.batch(batchSql, batchSql.size());
						batchSql.clear();
						uninserted.clear();
					}
					
					truecount++;
				}
				if(batchSql.size()>0){
					Db.batch(batchSql, batchSql.size());
					batchSql.clear();
					uninserted.clear();
				}
			}
		}
		report.setList(ritem);
		report.setFinalcount(truecount);
		//放入Session
		this.setSessionAttr("randresult",report);
		return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral/exam/randomReport", "成功",true);
	}
	
	/**
	 * 随机结果报告
	 */
	public String randomReport(){
		RandomReport report=this.getSessionAttr("randresult");
		
		if(report==null){
			return this.error("没有正确随机生成考试用户");
		}
		
		this.setAttr("report", report);
		this.render("/contral/exam/randomresult.html");
		return null;
	}



}
