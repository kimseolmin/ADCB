package com.nexgrid.adcb.api.submitMT.service;

import java.util.Map;


import org.springframework.stereotype.Service;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.StringUtil;

@Service("submitMT")
public class SubmitMTService {

	
	/**
	 * SubmitMT API body 필수값 체크
	 * @param request
	 * @param logVO
	 * @throws CommonException
	 */
	public void reqBodyCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		// body key 체크
		if( paramMap == null || paramMap.size() == 0 || !paramMap.containsKey("messageId")
				|| !paramMap.containsKey("message") || !paramMap.containsKey("msisdn") 
				|| !paramMap.containsKey("originator") ) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		
		// body value 체크
		String messageId = paramMap.get("messageId") == null ? "" : paramMap.get("messageId").toString();
		String message = paramMap.get("message") == null ? "" : paramMap.get("message").toString();
		String msisdn = paramMap.get("msisdn") == null ? "" : paramMap.get("msisdn").toString();
		String originator = paramMap.get("originator") == null ? "" : paramMap.get("originator").toString();
		if( "".equals(messageId) || StringUtil.hasSpecialCharacter(messageId) || StringUtil.spaceCheck(messageId) || StringUtil.maxCheck(messageId, 60)
				|| "".equals(message) || StringUtil.maxCheck(message, 160)
				|| "".equals(msisdn) || StringUtil.hasSpecialCharacter(msisdn) || StringUtil.spaceCheck(msisdn) || StringUtil.maxCheck(msisdn, 12)
				|| "".equals(originator)
				
				) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_VALUE);
		}
	}
}
