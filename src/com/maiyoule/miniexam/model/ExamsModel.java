package com.maiyoule.miniexam.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.maiyoule.miniexam.entity.ExamlayoutStatus;

public class ExamsModel extends Model<ExamsModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -72710380474925649L;
	public static ExamsModel dao = new ExamsModel();

	/**
	 * 根据考试安排 删除考试
	 * 
	 * @param id
	 */
	public void delByLayoutId(int id) {
		Db.update("delete from exams where layout_id=?", id);
	}

	/**
	 * 根据标识删除
	 * 
	 * @param sign
	 */
	public void delBySign(String sign) {
		Db.update("delete from exams where sign=? and layout_id=?", sign, 0);
	}

	public int getCountByLayoutId(int id) {
		String sql = "select count(id) as al from exams where layout_id=" + id;
		ExamsModel m = this.findFirst(sql);
		if (m == null) {
			return 0;
		}
		return m.getInt("al");
	}

	/**
	 * 从缓存中读取考试信息
	 * 
	 * @param id
	 * @return
	 */
	public ExamsModel fCacheById(int id) {
		List<ExamsModel> m = ExamsModel.dao.findByCache("exams", "exams_" + id,
				"select * from exams where id=" + id);
		if (m == null) {
			return null;
		}
		return m.get(0);
	}

	public List<ExamlayoutStatus> findByIds(String[] ids) {
		List<ExamlayoutStatus> examstatus = new ArrayList<ExamlayoutStatus>();
		if (ids == null) {
			return examstatus;
		}
		if (ids.length < 1) {
			return examstatus;
		}
		String sql = "select id,layouts_status,user_status from exams where id in("
				+ StringUtils.join(ids, ",") + ")";

		List<ExamsModel> exams = this.find(sql);
		for (ExamsModel exam : exams) {
			examstatus.add(new ExamlayoutStatus(exam.getInt("id"), exam
					.getInt("layouts_status"), exam.getInt("user_status")));
		}

		return examstatus;
	}
	
	
	
	public int getCountExam(int layoutid,int type,String utypeids){
		String sql="select count(id) as al from exams where end_time>"+System.currentTimeMillis()+" and utype in("+utypeids+")";
		if(layoutid>0){
			sql+=" and layout_id="+layoutid;
		}
		sql+=" and layouts_status="+type;
		ExamsModel m=this.findFirst(sql);
		if(m==null){
			return 0;
		}
		return m.getInt("al");
	}
	
	
	public List<ExamsModel> findAllByLayoutid(String layoutid){
		String sql = "select * from exams where layout_id="+layoutid;
		
		return this.find(sql);
	}

}
