package com.maiyoule.miniexam.control.admin.results;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Page;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.model.LibraryModel;
import com.maiyoule.miniexam.model.QuestionModel;

/**
 * 试卷分析
 * 
 * @author shengli
 *
 */
public class AnalysisController extends C {

    public void index() {
        int page = this.getParaToInt("page", 1);
        int lid = this.getParaToInt("lid", 0);
        String type = this.getPara("type", "");
        StringBuffer sb = new StringBuffer();
        StringBuffer queryString = new StringBuffer();
        if (lid > 0) {
            sb.append("libid=" + lid);
            queryString.append("lid=" + lid);
        }
        if (StringUtils.isNotBlank(type)) {
            if (sb.length() > 0) {
                sb.append(" and ");
                queryString.append("&");
            }
            sb.append("type='" + type + "'");
            queryString.append("type=" + type);
        }
        String key = String.format("p_%d_id_%d_t_%s", page, lid, type);

        Page<QuestionModel> questions = QuestionModel.dao.paginateByCache("answer", key, page,
                GUIConstants.PAGE_SIZE, "select *", "from questions"
                        + (sb.length() > 0 ? " where " + sb.toString() : ""));

        this.setAttr("lists", questions);
        this.setAttr("lid", lid);
        this.setAttr("type", type);
        String querystr = queryString.toString();
        if (StringUtils.isNotBlank(querystr)) {
            this.setAttr("queryString", querystr);
        }
        //
        List<LibraryModel> librarys = LibraryModel.dao.getAll();
        this.setAttr("librarys", librarys);
        this.render("/contral/results/analisys.html");
    }
}
