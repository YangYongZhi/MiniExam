package com.maiyoule.miniexam.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.maiyoule.miniexam.utils.StringHelper;

public class QuestionModel extends Model<QuestionModel> {
	public static final String TYPE_SINGLE = "single";
	public static final String TYPE_MUTI = "muti";
	public static final String TYPE_JUDGE = "judge";

	/**
	 * 
	 */
	private static final long serialVersionUID = -8174917109145496389L;
	public static QuestionModel dao = new QuestionModel();

	/**
	 * 根据题库获取个数
	 * 
	 * @param libraryId
	 * @return
	 */
	public int getCountByLibraryId(int libraryId, String type) {
		QuestionModel questionModel = this
				.findFirst(String
						.format("select count(*) as al from questions where libid=%d and type='%s'",
								libraryId, type));
		if (questionModel == null) {
			return 0;
		}
		return questionModel.getInt("al");
	}

	/**
	 * 根据ID列表删除试题
	 * 
	 * @param ids
	 */
	public void deleteByIds(String[] ids) {
		if (ids == null || ids.length < 1) {
			return;
		}
		String idstr = StringHelper.join(ids, ",");
		Db.update("delete from questions where id in (" + idstr + ")");
	}

	/**
	 * 批量更新状态
	 * 
	 * @param ids
	 * @param status
	 */
	public void updateStatusByIds(String[] ids, int status) {

		if (ids == null || ids.length < 1) {
			return;
		}
		String idstr = StringHelper.join(ids, ",");
		Db.update("update questions set status=? where id in (?)", status,
				idstr);
	}

	public List<QuestionModel> findRandomByLirary(int libraryId, String type,
			int maxnumber) {
		//修改BUG，只从启用的题中选取
		return this
				.find("select * from questions where libid=? and type=? and status = 1 order by RANDOM() limit ?",
						libraryId, type, maxnumber);
	}

	public QuestionModel findCById(int id) {
		List<QuestionModel> tmplist = this.findByCache("questions",
				"questions_" + id, "select * from questions where id=" + id);
		if (tmplist != null && tmplist.size() > 0) {
			return tmplist.get(0);
		}
		return null;
	}
}
