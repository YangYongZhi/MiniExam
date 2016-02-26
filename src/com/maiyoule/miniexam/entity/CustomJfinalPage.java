package com.maiyoule.miniexam.entity;

import java.util.List;

import com.jfinal.plugin.activerecord.Page;

public class CustomJfinalPage extends Page{

    public CustomJfinalPage(List list, int pageNumber, int pageSize, int totalPage, int totalRow) {
        super(list, pageNumber, pageSize, totalPage, totalRow);
    }
    

}
