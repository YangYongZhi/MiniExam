package com.maiyoule.miniexam.control.admin.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.entity.AreaItem;
import com.maiyoule.miniexam.model.AreaModel;
import com.maiyoule.miniexam.model.BanksModel;
import com.maiyoule.miniexam.model.ConfigModel;
import com.maiyoule.miniexam.model.ExamsModel;
import com.maiyoule.miniexam.model.LayoutsModel;
import com.maiyoule.miniexam.model.LibraryModel;
import com.maiyoule.miniexam.model.OrgtypeModel;
import com.maiyoule.miniexam.model.PaperScaleModel;
import com.maiyoule.miniexam.model.PapersModel;
import com.maiyoule.miniexam.model.PapersUserTypeModel;
import com.maiyoule.miniexam.model.PcbModel;
import com.maiyoule.miniexam.model.QuestionModel;
import com.maiyoule.miniexam.model.UserTypeModel;
import com.maiyoule.miniexam.model.UsersModel;
import com.maiyoule.miniexam.model.UsersUtypesModel;
import com.maiyoule.miniexam.utils.S;

public class InstallPackage {

	private Logger log = Logger.getLogger(InstallPackage.class);
	private String[] layoutids;
	private String zippassword;
	private String username;
	private String password;

	public void setParams(String[] layoutids, String zippassword,
			String username, String password) {
		this.layoutids = layoutids;
		this.zippassword = zippassword;
		this.username = username;
		this.password = password;
	}

	public String make() {

		// 清空表格
		List<String> batchSql = new ArrayList<String>();
		batchSql.add("DROP TABLE IF EXISTS admin");
		batchSql.add("CREATE TABLE admin (id  INTEGER PRIMARY KEY  NOT NULL,uname  TEXT(50) NOT NULL,upassword  TEXT(50))");
		batchSql.add("DROP TABLE IF EXISTS answers");
		batchSql.add("CREATE TABLE answers (id  INTEGER PRIMARY KEY  NOT NULL,exam_id  INTEGER,layout_id  INTEGER,question_id  INTEGER,status  INTEGER,label  TEXT)");
		batchSql.add("DROP TABLE IF EXISTS areas");
		batchSql.add("CREATE TABLE areas (id  INTEGER NOT NULL,name  TEXT,parent  INTEGER,level  TEXT,isleaf  TEXT,short_name  TEXT,status  TEXT,flag  TEXT,PRIMARY KEY (id))");
		batchSql.add("DROP TABLE IF EXISTS banks");
		batchSql.add("CREATE TABLE banks (id  INTEGER PRIMARY KEY  NOT NULL,no  TEXT,name  TEXT,parent_no  TEXT,parent_name  TEXT,pcb_no  TEXT,pcb_name  TEXT,city_no  INTEGER,city_name  TEXT,country_no  INTEGER,country_name  TEXT,orgtype_no  TEXT,orgtype_name  TEXT,isleaf  TEXT)");
		batchSql.add("DROP TABLE IF EXISTS config");
		batchSql.add("CREATE TABLE config (id  TEXT NOT NULL,value  TEXT,PRIMARY KEY (id))");
		batchSql.add("DROP TABLE IF EXISTS exams");
		batchSql.add("CREATE TABLE exams (id  INTEGER PRIMARY KEY  NOT NULL,cardno  TEXT,bankno  TEXT,city  INTEGER,country  INTEGER,utype  INTEGER,layout_id  INTEGER DEFAULT 0,score  INTEGER DEFAULT 0,layouts_status  INTEGER DEFAULT 0,user_status  INTEGER DEFAULT 0,start_time  INTEGER DEFAULT 0,end_time  INTEGER DEFAULT 0,use_time  INTEGER DEFAULT 0,enter_time  INTEGER DEFAULT 0,exit_time  INTEGER DEFAULT 0,next_time  INTEGER DEFAULT 0,sign  TEXT)");
		batchSql.add("DROP TABLE IF EXISTS layouts");
		batchSql.add("CREATE TABLE layouts (id  INTEGER PRIMARY KEY  NOT NULL,start_time  NUMERIC,end_time  NUMERIC,count  INTEGER)");
		batchSql.add("DROP TABLE IF EXISTS library");
		batchSql.add("CREATE TABLE library (id  INTEGER PRIMARY KEY  NOT NULL,name  TEXT(100),single_count  INTEGER DEFAULT 0,muti_count  INTEGER DEFAULT 0,juge_count  INTEGER DEFAULT 0)");
		batchSql.add("DROP TABLE IF EXISTS orgtype");
		batchSql.add("CREATE TABLE orgtype (id  INTEGER PRIMARY KEY  NOT NULL,no  TEXT,name  TEXT,parent_no  TEXT,parent_name  TEXT)");
		batchSql.add("DROP TABLE IF EXISTS papers");
		batchSql.add("CREATE TABLE papers (id  INTEGER PRIMARY KEY  NOT NULL,name  TEXT,status  INTEGER,score  INTEGER,min_score  INTEGER,single_score  INTEGER,muti_score  INTEGER,juge_score  INTEGER,single_count  INTEGER,muti_count  INTEGER,juge_count  INTEGER,user_types  TEXT)");
		batchSql.add("DROP TABLE IF EXISTS papers_scale");
		batchSql.add("CREATE TABLE papers_scale (id  INTEGER PRIMARY KEY  NOT NULL,paper_id  INTEGER,library_id  INTEGER,scale_single  INTEGER,scale_muti  INTEGER,scale_juge  INTEGER)");
		batchSql.add("DROP TABLE IF EXISTS papers_user_type");
		batchSql.add("CREATE TABLE papers_user_type (id  INTEGER PRIMARY KEY  NOT NULL,paper_id  INTEGER,user_type_id  INTEGER)");
		batchSql.add("DROP TABLE IF EXISTS pcb");
		batchSql.add("CREATE TABLE pcb (id  INTEGER PRIMARY KEY  NOT NULL,no  TEXT,name  TEXT,parent_no  TEXT,parent_name  TEXT,pcb_no  TEXT,pcb_name  TEXT)");
		batchSql.add("DROP TABLE IF EXISTS questions");
		batchSql.add("CREATE TABLE questions (id  INTEGER PRIMARY KEY  NOT NULL,libid  INTEGER NOT NULL,title  TEXT,answer  TEXT,type  TEXT,answer_list  TEXT,status  INTEGER)");
		batchSql.add("DROP TABLE IF EXISTS user_type");
		batchSql.add("CREATE TABLE user_type (id  INTEGER PRIMARY KEY  NOT NULL,name  TEXT)");
		batchSql.add("DROP TABLE IF EXISTS users");
		batchSql.add("CREATE TABLE users (id  INTEGER PRIMARY KEY NOT NULL,cardno  TEXT,name  TEXT,card_type  TEXT,last_exam_time  TEXT,bankno  TEXT,bank_name  TEXT,telphone  TEXT,department  TEXT,status  INTEGER)");
		batchSql.add("DROP TABLE IF EXISTS users_utypes");
		batchSql.add("CREATE TABLE users_utypes (id  INTEGER PRIMARY KEY NOT NULL,cardno  TEXT,type_id  INTEGER,bank_no  TEXT,bank_name  TEXT,bank_path  TEXT,city  INTEGER,country  INTEGER)");

		String outfilepath = GUIConstants.CACHE_DIR + "/out.data";
		File outfile = new File(outfilepath);
		if (outfile.exists()) {
			outfile.delete();
		}
		FileOutputStream fos = null;
		try {
			if (!outfile.createNewFile()) {
				return null;
			}
			fos = new FileOutputStream(outfile);
			// // 头部
			// fos.write(0x0F);
			// fos.write(0xE1);
			// fos.write(0xA9);
			// fos.flush();
			// 内容格式
			// [1:TYPE][4:内容长度][内容][4:内容长度]
			for (String sql : batchSql) {
				this.exportdatatofile(fos, sql);
			}
			batchSql.clear();
			// 管理员数据
			String mpwd = S.m(password); // 取密码的MD5值
			String epwd = S.e(mpwd); // 对密码的MD5值加密
			this.exportdatatofile(fos, String.format(
					"insert into admin(uname,upassword) values('%s','%s')",
					username, epwd));

			// 地区
			List<AreaItem> areasModels = AreaModel.dao.findAll();
			for (AreaItem item : areasModels) {
				String sql = String
						.format("INSERT INTO areas VALUES (%d, '%s', %d, '%s', %d, '', 1, 1)",
								item.getId(), item.getName(), item.getParent(),
								item.getLevel(), (item.isLeaf() ? 1 : 0));
				this.exportdatatofile(fos, sql);
			}
			// 银行
			List<BanksModel> banks = BanksModel.dao
					.find("select * from banks order by id asc");
			for (BanksModel b : banks) {
				String sql = String
						.format("INSERT INTO banks VALUES (%s, '%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', %s, '%s', '%s', '%s', '%s')",
								b.get("id", "0"),
								b.get("no", StringUtils.EMPTY),
								b.get("name", StringUtils.EMPTY),
								b.get("parent_no", StringUtils.EMPTY),
								b.get("parent_name", StringUtils.EMPTY),
								b.get("pcb_no", StringUtils.EMPTY),
								b.get("pcb_name", StringUtils.EMPTY),
								b.get("city_no") == null
										|| b.get("city_no").equals(
												StringUtils.EMPTY) ? "0" : b
										.get("city_no"),
								b.get("city_name", StringUtils.EMPTY),
								b.get("country_no") == null
										|| b.get("country_no").equals(
												StringUtils.EMPTY) ? "0" : b
										.get("country_no"), b.get(
										"country_name", StringUtils.EMPTY), b
										.get("orgtype_no", StringUtils.EMPTY),
								b.get("orgtype_name", StringUtils.EMPTY), b
										.get("isleaf", StringUtils.EMPTY));
				this.exportdatatofile(fos, sql);
			}
			// 设置
			List<ConfigModel> configs = ConfigModel.dao
					.find("select * from config");
			for (ConfigModel c : configs) {
				String id = c.getStr("id");
				String value = c.getStr("value");
				if (id.equalsIgnoreCase("examNotice")) {
					value = value.replaceAll("[\\t\\n\\r]", ""); // 替换回车换行符
				}
				String sql = String.format(
						"INSERT INTO config VALUES ('%s', '%s')", id, value);
				this.exportdatatofile(fos, sql);
			}

			// 考试
			List<ExamsModel> exams = ExamsModel.dao
					.find("select * from exams where layout_id in("
							+ StringUtils.join(layoutids, ",")
							+ ") order by id asc");
			for (ExamsModel exam : exams) {
				String sql = String
						.format("INSERT INTO exams VALUES (%d, '%s', '%s', %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, '%s')",
								exam.getInt("id"), exam.getStr("cardno"),
								exam.getStr("bankno"), exam.getInt("city"),
								exam.getInt("country"), exam.getInt("utype"),
								exam.getInt("layout_id"), exam.getInt("score"),
								exam.getInt("layouts_status"),
								exam.getInt("user_status"),
								exam.getLong("start_time"),
								exam.getLong("end_time"),
								exam.getInt("use_time"),
								exam.getNumber("enter_time"),
								exam.getNumber("exit_time"),
								exam.getNumber("next_time"),
								exam.getStr("sign"));
				this.exportdatatofile(fos, sql);
			}

			// 考试安排
			List<LayoutsModel> layouts = LayoutsModel.dao
					.find("select * from layouts where id in ("
							+ StringUtils.join(layoutids, ",")
							+ ") order by id asc");

			for (LayoutsModel layout : layouts) {
				String sql = String.format(
						"INSERT INTO layouts VALUES (%d, %d, %d, %d)",
						layout.getInt("id"), layout.getLong("start_time"),
						layout.getLong("end_time"), layout.getInt("count"));

				this.exportdatatofile(fos, sql);
			}

			List<LibraryModel> librarys = LibraryModel.dao
					.find("select * from library order by id asc");
			for (LibraryModel lib : librarys) {
				String sql = String.format(
						"INSERT INTO library VALUES (%d,'%s',%d,%d,%d)",
						lib.getInt("id"), lib.getStr("name"),
						lib.getInt("single_count"), lib.getInt("muti_count"),
						lib.getInt("juge_count"));

				this.exportdatatofile(fos, sql);
			}

			List<OrgtypeModel> orgtypes = OrgtypeModel.dao
					.find("select * from orgtype order by id asc");
			for (OrgtypeModel org : orgtypes) {
				String sql = String
						.format("INSERT INTO orgtype VALUES (%d, '%s', '%s', '%s', '')",
								org.getInt("id"), org.getStr("no"),
								org.getStr("name"), org.getStr("parent_no"));
				this.exportdatatofile(fos, sql);
			}

			List<PapersModel> papers = PapersModel.dao
					.find("select * from papers order by id asc");
			for (PapersModel p : papers) {
				String sql = String
						.format("INSERT INTO papers VALUES (%d, '%s', %d, %d, %d, %d, %d, %d, %d, %d, %d, null)",
								p.getInt("id"), p.getStr("name"),
								p.getInt("status"), p.getInt("score"),
								p.getInt("min_score"),
								p.getInt("single_score"),
								p.getInt("muti_score"), p.getInt("juge_score"),
								p.getInt("single_count"),
								p.getInt("muti_count"), p.getInt("juge_count"));
				this.exportdatatofile(fos, sql);
			}

			List<PaperScaleModel> paperscale = PaperScaleModel.dao
					.find("select * from papers_scale order by id asc");
			for (PaperScaleModel p : paperscale) {
				String sql = String
						.format("INSERT INTO papers_scale VALUES (%d, %d, %d, %d, %d, %d)",
								p.getInt("id"), p.getInt("paper_id"),
								p.getInt("library_id"),
								p.getInt("scale_single"),
								p.getInt("scale_muti"), p.getInt("scale_juge"));
				this.exportdatatofile(fos, sql);
			}
			List<PapersUserTypeModel> putms = PapersUserTypeModel.dao
					.find("select * from papers_user_type order by id asc");

			for (PapersUserTypeModel p : putms) {
				String sql = String.format(
						"INSERT INTO papers_user_type VALUES (%d,%d,%d)",
						p.getInt("id"), p.getInt("paper_id"),
						p.getInt("user_type_id"));
				this.exportdatatofile(fos, sql);
			}
			List<PcbModel> pcbs = PcbModel.dao
					.find("select * from pcb order by id asc");
			for (PcbModel p : pcbs) {
				String sql = String
						.format("INSERT INTO pcb VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s')",
								p.getInt("id"), p.getStr("no"),
								p.getStr("name"), p.getStr("parent_no"),
								p.getStr("parent_name"), p.getStr("pcb_no"),
								p.getStr("pcb_name"));
				this.exportdatatofile(fos, sql);
			}

			List<QuestionModel> questions = QuestionModel.dao
					.find("select * from questions order by id asc");
			for (QuestionModel p : questions) {
				String sql = String
						.format("INSERT INTO questions VALUES (%d, %d, '%s', '%s', '%s', '%s', %d)",
								p.getInt("id"),
								p.getInt("libid"),
								StringUtils.isNotBlank(p.getStr("title")) ? p
										.getStr("title").replace('\r', ' ')
										.replace('\n', ' ')
										.replaceAll(" {2,}", " ")
										: StringUtils.EMPTY,
								p.getStr("answer"),
								p.getStr("type"),
								StringUtils.isNotBlank(p.getStr("answer_list")) ? p
										.getStr("answer_list")
										.replace('\r', ' ').replace('\n', ' ')
										.replaceAll(" {2,}", " ")
										: StringUtils.EMPTY, p.getInt("status"));
				this.exportdatatofile(fos, sql);
			}

			List<UserTypeModel> usertype = UserTypeModel.dao
					.find("select * from user_type order by id asc");
			for (UserTypeModel u : usertype) {
				String sql = String.format(
						"INSERT INTO user_type VALUES (%d,'%s')",
						u.getInt("id"), u.getStr("name"));
				this.exportdatatofile(fos, sql);
			}

			List<UsersModel> users = UsersModel.dao
					.find("select u.* from users as u INNER JOIN exams as e on u.cardno=e.cardno and e.layout_id in("
							+ StringUtils.join(layoutids, ",")
							+ ") order by u.id asc");
			Set<String> userIds = new HashSet<String>();
			for (UsersModel u : users) {
				if (!userIds.contains(u.getStr("cardno"))) {
					String sql = String
							.format("INSERT INTO users VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %d)",
									u.getInt("id"), u.getStr("cardno"),
									u.getStr("name"), u.getStr("card_type"),
									u.getStr("last_exam_time"),
									u.getStr("bankno"), u.getStr("bank_name"),
									u.getStr("telphone"),
									u.getStr("department"), u.getInt("status"));
					this.exportdatatofile(fos, sql);
					// 查找该用户的用户类型
					List<UsersUtypesModel> utypes = UsersUtypesModel.dao.find(
							"select * from users_utypes where cardno=?",
							u.getStr("cardno"));
					if (utypes != null && utypes.size() > 0) {
						UsersUtypesModel uu = utypes.get(0);
						sql = String
								.format("INSERT INTO users_utypes VALUES (%d, '%s', %d, '%s', '%s', '%s', %d, %d)",
										uu.getInt("id"), uu.getStr("cardno"),
										uu.getInt("type_id"),
										uu.getStr("bank_no"),
										uu.getStr("bank_name"),
										uu.getStr("bank_path"),
										uu.getInt("city"), uu.getInt("country"));
						this.exportdatatofile(fos, sql);
					}
					userIds.add(u.getStr("cardno"));
				}
			}

			fos.flush();

			String downfile = GUIConstants.CACHE_DIR + "/datapackage.zip";
			File zfile = new File(downfile);
			if (zfile.exists()) {
				zfile.delete();
			}
			// 生成ZIP加密文件
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters
					.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
			parameters.setPassword(zippassword);

			ZipFile zipfile = new ZipFile(zfile);
			zipfile.addFile(outfile, parameters);

			return downfile;
		} catch (IOException e) {
			log.error("异地部署包", e);
			return null;
		} catch (ZipException e) {
			log.error("异地部署包，压缩", e);
			return null;
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void exportdatatofile(FileOutputStream pos, String str)
			throws IOException {
		if (str == null) {
			return;
		}
		byte[] b = str.getBytes("UTF-8");
		// pos.write(FileTool.intToByte(b.length));
		pos.write(b);
		pos.write("\r\n".getBytes("UTF-8"));
		pos.flush();
	}

}
