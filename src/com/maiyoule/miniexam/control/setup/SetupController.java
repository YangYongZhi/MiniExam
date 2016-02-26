package com.maiyoule.miniexam.control.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.core.ZipFile;
import com.jfinal.aop.ClearInterceptor;
import com.jfinal.aop.ClearLayer;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.upload.UploadFile;
import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.utils.Ajax;

@ClearInterceptor(ClearLayer.ALL)
public class SetupController extends Controller {

    public void index() {
        if (GUIConstants.isInstall()) {
            this.render("/setup/installed.html");
            return;
        }
        this.render("/setup/index.html");
    }

    /**
     * 异地考试开始前，将总行分发的考试数据进行导入，做考试基础数据初始化
     */
    public void imports() {
        UploadFile packagefile = this.getFile("datapackage", GUIConstants.CACHE_DIR,
                GUIConstants.FILE_MAXSIZE);
        if (packagefile == null) {
            this.renderText(Ajax.message("请上传数据安装文件", "安装失败"), "text/html;charset=utf-8");
            return;
        }

        String password = this.getPara("password");

        ZipFile zipFile;
        FileReader fis = null;
        BufferedReader br = null;
        try {
            zipFile = new ZipFile(packagefile.getFile());

            if (!zipFile.isEncrypted()) {
                this.renderText(Ajax.message("请上传正确的数据文件", "导入失败"), "text/html;charset=utf-8");
                return;
            }
            zipFile.setPassword(password);
            if (!zipFile.isValidZipFile()) {
                this.renderText(Ajax.message("无效的数据文件，请重新上传", "失败"), "text/html;charset=utf-8");
                return;
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
                    Db.batch(sqlbatch, sqlbatch.size());
                    sqlbatch.clear();
                }
            }

            //执行剩下的SQL
            if (sqlbatch.size() > 0) {
                Db.batch(sqlbatch, sqlbatch.size());
                sqlbatch.clear();
            }

            //创建标志文件，表示数据已经安装成功
            File lockfile = new File(String.format("%sinstall.lock", GUIConstants.WEB_ROOT));
            if (!lockfile.exists()) {
                int retry = 0;
                while (retry < 3 && !lockfile.createNewFile()) {
                    lockfile.createNewFile();
                }
            }
            this.renderText(Ajax.message("数据安装成功，可以开始考试！", "成功", true), "text/html;charset=utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            this.renderText(Ajax.message("数据安装出现异常，请检查您的安装数据包与密码是否正确并重新导入！", "失败"),
                    "text/html;charset=utf-8");
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
}
