package com.maiyoule.miniexam.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.entity.BankInfo;
import com.maiyoule.miniexam.entity.BankSelectBoxItem;
import com.maiyoule.miniexam.utils.S;
import com.maiyoule.miniexam.utils.StringHelper;

public class BanksModel extends Model<BanksModel> {

    /**
	 * 
	 */
    private static final long serialVersionUID = -5561038143272611081L;
    public static BanksModel dao = new BanksModel();

    public String findPath(String bankno, String parentPath) {
        if (StringUtils.equals(bankno, "0")) {
            return parentPath;
        }
        BankInfo bank = this.getByNo(bankno);
        if (bank == null) {
            return bankno + GUIConstants.STRING_SPLITE + parentPath;
        } else {
            return this.findPath(bank.getParentno(), bank.getNo() + GUIConstants.STRING_SPLITE
                    + parentPath);
        }
    }

    public BankInfo getByNo(String no) {
        if (StringHelper.isNullOrEmpty(no)) {
            return null;
        }
        no = no.trim();
        BankInfo bankInfo = CacheKit.get("bankcache", no);
        if (bankInfo != null) {
            return bankInfo;
        }
        BanksModel banksModel = this.findFirst("select * from banks where no=?", no);
        if (banksModel == null) {
            return null;
        }
        bankInfo = new BankInfo();
        bankInfo.setNo(banksModel.getStr("no"));
        bankInfo.setName(banksModel.getStr("name"));
        bankInfo.setParentno(banksModel.getStr("parent_no"));
        try {
            bankInfo.setCityNo(banksModel.getInt("city_no"));
        } catch (Exception e) {
            bankInfo.setCityNo(0);
        }
        bankInfo.setCityName(banksModel.getStr("city_name"));
        try {
            bankInfo.setCountryNo(banksModel.getInt("country_no"));
        } catch (Exception e) {
            bankInfo.setCountryNo(0);
        }
        bankInfo.setConntryName(banksModel.getStr("country_name"));
        CacheKit.put("bankcache", no, bankInfo);
        // mcache.set(no, bankInfo);
        return bankInfo;
    }

    public List<BankInfo> findByArea(int city, int country, String parent) {

        StringBuffer sql = new StringBuffer();
        sql.append("select * from banks where parent_no=? and city_no=? ");
        if (country > 0) {
            sql.append(" and country_no=? ");
        }

        List<BanksModel> list = null;
        if (country > 0) {
            list = this.find(sql.toString(), parent, city, country);
        } else {
            list = this.find(sql.toString(), parent, city);
        }
        List<BankInfo> banks = new ArrayList<BankInfo>();
        if (list != null) {
            for (BanksModel b : list) {
                BankInfo bankInfo = new BankInfo();
                bankInfo.setNo(b.getStr("no"));
                bankInfo.setName(b.getStr("name"));
                bankInfo.setParentno(b.getStr("parent_no"));
                bankInfo.setCityNo(b.getInt("city_no"));
                bankInfo.setCityName(b.getStr("city_name"));
                bankInfo.setCountryNo(b.getInt("country_no"));
                bankInfo.setConntryName(b.getStr("country_name"));
                bankInfo.setIsleaf(b.getStr("isleaf"));
                banks.add(bankInfo);
            }
        }
        return banks;
    }

    public List<BankSelectBoxItem> findTreeBox(List<String> bankPaths) {

        List<BankSelectBoxItem> list = new ArrayList<BankSelectBoxItem>();

        for (String str : bankPaths) {

            String[] tmp = str.split(GUIConstants.STRING_SPLITE);
            if (tmp.length <= 1) {
                continue;
            }

            checkChildTreeBox(list, tmp, 1);
        }

        return list;
    }

    private List<BankSelectBoxItem> checkChildTreeBox(List<BankSelectBoxItem> list, String[] codes,
            int point) {
        if (list == null) {
            list = new ArrayList<BankSelectBoxItem>();
        }
        if (codes.length <= point) {
            return null;
        }
        String traget = codes[point];

        BankSelectBoxItem tragetbox = null;
        for (BankSelectBoxItem item : list) {
            if (item.getV().equals(traget)) {
                tragetbox = item;
                break;
            }
        }
        if (tragetbox != null) {
            // 查找子节点
            tragetbox.setS(checkChildTreeBox(tragetbox.getS(), codes, (++point)));
        } else {
            BankInfo bank = this.getByNo(traget);
            if (bank != null) {
                tragetbox = new BankSelectBoxItem();
                tragetbox.setN(bank.getName());
                tragetbox.setV(bank.getNo());
                tragetbox.setS(this.checkChildTreeBox(tragetbox.getS(), codes, (++point)));

                list.add(tragetbox);
            }
        }

        return list;
    }

    public List<BankTreeItem> findByAreaAndType(String[] areas, String[] orgtype) {
        StringBuffer condition = new StringBuffer();

        if (areas != null && areas.length > 0) {
            condition.append("country_no in('");
            condition.append(StringUtils.join(areas, "','"));
            condition.append("')");
        }
        if (orgtype != null && orgtype.length > 0) {
            if (condition.length() > 0) {
                condition.append(" and ");
            }
            condition.append("orgtype_no in('");
            condition.append(StringUtils.join(orgtype, "','"));
            condition.append("')");
        }
        String sql = "select no,name,parent_no from banks";
        if (condition.length() > 0) {
            sql += " where " + condition.toString();
        }
        String key = S.md5Encoding(sql);
        List<BankTreeItem> items = CacheKit.get("bankcache", key);
        if (items != null) {
            return items;
        }
        items = new ArrayList<BankTreeItem>();
        List<BanksModel> banks = this.find(sql);
        for (BanksModel b : banks) {
            items.add(new BankTreeItem(b.getStr("no"), b.getStr("parent_no"), b.getStr("name")));
        }
        CacheKit.put("bankcache", key, items);

        return items;
    }

}
