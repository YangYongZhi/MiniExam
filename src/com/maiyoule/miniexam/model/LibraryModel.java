package com.maiyoule.miniexam.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 题库
 * 
 * @author shengli
 *
 */
public class LibraryModel extends Model<LibraryModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9064677452115635200L;

	public static LibraryModel dao = new LibraryModel();

	/**
	 * 更新统计计数
	 * @param libraryId
	 */
	public void updatecount(int libraryId) {
		LibraryModel library = this.findById(libraryId);
		if (library == null) {
			return;
		}
		// 获取个数
		int single = QuestionModel.dao.getCountByLibraryId(libraryId,
				QuestionModel.TYPE_SINGLE);
		int muti = QuestionModel.dao.getCountByLibraryId(libraryId,
				QuestionModel.TYPE_MUTI);
		int judge = QuestionModel.dao.getCountByLibraryId(libraryId,
				QuestionModel.TYPE_JUDGE);
		library.set("single_count", single);
		library.set("muti_count", muti);
		library.set("juge_count", judge);
		library.update();
	}
	
	/**
	 * 获取全部题库
	 * @return
	 */
	public List<LibraryModel> getAll(){
		return this.findByCache("questions", "all", "select * from library");
	}
	public LibraryModel findCById(int id){
		List<LibraryModel> libs=this.findByCache("questions", "lib_"+id, "select * from library where id="+id);
		if(libs!=null&&libs.size()>0){
			return libs.get(0);
		}
		return null;
	}
}
