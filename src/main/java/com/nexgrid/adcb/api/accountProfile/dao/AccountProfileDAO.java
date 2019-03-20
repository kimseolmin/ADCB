package com.nexgrid.adcb.api.accountProfile.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountProfileDAO {
	
	// 30일 이내 구매 여부
	public int getPurchase(@Param("param") Map<String, Object> paramMap) throws Exception;

}
