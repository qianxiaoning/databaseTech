package com.qxn.mapper;

import com.qxn.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MysqlMapper {
	@Insert("insert into user values()")
	int insert();
	@Select("select * from user")
	List<User> selectUser();
}
