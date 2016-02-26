package com.maiyoule.miniexam.control.admin.layouts;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.entity.BankSelectBoxItem;
import com.maiyoule.miniexam.entity.CustomJfinalPage;
import com.maiyoule.miniexam.model.AnswersModel;
import com.maiyoule.miniexam.model.BanksModel;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.LayoutsModel;
import com.maiyoule.miniexam.utils.StringHelper;

/**
 * 考试安排
 * 
 * @author shengli
 *
 */
public class LayoutController extends C {
	public void index() {

		int page = this.getParaToInt("page", 1);
		Page<LayoutsModel> layouts = LayoutsModel.dao.paginate(page,
				GUIConstants.PAGE_SIZE, "select *",
				"from layouts order by id desc");
		this.setAttr("lists", layouts);
		this.render("/contral/layouts/index.html");
	}

	public void add() {
		this.render("/contral/layouts/add.html");
	}

	public String insert() {
		String start = this.getPara("start", null);
		if (StringHelper.isNullOrEmpty(start)) {
			return this.ajaxMessage("请选择开始日期和时间", "开始");
		}
		String end = this.getPara("end", null);
		if (StringHelper.isNullOrEmpty(end)) {
			return this.ajaxMessage("请选择结束日期和时间", "结束");
		}

		// 转换为Unix时间戳
		Timestamp ttstart = Timestamp.valueOf(start);
		Timestamp ttend = Timestamp.valueOf(end);

		long lstart = ttstart.getTime();
		long lend = ttend.getTime();
		
		if (lstart <= System.currentTimeMillis()) {
			return this.ajaxMessage("开始时间不能小于当前时间", "时间不正确");
		}
		
		if((lend - lstart) > 1000*60*60*2) {
			return this.ajaxMessage("考试时间不能大于2小时！", "时间不正确");
		}
		if (lend <= lstart) {
			return this.ajaxMessage("开始时间不能在结束时间之后", "时间不正确");
		}

		LayoutsModel layouts = new LayoutsModel();
		LayoutsModel exist = LayoutsModel.dao.findFirst("select * from layouts where (start_time <  " +lstart+ " and end_time > " +lstart+ ") or (start_time < "+lend+"  and end_time > "+lend+") or(start_time < "+lstart+" and end_time > "+lend+") or (start_time > "+lstart+" and end_time < "+lend+")");
		if(exist != null) {
			return this.ajaxMessage("新增失败，考试时间有重复！", "失败");
		}
		layouts.set("start_time", lstart);
		layouts.set("end_time", lend);
		layouts.set("count", 0);
		if (layouts.save()) {
			return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral/layouts", "成功", true);
		} else {
			return this.ajaxMessage("新增失败，请稍后再试！", "失败");
		}

	}

	public void edit() {
		int id = this.getParaToInt("id", 0);
		if (id < 1) {
			this.error("无效数据请求");
			return;
		}
		LayoutsModel layout = LayoutsModel.dao.findById(id);
		long startTime = layout.get("start_time");
		if(startTime <= System.currentTimeMillis()) {
			this.index();
		} else {
			this.setAttr("layout", layout);
			this.render("/contral/layouts/edit.html");			
		}
	}

	/**
	 * 删除考试安排
	 * 
	 * @return
	 */
	public String del() {

		int id = this.getParaToInt("id", 0);
		if (id < 1) {
			return this.ajaxMessage("无效请求", "请求");
		}
		LayoutsModel layout = LayoutsModel.dao.findById(id);
		if (layout == null) {
			return this.ajaxMessage("未找到相关信息", "安排");
		}
		// 删除回答
		AnswersModel.dao.delByLayoutId(id);
		ExamsModel.dao.delByLayoutId(id);

		if (layout.delete()) {
			return this.ajaxMessage("删除成功", "成功", true);
		} else {
			return this.ajaxMessage("删除失败，请稍后再试", "失败");
		}
	}

	/**
	 * 修改考试安排
	 * 
	 * @return
	 */
	public String update() {
		int id = this.getParaToInt("id", 0);
		if (id < 1) {
			return this.ajaxMessage("无效请求", "无效");
		}
		String start = this.getPara("start", null);
		if (StringHelper.isNullOrEmpty(start)) {
			return this.ajaxMessage("请选择开始日期和时间", "开始");
		}
		String end = this.getPara("end", null);
		if (StringHelper.isNullOrEmpty(end)) {
			return this.ajaxMessage("请选择结束日期和时间", "结束");
		}

		// 转换为Unix时间戳
		Timestamp ttstart = Timestamp.valueOf(start);
		Timestamp ttend = Timestamp.valueOf(end);

		long lstart = ttstart.getTime();
		long lend = ttend.getTime();
		
		if (lstart <= System.currentTimeMillis()) {
			return this.ajaxMessage("开始时间不能小于当前时间", "时间不正确");
		}
		
		if (lend <= lstart) {
			return this.ajaxMessage("开始时间不能在结束时间之后", "时间不正确");
		}
				
		if((lend - lstart) > 1000*60*60*2) {
			return this.ajaxMessage("考试时间不能大于2小时！", "时间不正确");
		}				
				
		LayoutsModel layouts = LayoutsModel.dao.findById(id);// new
																// LayoutsModel();
		if (layouts == null) {
			return this.ajaxMessage("未找到相关信息", "安排");
		}
		layouts.set("start_time", lstart);
		layouts.set("end_time", lend);
		if (layouts.update()) {
			//更新考试信息
			String sql="update exams set start_time="+lstart+",end_time="+lend+" where layout_id="+id;
			Db.update(sql);
			return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral/layouts", "成功", true);
		} else {
			return this.ajaxMessage("修改失败，请稍后再试", "失败");
		}
	}

	/**
	 * 添加考试人员到指定时间
	 * 
	 * @return
	 */
	public String addusers() {
		int id = this.getParaToInt("id", 0);
		if (id < 1) {
			return this.error("无效请求");
		}

		// 查询

		ExamsModel count = ExamsModel.dao
				.findFirst("select count(id) as al from exams where layout_id=0");
		this.setAttr("count", count.getInt("al"));
		this.setAttr("layout_id", id);// 考试安排ID

		this.render("/contral/layouts/addusers.html");
		return null;
	}

	/**
	 * 设置用户
	 */
	public String setupusers() {
		int id = this.getParaToInt("layout_id", 0);
		if (id < 1) {
			return this.error("无效请求");
		}
		this.setAttr("layout_id", id);
		// 查询该地区的用户
		String cardno = this.getPara("cardno");
		int city = this.getParaToInt("ucity", 0);
		int country = this.getParaToInt("ucountry", 0);

		StringBuffer condition = new StringBuffer();
		StringBuffer queryString = new StringBuffer("layout_id="+id);
		if(StringUtils.isNotBlank(cardno)) {
			condition.append("uu.cardno='" + cardno +"'");
			queryString.append("&cardno=" + cardno);
		}
		if (city > 0) {
			if (condition.length() > 0) {
				condition.append(" and ");
				queryString.append("&");
			}
			condition.append("uu.city=" + city);
			queryString.append("&ucity=" + city);
		}
		if (country > 0) {
			if (condition.length() > 0) {
				condition.append(" and ");
				queryString.append("&");
			}
			condition.append("uu.country=" + country);
			queryString.append("ucountry=" + country);
		}
		int pageNum = this.getParaToInt("page", 1);

		// 金融机构
		String[] fillbanks = new String[] { "", "", "", "", "", "", "", "", "",
				"" };

		String[] banknos = this.getParaValues("ubankNo");
		if (banknos != null && banknos.length > 0) {
			for (int i = 0; i < banknos.length; i++) {
				fillbanks[i] = banknos[i];
				if (queryString.length() > 0) {
					queryString.append("&");
				}
				queryString.append("ubankNo=" + banknos[i]);
			}
			if (condition.length() > 0) {
				condition.append(" and ");
			}
			condition.append("uu.bank_path like '%"
					+ StringUtils.join(banknos, GUIConstants.STRING_SPLITE)
					+ "%'");
		}
		this.setAttr("fillbanks", fillbanks);
		
		this.setAttr("queryString", queryString.toString());

		// 分页查找
		Page<ExamsModel> lists = ExamsModel.dao
				.paginate(
						pageNum,
						GUIConstants.PAGE_SIZE,
						"select distinct e.*",
						"from users_utypes as uu INNER JOIN exams as e on e.layout_id=0 and uu.cardno = e.cardno AND uu.type_id = e.utype "
								+ (condition.length()>0?" and "+condition.toString():"") + " order by e.id desc");
		
	
		int count=	Db.queryInt("select count(distinct e.cardno) from users_utypes as uu INNER JOIN exams as e on e.layout_id=0 and uu.cardno = e.cardno AND uu.type_id = e.utype "
                                + (condition.length()>0?" and "+condition.toString():"") + " order by e.id desc");
		int totalPage=0; 
		if(lists.getPageSize() != 0 && count % lists.getPageSize() != 0){
		    totalPage =  count/lists.getPageSize() +1;
		}else{
		    totalPage =  count/lists.getPageSize() ;
		}
		 
		CustomJfinalPage cpage=new CustomJfinalPage(lists.getList(),lists.getPageNumber(),lists.getPageSize(),totalPage,count);
		this.setAttr("lists", cpage);

		// 查询路径
		StringBuffer sql = new StringBuffer("select uu.bank_path from users_utypes as uu INNER JOIN exams as e ON e.layout_id=0 and uu.cardno = e.cardno ");
		//sql.append(" AND uu.type_id = e.utype ");
		sql.append(condition.length()>0?" and "+condition.toString():"");
		
		sql.append(" group by uu.bank_path "); 
		sql.append(" order by uu.bank_name ASC "); 
		List<ExamsModel> models = ExamsModel.dao.find(sql.toString());
		List<String> paths = new ArrayList<String>();
		for (ExamsModel em : models) {
			paths.add(em.getStr("bank_path"));
		}
		List<BankSelectBoxItem> treebox = BanksModel.dao.findTreeBox(paths);
		this.setAttr("treebox", JSON.toJSONString(treebox));


		this.render("/contral/layouts/setupusers.html");
		return null;
	}
	
	
	/**
	 * 更新用户考试
	 * @return
	 */
	public String updateUsers(){
		String act=this.getPara("act");
		if(StringHelper.isNullOrEmpty(act)){
			return this.ajaxMessage("无效请求", "请求");
		}
		int layout_id=this.getParaToInt("layout_id", 0);
		if(layout_id<1){
			return this.ajaxMessage("无效请求", "请求");
		}
		LayoutsModel layout=LayoutsModel.dao.findById(layout_id);
		if(layout==null){
			return this.ajaxMessage("无效请求", "请求");
		}
		long start_time=layout.getLong("start_time");
		long end_time=layout.getLong("end_time");
		
		long currentTime=Calendar.getInstance().getTimeInMillis();
		if(currentTime > start_time){
		    return this.ajaxMessage("考试已经开始，不完成相关操作", "失败");
		}
		
		//单个
		if(act.equals("single")){
			int id=this.getParaToInt("id", 0);
			if(id<1){
				return this.ajaxMessage("请选择正确的用户", "用户");
			}
			
			ExamsModel exam=ExamsModel.dao.findById(id);
			if(exam==null){
				return this.ajaxMessage("请选择正确的用户", "用户");
			}
			
			//判断是否存在
			ExamsModel isExists = ExamsModel.dao
					.findFirst(
							"select id from exams where layout_id=? and cardno=? and bankno=? and utype=?",
							layout_id, 
							exam.getStr("cardno"), 
							exam.getStr("bankno"),
							exam.getInt("utype"));
			if (isExists != null) {
				return this.ajaxMessage("此用户已经存在于本场考试，不需要重新加入！", "成功", true);
			}
			
			exam.remove("id");
			exam.set("layout_id", layout_id);
			exam.set("start_time", start_time);
			exam.set("end_time", end_time);
			if(exam.save()){
				LayoutsModel.dao.updateCount(layout_id);
				return this.ajaxMessage("加入成功", "成功", true);
			}else{
				return this.ajaxMessage("加入失败，请稍后再试", "失败");
			}
			
		}else if(act.equals("select")){
			//选择的用户
			String[] ids=this.getParaValues("ids");
			if(ids==null||ids.length<1){
				return this.ajaxMessage("请选择要加入考试的用户","用户");
			}
			
			for(String id :ids){
				ExamsModel exam=ExamsModel.dao.findById(id);
				if(exam == null) {
					continue;
				}
				
				//判断是否存在
				ExamsModel isExists = ExamsModel.dao
						.findFirst(
								"select id from exams where layout_id=? and cardno=? and bankno=? and utype=?",
								layout_id, 
								exam.getStr("cardno"), 
								exam.getStr("bankno"),
								exam.getInt("utype"));
				if (isExists != null) {
					continue;
				}
				
				exam.remove("id");
				exam.set("layout_id", layout_id);
				exam.set("start_time", start_time);
				exam.set("end_time", end_time);
				exam.save();
			}

			LayoutsModel.dao.updateCount(layout_id);
			return this.ajaxMessage("加入成功", "成功", true);
			
		}else if(act.equals("all")){
			
			String cardno = this.getPara("cardno");
			int city = this.getParaToInt("ucity", 0);
			int country = this.getParaToInt("ucountry", 0);

			StringBuffer condition = new StringBuffer();
			if(StringUtils.isNotBlank(cardno)) {
				condition.append("uu.cardno=" + cardno);
			}
			if (city > 0) {
				if (condition.length() > 0) {
					condition.append(" and ");
				}
				condition.append("uu.city=" + city);
			}
			if (country > 0) {
				if (condition.length() > 0) {
					condition.append(" and ");
				}
				condition.append("uu.country=" + country);
			}
			String[] banknos = this.getParaValues("ubankNo");
			if (banknos != null) {
				if (condition.length() > 0) {
					condition.append(" and ");
				}
				condition.append("uu.bank_path like '%"
						+ StringUtils.join(banknos, GUIConstants.STRING_SPLITE)
						+ "%'");
			}
			
			
			// 查询路径
			String sql = "select e.* from users_utypes as uu INNER JOIN exams as e ON e.layout_id=0 and uu.cardno = e.cardno AND uu.type_id = e.utype";
			sql += (condition.length()>0?" and "+condition.toString():"");
			sql += " group by e.id";
			List<String> batchSql=new ArrayList<String>();
			
			List<ExamsModel> conditionExam=ExamsModel.dao.find(sql);
			for(ExamsModel ex:conditionExam){
				
				//判断是否存在
				ExamsModel isExists = ExamsModel.dao
						.findFirst(
								"select id from exams where layout_id=? and cardno=? and bankno=? and utype=?",
								layout_id, 
								ex.getStr("cardno"), 
								ex.getStr("bankno"),
								ex.getInt("utype"));
				if (isExists != null) {
					continue;
				}
				
				StringBuffer insertSql = new StringBuffer();
				insertSql.append("insert into exams (cardno, bankno, city, country, utype, layout_id, score, layouts_status, user_status, start_time, end_time, use_time, enter_time, exit_time, next_time, sign) values(");
				insertSql.append("'" + ex.getStr("cardno")).append("', ");
				insertSql.append("'" + ex.getStr("bankno")).append("', ");
				insertSql.append(ex.getInt("city")).append(", ");
				insertSql.append(ex.getInt("country")).append(", ");
				insertSql.append(ex.getInt("utype")).append(", ");
				insertSql.append(layout_id).append(", ");
				insertSql.append(ex.getInt("score")).append(", ");
				insertSql.append(ex.getInt("layouts_status")).append(", ");
				insertSql.append(ex.getInt("user_status")).append(", ");
				insertSql.append(start_time).append(", ");
				insertSql.append(end_time).append(", ");
				insertSql.append(ex.getInt("use_time")).append(", ");
				insertSql.append(ex.getInt("enter_time")).append(", ");
				insertSql.append(ex.getInt("exit_time")).append(", ");
				insertSql.append(ex.getInt("next_time")).append(", ");
				insertSql.append("'" + ex.getStr("sign")).append("')");
				//batchSql.add("update exams set layout_id="+layout_id+",start_time="+start_time+",end_time="+end_time+" where id="+ex.getInt("id"));
				batchSql.add(insertSql.toString());
				if(batchSql.size()>50){
					Db.batch(batchSql, batchSql.size());
					batchSql.clear();
				}
			}
			if(batchSql.size()>0){
				Db.batch(batchSql, batchSql.size());
				batchSql.clear();
			}
			LayoutsModel.dao.updateCount(layout_id);

			return this.ajaxMessage("加入成功", "成功", true);
			
		}
		return this.ajaxMessage("无效请求","请求");
	}

	/**
	 * 查看考试安排的用户
	 * @return
	 */
	public String viewusers() {
		int id=this.getParaToInt("id", 0);
		if(id<1){
			return this.error("无效请求");
		}
		StringBuffer queryString = new StringBuffer();
		queryString.append("&id=" + id);
		this.setAttr("queryString", queryString.toString());
		LayoutsModel layout=LayoutsModel.dao.findById(id);
		if(layout==null){
			return this.error("没有找到相关信息");
		}
		
		this.setAttr("layout", layout);
		
		int pageNumber=this.getParaToInt("page", 1);
		
		//查看列表
		Page<ExamsModel> lists=ExamsModel.dao.paginate(pageNumber, GUIConstants.PAGE_SIZE, "select *", "from exams where layout_id="+id+" order by id desc");
		
		this.setAttr("lists", lists);
		this.render("/contral/layouts/viewusers.html");
		
		return null;
	}
	
	/**
	 * 根据查询条件查询用户
	 * @return
	 */
	public String queryusers() {
		
		int layout_id=this.getParaToInt("layout_id", 0);
		String wheresql = " where layout_id = " + layout_id; 
		
		StringBuffer queryString = new StringBuffer();
		queryString.append("&layout_id=" + layout_id);
		
		String cardno = this.getPara("cardno", null);
		if (!StringHelper.isNullOrEmpty(cardno)) {
			queryString.append("&cardno=" + cardno);
			wheresql += " and cardno = '" + cardno + "'"; 
		}
		String bankno = this.getPara("bankno", null);
		if (!StringHelper.isNullOrEmpty(bankno)) {
			queryString.append("&bankno=" + bankno);
			wheresql += " and bankno = '" + bankno + "'"; 
		}
		if(layout_id<1){
			return this.error("无效请求");
		}
		
		LayoutsModel layout=LayoutsModel.dao.findById(layout_id);
		if(layout==null){
			return this.error("没有找到相关信息");
		}
		
		this.setAttr("layout", layout);
		this.setAttr("queryString", queryString.toString());
		
		int pageNumber=this.getParaToInt("page", 1);
		
		//查看列表
		Page<ExamsModel> lists=ExamsModel.dao.paginate(pageNumber, GUIConstants.PAGE_SIZE, "select *", "from exams "+wheresql+" order by id desc");
		
		this.setAttr("lists", lists);
		this.render("/contral/layouts/viewusers.html");
		
		return null;
	}
	
	
	/**
	 * 移除用户
	 * @return
	 */
	public String removeUser(){
		int id=this.getParaToInt("id", 0);
		if(id<1){
			return this.ajaxMessage("无效请求", "无效");
		}
		
		ExamsModel m=ExamsModel.dao.findById(id);
		if(m==null){
			return this.ajaxMessage("没有找到相关信息", "信息");
		}
		int layoutid=m.getInt("layout_id");


//		int layout_id=this.getParaToInt("layout_id", 0);
        if(layoutid<1){
            return this.ajaxMessage("无效请求", "请求");
        }
        LayoutsModel layout=LayoutsModel.dao.findById(layoutid);
        if(layout==null){
            return this.ajaxMessage("无效请求", "请求");
        }
        long start_time=layout.getLong("start_time");
//        long end_time=layout.getLong("end_time");
        
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if(currentTime > start_time){
            return this.ajaxMessage("考试已经开始，不完成相关操作", "失败");
        }
		
		if(!m.delete()){
			return this.ajaxMessage("删除失败", "失败");
		}
		LayoutsModel.dao.updateCount(layoutid);
		
		return this.ajaxMessage("删除成功", "成功", true);
	}
	
	public String removeSelectUser(){
		int layout_id=this.getParaToInt("layout_id", 0);
		if(layout_id<1){
			return this.ajaxMessage("无效请求", "无效");
		}
	        LayoutsModel layout=LayoutsModel.dao.findById(layout_id);
	        if(layout==null){
	            return this.ajaxMessage("无效请求", "请求");
	        }
	        long start_time=layout.getLong("start_time");
//	        long end_time=layout.getLong("end_time");
	        
	        long currentTime = Calendar.getInstance().getTimeInMillis();
	        if(currentTime > start_time){
	            return this.ajaxMessage("考试已经开始，不完成相关操作", "失败");
	        }
		String[] ids=this.getParaValues("ids");
		if(ids==null||ids.length<1){
			return this.ajaxMessage("请选择要移除的用户", "用户");
		}
		
		String sql="delete from exams where id in("+StringUtils.join(ids, ",")+")";
		Db.update(sql);

		LayoutsModel.dao.updateCount(layout_id);
		return this.ajaxMessage("移除成功", "成功", true);
		
		
	}
}
