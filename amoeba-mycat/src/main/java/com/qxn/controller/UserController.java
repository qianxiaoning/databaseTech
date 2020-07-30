package com.qxn.controller;

import com.qxn.mapper.MysqlMapper;
import com.qxn.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private MysqlMapper mysqlMapper;
    @RequestMapping("/a")
    public List<User> selectUser(){
        List<User> list = mysqlMapper.selectUser();
        System.out.println(list);
        return list;
    }
}
