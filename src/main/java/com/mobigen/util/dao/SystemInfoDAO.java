package com.mobigen.util.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SystemInfoDAO {

	@Autowired
	private SqlSession sqlSession;
	
	public List<Map<String,String>> selectSystem(Map<String, String> resultMap) {
		return sqlSession.selectList("systemMapper.selectSystem", resultMap);
	}
	
	public void insertSystem(Map<String, String> resultMap) {
		sqlSession.insert("systemMapper.insertSystem", resultMap);
	}
	
	public void updateSystem(Map<String, String> resultMap) {
		sqlSession.update("systemMapper.updateSystem", resultMap);
	}
}
