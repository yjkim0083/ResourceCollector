package com.mobigen.util.dao;

import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceCollectDAO {
	
	@Autowired
	private SqlSession sqlSession;
	
	public void insertResource(Map<String, String> resultMap) {
		sqlSession.insert("resourceMapper.insertResource", resultMap);
	}

}
