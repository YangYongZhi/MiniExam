package com.maiyoule.miniexam.control.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.ehcache.CacheKit;
import com.maiyoule.miniexam.model.ConfigModel;

public class ConfigController extends C {
    public void index() {
        List<ConfigModel> configs = ConfigModel.dao.find("select * from config");
        if (configs != null) {

            for (ConfigModel cfg : configs) {
                String id = cfg.getStr("id");
                String value = cfg.get("value");
                if (id.equalsIgnoreCase("examNotice")) {
                    value = value.replaceAll("<br>", "");
                }
                this.setAttr(id, value);
            }

        }
        this.render("/contral/config/index.html");
    }

    public String update() {
        Map<String, String[]> kvs = this.getParaMap();

        Set<String> keys = kvs.keySet();

        for (String key : keys) {
            String[] values = kvs.get(key);
            String val = values[0];
            //            System.out.println(val);

            if (key.equalsIgnoreCase("examNotice")) {
                val = val.replaceAll("\n", "<br>\n");
            }

            ConfigModel cfg = ConfigModel.dao.findById(key);
            if (cfg == null) {
                cfg = new ConfigModel();
                cfg.set("id", key);
                cfg.set("value", val);
                cfg.save();
            } else {
                cfg.set("id", key);
                cfg.set("value", val);
                cfg.update();
            }
        }
        try {
            CacheKit.removeAll("config");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.ajaxMessage("设置成功", "成功", true);
    }

    /**
     * 恢复数据库，清空考试数据初始页面.
     */
    public void recoverdb() {
        this.render("/contral/config/recoverdb.html");
    }

    /**
     * 恢复考试相关数据库及缓存，清空考试数据
     * 
     * @return
     */
    public String dorecoverdb() {
        List<String> batchSql = new ArrayList<String>();
        batchSql.add("DELETE FROM ANSWERS");
        batchSql.add("UPDATE SQLITE_SEQUENCE SET SEQ = 0 WHERE NAME = 'answers' OR NAME = 'ANSWERS'");
        batchSql.add("DELETE FROM EXAMS");
        batchSql.add("UPDATE SQLITE_SEQUENCE SET SEQ = 0 WHERE NAME = 'exams' OR NAME = 'EXAMS'");
        batchSql.add("DELETE FROM LAYOUTS");
        batchSql.add("UPDATE SQLITE_SEQUENCE SET SEQ = 0 WHERE NAME = 'layouts' OR NAME = 'LAYOUTS'");
        try {
            Db.batch(batchSql, batchSql.size());
        } catch (Exception e) {
            return this.ajaxMessage("清空考试数据失败，请稍后再试！", "失败");
        }
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
            return this.ajaxMessage("清空考试数据缓存失败，请稍后再试！", "失败");
        }
        return this.ajaxMessage("清空考试数据成功！", "成功", true);
    }
}
