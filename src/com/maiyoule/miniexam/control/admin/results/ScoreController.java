package com.maiyoule.miniexam.control.admin.results;

import java.util.Iterator;
import java.util.List;

import org.apache.xmlbeans.impl.util.Base64;

import com.jfinal.plugin.activerecord.Page;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.model.AnswersModel;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.LayoutsModel;
import com.maiyoule.miniexam.utils.ExamlayoutUtil;
import com.maiyoule.miniexam.utils.StringHelper;

/**
 * 考试成绩
 * @author shengli
 *
 */
public class ScoreController extends C {
	
	public void index(){
		int layoutid=this.getParaToInt("layoutid", 0);
		String cardno=this.getPara("cardno","");
		
		//获取考试已经完成的安排
		List<LayoutsModel> layouts=LayoutsModel.dao.find("select * from layouts where start_time<"+System.currentTimeMillis());
		
		this.setAttr("layouts", layouts);
		
		int pageNumber=this.getParaToInt("page", 1);
		
		Page<ExamsModel> lists=ExamsModel.dao.paginate(pageNumber, GUIConstants.PAGE_SIZE, "select id,cardno,bankno,utype,layout_id,score", 
				"from exams where start_time<"+System.currentTimeMillis()+" and layouts_status="+ExamlayoutUtil.LAYOUT_FLISH
				+(StringHelper.isNullOrEmpty(cardno)?"":" and cardno = " + cardno)
				+(layoutid == 0 ? " and layout_id != 0 " : " and layout_id = " + layoutid));
		
		this.setAttr("lists", lists);
		this.setAttr("layoutid", layoutid);
		this.setAttr("cardno", cardno);
		
		String history="page="+pageNumber+"&layoutid="+layoutid+"&cardno="+cardno;
		history=new String(Base64.encode(history.getBytes()));
		this.setAttr("history", history);
		this.setAttr("queryString", "layoutid="+layoutid+"&cardno="+cardno);
		this.render("/contral/results/index.html");
	}
	
	/**
	 * 编辑成绩
	 */
	public String edit(){
		int id=this.getParaToInt("id", 0);
		if(id<1){
			return this.error("无效请求");
		}
		ExamsModel model=ExamsModel.dao.findById(id);
		if(model==null){
			return this.error("无效请求");
		}
		this.setAttr("exam", model);
		String history=this.getPara("history", "");
		this.setAttr("history", history);
		this.render("/contral/results/edit.html");
		return null;
	}
	
	public String update(){
		int score=this.getParaToInt("score", -1);
		if(score<0){
			return this.ajaxMessage("请输入成绩", "成绩");
		}
		
		int id=this.getParaToInt("id", 0);
		if(id<1){
			return this.ajaxMessage("无效请求", "请求");
		}
		
		ExamsModel model=ExamsModel.dao.findById(id);
		if(model==null){
			return this.ajaxMessage("无效请求", "请求");
		}
		
		model.set("score", score);
		if(model.update()){
			String history=this.getPara("history", "");
			if(!StringHelper.isNullOrEmpty(history)){
				history=new String(Base64.decode(history.getBytes()));
			}
			return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH) + "/contral/results/score?"+history, "成功", true);
			
			
		}else{
			return this.ajaxMessage("修改失败，请稍后再试", "失败");
		}
	}
	
	
	public String answer(){
		int id=this.getParaToInt("id", 0);
		if(id<1){
			return this.error("无效请求");
		}
		
		int pageNum=this.getParaToInt("page", 1);
		
		Page<AnswersModel> answers=AnswersModel.dao.paginate(pageNum, GUIConstants.PAGE_SIZE, "select *", "from answers where exam_id="+id);
		
		// Fill the null status.
		List<AnswersModel> answersData= answers.getList();
		for (Iterator<AnswersModel>  iterator = answersData.iterator(); iterator.hasNext();) {
	        AnswersModel answersModel = iterator.next();
	        if(answersModel.get("status")==null){
	        	// If there is a null status of this answer, we can consider as wrong.
	        	answersModel.set("status", 0);
	        }
        }
		
		this.setAttr("lists", answers);
		
		this.setAttr("queryString", "id="+id);
		
		this.render("/contral/results/answers.html");
		return null;
	}
}
