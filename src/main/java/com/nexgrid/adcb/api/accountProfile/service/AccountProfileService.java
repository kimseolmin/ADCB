package com.nexgrid.adcb.api.accountProfile.service;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.StringUtil;

@Service("accountProfileService")
public class AccountProfileService {

	
	// AccountProfile API 필수 body값 체크
	public void reqBodyCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		
		// body key 체크
		if( paramMap==null || paramMap.size() == 0 || !paramMap.containsKey("msisdn") ) {
			throw new CommonException("400", "2", "30300001", "Invalid Request Body[Key]: msisdn", logVO.getFlow());
		}
		
		// body value 체크
		if( paramMap.get("msisdn") == null || paramMap.get("msisdn").equals("")  ) {
			throw new CommonException("400", "2", "30300002", "Invalid Request Body[Value]: msisdn", logVO.getFlow());
		}
	}
	
	
	// NCAS 연동 결과 -> boku 응답 
	public void getAccountProfile(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		Map<String, String> ncasRes = (Map<String, String>) paramMap.get("ncasRes");
		
		String msisdn = StringUtil.getCtn344(ncasRes.get("CTN")); // 소비자의 전체 MSISDN
		
		String account_type = ""; // 계정 유형(선불, 후불 또는 기업 계정)
		if("I".equals(ncasRes.get("CUST_TYPE_CODE"))) {	// 개인 계정
			if("".equals(ncasRes.get("PRE_PAY_CODE"))) { // ""일 경우 후불
				account_type = "post-paid";
			}else{ // 선불
				account_type = "prepaid";
			}
		}else{	// 기업 계정
			account_type = "enterprise account";
		}
		

		
		
		
		
	}
	
	
	


}
