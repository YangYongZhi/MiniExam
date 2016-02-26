package com.maiyoule.miniexam.ui;

import com.jfinal.core.Controller;

public interface IPage {
	public void run(Controller c);
	public String toQueryString();
}
