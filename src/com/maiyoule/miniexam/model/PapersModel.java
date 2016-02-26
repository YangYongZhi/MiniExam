package com.maiyoule.miniexam.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.entity.Paper;

public class PapersModel extends Model<PapersModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7113332739838320703L;
	public static PapersModel dao=new PapersModel();
	
	public Paper findPaperByCardno(String cardno){
		
		//默认考非数据报送的试卷
		String paperId = GUIConstants.NON_DATA_PAPER;
		List<UsersUtypesModel> utypemodels=UsersUtypesModel.dao.find("select type_id from users_utypes where cardno=?",cardno);
		for(UsersUtypesModel uum : utypemodels) {
			Integer typeId = uum.getInt("type_id");
			if(typeId==7 || typeId==8) {
				paperId = GUIConstants.DATA_PAPER;
				break;
			}
		}
		
		Paper paper=CacheKit.get("userpaper", "paperid_"+paperId);
		if(paper!=null){
			return paper;
		}
		
		PapersModel m=this.findFirst("select p.id, p.name, p.status, p.score, p.min_score from papers as p where id="+paperId);
		if(m==null){
			return null;
		}
		paper=new Paper();
		paper.setId(m.getInt("id"));
		paper.setName(m.getStr("name"));
		paper.setStatus(m.getInt("status"));
		paper.setScore(m.getInt("score"));
		paper.setMinScore(m.getInt("min_score"));
		
		CacheKit.put("userpaper", "paperid_"+paperId, paper);
		return paper;
	}
	
	public Paper findByUtype(Integer utype){

		//默认考非数据报送的试卷
		String paperId = GUIConstants.NON_DATA_PAPER;
		if(utype==7 || utype==8) {
			paperId = GUIConstants.DATA_PAPER;
		}

		Paper paper=CacheKit.get("userpaper", "paperid_"+paperId);
		if(paper!=null){
			return paper;
		}

		PapersModel m=this.findFirst("select p.id, p.name, p.status, p.score, p.min_score from papers as p where id="+paperId);
		if(m==null){
			return null;
		}
		paper=new Paper();
		paper.setId(m.getInt("id"));
		paper.setName(m.getStr("name"));
		paper.setStatus(m.getInt("status"));
		paper.setScore(m.getInt("score"));
		paper.setMinScore(m.getInt("min_score"));

		CacheKit.put("userpaper", "paperid_"+paperId, paper);
		return paper;
	}
	
}
