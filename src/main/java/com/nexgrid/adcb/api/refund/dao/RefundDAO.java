package com.nexgrid.adcb.api.refund.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundDAO {

	/**
	 * 환불 API 요청 중복 체크
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	Map<String, String> reqDuplicateCheck(Map<String, Object> paramMap) throws Exception;
	
	
	/**
	 * BOKU의 환불 API 최초 요청 데이터 INSERT
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	int insertRefundReq(@Param("param") Map<String, Object> paramMap) throws Exception;
	
		
	
	/**
	 * 환불 완료 또는 실패 정보 UPDATE
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	int updateRefundInfo(@Param("param") Map<String, Object> paramMap) throws Exception;
	
	
	

	
}
