package com.maiyoule.miniexam.control.admin.paper;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.entity.GenPaper;
import com.maiyoule.miniexam.entity.UserType;
import com.maiyoule.miniexam.model.LibraryModel;
import com.maiyoule.miniexam.model.PaperScaleModel;
import com.maiyoule.miniexam.model.PapersModel;
import com.maiyoule.miniexam.model.PapersUserTypeModel;
import com.maiyoule.miniexam.model.UserTypeModel;
import com.maiyoule.miniexam.services.PaperService;

public class PaperController extends C {
	
	public String index(){
		
		List<PapersModel> papers=PapersModel.dao.find("select * from papers");
		
		this.setAttr("papers", papers);
		this.render("/contral/paper/lists.html");
		return null;
	}
	
	
	public String edit(){
		int id=this.getParaToInt("id", 0);
		
		if(id<1){
			return this.error("无效的数据请求");
		}
		//获取试卷信息
		PapersModel paper=PapersModel.dao.findById(id);
		this.setAttr("paper", paper);
		
		//获取题库
		List<LibraryModel> librarys=LibraryModel.dao.getAll();
		this.setAttr("librarys", librarys);
		
		//用户类型
		List<UserType> usertypes=UserTypeModel.dao.getAll();
		this.setAttr("usertypes", usertypes);
		
		//获取设置比例
		List<PaperScaleModel> scales=PaperScaleModel.dao.getScaleByPaperId(id);
		this.setAttr("scales", scales);
		
		//获取绑定的用户
		List<PapersUserTypeModel> paperusertypes=PapersUserTypeModel.dao.getTypeByPaperId(id);
		this.setAttr("bindusertypes", paperusertypes);
		
		
		this.render("/contral/paper/edit.html");
		return null;
	}
	
	/**
	 * 试卷修改
	 * @return
	 */
	public String update(){
		int id=this.getParaToInt("id", 0);
		if(id<1){
			return this.ajaxMessage("无效的数据请求", "无效");
		}
		//题库
		String[] libs=this.getParaValues("libs");
		if(libs==null||libs.length<1){
			return this.ajaxMessage("请选择试题来源","试题");
		}
		//分值设置
		int singScore=this.getParaToInt("paperSingleScore", 0);
		int mutiScore=this.getParaToInt("paperMutiScore", 0);
		int jugeScore=this.getParaToInt("paperJugeScore", 0);
		//数量
		int singleCount=this.getParaToInt("paperSingleCount", 0);
		int mutiCount=this.getParaToInt("paperMutiCount", 0);
		int jugeCount=this.getParaToInt("paperJugeCount", 0);
		//总分
		int score=this.getParaToInt("paperScore", 0);
		int minScore=this.getParaToInt("paperMinScore", 0);
		
		//比例
		//用户类型
		String[] utypes=this.getParaValues("utype");
		if(utypes==null||utypes.length<1){
			return this.ajaxMessage("请选择用户类型","用户类型");
		}
		
		PapersModel paper=PapersModel.dao.findById(id);
		if(paper==null){
			return this.ajaxMessage("未找到相关试卷信息", "试卷");
		}
		paper.set("score", score);
		paper.set("min_score", minScore);
		paper.set("single_score",singScore);
		paper.set("muti_score", mutiScore);
		paper.set("juge_score", jugeScore);
		paper.set("single_count", singleCount);
		paper.set("muti_count", mutiCount);
		paper.set("juge_count", jugeCount);
		if(!paper.update()){
			return this.ajaxMessage("保存失败，请稍后再试","失败");
		}
		
		//比例
		//新增比例
		String[] addScaleIds=this.getParaValues("addscaleids");
		if(addScaleIds!=null&&addScaleIds.length>0){
			for(String newscale:addScaleIds){
				int bili_single=this.getParaToInt("bili_single_"+newscale, 0);
				int bili_muti=this.getParaToInt("bili_muti_"+newscale,0);
				int bili_juge=this.getParaToInt("bili_jugde_"+newscale, 0);
				
				PaperScaleModel paperscale=new PaperScaleModel();
				paperscale.set("paper_id", id);
				paperscale.set("library_id", newscale);
				paperscale.set("scale_single", bili_single);
				paperscale.set("scale_muti", bili_muti);
				paperscale.set("scale_juge", bili_juge);
				if(paperscale.save()){
					//移除libs中的id 以便进行修改比例
					if (libs != null && libs.length > 0) {
						for (int j = 0; j < libs.length; j++) {
							if (libs[j].equals(addScaleIds)) {
								libs[j] = "";
								break;
							}
						}
					}
					
				}
			}
		}
		//删除比例
		String[] removeIds=this.getParaValues("removeids");
		if(removeIds!=null&&removeIds.length>0){
			for(String rid:removeIds){
				PaperScaleModel.dao.deleteById(rid);
			}
		}
		//修改试卷比例
		if (libs != null && libs.length > 0) {
			// 修改
			for (String libid:libs) {
				if (libid.equals("")) {
					continue;
				}
				// 获取修改ID
				int scaleid = this.getParaToInt("scale_update_" + libid,0);
				if(scaleid<1){
					continue;
				}
				PaperScaleModel curpaperscale=PaperScaleModel.dao.findById(scaleid);

				int bili_single=this.getParaToInt("bili_single_"+libid, 0);
				int bili_muti=this.getParaToInt("bili_muti_"+libid,0);
				int bili_juge=this.getParaToInt("bili_jugde_"+libid, 0);

				curpaperscale.set("scale_single", bili_single);
				curpaperscale.set("scale_muti", bili_muti);
				curpaperscale.set("scale_juge", bili_juge);
				curpaperscale.update();
			}
		}
		//修改用户类型
		//获取绑定的用户类型
		List<PapersUserTypeModel> paperutypes=PapersUserTypeModel.dao.getTypeByPaperId(id);
		List<Integer> utypeadd=new ArrayList<Integer>();
		List<Integer> utypedel=new ArrayList<Integer>();
		//检查是否存在新增
		for(String utype:utypes){
			int utypeid=Integer.parseInt(utype);
			if(paperutypes==null){
				utypeadd.add(utypeid);
			}else{
				//检查是否存在
				boolean isExists=false;
				for(PapersUserTypeModel m:paperutypes){
					if(m.getInt("user_type_id")==utypeid){
						isExists=true;
						break;
					}
				}
				if(!isExists){
					utypeadd.add(utypeid);
				}
			}
		}
		//检查已有的是否存在提交的
		if(paperutypes!=null){
			for(PapersUserTypeModel m:paperutypes){
				boolean isExists=false;
				for(String utype:utypes){
					int utypeid=Integer.parseInt(utype);
					if(m.getInt("user_type_id")==utypeid){
						isExists=true;
						break;
					}
				}
				if(!isExists){
					utypedel.add(m.getInt("id"));
				}
			}
		}
		for(int typeadd:utypeadd){
			PapersUserTypeModel model=new PapersUserTypeModel();
			model.set("paper_id", id);
			model.set("user_type_id", typeadd);
			model.save();
		}
		for(int typedel:utypedel){
			PapersUserTypeModel.dao.deleteById(typedel);
		}
		
		
		CacheKit.remove("userpaper", "type_"+id);
		
		//修改成功
		return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral/papers", "成功",true);
		
		
	}
	
	
	/**
	 * 试卷打印
	 * @return
	 */
	public String printer(){
		int id=this.getParaToInt("id",0);
		if(id<1){
			return this.error("无效的数据请求");
		}

		//PapersModel paper=PapersModel.dao.findById(id);
		PaperService paperService=new PaperService();
		GenPaper genpaper=paperService.generatePaper(id);
		
		this.setAttr("genpaper", genpaper);

		this.setAttr("singlecount", genpaper.getQuestion().getRadio()!=null?genpaper.getQuestion().getRadio().size():0);
		this.setAttr("multicount", genpaper.getQuestion().getMulti()!=null?genpaper.getQuestion().getMulti().size():0);
		this.setAttr("judgecount", genpaper.getQuestion().getJudge()!=null?genpaper.getQuestion().getJudge().size():0);
		
		this.render("/contral/paper/print.html");
		return null;
	}
}
