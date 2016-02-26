package com.maiyoule.miniexam.control.admin.question;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jfinal.plugin.activerecord.Page;
import com.jfinal.upload.UploadFile;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.model.AnswersModel;
import com.maiyoule.miniexam.model.LibraryModel;
import com.maiyoule.miniexam.model.QuestionModel;
import com.maiyoule.miniexam.ui.QuestionPage;
import com.maiyoule.miniexam.utils.StringHelper;

public class QuestionController extends C {

    public String index() {
        QuestionPage qp = new QuestionPage();
        qp.run(this);
        if (qp.getLibraryId() < 1) {
            return this.error("无效请求");
        }

        this.setAttr("qp", qp);
        this.setAttr("queryString", qp.toQueryString());
        int p = qp.getPage();

        StringBuffer condition = new StringBuffer();
        condition.append("libid=" + qp.getLibraryId());
        if (qp.getQuestionStatus() >= 0) {
            if (condition.length() > 0) {
                condition.append(" and ");
            }
            condition.append("status=" + qp.getQuestionStatus());
        }

        if (!StringHelper.isNullOrEmpty(qp.getQuestionType())) {
            if (condition.length() > 0) {
                condition.append(" and ");
            }
            condition.append("type='" + qp.getQuestionType() + "'");
        }
        // 查询题库
        Page<QuestionModel> questions = QuestionModel.dao.paginate(p, GUIConstants.PAGE_SIZE,
                "select * ",
                " from questions "
                        + (condition.length() > 0 ? "where " + condition.toString() : "")
                        + " order by id desc");
        this.setAttr("lists", questions);

        LibraryModel library = LibraryModel.dao.findById(qp.getLibraryId());
        this.setAttr("library", library);
        this.render("/contral/question/list.html");

        return null;
    }

    /**
     * 添加试题页面
     * 
     * @return
     */
    public String add() {
        QuestionPage qp = new QuestionPage();
        qp.run(this);
        if (qp.getLibraryId() < 1) {
            return this.error("无效请求");
        }

        LibraryModel library = LibraryModel.dao.findById(qp.getLibraryId());
        this.setAttr("library", library);

        this.render("/contral/question/add.html");
        return null;
    }

    /**
     * 执行试题添加
     * 
     * @return
     */
    public String insert() {

        int libraryId = this.getParaToInt("libraryId", 0);
        if (libraryId < 1) {
            return this.ajaxMessage("无效的数据请求", "题库");
        }
        String questionType = this.getPara("questionType", null);
        if (questionType == null) {
            return this.ajaxMessage("请选择正确的试题类型", "类型");
        }
        int questionStatus = this.getParaToInt("questionStatus", 1);

        String questionTitle = this.getPara("questionTitle", null);
        if (questionTitle == null) {
            return this.ajaxMessage("请输入试题内容", "内容");
        }

        String trueanswers = null;
        String answerlabels = null;
        int answerCount = this.getParaToInt("answeritemcount");

        // 单选
        if (questionType.equals(QuestionModel.TYPE_SINGLE)) {
            String[] answerlabel = new String[answerCount];
            for (int i = 0; i < answerCount; i++) {
                answerlabel[i] = this.getPara("answerlabel" + i);
            }

            trueanswers = this.getPara("trueanswer");
            answerlabels = StringHelper.join(answerlabel, GUIConstants.STRING_SPLITE);

        }
        // 多选
        else if (questionType.equals(QuestionModel.TYPE_MUTI)) {
            String[] trueanswer = this.getParaValues("trueanswer");
            String[] answerlabel = new String[answerCount];
            for (int i = 0; i < answerCount; i++) {
                answerlabel[i] = this.getPara("answerlabel" + i);
            }
            trueanswers = StringHelper.join(trueanswer, GUIConstants.STRING_SPLITE);
            answerlabels = StringHelper.join(answerlabel, GUIConstants.STRING_SPLITE);

        }
        // 判断
        else if (questionType.equals(QuestionModel.TYPE_JUDGE)) {
            // 判断
            trueanswers = this.getPara("trueanswer");

        }
        if (StringHelper.isNullOrEmpty(trueanswers)) {
            return this.ajaxMessage("请选择正确的答案", "正确答案");
        }
        // 新增数据到数据库
        QuestionModel question = new QuestionModel();
        question.set("libid", libraryId);
        question.set("title", questionTitle);
        question.set("answer", trueanswers);
        question.set("type", questionType);
        question.set("answer_list", answerlabels);
        question.set("status", questionStatus);
        if (question.save()) {
            // 更新计数
            LibraryModel.dao.updatecount(libraryId);
            return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH)
                    + "/contral/ques?libraryId=" + libraryId, "新增成功", true);
        } else {
            return this.ajaxMessage("新增试题失败", "失败");
        }
    }

    /**
     * 试题模板下载
     * 
     * @return
     */
    public String template() {
        this.renderFile(new File(GUIConstants.WEB_ROOT + "WEB-INF/exam_questions_template.xls"));
        return null;
    }

    /**
     * 批量操作
     * 
     * @return
     */
    public String opeart() {

        String op = this.getPara("op", null);
        if (op == null) {
            return this.ajaxMessage("无效操作", "无效");
        }
        String[] ids = this.getParaValues("questionIds");
        if (ids == null || ids.length < 1) {
            return this.ajaxMessage("请选择要操作的试题", "试题");
        }
        int libid = this.getParaToInt("libid", 0);

        if (op.equals("delete")) {
            // 删除
            AnswersModel.dao.delByQuestionIds(ids);
            QuestionModel.dao.deleteByIds(ids);
            // 更新统计
            if (libid > 0) {
                LibraryModel.dao.updatecount(libid);
            }
            return this.ajaxMessage("删除试题成功", "成功", true);
        } else if (op.equals("stop")) {
            // 停止
            QuestionModel.dao.updateStatusByIds(ids, 0);

            return this.ajaxMessage("操作成功", "成功", true);
        } else if (op.equals("open")) {
            // 停止
            QuestionModel.dao.updateStatusByIds(ids, 1);

            return this.ajaxMessage("操作成功", "成功", true);
        } else {
            return this.ajaxMessage("未知操作", "未知");
        }

    }

    /**
     * 修改试题
     * 
     * @return
     */
    public String update() {
        int id = this.getParaToInt("id", 0);
        if (id < 1) {
            return this.error("无效请求");
        }

        // 查询
        QuestionModel questionModel = QuestionModel.dao.findById(id);
        if (questionModel == null) {
            return this.error("未找到相关数据");
        }

        // 查找题库
        LibraryModel library = LibraryModel.dao.findById(questionModel.getInt("libid"));

        this.setAttr("question", questionModel);
        this.setAttr("library", library);

        this.setAttr("STRING_SPLITER", GUIConstants.STRING_SPLITE);

        int[] itemsize = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
        this.setAttr("itemsize", itemsize);

        this.render("/contral/question/update.html");
        return null;
    }

    /**
     * 执行试题修改
     * 
     * @return
     */
    public String doupdate() {

        int id = this.getParaToInt("id", 0);
        if (id < 1) {
            return this.ajaxMessage("无效的编辑请求", "试题编辑");
        }

        String questionType = this.getPara("questionType", null);
        if (questionType == null) {
            return this.ajaxMessage("请选择正确的试题类型", "类型");
        }
        int questionStatus = this.getParaToInt("questionStatus", 1);

        String questionTitle = this.getPara("questionTitle", null);
        if (questionTitle == null) {
            return this.ajaxMessage("请输入试题内容", "内容");
        }

        String trueanswers = null;
        String answerlabels = null;
        int answerCount = this.getParaToInt("answeritemcount");

        // 单选
        if (questionType.equals(QuestionModel.TYPE_SINGLE)) {
            String[] answerlabel = new String[answerCount];
            for (int i = 0; i < answerCount; i++) {
                answerlabel[i] = this.getPara("answerlabel" + i);
            }
            trueanswers = this.getPara("trueanswer");
            answerlabels = StringHelper.join(answerlabel, GUIConstants.STRING_SPLITE);

        }
        // 多选
        else if (questionType.equals(QuestionModel.TYPE_MUTI)) {
            String[] trueanswer = this.getParaValues("trueanswer");
            String[] answerlabel = new String[answerCount];
            for (int i = 0; i < answerCount; i++) {
                answerlabel[i] = this.getPara("answerlabel" + i);
            }
            trueanswers = StringHelper.join(trueanswer, GUIConstants.STRING_SPLITE);
            answerlabels = StringHelper.join(answerlabel, GUIConstants.STRING_SPLITE);

        }
        // 判断
        else if (questionType.equals(QuestionModel.TYPE_JUDGE)) {
            // 判断
            trueanswers = this.getPara("trueanswer");

        }
        if (StringHelper.isNullOrEmpty(trueanswers)) {
            return this.ajaxMessage("请选择正确的答案", "正确答案");
        }

        QuestionModel question = QuestionModel.dao.findById(id);
        if (question == null) {
            return this.ajaxMessage("没有找到相关的试题信息，修改失败", "试题");
        }
        int libraryId = question.get("libid");
        question.set("title", questionTitle);
        question.set("answer", trueanswers);
        question.set("type", questionType);
        question.set("answer_list", answerlabels);
        question.set("status", questionStatus);
        if (question.update()) {
            return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH)
                    + "/contral/ques?libraryId=" + libraryId, "修改成功", true);
        } else {
            return this.ajaxMessage("试题修改失败", "失败");
        }

    }

    /**
     * 删除
     * 
     * @return
     */
    public String del() {

        // 删除
        int id = this.getParaToInt("id", 0);
        if (id < 1) {
            return this.ajaxMessage("无效的数据请求", "请求");
        }
        QuestionModel question = QuestionModel.dao.findById(id);
        if (question == null) {
            return this.ajaxMessage("未找到相关试题", "试题");
        }
        int library = question.get("libid");
        // 删除数据
        AnswersModel.dao.delByQuestionId(id);

        if (question.delete()) {
            LibraryModel.dao.updatecount(library);
            return this.ajaxMessage("删除成功", "成功", true);
        } else {
            return this.ajaxMessage("删除失败", "失败");
        }

    }

    // =========================

    /**
     * 从文件导入试题
     * 
     * @return
     */
    public String importfromfile() {
        int libraryId = this.getParaToInt("libraryId", 0);
        if (libraryId < 1) {
            return this.error("无效的请求");
        }
        LibraryModel library = LibraryModel.dao.findById(libraryId);

        this.setAttr("library", library);

        this.render("/contral/question/import.html");
        return null;
    }

    /**
     * 根据小数转化为字符串.<br>
     * 
     * 如果小数部分为0，则省去<br>
     * 否则返回小数形式
     * 
     * @param d
     * @return
     */
    private String toStringByDouble(double d) {
        BigDecimal b = BigDecimal.valueOf(d);
        BigDecimal roundingBouble = b.setScale(0, BigDecimal.ROUND_HALF_UP);
        if (b.compareTo(roundingBouble) != 0) {
            return b.toPlainString();
        } else {
            return roundingBouble.toPlainString();
        }
    }

    /**
     * XLS文件导入试题
     * 
     * @return
     */
    public String fileimport() {
        UploadFile packagefile = this.getFile("workfile", GUIConstants.CACHE_DIR,
                GUIConstants.FILE_MAXSIZE);
        if (packagefile == null) {
            return this.ajaxMessage("请上传试题文件", "文件");
        }

        int libraryId = this.getParaToInt("libraryId", 0);
        if (libraryId < 1) {
            return this.ajaxMessage("无效的操作", "无效");
        }
        File tfile = packagefile.getFile();
        String filename = tfile.getName();
        String prefix = filename.substring(filename.lastIndexOf(".") + 1);
        File file = new File(GUIConstants.CACHE_DIR + "/" + System.currentTimeMillis() + "."
                + prefix);
        if (file.exists()) {
            file.delete();
        }
        // 导入试题

        tfile.renameTo(file);

        FileInputStream fis = null;

        // 2007以后版本
        XSSFWorkbook xworkbook = null;
        // 2007以前版本
        HSSFWorkbook hworkbook = null;

        try {
            fis = new FileInputStream(file);

            if (file.getName().indexOf(".xlsx") > 0) {
                // Office 2007 2010
                xworkbook = new XSSFWorkbook(fis);
            } else {
                // Office 2000 2003 2007
                hworkbook = new HSSFWorkbook(fis);
            }

            // 读取工作簿
            if (xworkbook != null) {
                XSSFSheet singlesheet = xworkbook.getSheet("单选题");
                XSSFSheet mutisheet = xworkbook.getSheet("多选题");
                XSSFSheet judgesheet = xworkbook.getSheet("判断题");
                this._importXS(libraryId, singlesheet, mutisheet, judgesheet);
            } else if (hworkbook != null) {

                HSSFSheet singlesheet = hworkbook.getSheet("单选题");
                HSSFSheet mutisheet = hworkbook.getSheet("多选题");
                HSSFSheet judgesheet = hworkbook.getSheet("判断题");
                this._importHS(libraryId, singlesheet, mutisheet, judgesheet);
            }

            LibraryModel.dao.updatecount(libraryId);
            return this.ajaxMessage(this.getRequest().getAttribute(GUIConstants.BASEPATH)
                    + "/contral/ques?libraryId=" + libraryId, "成功", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return this.ajaxMessage("未找到上传文件，请重新上传", "数据文件");
        } catch (IOException e) {
            e.printStackTrace();
            return this.ajaxMessage("读取数据文件失败", "数据文件");
        } catch (Exception e) {
            e.printStackTrace();
            return this.ajaxMessage("读取数据文件失败");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 导入Office 2007以后的文件
     * 
     * @param library
     * @param singlesheet
     * @param mutisheet
     * @param judgesheet
     */
    private void _importXS(int libraryId, XSSFSheet singlesheet, XSSFSheet mutisheet,
            XSSFSheet judgesheet) {
        // List<ExQuestions> _importlist=new ArrayList<ExQuestions>();
        // 读取试题
        // 导入单选题
        int rows = singlesheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            if (i == 0) {
                // 因为第一行为标题，读取列数后跳过第一行
                continue;
            }
            XSSFRow rowitem = singlesheet.getRow(i);
            if (rowitem == null) {
                continue;
            }

            // 标题
            XSSFCell celltitle = rowitem.getCell(0);
            if (celltitle == null) {
                continue;
            }

            String title = celltitle.getStringCellValue();
            if (title != null) {
                title = title.trim();
            }
            if (StringHelper.isNullOrEmpty(title)) {
                // 标题为空，不处理
                continue;
            }
            if (title == null) {
                continue;
            }
            String trueanswer = null;
            // 正确答案
            XSSFCell cellanswer = rowitem.getCell(1);
            if (cellanswer != null) {
                trueanswer = cellanswer.getStringCellValue();
                if (trueanswer != null) {
                    trueanswer = trueanswer.trim();
                }
                trueanswer = StringHelper.anwserConvertToFromat(trueanswer);
            }
            StringBuffer answersitem = new StringBuffer();
            // 答案项
            for (int j = 2; j < rowitem.getLastCellNum(); j++) {
                if (answersitem.length() > 0) {
                    answersitem.append(GUIConstants.STRING_SPLITE);
                }

                XSSFCell tempanswers = rowitem.getCell(j);
                if (tempanswers != null) {
                    int type = tempanswers.getCellType();
                    String v = null;
                    switch (type) {
                    case Cell.CELL_TYPE_NUMERIC:
                        v = this.toStringByDouble(tempanswers.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        v = tempanswers.getStringCellValue();
                        break;
                    default:
                        v = tempanswers.getStringCellValue();
                        break;
                    }
                    if (v != null && !v.equals("")) {
                        answersitem.append(v.trim());
                    }
                }
            }
            this.saveSingle(libraryId, title, trueanswer, answersitem.toString());
        }

        // 导入多选题
        rows = mutisheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            if (i == 0) {
                // 因为第一行为标题，读取列数后跳过第一行
                continue;
            }
            XSSFRow rowitem = mutisheet.getRow(i);
            if (rowitem == null) {
                continue;
            }
            // 标题
            XSSFCell celltitle = rowitem.getCell(0);
            if (celltitle == null) {
                continue;
            }
            String title = celltitle.getStringCellValue();
            if (title != null) {
                title = title.trim();
            }
            if (StringHelper.isNullOrEmpty(title)) {
                // 标题为空，不处理
                continue;
            }
            String trueanswer = null;
            // 正确答案
            XSSFCell cellanswer = rowitem.getCell(1);
            if (cellanswer != null) {
                String tmptrueanswerstr = cellanswer.getStringCellValue();
                if (tmptrueanswerstr != null) {
                    tmptrueanswerstr = tmptrueanswerstr.trim().replace('，', ',');
                    String[] tmpstrarr = tmptrueanswerstr.split(",");
                    for (int k = 0; k < tmpstrarr.length; k++) {
                        tmpstrarr[k] = StringHelper.anwserConvertToFromat(tmpstrarr[k].trim());
                    }
                    trueanswer = StringHelper.join(tmpstrarr, GUIConstants.STRING_SPLITE);
                }

            }

            StringBuffer answersitem = new StringBuffer();
            // 答案项
            for (int j = 2; j < rowitem.getLastCellNum(); j++) {
                if (answersitem.length() > 0) {
                    answersitem.append(GUIConstants.STRING_SPLITE);
                }

                XSSFCell tempanswers = rowitem.getCell(j);
                if (tempanswers != null) {
                    int type = tempanswers.getCellType();
                    String v = null;
                    switch (type) {
                    case Cell.CELL_TYPE_NUMERIC:
                        v = this.toStringByDouble(tempanswers.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        v = tempanswers.getStringCellValue();
                        break;
                    default:
                        v = tempanswers.getStringCellValue();
                        break;
                    }
                    if (v != null && !v.equals("")) {
                        answersitem.append(v.trim());
                    }
                }
            }
            this.saveMuti(libraryId, title, trueanswer, answersitem.toString());
        }

        // 导入判断
        rows = judgesheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            if (i == 0) {
                // 因为第一行为标题，读取列数后跳过第一行
                continue;
            }
            XSSFRow rowitem = judgesheet.getRow(i);
            if (rowitem == null) {
                continue;
            }

            // 标题
            XSSFCell celltitle = rowitem.getCell(0);
            if (celltitle == null) {
                continue;
            }

            String title = celltitle.getStringCellValue();
            if (title != null) {
                title = title.trim();
            }
            if (StringHelper.isNullOrEmpty(title)) {
                // 标题为空，不处理
                continue;
            }
            String trueanswer = null;
            // 正确答案
            XSSFCell cellanswer = rowitem.getCell(1);
            if (cellanswer != null) {
                String trueanswerstr = cellanswer.getStringCellValue();
                if (trueanswerstr != null) {
                    trueanswer = trueanswerstr.trim().equals("正确") ? "1" : "0";
                }
            }
            this.saveJudge(libraryId, title, trueanswer);
        }
    }

    /**
     * 导入Office 2007以前的文件
     * 
     * @param library
     * @param singlesheet
     * @param mutisheet
     * @param judgesheet
     */
    private void _importHS(int libraryId, HSSFSheet singlesheet, HSSFSheet mutisheet,
            HSSFSheet judgesheet) {

        // 读取试题
        // 导入单选题
        // 获取总行数
        int rows = singlesheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            if (i == 0) {
                // 因为第一行为标题，读取列数后跳过第一行
                continue;
            }
            HSSFRow rowitem = singlesheet.getRow(i);
            if (rowitem == null) {
                continue;
            }
            // 标题
            HSSFCell celltitle = rowitem.getCell(0);
            if (celltitle == null) {
                continue;
            }

            String title = celltitle.getStringCellValue();
            if (title != null) {
                title = title.trim();// 去除空格
            }
            if (StringHelper.isNullOrEmpty(title)) {
                // 标题为空，不处理
                continue;
            }

            String trueanswer = null;
            // 正确答案
            HSSFCell cellanswer = rowitem.getCell(1);
            if (cellanswer != null) {
                trueanswer = cellanswer.getStringCellValue();
                if (trueanswer != null) {
                    trueanswer = trueanswer.trim();
                }
                trueanswer = StringHelper.anwserConvertToFromat(trueanswer);

            }
            StringBuffer answersitem = new StringBuffer();
            // 答案项
            // 最后一列
            int itemlast = rowitem.getLastCellNum();
            for (int j = 2; j < itemlast; j++) {
                if (answersitem.length() > 0) {
                    answersitem.append(GUIConstants.STRING_SPLITE);
                }

                HSSFCell tempanswers = rowitem.getCell(j);
                if (tempanswers != null) {
                    int type = tempanswers.getCellType();
                    String v = null;
                    switch (type) {
                    case Cell.CELL_TYPE_NUMERIC:
                        v = this.toStringByDouble(tempanswers.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        v = tempanswers.getStringCellValue();
                        break;
                    default:
                        v = tempanswers.getStringCellValue();
                        break;
                    }
                    if (v != null && !v.equals("")) {
                        answersitem.append(v.trim());
                    }
                }
            }
            this.saveSingle(libraryId, title, trueanswer, answersitem.toString());
        }

        // 导入多选题
        rows = mutisheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            if (i == 0) {
                // 因为第一行为标题，读取列数后跳过第一行
                continue;
            }
            HSSFRow rowitem = mutisheet.getRow(i);
            if (rowitem == null) {
                continue;
            }
            // 创建单选题
            QuestionModel question = new QuestionModel();
            question.set("libid", libraryId);

            // 标题
            HSSFCell celltitle = rowitem.getCell(0);
            if (celltitle == null) {
                continue;
            }
            String title = celltitle.getStringCellValue();
            if (title != null) {
                title = title.trim();// 去除空格
            }
            if (StringHelper.isNullOrEmpty(title)) {
                // 标题为空，不处理
                continue;
            }

            String trueanswer = null;
            // 正确答案
            HSSFCell cellanswer = rowitem.getCell(1);
            if (cellanswer != null) {
                String tmpanswerstr = cellanswer.getStringCellValue();
                if (tmpanswerstr != null) {
                    // 去除空格，将全角 转为半角
                    tmpanswerstr = tmpanswerstr.trim().replace('，', ',');
                    String[] tmpstrarr = tmpanswerstr.split(",");
                    for (int k = 0; k < tmpstrarr.length; k++) {
                        tmpstrarr[k] = StringHelper.anwserConvertToFromat(tmpstrarr[k].trim());
                    }
                    trueanswer = StringHelper.join(tmpstrarr, GUIConstants.STRING_SPLITE);
                }
            }

            // 答案项
            StringBuffer answersitem = new StringBuffer();
            for (int j = 2; j < rowitem.getLastCellNum(); j++) {
                if (answersitem.length() > 0) {
                    answersitem.append(GUIConstants.STRING_SPLITE);
                }

                HSSFCell tempanswers = rowitem.getCell(j);
                if (tempanswers != null) {
                    int type = tempanswers.getCellType();
                    String v = null;
                    switch (type) {
                    case Cell.CELL_TYPE_NUMERIC:
                        v = this.toStringByDouble(tempanswers.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        v = tempanswers.getStringCellValue();
                        break;
                    default:
                        v = tempanswers.getStringCellValue();
                        break;
                    }
                    if (v != null && !v.equals("")) {
                        answersitem.append(v.trim());
                    }
                }
            }
            this.saveMuti(libraryId, title, trueanswer, answersitem.toString());
        }

        // 导入判断
        rows = judgesheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            if (i == 0) {
                // 因为第一行为标题，读取列数后跳过第一行
                // cells = judgesheet.getRow(i).getLastCellNum();
                continue;
            }

            HSSFRow rowitem = judgesheet.getRow(i);

            if (rowitem == null) {
                continue;
            }
            // 创建判断
            QuestionModel question = new QuestionModel();
            question.set("libid", libraryId);
            // 标题
            HSSFCell celltitle = rowitem.getCell(0);
            if (celltitle == null) {
                continue;
            }
            String title = celltitle.getStringCellValue();
            if (title != null) {
                title = title.trim();// 去除空格
            }
            if (StringHelper.isNullOrEmpty(title)) {
                // 标题为空，不处理
                continue;
            }

            // 正确答案
            String trueanswer = null;
            HSSFCell cellanswer = rowitem.getCell(1);
            if (cellanswer != null) {
                trueanswer = cellanswer.getStringCellValue();
                if (trueanswer != null) {
                    trueanswer = trueanswer.trim().equals("正确") ? "1" : "0";
                }

            }
            this.saveJudge(libraryId, title, trueanswer);
        }

    }

    /**
     * 保存单选题
     * 
     * @param libraryId
     * @param title
     * @param trueanswer
     * @param answersitem
     */
    private void saveSingle(int libraryId, String title, String trueanswer, String answersitem) {
        // 查询是否存在该题
        String sql = String.format(
                "select * from questions where libid=%d and title='%s' and type='%s'", libraryId,
                title, QuestionModel.TYPE_SINGLE);
        QuestionModel oldquestion = QuestionModel.dao.findFirst(sql);
        if (oldquestion != null) {
            return;
        }
        QuestionModel question = new QuestionModel();
        question.set("libid", libraryId);
        question.set("title", title);
        question.set("answer", trueanswer);
        question.set("type", QuestionModel.TYPE_SINGLE);
        question.set("answer_list", answersitem);
        question.set("status", 1);
        question.save();
    }

    private void saveMuti(int libraryId, String title, String trueanswer, String answersitem) {
        String sql = String.format(
                "select * from questions where libid=%d and title='%s' and type='%s'", libraryId,
                title, QuestionModel.TYPE_MUTI);
        QuestionModel oldquestion = QuestionModel.dao.findFirst(sql);
        if (oldquestion != null) {
            return;
        }
        QuestionModel question = new QuestionModel();
        question.set("libid", libraryId);
        question.set("title", title);
        question.set("answer", trueanswer);
        question.set("type", QuestionModel.TYPE_MUTI);
        question.set("answer_list", answersitem);
        question.set("status", 1);
        question.save();
    }

    private void saveJudge(int libraryId, String title, String trueanswer) {
        String sql = String.format(
                "select * from questions where libid=%d and title='%s' and type='%s'", libraryId,
                title, QuestionModel.TYPE_JUDGE);
        QuestionModel oldquestion = QuestionModel.dao.findFirst(sql);
        if (oldquestion != null) {
            return;
        }
        QuestionModel question = new QuestionModel();
        question.set("libid", libraryId);
        question.set("title", title);
        question.set("answer", trueanswer);
        question.set("type", QuestionModel.TYPE_JUDGE);
        question.set("status", 1);
        question.save();
    }

}
