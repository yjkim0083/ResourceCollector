package com.mobigen.util.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceCollectDAO {
	
	@Autowired
	private SqlSession sqlSession;
	
	public void insertResource(Map<String, String> resultMap) throws Exception {
		sqlSession.insert("resourceMapper.insertResource", resultMap);
	}
	
	public Map<String,Object> selectPrevResource(String serverIp) throws Exception {
		Map<String,String> param = new HashMap<>();
		param.put("serverIp", serverIp);
		return sqlSession.selectOne("resourceMapper.selectPrevResource", param);
	}
	
	public Map<String, Integer> selectThresholdByServerIp(String serverIp)  throws Exception {
		Map<String,String> param = new HashMap<>();
		param.put("serverIp", serverIp);
		return sqlSession.selectOne("resourceMapper.selectThresholdByServerIp", param);
	}

	public void insertAlert(List<Map<String, Object>> thresholdOverList) {
		sqlSession.insert("resourceMapper.insertAlert", thresholdOverList);
	}

}
