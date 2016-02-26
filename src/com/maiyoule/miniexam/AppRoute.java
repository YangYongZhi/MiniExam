package com.maiyoule.miniexam;

import java.util.Map.Entry;
import java.util.Set;

import com.jfinal.config.Routes;
import com.jfinal.core.Controller;
import com.maiyoule.miniexam.control.IndexController;
import com.maiyoule.miniexam.control.admin.AjaxController;
import com.maiyoule.miniexam.control.admin.ConfigController;
import com.maiyoule.miniexam.control.admin.DummyController;
import com.maiyoule.miniexam.control.admin.LoginController;
import com.maiyoule.miniexam.control.admin.MainController;
import com.maiyoule.miniexam.control.admin.data.ImportExportController;
import com.maiyoule.miniexam.control.admin.exam.ExamController;
import com.maiyoule.miniexam.control.admin.exam.OnlineController;
import com.maiyoule.miniexam.control.admin.layouts.LayoutController;
import com.maiyoule.miniexam.control.admin.paper.PaperController;
import com.maiyoule.miniexam.control.admin.question.LibraryController;
import com.maiyoule.miniexam.control.admin.question.QuestionController;
import com.maiyoule.miniexam.control.admin.results.AbsentController;
import com.maiyoule.miniexam.control.admin.results.AnalysisController;
import com.maiyoule.miniexam.control.admin.results.ScoreController;
import com.maiyoule.miniexam.control.admin.users.UsersController;
import com.maiyoule.miniexam.control.setup.SetupController;

public class AppRoute extends Routes {

	@Override
	public void config() {
		this.add("/", IndexController.class);
		if (GUIConstants.APP_MODEL == 1) {
			//Mini模式
			// 数据安装路径
			this.add("/setup", SetupController.class);
		} else {
			//Dummy
			this.add("/dummy", DummyController.class);

			this.add("/contral/papers", PaperController.class);
			// 用户
			this.add("/contral/users", UsersController.class);
			// 安排
			this.add("/contral/layouts", LayoutController.class);
			// 设置
			this.add("/contral/config", ConfigController.class);
			// 试卷分析
			this.add("/contral/results/analysis", AnalysisController.class);
			
			//全部
			// 试题管理
			this.add("/contral/ques", QuestionController.class);
			// 题库
			this.add("/contral/lib", LibraryController.class);
		}
		// 管理登录
		this.add("/login", LoginController.class);
		// 控制中心
		this.add("/contral", MainController.class);
		
		// 考试
		this.add("/contral/exam", ExamController.class);
		// Ajax
		this.add("/contral/ajax", AjaxController.class);
		// 在线状态
		this.add("/contral/online", OnlineController.class);
		// 考试结果
		this.add("/contral/results/score", ScoreController.class);

		// 缺考
		this.add("/contral/results/absent", AbsentController.class);
		// 数据导入导出
		this.add("/contral/data", ImportExportController.class);

		// 考试首页
		this.add("/flex",
				com.maiyoule.miniexam.control.flex.IndexController.class);
		this.add("/flex/users",
				com.maiyoule.miniexam.control.flex.UsersController.class);
		this.add("/flex/exam",
				com.maiyoule.miniexam.control.flex.ExamController.class);
		
		Set<Entry<String, Class<? extends Controller>>> set = this.getEntrySet();
		System.out.println("---------请求映射关系-------");
		for(Entry<String, Class<? extends Controller>> entry : set) {
			System.out.println(entry.getKey() + " --> " + entry.getValue());
		}
	}

}
