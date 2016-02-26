package com.maiyoule.miniexam;

import java.io.File;

import org.apache.log4j.Logger;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.cache.EhCache;
import com.jfinal.plugin.activerecord.dialect.Sqlite3Dialect;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.FreeMarkerRender;
import com.maiyoule.miniexam.db.Db2Dialect;
import com.maiyoule.miniexam.interceptor.AuthInterceptor;
import com.maiyoule.miniexam.interceptor.InstallInterceptor;
import com.maiyoule.miniexam.model.AdminModel;
import com.maiyoule.miniexam.model.AnswersModel;
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
import com.maiyoule.miniexam.services.AyncWork;
import com.maiyoule.miniexam.utils.FileTool;
import com.maiyoule.miniexam.utils.PropHelper;
import com.maiyoule.miniexam.utils.S;

import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;

public class AppConfig extends JFinalConfig {
	private Logger log = Logger.getLogger(AppConfig.class);

	@Override
	public void configConstant(Constants constants) {
		// 开启调试模式，正式环境关闭
		constants.setDevMode(true);

	}

	@Override
	public void configHandler(Handlers handlers) {

	}

	@Override
	public void configInterceptor(Interceptors interceptors) {
		InstallInterceptor install = new InstallInterceptor();
		interceptors.add(install);
		// 验证是否登录
		AuthInterceptor auth = new AuthInterceptor();
		interceptors.add(auth);
	}

	@Override
	public void configPlugin(Plugins plugins) {
		GUIConstants.WEB_ROOT = JFinal.me().getServletContext().getRealPath("/") + File.separator;
		System.out.println("WebRoot === " + GUIConstants.WEB_ROOT);
		// 创建必要的目录
		File cachedir = new File(GUIConstants.WEB_ROOT + "cache");
		if (!cachedir.exists()) {
			cachedir.mkdirs();
		}
		GUIConstants.CACHE_DIR = GUIConstants.WEB_ROOT + "cache";
		
		System.out.println("Cache dir === " + GUIConstants.CACHE_DIR);

		// 读取用户名
		File keyfile = new File(String.format("%sWEB-INF/key.prop", GUIConstants.WEB_ROOT));

		if (!keyfile.exists()) {
			log.error("没有正确安装数据，key.prop不存在！");
			return;
		}

		String dbkeyfile = FileTool.readFileContent(keyfile);
		if (dbkeyfile == null) {
			log.error("没有正确安装数据，key.prop内容不正确！");
			return;
		}
		/**
		 * 公钥 MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDICt5CFtmAJ6i/1
		 * SWXCONGNQmjwb8zRW5dqczRH6RSKzhoAWt/50nZtAlQdFVViWL+
		 * knw2cScOjklPPuotNmSZLsUM16bvhrFCnCjkF1Bkj6oTmUVk2TlfmobaSbl1vFDtlo0EH
		 * /JonbK1e8ZeRfGhFFF6lXJD2fZDc45A8slkWQIDAQAB 私钥
		 * MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMgK3kIW2YAnqL
		 * /VJZcI40Y1CaPBvzNFbl2pzNEfpFIrOGgBa3
		 * /nSdm0CVB0VVWJYv6SfDZxJw6OSU8+6i02ZJkuxQzXpu+GsUKcKOQXUGSPqhOZRWTZOV+
		 * ahtpJuXW8UO2WjQQf8midsrV7xl5F8aEUUXqVckPZ9kNzjkDyyWRZAgMBAAECgYB780AEc2QRpIFLXCcOjtNEjViLv2FZw4u
		 * /PZdLfO+
		 * 0elNZHogdmJKJ8qRS1cKKXbaZRurwUOyCeGe0UqeHnEx4Bakpv7GACTE2bL7OS3Wk9ASQXOrT9NXxfnY3lnNr1GQpqsRppxCYiXJgjBQuX31JEcDDKjkzr7YisffRJV1+gQJBAOXSE/UuzeSeV3PBrn/DbXsb8k9IJFpulmv2Lx6wYLSKDwlz8DZXB39AxvlqBVMkyal8I7FyfsakxdYTDyHBRzsCQQDe1GnMtkRdYC9bTtSx8/KUzDyDg4ALIudQNL2l0mXgVoQUkv93DvmqyGmTlEM9Kpf6Z94vRMaiFXWyS100/9F7AkEAmTHuR9PJP4olNW54CYaV5ih830hxoy5wassSIdzkubQFzgFQsIhW4QanG/8GSaQOIUI08MYnp/aSQd82iQOznQJAGeNXv17VpnuMRPFtqJcM8digly7p62FMunbGKcO97khe4/0IDQ8CVqMeEPVCspKGQaNbnuZApYgUungjBgtKjQJBAJdPB5kt2BXouYjsFIHpul+iiM/wp6h79tF2dn/B/ILIV6mZ2dMoQNr5ZbYZFL5CfYV/GU/l/K+UUWUoQ77df
		 * H Y =
		 */
		// String
		// privatekey="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMgK3kIW2YAnqL/VJZcI40Y1CaPBvzNFbl2pzNEfpFIrOGgBa3/nSdm0CVB0VVWJYv6SfDZxJw6OSU8+6i02ZJkuxQzXpu+GsUKcKOQXUGSPqhOZRWTZOV+ahtpJuXW8UO2WjQQf8midsrV7xl5F8aEUUXqVckPZ9kNzjkDyyWRZAgMBAAECgYB780AEc2QRpIFLXCcOjtNEjViLv2FZw4u/PZdLfO+0elNZHogdmJKJ8qRS1cKKXbaZRurwUOyCeGe0UqeHnEx4Bakpv7GACTE2bL7OS3Wk9ASQXOrT9NXxfnY3lnNr1GQpqsRppxCYiXJgjBQuX31JEcDDKjkzr7YisffRJV1+gQJBAOXSE/UuzeSeV3PBrn/DbXsb8k9IJFpulmv2Lx6wYLSKDwlz8DZXB39AxvlqBVMkyal8I7FyfsakxdYTDyHBRzsCQQDe1GnMtkRdYC9bTtSx8/KUzDyDg4ALIudQNL2l0mXgVoQUkv93DvmqyGmTlEM9Kpf6Z94vRMaiFXWyS100/9F7AkEAmTHuR9PJP4olNW54CYaV5ih830hxoy5wassSIdzkubQFzgFQsIhW4QanG/8GSaQOIUI08MYnp/aSQd82iQOznQJAGeNXv17VpnuMRPFtqJcM8digly7p62FMunbGKcO97khe4/0IDQ8CVqMeEPVCspKGQaNbnuZApYgUungjBgtKjQJBAJdPB5kt2BXouYjsFIHpul+iiM/wp6h79tF2dn/B/ILIV6mZ2dMoQNr5ZbYZFL5CfYV/GU/l/K+UUWUoQ77dfHY= ";
		String dbuserinfo = S.d(dbkeyfile);
		// 分割
		if (dbuserinfo == null || dbuserinfo.length() < 5) {
			log.error("没有正确安装数据，sqlite数据库用户信息不正确！");
			return;
		}
		dbuserinfo = dbuserinfo.substring(5);
		String url = String.format("jdbc:sqlite:%sWEB-INF/final.prop",
				GUIConstants.WEB_ROOT, dbuserinfo);

		C3p0Plugin c3p0Plugin = new C3p0Plugin(url, null, dbuserinfo);
		c3p0Plugin.setDriverClass("org.sqlite.JDBC");
		plugins.add(c3p0Plugin);

		ActiveRecordPlugin arp = new ActiveRecordPlugin(DbKit.MAIN_CONFIG_NAME,c3p0Plugin);
		plugins.add(arp);
		
		arp.setDialect(new Sqlite3Dialect());
		
		// ehcache
		plugins.add(new EhCachePlugin());
		arp.setCache(new EhCache());
		
		arp.addMapping("admin", AdminModel.class);
		arp.addMapping("library", LibraryModel.class);
		arp.addMapping("answers", AnswersModel.class);
		arp.addMapping("areas", AreaModel.class);
		arp.addMapping("banks", BanksModel.class);
		arp.addMapping("config", ConfigModel.class);
		arp.addMapping("exams", ExamsModel.class);
		arp.addMapping("layouts", LayoutsModel.class);
		arp.addMapping("orgtype", OrgtypeModel.class);
		arp.addMapping("papers", PapersModel.class);
		arp.addMapping("papers_scale", PaperScaleModel.class);
		arp.addMapping("pcb", PcbModel.class);
		arp.addMapping("questions", QuestionModel.class);
		arp.addMapping("users", UsersModel.class);
		arp.addMapping("papers_user_type", PapersUserTypeModel.class);
		arp.addMapping("user_type", UserTypeModel.class);
		arp.addMapping("users_utypes", UsersUtypesModel.class);

		//TODO 暂时不同连DB2数据库
		if (GUIConstants.APP_MODEL == 0) {

			String db2url = PropHelper.getDb2Config(GUIConstants.WEB_ROOT, "db.url");
			String db2user = PropHelper.getDb2Config(GUIConstants.WEB_ROOT, "db.user");
			String db2pwd = PropHelper.getDb2Config(GUIConstants.WEB_ROOT, "db.password");
			String db2driver = PropHelper.getDb2Config(GUIConstants.WEB_ROOT, "db.driver");

			C3p0Plugin db2c3p0 = new C3p0Plugin(db2url, db2user, db2pwd, db2driver);
			plugins.add(db2c3p0);
			
			ActiveRecordPlugin arpdb2=new ActiveRecordPlugin("db2",db2c3p0);
			plugins.add(arpdb2);
			arpdb2.setDialect(new Db2Dialect());
		}

	}

	@Override
	public void configRoute(Routes routes) {
		// 添加路由
		routes.add(new AppRoute());
	}

	@Override
	public void afterJFinalStart() {
		try {
			Configuration cfg = FreeMarkerRender.getConfiguration();
			cfg.setSharedVariable("APP_NAME", "在线考试系统");
			cfg.setSharedVariable("APP_MODEL", GUIConstants.APP_MODEL);

		} catch (TemplateModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 启动异步工作
		AyncWork ayncwork = AyncWork.getInstance();
		ayncwork.start();

		super.afterJFinalStart();
	}

	@Override
	public void beforeJFinalStop() {
		// 清理必要的数据
		AyncWork ayncwork = AyncWork.getInstance();
		ayncwork.stop();

		File outdatafile = new File(GUIConstants.CACHE_DIR + "/out.data");
		if (outdatafile.exists()) {
			outdatafile.delete();
		}
		File zfile = new File(GUIConstants.CACHE_DIR + "/datapackage.zip");
		if (zfile.exists())
			zfile.delete();

		super.beforeJFinalStop();
	}

}
