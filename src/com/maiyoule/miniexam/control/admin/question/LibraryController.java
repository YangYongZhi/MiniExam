package com.maiyoule.miniexam.control.admin.question;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.model.LibraryModel;
import com.maiyoule.miniexam.utils.StringHelper;

public class LibraryController extends C {
    public void index() {

        String page = this.getPara("page", null);
        if (page == null) {
            page = "1";
        }
        int p = Integer.parseInt(page);

        // 查询题库
        Page<LibraryModel> librarys = LibraryModel.dao.paginate(p, GUIConstants.PAGE_SIZE,
                "select * from library order by id asc", "");
        this.setAttr("lists", librarys);
        this.render("/contral/library/list.html");
    }

    /**
     * 新增题库
     */
    public void add() {
        this.render("/contral/library/add.html");
    }

    /**
     * 新插入
     */
    public String insert() {

        String libname = this.getPara("libname");
        if (libname == null || libname.equals("")) {
            return this.ajaxMessage("请输入题库名称", "题库");
        }
        // 检查是否存在
        LibraryModel lbl = LibraryModel.dao.findFirst(String.format(
                "select * from library where name='%s'", libname));
        if (lbl != null) {
            return this.ajaxMessage("已存在的题库", "题库");
        }
        lbl = new LibraryModel();
        lbl.set("name", libname);
        lbl.set("single_count", 0);
        lbl.set("muti_count", 0);
        lbl.set("juge_count", 0);
        if (lbl.save()) {
            return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH)
                    + "/contral/lib", "成功", true);
        } else {
            return this.ajaxMessage("新增题库失败", "失败");
        }
    }

    /**
     * 修改
     */
    public String update() {
        String id = this.getPara("id");
        if (!StringHelper.isInteger(id)) {
            return this.error("无效的请求");
        }
        // 查找题库
        LibraryModel lib = LibraryModel.dao.findById(id);
        if (lib == null) {
            return this.error("未找到相关的题库");
        }
        this.setAttr("lib", lib);
        this.render("/contral/library/update.html");
        return null;
    }

    /**
     * 执行修改
     * 
     * @return
     */
    public String doupdate() {
        String id = this.getPara("id");
        if (!StringHelper.isInteger(id)) {
            return this.ajaxMessage("无效的请求");
        }
        String libname = this.getPara("libname");
        if (libname == null || libname.equals("")) {
            return this.ajaxMessage("请输入题库名称", "题库");
        }
        // 查找题库
        LibraryModel lib = LibraryModel.dao.findById(id);
        if (lib == null) {
            return this.ajaxMessage("未找到相关的题库", "题库");
        }

        if (lib.getStr("name").equals(libname)) {
            return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH)
                    + "/contral/lib", "成功", true);
        }
        // 不同，查询是否存在相同的名称
        LibraryModel existslblcount = LibraryModel.dao.findFirst(String.format(
                "select count(*) a from library where name='%s'", libname));
        if (existslblcount != null && existslblcount.getInt("a") > 1) {
            return this.ajaxMessage("已存在的题库", "题库");
        }
        // 修改
        if (lib.set("name", libname).update()) {
            return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH)
                    + "/contral/lib", "成功", true);
        } else {
            return this.ajaxMessage("题库修改失败，请稍后再试", "题库");
        }

    }

    /**
     * 删除题库
     * 
     * @return
     */
    public String del() {
        String id = this.getPara("id");
        if (!StringHelper.isInteger(id)) {
            return this.ajaxMessage("无效的请求");
        }
        final LibraryModel lib = LibraryModel.dao.findById(id);
        if (lib == null) {
            return this.ajaxMessage("未找到相关题库", "题库");
        }
        int count = Db.queryInt("select count(id) from papers_scale where library_id = '" + id
                + "'");
        if (count > 0) {
            return this.ajaxMessage("当前题库已经被试卷使用，如需删除该题库，请先取消题库中对应的该题库", "失败");
        }
        try {
            // 删除题库
            LibraryModel.dao.deleteById(lib.getInt("id"));
            // 删除作答情况
            Db.update("DELETE from answers where question_id in (select libid from questions where libid='"
                    + lib.getInt("id") + "')");
            // 删除试题
            Db.update("DELETE from questions where libid='" + lib.getInt("id") + "'");
            return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH)
                    + "/contral/lib", "成功", true);
        } catch (Exception e) {
            e.printStackTrace();
            return this.ajaxMessage("删除失败", "失败");
        }

        /*I
        // 删除相关试题
        // 删除回答
        Db.update("DELETE from a from answers a  INNER JOIN questions b ON a.question_id=b.id where b.libid='"
        		+ id + "'");
        // 删除试题
        Db.update("DELETE from questions b where b.libid='" + id + "'");
        // 删除题库
        if (lib.delete()) {
        	return this.ajaxMessage("删除成功", "成功", true);
        } else {
        	return this.ajaxMessag("删除失败", "失败");
        }*/
    }

}
