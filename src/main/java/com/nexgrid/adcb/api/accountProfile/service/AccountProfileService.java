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
		
		if( paramMap==null || paramMap.size() == 0 || !paramMap.containsKey("msisdn") ) {
			throw new CommonException("400", "2", "30300001", "Invalid Request Body[Key]: msisdn", logVO.getFlow());
		}
		
		if( paramMap.get("msisdn") == null || paramMap.get("msisdn").equals("")  ) {
			throw new CommonException("400", "2", "30300002", "Invalid Request Body[Value]: msisdn", logVO.getFlow());
		}
	}
	
	
	


}
