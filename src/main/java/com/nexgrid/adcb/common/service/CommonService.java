package com.nexgrid.adcb.common.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.nexgrid.adcb.common.vo.LogVO;



public interface CommonService {

	//void getAuthenticationDAS(Map<String, Object> paramMap, LogVO logVO) throws Exception;

	//void getDataDevices(Map<String, Object> paramMap, LogVO logVO) throws Exception;

	//void getServiceId(Map<String, Object> paramMap, LogVO logVO) throws Exception;

	//void getServiceId2(Map<String, Object> paramMap, LogVO logVO) throws Exception;

	
	//void seperateDevices(Map<String, Object> paramMap, LogVO logVO) throws Exception;

	String getIpAddr(HttpServletRequest request);
	
	void contentTypeCheck(HttpServletRequest request, LogVO logVO) throws Exception;
	
	void getNcasGetMethod(Map<String, Object> paramMap, LogVO logVO) throws Exception;

	void omsLogWrite(LogVO logVO);
	
}
