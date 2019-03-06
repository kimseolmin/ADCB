package com.nexgrid.adcb.common.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import com.nexgrid.adcb.common.vo.LogVO;



public interface CommonService {

	String getIpAddr(HttpServletRequest request);
	
	void contentTypeCheck(HttpServletRequest request, LogVO logVO) throws Exception;
	
	void getNcasGetMethod(Map<String, Object> paramMap, LogVO logVO) throws Exception;
	
	Map<String,String> getNcasResHeader(ResponseEntity<String> responseEntity) throws Exception;

	void omsLogWrite(LogVO logVO);
	
	Map<String,Object> getSuccessResult() throws Exception;
	
	boolean userEligibilityCheck(Map<String, Object> ncasRes, LogVO logVO) throws Exception;
	
}
