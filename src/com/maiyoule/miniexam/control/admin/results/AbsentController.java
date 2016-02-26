package com.maiyoule.miniexam.control.admin.results;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.entity.AreaItem;
import com.maiyoule.miniexam.entity.BankInfo;
import com.maiyoule.miniexam.entity.BankSelectBoxItem;
import com.maiyoule.miniexam.entity.ExamAreaCounter;
import com.maiyoule.miniexam.entity.UserInfo;
import com.maiyoule.miniexam.entity.UserType;
import com.maiyoule.miniexam.model.AreaModel;
import com.maiyoule.miniexam.model.BanksModel;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.LayoutsModel;
import com.maiyoule.miniexam.model.UserTypeModel;
import com.maiyoule.miniexam.model.UsersModel;
import com.maiyoule.miniexam.utils.ExamlayoutUtil;

/**
 * 缺考统计
 * @author shengli
 *
 */
public class AbsentController extends C {
	public void index(){
		int layoutid=this.getParaToInt("layoutid", 0);
		String wheresql="";
		if(layoutid>0){
			wheresql+=" layout_id="+layoutid+" and ";
		} else {
			wheresql+=" layout_id != 0 and ";
		}
		this.setAttr("layoutid", layoutid);
		String sql=String.format("select count(id) as al from exams where %s end_time<%d and layouts_status!=%d and layouts_status!=%d",wheresql,System.currentTimeMillis(),ExamlayoutUtil.LAYOUT_FLISH,ExamlayoutUtil.LAYOUT_CLOSE);
		ExamsModel ex=ExamsModel.dao.findFirst(sql);
		int count=0;
		if(ex!=null){
			count=ex.getInt("al");
		}
		this.setAttr("count", count);
		List<LayoutsModel> layouts=LayoutsModel.dao.find("select * from layouts where end_time<"+System.currentTimeMillis());
		this.setAttr("layouts", layouts);
		
		this.render("/contral/results/absent.html");
	}
	
	public String query(){
		int parentId=this.getParaToInt("parentid", 0);
		
		int layoutid=this.getParaToInt("layoutid", 0);
		
		List<AreaItem> areas=AreaModel.dao.findByParentId(parentId);
		
		List<ExamAreaCounter> counters =CacheKit.get("areacount", "absendcount_"+parentId+"_"+layoutid);//new ArrayList<ExamAreaCounter>();
		if(counters!=null){
			return this.ajaxMessage(counters, "Success", true);
		}
		counters=new ArrayList<ExamAreaCounter>();
		
		String layoutidsql=layoutid>0?" and layout_id="+layoutid:"layout_id!=0";
		
		for(AreaItem area:areas){

			ExamAreaCounter eac=new ExamAreaCounter();
			eac.setId(area.getId());
			eac.setParentId(area.getParent());
			eac.setName(area.getName());
			String areasql="";
			if(area.getLevel().equals("CITY")){
				eac.setHasChild(true);
				areasql+=" and city="+area.getId();
			}else if(area.getLevel().equals("COUNTRY")){
				eac.setHasChild(false);
				areasql+=" and country="+area.getId();
			}else{
				eac.setCount(0);
				counters.add(eac);
				continue;
			}
			
			//查询个数
			String sql=String.format("select count(id) as al from exams where layouts_status!=%d and layouts_status!=%d and end_time<%d %s %s",ExamlayoutUtil.LAYOUT_CLOSE,ExamlayoutUtil.LAYOUT_FLISH,System.currentTimeMillis(),layoutidsql,areasql);
			
			ExamsModel exam=ExamsModel.dao.findFirst(sql);
			if(exam==null){
				continue;
			}
			int c=exam.getInt("al");
			if(c<1){
				continue;
			}
			eac.setCount(c);
			counters.add(eac);
		}
		CacheKit.put("areacount", "absendcount_"+parentId+"_"+layoutid,counters);//new ArrayList<ExamAreaCounter>();
		return this.ajaxMessage(counters, "Success", true);
	}
	
	/**
	 * 查看用户
	 * @return
	 */
	public String users(){
		int layoutid=this.getParaToInt("layoutid", 0);
	
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
			wheresql += " and uu.city=" + city;
		}
		if (country > 0) {
			wheresql += " and uu.country=" + country;
		}
		if(banknos!=null&&banknos.length>0){
			wheresql+=" and uu.bank_path like '%"+StringUtils.join(banknos,GUIConstants.STRING_SPLITE)+"%'";
		}
		
		
		// 分页查找
		Page<ExamsModel> lists = ExamsModel.dao.paginate(page,
				GUIConstants.PAGE_SIZE, "select e.*",
				"from users_utypes as uu INNER JOIN exams as e on "+(layoutid>0?"e.layout_id="+layoutid+" and ":"")+"e.layouts_status<>"+ExamlayoutUtil.LAYOUT_FLISH+" and e.layouts_status<>"+ExamlayoutUtil.LAYOUT_CLOSE+" and uu.cardno = e.cardno AND uu.type_id = e.utype "+wheresql+" and e.layout_id <> 0 and e.end_time < "+System.currentTimeMillis()+" order by e.id desc");

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
		this.setAttr("layoutid", layoutid);
		this.setAttr("queryString", sb.toString());
		// 查询路径
		String sql = "select uu.bank_path from users_utypes as uu INNER JOIN exams as e ON ";
		if(layoutid>0){
			sql+="e.layout_id="+layoutid+" and ";
		}
		sql+="uu.cardno = e.cardno AND uu.type_id = e.utype";
		if (city > 0) {
			sql += " and uu.city=" + city;
		}
		if (country > 0) {
			sql += " and uu.country=" + country;
		}
		//if(banknos!=null&&banknos.length>0){
			//sql+=" and uu.bank_path like '%"+StringUtils.join(banknos,GUIConstants.STRING_SPLITE)+"%'";
		//}
		
		sql += " group by uu.bank_path";
		List<ExamsModel> models = ExamsModel.dao.find(sql);
		List<String> paths = new ArrayList<String>();
		for (ExamsModel em : models) {
			paths.add(em.getStr("bank_path"));
		}
		List<BankSelectBoxItem> treebox = BanksModel.dao.findTreeBox(paths);
		this.setAttr("treecbox", JSON.toJSONString(treebox));

		

		this.render("/contral/results/absentusers.html");
		
		
		return null;
	}
	
	/**
	 * 导出缺考信息
	 */
	public void export() {

		int layoutid=this.getParaToInt("layoutid", 0);
		
		StringBuffer sb = new StringBuffer();
		AreaModel area = null;
		StringBuffer fileName = new StringBuffer();
		int city = this.getParaToInt("city", 0);
		if (city > 0) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append("city=" + city);
			area = AreaModel.dao.findFirst("select name from areas where id=" + city);
			if(area != null) {
				fileName.append(area.getStr("name"));
			}
		}
		int country = this.getParaToInt("country", 0);
		if (country > 0) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append("country=" + country);
			area = AreaModel.dao.findFirst("select name from areas where id=" + country);
			if(area != null) {
				fileName.append(area.getStr("name"));
			}
		}
		fileName.append("缺考列表");
		
		String[] banknos = this.getParaValues("ubankNo");

		String wheresql=" and e.layout_id<>0 ";
		
		if (city > 0) {
			wheresql += " and uu.city=" + city;
		}
		if (country > 0) {
			wheresql += " and uu.country=" + country;
		}
		if(banknos!=null&&banknos.length>0){
			wheresql+=" and uu.bank_path like '%"+StringUtils.join(banknos,GUIConstants.STRING_SPLITE)+"%'";
		}
		
        HttpServletResponse response = this.getResponse();
        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Content-disposition",
                "attachment;filename=" + System.currentTimeMillis() + ".csv");
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            os.write(new byte []{( byte ) 0xEF ,( byte ) 0xBB ,( byte ) 0xBF });
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // 生成csv
        try {
            StringBuffer sbtitle = new StringBuffer();
            sbtitle.append("姓名");
            sbtitle.append(",");
            sbtitle.append("准考证号");
            sbtitle.append(",");
            sbtitle.append("所在金融机构");
            sbtitle.append(",");
            sbtitle.append("用户类型");
            sbtitle.append(",");
            sbtitle.append("考试时间");


            //处理中文乱码
            os.write(sbtitle.toString().getBytes(GUIConstants.CHARSET));
            os.write(GUIConstants.NEWLINE.getBytes(GUIConstants.CHARSET));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);

            List<ExamsModel> absents = ExamsModel.dao.find("select e.* from users_utypes as uu INNER JOIN exams as e on "+(layoutid>0?"e.layout_id="+layoutid+" and ":"")+"e.layouts_status<>"+ExamlayoutUtil.LAYOUT_FLISH+" and e.layouts_status<>"+ExamlayoutUtil.LAYOUT_CLOSE+" and uu.cardno = e.cardno AND uu.type_id = e.utype "+wheresql+" order by e.id desc");

            if (absents == null) {
            	return;
            }
            for (ExamsModel exam : absents) {

            	StringBuffer item = new StringBuffer();
            	UserInfo user = UsersModel.dao.getByCardNo(exam.getStr("cardno"));
            	if(user != null) {
            		item.append(user.getName());
            		item.append(",");
            		item.append(user.getCardno());
            		item.append(",");            		
            	} else {
            		item.append(",,");
            	}
            	
            	BankInfo bankInfo = BanksModel.dao.getByNo(exam.getStr("bankno"));
            	if(bankInfo != null) {
            		item.append(bankInfo.getName());
            		item.append(",");            		
            	} else {
            		item.append(",");
            	}
            	
            	UserType userType = UserTypeModel.dao.getById(exam.getInt("utype"));
            	if(userType != null) {
            		item.append(userType.getName());
            		item.append(",");            		
            	} else {
            		item.append(",");
            	}

            	LayoutsModel layout = LayoutsModel.dao.findFirst("select * from layouts where id=" + layoutid);
            	if(layout != null) {
            		item.append(sdf.format(new Date(layout.getLong("start_time"))));
            		item.append("-");
            		item.append(sdf.format(new Date(layout.getLong("end_time"))));         		
            	}

            	os.write(item.toString().getBytes(GUIConstants.CHARSET));
            	os.write(GUIConstants.NEWLINE.getBytes(GUIConstants.CHARSET));
            	os.flush();
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	
}
