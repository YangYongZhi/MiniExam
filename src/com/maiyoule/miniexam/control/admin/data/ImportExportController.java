package com.maiyoule.miniexam.control.admin.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.upload.UploadFile;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.entity.BankInfo;
import com.maiyoule.miniexam.entity.Paper;
import com.maiyoule.miniexam.entity.UserInfo;
import com.maiyoule.miniexam.entity.UserType;
import com.maiyoule.miniexam.model.AdminModel;
import com.maiyoule.miniexam.model.BanksModel;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.LayoutsModel;
import com.maiyoule.miniexam.model.PapersModel;
import com.maiyoule.miniexam.model.UserTypeModel;
import com.maiyoule.miniexam.model.UsersModel;
import com.maiyoule.miniexam.utils.Ajax;
import com.maiyoule.miniexam.utils.MyFileWriter;
import com.maiyoule.miniexam.utils.S;
import com.maiyoule.miniexam.utils.StringHelper;

public class ImportExportController extends C {
	/**
	 * 导入导出数据
	 */
	public void index() {
		this.render("/contral/data/index.html");
	}

	/**
	 * 导出安装数据（考试人员信息，用于异地考试进行数据初始化）
	 */
	public void exportinstall() {
		// 获取未结束的考试安排
		List<LayoutsModel> layouts = LayoutsModel.dao.find("select * from layouts where end_time>"
		        + System.currentTimeMillis());
		this.setAttr("layouts", layouts);
		this.render("/contral/data/exportinstall.html");
	}

	/**
	 * 修改密码初始页面.
	 */
	public void modifypass() {
		this.render("/contral/data/modifypass.html");
	}

	/**
	 * 总行导出异地所需的考试数据，用于异地进行导入
	 * 
	 * @return
	 */
	public String domodifypass() {
		String oldpass = this.getPara("oldpass");
		String pass = this.getPara("pass");
		String passagain = this.getPara("passagain");

		if (StringUtils.isEmpty(oldpass)) {
			return this.error("旧密码不能为空");
		}
		if (StringUtils.isEmpty(pass)) {
			return this.error("新密码不能为空");
		}
		if (StringUtils.isEmpty(passagain)) {
			return this.error("新密码确认信息不能为空");
		}
		if (!StringUtils.equals(passagain, pass)) {
			return this.error("两次密码输入必须相同");
		}
		if (StringUtils.equals(oldpass, pass)) {
			return this.error("旧密码和新密码相同");
		}
		AdminModel admin = new AdminModel();
		AdminModel m = admin.findFirst(String.format("select * from admin where uname='%s'", "admin"));

		String dpwd = m.getStr("upassword");
		if (StringUtils.isEmpty(dpwd)) {
			return this.error("获取旧密码发生错误");
		}

		String oldchpwd = S.m(oldpass);
		String conrrentdpwd = S.d(dpwd);
		if (!StringUtils.equals(oldchpwd, conrrentdpwd)) {
			return this.error("旧密码输入错误");
		}

		String newMPass = S.m(passagain);
		String newEPass = S.e(newMPass);
		// 更新密码
		if (m.set("upassword", newEPass).update()) {
			return this.success(this.getAttr("basePath").toString() + "/contral/data/modifypass");
		} else {
			return this.error("修改密码发生错误");
		}

	}

	public static void main(String[] a) {
		System.out.println("c3284d094606de1fd2af172aba15bf3");
		System.out.println(S.e("c3284d094606de1fd2af172aba15bf3"));
		System.out.println(S.e("admin"));
		System.out
		        .println(S
		                .d("r3MTTOYab7l3nuvnOEkBivpURWgVtU1L8RnaXvsy0p1HHTyL8kNnGTMcSnFMYnY4tmimkUDwqI5HDUwYh68HKlxHBlssNl2zfYLoP1CTk8cwFJyKO1gebFHeZnhieAHXao/lJcuP1SBPnfbfHM0RGFpoU+Ydua6olxgiu8Q5e6k="));
	}

	/**
	 * 总行导出异地所需的考试数据，用于异地进行导入
	 * 
	 * @return
	 */
	public String doexportinstall() {
		String[] ids = this.getParaValues("ids");
		if (ids == null || ids.length < 1) {
			return this.error("请选择要导出的考试安排");
		}
		String zippassword = this.getPara("zippassword", null);
		if (StringHelper.isNullOrEmpty(zippassword)) {
			return this.error("请输入安装密码");
		}
		String username = this.getPara("username", null);
		if (StringHelper.isNullOrEmpty(username)) {
			return this.error("请输入登录管理帐号");
		}
		String userpassword = this.getPara("userpassword", null);
		if (StringHelper.isNullOrEmpty(userpassword)) {
			return this.error("请输入登录管理密码");
		}
		InstallPackage install = new InstallPackage();
		install.setParams(ids, zippassword, username, userpassword);
		String downpath = install.make();
		if (downpath == null) {
			return error("生成失败，请稍后再试");
		}
		this.renderFile(new File(downpath));
		return null;
	}

	/**
	 * 异地部署程序，导出考试成绩，加密 异地考试结束后到处考试结果，用于总部数据导入
	 */
	public String exportresult() {
		// 导出所有的考试结果信息

		// 写入文件中
		String outfilepath = GUIConstants.CACHE_DIR + "/out.data";
		File outfile = new File(outfilepath);
		if (outfile.exists()) {
			outfile.delete();
		}
		MyFileWriter mfw = null;
		try {
			if (!outfile.createNewFile()) {
				this.renderText("没有写入权限");
				return null;
			}

			mfw = new MyFileWriter(outfile, true);

			List<ExamsModel> exams = ExamsModel.dao.find("select * from exams");
			for (ExamsModel em : exams) {

				String sql = String
				        .format("update exams set score=%s,layouts_status=%s,user_status=%s,start_time=%s,end_time=%s,use_time=%s,enter_time=%s where id=%s",
				                em.get("score"), em.get("layouts_status"), em.get("user_status"), em.get("start_time"),
				                em.get("end_time"), em.get("use_time"), em.get("enter_time"), em.get("id"));
				mfw.appendStr(sql);
			}
			mfw.flush();

			// 压缩为ZIP加密

			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
			parameters.setPassword(GUIConstants.ZIP_PASSWORD);

			ZipFile zipfile = new ZipFile(GUIConstants.CACHE_DIR + "/ExamResult.data");
			zipfile.addFile(outfile, parameters);

			if (outfile.exists()) {
				outfile.delete();
			}
			this.renderFile(new File(GUIConstants.CACHE_DIR + "/ExamResult.data"));

		} catch (IOException e) {
			this.render("导出异常，没有相关系统权限");
			return null;
		} catch (ZipException e) {
			this.render("导出异常，没有相关系统权限");
			return null;
		} finally {

			try {
				if (mfw != null) {
					mfw.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// this.render("/contral/data/result.html");
		return null;
	}

	/**
	 * 总行将异地考试结果导入
	 * 
	 * @return
	 */
	public String dataToLocal() {
		this.render("/contral/data/datatolocal.html");
		return null;
	}

	/**
	 * 总行将异地考试结果导入
	 * 
	 * @return
	 */
	public String doimport2local() {

		UploadFile packagefile = this.getFile("datapackage", GUIConstants.CACHE_DIR, GUIConstants.FILE_MAXSIZE);
		if (packagefile == null) {
			this.renderText(Ajax.message("请上传数据文件", "导入失败"), "text/html;charset=utf-8");
			return null;
		}
		ZipFile zipFile;
		FileReader fis = null;
		BufferedReader br = null;
		try {
			zipFile = new ZipFile(packagefile.getFile());

			if (!zipFile.isEncrypted()) {
				this.renderText(Ajax.message("请上传正确的数据文件", "导入失败"), "text/html;charset=utf-8");
				return null;
			}
			zipFile.setPassword(GUIConstants.ZIP_PASSWORD);
			if (!zipFile.isValidZipFile()) {
				this.renderText(Ajax.message("无效的数据文件，请重新上传", "失败"), "text/html;charset=utf-8");
				return null;
			}
			String outfilepath = GUIConstants.CACHE_DIR + "/out.data";
			File outfile = new File(outfilepath);
			if (outfile.exists()) {
				outfile.delete();
			}

			String webinf = String.format("%scache", GUIConstants.WEB_ROOT);
			zipFile.extractFile("out.data", webinf);

			// 读取
			fis = new FileReader(outfile);
			br = new BufferedReader(fis);
			List<String> sqlbatch = new ArrayList<String>();

			String tmpsql = null;
			while ((tmpsql = br.readLine()) != null) {
				sqlbatch.add(tmpsql);
				if (sqlbatch.size() > 80) {
					// 批量执行Sql
					Db.use(DbKit.MAIN_CONFIG_NAME).batch(sqlbatch, sqlbatch.size());
					sqlbatch.clear();
				}

			}

			// 执行剩下的SQL
			if (sqlbatch.size() > 0) {
				Db.use(DbKit.MAIN_CONFIG_NAME).batch(sqlbatch, sqlbatch.size());
				sqlbatch.clear();
			}
			return this.ajaxMessage("导入成功", "成功", true);

		} catch (ZipException e) {
			return this.ajaxMessage("导入错误");
		} catch (FileNotFoundException e) {
			return this.ajaxMessage("导入错误");
		} catch (IOException e) {
			return this.ajaxMessage("导入错误");
		} finally {

			try {
				if (br != null) {
					br.close();
				}
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}

		}
	}

	/**
	 * 导出分析数据
	 */
	public void exportzhenxin() {
		// 获取考试已经完成的安排
		List<LayoutsModel> layouts = LayoutsModel.dao.find("select * from layouts where start_time<"
		        + System.currentTimeMillis());

		this.setAttr("layouts", layouts);

		this.render("/contral/data/zhenxin.html");
	}

	/**
	 * 总行导出考试数据给征信系统
	 */
	public void doExportZhenxin() {
		String[] ids = this.getParaValues("id");
		if (ids == null || ids.length < 1) {
			this.error("请选择要导出的考试安排");
			return;
		}

		HttpServletResponse response = this.getResponse();
		response.setContentType("text/csv");
		response.setHeader("Content-disposition", "attachment;filename=" + System.currentTimeMillis() + ".csv");
		OutputStream os = null;
		try {
			os = response.getOutputStream();
			os.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
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
			sbtitle.append("证件类型");
			sbtitle.append(",");
			sbtitle.append("用户类型");
			sbtitle.append(",");
			sbtitle.append("金融机构");
			sbtitle.append(",");
			sbtitle.append("金融机构编码");
			sbtitle.append(",");
			sbtitle.append("城市编码");
			sbtitle.append(",");
			sbtitle.append("地区编码");
			sbtitle.append(",");
			sbtitle.append("联系电话");
			sbtitle.append(",");
			sbtitle.append("考试开始时间");
			sbtitle.append(",");
			sbtitle.append("考试结束时间");
			sbtitle.append(",");
			sbtitle.append("试卷");
			sbtitle.append(",");
			sbtitle.append("成绩");
			sbtitle.append(",");
			sbtitle.append("及格分数");
			sbtitle.append(",");
			sbtitle.append("总分");

			// 处理中文乱码
			os.write(sbtitle.toString().getBytes(GUIConstants.CHARSET));
			os.write(GUIConstants.NEWLINE.getBytes(GUIConstants.CHARSET));

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);

			for (String layoutid : ids) {

				// 查询考试安排信息
				List<ExamsModel> exams = ExamsModel.dao.findAllByLayoutid(layoutid);
				if (exams == null) {
					continue;
				}
				for (ExamsModel exam : exams) {
					StringBuffer item = new StringBuffer();
					UserInfo user = null;
					try {
						user = UsersModel.dao.getByCardNo(exam.getStr("cardno"));
						item.append(user.getName());
						item.append(",");
						item.append(user.getCardno());
						item.append(",");
						item.append(user.getCardType().equals("identity") ? "身份证" : "其它");
						item.append(",");
					} catch (Exception e) {
						System.out.println("Getting user has an error:" + exam.getStr("cardno"));
						continue;
					}

					UserType userType = null;
					try {
						userType = UserTypeModel.dao.getById(exam.getInt("utype"));
						item.append(userType.getName());
						item.append(",");
					} catch (Exception e) {
						System.out.println("Getting userType has an error:" + exam.getInt("utype"));
						item.append(",");
					}

					// 金融机构
					BankInfo bankInfo = null;
					try {
						bankInfo = BanksModel.dao.getByNo(exam.getStr("bankno"));
						item.append(bankInfo.getName());
						item.append(",");
						item.append(bankInfo.getNo());
						item.append(",");
						item.append(bankInfo.getCityNo());
						item.append(",");
						item.append(bankInfo.getCountryNo());
						item.append(",");
						item.append(user.getTelphone());
						item.append(",");
						item.append(sdf.format(new Date(exam.getLong("start_time"))));
						item.append(",");
						item.append(sdf.format(new Date(exam.getLong("end_time"))));
					} catch (Exception e) {
						System.out.println("Getting bankInfo has an error:" + exam.getStr("bankno"));
						item.append(",");
						item.append(",");
						item.append(",");
						item.append(",");
						item.append(",");
						item.append(",");
					}

					Paper paper = null;
					try {
						paper = PapersModel.dao.findByUtype(exam.getInt("utype"));
						item.append(",");
						item.append(paper.getName());
						item.append(",");
						item.append(exam.getInt("score"));
						item.append(",");
						item.append(paper.getMinScore());
						item.append(",");
						item.append(paper.getScore());
					} catch (Exception e) {
						System.out.println("Getting paper has an error:" + exam.getInt("utype"));
						item.append(",");
						item.append(",");
						item.append(",");
						item.append(",");
					}

					os.write(item.toString().getBytes(GUIConstants.CHARSET));
					os.write(GUIConstants.NEWLINE.getBytes(GUIConstants.CHARSET));
					os.flush();
					// } catch (Exception e) {
					// // e.printStackTrace();
					// System.out.println(item.toString());
					// }
				}
			}
			os.write("到此为止".getBytes(GUIConstants.CHARSET));
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
