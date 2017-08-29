package com.maiyoule.miniexam;

import java.io.File;

public class GUIConstants {

	/**
	 * 应用程序运行模式<br>
	 * 0 全部 : 服务器版本模式<br>
	 * 1 单机版模式 : 单机版模式只保留了部分功能菜单，不提供过多管理功能
	 */
	public static short APP_MODEL = 0;

	// 网站根目录
	public static String WEB_ROOT = "";
	// 缓存目录
	public static String CACHE_DIR = "";

	public static String BASEPATH = "basePath";

	// 列表分页大小
	public static int PAGE_SIZE = 10;

	public static String STRING_SPLITE = ";-;-;";

	// Memcache缓存时间 1小时
	public static int CACHE_TIME_EXPIRE = 3600;

	// 文件大小 5M
	public static final int FILE_MAXSIZE = 1024 * 1024 * 5;

	// 加密文件密码
	public static final String ZIP_PASSWORD = "lop_sys_mm_go_pa_325";

	// 换行符
	public static final String NEWLINE = "\r\n";

	// 编码
	public static final String CHARSET = "UTF-8";

	public static final String DATA_PAPER = "1";

	public static final String NON_DATA_PAPER = "2";

	/**
	 * 是否安装
	 * 
	 * @return
	 */
	public static boolean isInstall() {
		File installLockFile = new File(String.format("%sinstall.lock", GUIConstants.WEB_ROOT));
		return installLockFile.exists();
	}
}
