package com.qxn;

import com.qxn.mapper.MysqlMapper;
import com.qxn.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MysqlTest {
	@Autowired
	private MysqlMapper mysqlMapper;

	@Test
	public void selectUser() {
		List<User> list = mysqlMapper.selectUser();
		System.out.println(list);
	}
	@Test
	public void insertUser() {
		int i = mysqlMapper.insert();
		System.out.println(i);
	}
}
