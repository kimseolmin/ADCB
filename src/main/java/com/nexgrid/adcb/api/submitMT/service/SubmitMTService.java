package com.nexgrid.adcb.api.submitMT.service;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.api.submitMT.dao.SubmitMTDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.common.vo.SmsSendVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.StringUtil;

@Service("submitMT")
public class SubmitMTService {

	@Autowired
	SubmitMTDAO submitMTDAO;
	
	
	
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
				|| "".equals(message) || StringUtil.maxCheck(message, 160) || message.indexOf("U+") < 0 
				|| "".equals(msisdn) || StringUtil.hasSpecialCharacter(msisdn) || StringUtil.spaceCheck(msisdn) || StringUtil.maxCheck(msisdn, 12)
				|| "".equals(originator)
				
				) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_VALUE);
		}
		
		logVO.setSid(msisdn);
	}
	
	
	
	/**
	 * SMS 정보 저장
	 * @param smsVO
	 * @param logVO
	 * @throws Exception
	 */
	public void insertSmsInfo(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		String cust_flag = ncasRes.get("CUST_FLAG"); //고객정보 구분값 (ex: YL00000000)
													// 1번째 byte: 결제차단여부 ('Y':결제차단->결제이용동의 필요, 'N':결제가능->결제이용동의 완료)
													// 2번째 byte: PIN번호 설정여부 ('Y':PIN번호사용, 'N':PIN번호사용안함, '0'(숫자):PIN번호미설정, 'L':5회실패로 잠금상태)
		
		// 결제이용동의 정보
		String terms_deny_yn = "";
    	if(!StringUtil.nullCheck(cust_flag)) {
    		terms_deny_yn = "Y";
    	} else {
    		terms_deny_yn = cust_flag.substring(0, 1);
    	}
		
		try {
			SmsSendVO smsVO = (SmsSendVO) paramMap.get("smsVO");
			smsVO.setGubun("02");
			
			String message = smsVO.getMessage() + " " + Init.readConfig.getSms_url();
			smsVO.setMessage(message);
			submitMTDAO.insertSmsSend(smsVO);
			
			// 결제이용동의가 필요한 경우에만 "http://www.uplus.co.kr/css/rfrm/prvs/RetrieveUbDnUseTermsPop_19.hpi?popYn=Y" 메시지를 추가해서 보낸다.
//			if("Y".equals(terms_deny_yn)) {
//				String message = smsVO.getMessage() + " " + Init.readConfig.getSms_url();
//				smsVO.setMessage(message);
//			}
			
			/*String message = smsVO.getMessage();
			smsVO.setMessage(message.substring(0, message.indexOf("http")-1));*/
			
			// 80byte가 넘어가면 잘라서 두번보낸다.
//			if(smsVO.getMessage().getBytes().length > 80) {
//				String message = smsVO.getMessage();
//				smsVO.setMessage(new String(message.getBytes(), 0, 78));
//				submitMTDAO.insertSmsSend(smsVO);
//				
//				smsVO.setMessage(new String(message.getBytes(), 78, message.getBytes().length-78));
//				submitMTDAO.insertSmsSend(smsVO);
//			}else{
//				submitMTDAO.insertSmsSend(smsVO);
//			}
			
			
			// 결제이용동의가 필요한 경우에만 "http://www.uplus.co.kr/css/rfrm/prvs/RetrieveUbDnUseTermsPop_19.hpi?popYn=Y" 메시지를 보낸다.
//			if("Y".equals(terms_deny_yn)) {
//				smsVO.setMessage(Init.readConfig.getSms_url());
//				submitMTDAO.insertSmsSend(smsVO);
//			}
			
			
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
