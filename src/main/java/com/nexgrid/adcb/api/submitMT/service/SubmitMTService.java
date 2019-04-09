package com.nexgrid.adcb.api.submitMT.service;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.common.dao.CommonDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.common.vo.SmsSendVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.StringUtil;

@Service("submitMT")
public class SubmitMTService {

	@Autowired
	CommonDAO commonDAO;
	
	/**
	 * SubmitMT API body 필수값 체크
	 * @param request
	 * @param logVO
	 * @throws CommonException
	 */
	public void reqBodyCheck(SmsSendVO smsVO, LogVO logVO) throws Exception {
		
		// body key 체크
		if( smsVO == null || smsVO.getMessageId() == null
				|| smsVO.getMessage() == null || smsVO.getMsisdn() == null 
				|| smsVO.getOriginator() == null ) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		
		// body value 체크
		String messageId = smsVO.getMessageId();
		String message = smsVO.getMessage();
		String msisdn = smsVO.getMsisdn();
		String originator = smsVO.getOriginator();
		if( "".equals(messageId) || StringUtil.hasSpecialCharacter(messageId) || StringUtil.spaceCheck(messageId) || StringUtil.maxCheck(messageId, 60)
				|| "".equals(message) || StringUtil.maxCheck(message, 160)
				|| "".equals(msisdn) || StringUtil.hasSpecialCharacter(msisdn) || StringUtil.spaceCheck(msisdn) || StringUtil.maxCheck(msisdn, 12)
				|| "".equals(originator)
				
				) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_VALUE);
		}
	}
	
	
	
	/**
	 * SMS 정보 저장
	 * @param smsVO
	 * @param logVO
	 * @throws Exception
	 */
	public void insertSmsInfo(SmsSendVO smsVO, LogVO logVO) throws Exception{
		
		try {
			
			commonDAO.insertSmsSend(smsVO);
		}
		catch(DataAccessException adcbExc){
			/*SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));*/
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
			
		}catch(ConnectException adcbExc) {
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
		}catch (Exception adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
		}
		
	}
}
