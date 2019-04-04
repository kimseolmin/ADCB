package com.nexgrid.adcb.api.refund.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.StringUtil;

@Service("refundService")
public class RefundService {
	
	private static final Logger logger = LoggerFactory.getLogger(RefundService.class);
	
	/**
	 * Refund API body 필수값 체크
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void reqBodyCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		// body key 체크
		if( paramMap == null || paramMap.size() == 0 || !paramMap.containsKey("requestId")
				|| !paramMap.containsKey("clientRefundId") || !paramMap.containsKey("issuerPaymentId") 
				|| !paramMap.containsKey("refundAmount") ) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		Map<String, Object> refundAmount = (HashMap<String, Object>)paramMap.get("refundAmount");
		if( refundAmount == null || refundAmount.size() == 0 || !refundAmount.containsKey("amount")
				|| !refundAmount.containsKey("currency")) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		
		// body value 체크
		String requestId = paramMap.get("requestId") == null ? "" : paramMap.get("requestId").toString();
		String clientRefundId = paramMap.get("clientRefundId") == null ? "" : paramMap.get("clientRefundId").toString();
		String issuerPaymentId = paramMap.get("issuerPaymentId") == null ? "" : paramMap.get("issuerPaymentId").toString();
		Integer amount = refundAmount.get("amount") == null ? 0 : (Integer)refundAmount.get("amount");
		String currency = refundAmount.get("currency") == null ? "" : refundAmount.get("currency").toString().toUpperCase();
		if( "".equals(requestId) || StringUtil.hasSpecialCharacter(requestId) || StringUtil.spaceCheck(requestId) || StringUtil.maxCheck(requestId, 50)
				||  "".equals(clientRefundId) || StringUtil.hasSpecialCharacter(clientRefundId) || StringUtil.spaceCheck(clientRefundId) || StringUtil.maxCheck(clientRefundId, 50) 
				||  "".equals(issuerPaymentId) || StringUtil.hasSpecialCharacter(issuerPaymentId) || StringUtil.spaceCheck(issuerPaymentId) || StringUtil.maxCheck(issuerPaymentId, 50)
				||  amount == 0 || !"KRW".equals(currency)) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_VALUE);
		}
	}

}
