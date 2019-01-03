package com.maiyoule.miniexam.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.entity.BankInfo;
import com.maiyoule.miniexam.entity.SyncUserStatus;
import com.maiyoule.miniexam.model.BanksModel;
import com.maiyoule.miniexam.utils.PropHelper;
import com.maiyoule.miniexam.utils.StringHelper;

/**
 * It used to synchronize users and organizations from db2.
 * 
 * @author yongzhi.yang
 *
 */
public class LoadUserService {
	private Logger log = Logger.getLogger(LoadUserService.class);

	/**
	 * For prod.
	 */
	private static final String ORIGINAL_ORG_TABLE_NAME = "BS_ORG_EXAM";

	/**
	 * You cannot use it after adding a redundant organization table.
	 */
	// @Deprecated
	// private static final String ORIGINAL_ORG_TABLE_NAME = "BS_ORG";

	public void doload() {
		try {
			// 连接
			if (isStop()) {
				return;
			}
			this.clearUser();
			this.clearBank();
			this.updateStatus(SyncUserStatus.STATUS_DOING);
			this.initBank();
			this.initUsers();
			this.updateStatus(SyncUserStatus.STATUS_FLISH);
		} catch (Exception e) {
			this.updateStatus(SyncUserStatus.STATUS_EXCEPTION);
			e.printStackTrace();
		}
	}

	/**
	 * 删除考试系统用户数据.
	 */
	private void clearUser() {
		/*
		 * 删除用户数据
		 */
		List<String> deleteBatchSql = new ArrayList<String>();
		deleteBatchSql.add("DELETE FROM users");
		deleteBatchSql.add("DELETE FROM users_utypes");
		Db.batch(deleteBatchSql, deleteBatchSql.size());
		try {
			CacheKit.removeAll("areacache");
			CacheKit.removeAll("bankcache");
			CacheKit.removeAll("utypecache");
			CacheKit.removeAll("orgtypes");
			CacheKit.removeAll("areacount");
			CacheKit.removeAll("userinfos");
			CacheKit.removeAll("layouts");
			CacheKit.removeAll("userpaper");
			CacheKit.removeAll("exams");
			CacheKit.removeAll("config");
			CacheKit.removeAll("answer");
			CacheKit.removeAll("questions");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除考试系统机构数据.
	 */
	private void clearBank() {
		List<String> deleteBatchSql = new ArrayList<String>();
		// deleteBatchSql.add("DELETE FROM banks WHERE orgtype_no <> 'PBC' ");
		deleteBatchSql.add("DELETE FROM banks");
		deleteBatchSql.add("DELETE FROM orgtype");
		Db.batch(deleteBatchSql, deleteBatchSql.size());
	}

	/**
	 * 从db2获取机构信息写入sqlite.
	 */
	private void initBank() {
		// 获取DB2中用户的总数
		String schame = PropHelper.getDb2Config(GUIConstants.WEB_ROOT, "db.schema");
		/*
		 * 从db2获取机构类型信息写入sqlite
		 */
		List<Object[]> db2orgtype = Db.use("db2").query(
		        String.format("SELECT NO,NAME FROM %s%s WHERE parentno ='0'", StringHelper.isNullOrEmpty(schame) ? ""
		                : schame + ".", ORIGINAL_ORG_TABLE_NAME));
		List<String> insertOrgTypeBatchSql = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for (Object[] orgtype : db2orgtype) {
			sb.append("insert into orgtype(no,name,parent_no,parent_name)");
			sb.append(" values(");
			sb.append(String.format("'%s','%s','AAA',''", orgtype[0], orgtype[1]));
			sb.append(");");
			insertOrgTypeBatchSql.add(sb.toString());
			sb.setLength(0);
		}
		// insertOrgTypeBatchSql
		// .add("insert into orgtype(no,name,parent_no,parent_name) values('PBC','人民银行','AAA','')");
		Db.batch(insertOrgTypeBatchSql, insertOrgTypeBatchSql.size());
		/*
		 * 从db2获取金融机构信息写入sqlite
		 */
		/*
		 * List<Object[]> db2bank = Db .use("db2") .query(String .format(
		 * "SELECT NO,NAME,PARENTNO,PARENTNAME,PCBNO,PCBNAME,CITYNO,CITYNAME,COUNTRYNO,COUNTRYNAME,ORGTYPENO,ORGTYPENAME,ISLEAF FROM %sBS_BANK"
		 * , StringHelper.isNullOrEmpty(schame) ? "" : schame + "."));
		 */
		List<Object[]> db2bank = Db.use("db2").query(
		        String.format("SELECT NO,NAME,PARENTNO,PARENTNAME,PCBNO,PCBNAME,P,Q,R,S FROM %s%s",
		                StringHelper.isNullOrEmpty(schame) ? "" : schame + ".", ORIGINAL_ORG_TABLE_NAME));
		List<String> insertBankBatchSql = new ArrayList<String>();
		/*
		 * for (Object[] bank : db2bank) { sb.append(
		 * "insert into banks(no,name,parent_no,parent_name,pcb_no,pcb_name,city_no,city_name,country_no,country_name,orgtype_no,orgtype_name,isleaf)"
		 * ); sb.append(" values("); sb.append(String.format(
		 * "'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s'",
		 * bank[0], bank[1], bank[2], bank[3], bank[4], bank[5], bank[6],
		 * bank[7], bank[8], bank[9], bank[10], bank[11], bank[12]));
		 * sb.append(");"); insertBankBatchSql.add(sb.toString());
		 * sb.setLength(0); }
		 */
		for (Object[] bank : db2bank) {
			sb.append("insert into banks(no,name,parent_no,parent_name,pcb_no,pcb_name,city_no,city_name,country_no,country_name,orgtype_no,orgtype_name,isleaf)");
			sb.append(" values(");
			sb.append(String.format("'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s'", bank[0],
			        bank[1], bank[2], bank[3], bank[4], bank[5], bank[6], bank[7], bank[8], bank[9], "", "", "NO"));
			sb.append(");");
			insertBankBatchSql.add(sb.toString());
			sb.setLength(0);
		}
		Db.batch(insertBankBatchSql, insertBankBatchSql.size());

		/*
		 * 从db2获取人民银行信息写入sqlite
		 */
		/*
		 * List<Object[]> db2pbc = Db .use("db2") .query(String
		 * .format("SELECT NO,NAME,PARENTNO,PCBNO FROM %BS_PCB",
		 * StringHelper.isNullOrEmpty(schame) ? "" : schame + "."));
		 * List<String> insertPbcBatchSql = new ArrayList<String>(); for
		 * (Object[] pbc : db2pbc) { sb.append(
		 * "insert into banks(no,name,parent_no,parent_name,pcb_no,pcb_name,city_no,city_name,country_no,country_name,orgtype_no,orgtype_name,isleaf)"
		 * ); sb.append(" values("); sb.append(String.format(
		 * "'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s'",
		 * pbc[0], pbc[1], pbc[2], pbc[2], pbc[4], pbc[5], pbc[6], pbc[7],
		 * pbc[8], pbc[9], pbc[10], pbc[11], pbc[12])); sb.append(");");
		 * insertPbcBatchSql.add(sb.toString()); sb.setLength(0); }
		 * Db.batch(insertPbcBatchSql, insertPbcBatchSql.size());
		 */
	}

	/**
	 * 从db2获取用户信息写入sqlite.
	 */
	private void initUsers() {
		// 获取DB2中用户的总数
		String schame = PropHelper.getDb2Config(GUIConstants.WEB_ROOT, "db.schema");
		// 获取DB2中用户
		List<Object[]> db2allusers = Db
		        .use("db2")
		        .query(String
		                .format("SELECT ID,LOGINID,NAME,CARDTYPE,ORGCODEOFJR,ORGNAME,DEPTNAME,PHONE,USERSTATUS FROM %sBS_USERINFOOFJG ofjg WHERE ofjg.CARDTYPE != '' AND nowuser = '1'",
		                        StringHelper.isNullOrEmpty(schame) ? "" : schame + "."));
		if (db2allusers == null || db2allusers.size() < 1) {
			this.updateStatus(SyncUserStatus.STATUS_EMPTY);
			return;
		}
		this.updateCount(db2allusers.size());

		List<String> bathsql = new ArrayList<String>();
		int i = 0;
		StringBuffer sb = new StringBuffer();
		for (Object[] u : db2allusers) {
			// System.out.println(u[0] + " " + u[1]);
			if (this.isStop()) {
				break;
			}
			// String db2ID=u[0].toString();
			String cardno = u[1].toString();
			String name = u[2].toString();
			String cardtype = u[3].toString();
			if (StringHelper.isNullOrEmpty(cardtype)) {
				continue;
			}
			String bankno = u[4].toString();
			String bankName = u[5].toString();
			String department = u[6].toString();
			String phome = u[7].toString();
			String userStatus = u[8].toString();

			sb.append("insert into users(cardno,name,card_type,bankno,bank_name,telphone,department,status)");
			sb.append(" values(");
			sb.append(String.format("'%s','%s','%s','%s','%s','%s','%s',%d", cardno, name,
			        (cardtype.equals("身份证") ? "identity" : "passport"), bankno, bankName, phome, department,
			        (userStatus.equals("启用") ? 1 : 0)));
			sb.append(");");
			bathsql.add(sb.toString());
			sb.setLength(0);
			// 在DB2中查询该用户类型记录
			List<Object[]> db2usertype = Db.use("db2").query(
			        String.format(
			                "SELECT ZXUSERTYPE,ZXUSERNAME,ORGCODE,ORGNAME FROM %sBS_USERINFOOFZX WHERE LOGINID='%s'",
			                StringHelper.isNullOrEmpty(schame) ? "" : schame + ".", cardno));

			sb.append("insert into users_utypes(cardno,type_id,bank_no,bank_name,bank_path,city,country) values(");
			sb.append("'" + cardno + "',");

			/*
			 * 如果该用户只要在zx表中对应的记录有数据报送类型，则认为是数据报送用户类型
			 */
			if (this.isDataReportType(cardno, db2usertype)) {
				sb.append("8,");
			} else {
				sb.append("1,");
			}
			sb.append("'" + bankno + "',");
			sb.append("'" + bankName + "',");

			String path = BanksModel.dao.findPath(bankno, StringUtils.EMPTY);
			sb.append("'" + path + "',");
			BankInfo bankInfo = BanksModel.dao.getByNo(bankno);
			if (bankInfo != null) {
				sb.append(bankInfo.getCityNo() + ",");
				sb.append(bankInfo.getCountryNo());
			} else {
				sb.append(0 + ",");
				sb.append(0);
			}
			sb.append(");");
			bathsql.add(sb.toString());
			sb.setLength(0);

			// 刷新正在拉取用户的信息个数
			i++;
			this.updateRead(i);
			if (bathsql.size() > 50) {
				Db.batch(bathsql, bathsql.size());
				bathsql.clear();
				bathsql = new ArrayList<String>();
				this.updateFlish(i);
			}
		}

		if (bathsql.size() > 0) {
			Db.batch(bathsql, bathsql.size());
			bathsql.clear();
			bathsql = null;
			this.updateFlish(i);
		}
	}

	/**
	 * 判断当前用户在db2中对应的用户类型是否包含数据上报用户类型.<br>
	 * 
	 * 如果包含返回true<br>
	 * 否则返回false
	 * 
	 * @param cardno
	 * @param db2usertype
	 * @return
	 */
	private boolean isDataReportType(String cardno, List<Object[]> db2usertype) {
		if (StringUtils.isBlank(cardno)) {
			return false;
		}
		if (db2usertype == null || db2usertype.size() <= 0) {
			return false;
		}
		for (Object[] db2utype : db2usertype) {
			if (db2utype != null) {
				if (StringUtils.equals(db2utype[0].toString(), "企业征信数据报送用户")
				        || StringUtils.equals(db2utype[0].toString(), "个人征信数据报送用户")) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isStop() {
		SyncUserStatus status = SyncUserStatus.getInstance();
		if (status.getOpstatus() == 1) {
			log.info("当前任务已经没有任务或者取消了，不执行操作");
			status.setStatus(SyncUserStatus.STATUS_CANCEL);
			return true;
		}
		return false;
	}

	private void updateStatus(short status) {
		SyncUserStatus sync = SyncUserStatus.getInstance();
		sync.setStatus(status);
	}

	/**
	 * 更新数量
	 * 
	 * @param count
	 */
	private void updateCount(int count) {
		SyncUserStatus sync = SyncUserStatus.getInstance();
		sync.setTotal(count);
	}

	private void updateRead(int count) {

		SyncUserStatus sync = SyncUserStatus.getInstance();
		sync.setReaded(count);
	}

	private void updateFlish(int count) {
		SyncUserStatus sync = SyncUserStatus.getInstance();
		sync.setFlished(count);
	}

}
