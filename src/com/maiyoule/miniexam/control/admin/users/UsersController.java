package com.maiyoule.miniexam.control.admin.users;

import com.maiyoule.miniexam.control.admin.C;
import com.maiyoule.miniexam.entity.SyncUserStatus;
import com.maiyoule.miniexam.model.UsersModel;
import com.maiyoule.miniexam.services.AyncWork;
import com.maiyoule.miniexam.services.LoadUserService;

public class UsersController extends C {
	
	public String index(){
		
		//读取现有人员数量
		UsersModel users=UsersModel.dao.findFirst("select count(*) as al from users");
		int count=users.getInt("al");
		this.setAttr("count", count);

		SyncUserStatus sync=SyncUserStatus.getInstance();
		this.setAttr("sync", sync);
		this.render("/contral/users/index.html");
		return null;
	}
	
	/**
	 * 拉取用户
	 * @return
	 */
	public String pullusers(){
		SyncUserStatus sync=SyncUserStatus.getInstance();
		sync.setOpstatus(SyncUserStatus.OP_OK);
		sync.setStatus(SyncUserStatus.STATUS_WAITING);
		AyncWork ayncwork=AyncWork.getInstance();
		ayncwork.getAnycService().addWork(LoadUserService.class, "doload");
		return this.ajaxMessage("已成功加入队列，等待处理，处理完成后将会将处理结果反馈到当前页面，请稍后", "成功",
				true);
	}
	
	/**
	 * 取消
	 * @return
	 */
	public String cancel(){
		SyncUserStatus sync=SyncUserStatus.getInstance();
		sync.setOpstatus(SyncUserStatus.OP_CANCEL);
		sync.setStatus(SyncUserStatus.STATUS_CANCEL);
		
		return this.ajaxMessage("停止成功","成功", true);
	}
	/**
	 * 查询状态
	 * @return
	 */
	public String querystatus(){
		SyncUserStatus sync=SyncUserStatus.getInstance();
		if(sync.getStatus()==0){
			return this.ajaxMessage("","状态",false);
		}else{
			return this.ajaxMessage(sync,"状态",true);
		}
	}
}
