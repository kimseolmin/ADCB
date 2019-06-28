package com.nexgrid.adcb.api.accountProfile.service;

import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import com.nexgrid.adcb.api.accountProfile.dao.AccountProfileDAO;
import com.nexgrid.adcb.common.dao.CommonDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.SendUtil;
import com.nexgrid.adcb.util.StringUtil;

@Service("accountProfileService")
public class AccountProfileService {
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private CommonDAO commonDAO;
	
	@Inject
	private AccountProfileDAO accountProfileDAO;

	
	
	/**
	 * NCAS 연동 결과를 AccountProfile API 응답값으로 매핑 
	 * @param paramMap
	 * @param logVO
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getAccountProfile(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		
		String ctn = StringUtil.getNcas444(paramMap.get("msisdn").toString());
		String ncasUrl = Init.readConfig.getNcas_url() + ctn;
		HttpHeaders headers = new HttpHeaders();
		int connTimeout = Integer.parseInt(Init.readConfig.getNcas_connect_time_out());
		int readTimeout = Integer.parseInt(Init.readConfig.getNcas_read_time_out());
		
		
		ResponseEntity<String> resEntity = null;
		Map<String, String> ncasRes = null;
		
		logVO.setNcasReqTime();
		logVO.setFlow("[ADCB] --> [NCAS]");
		try {
			resEntity = SendUtil.requestUrl(HttpMethod.GET, headers, null, ncasUrl, "NCAS", connTimeout, readTimeout, logVO);
			logVO.setFlow("[ADCB] <-- [NCAS]");
			logVO.setNcasResTime();	// ncas 연동 종료
			
			//NCAS연동 결과값
			ncasRes = commonService.getNcasResHeader(resEntity);
			
	    	// paramMap에 NCAS 결과값 저장
	    	paramMap.put("ncasRes", ncasRes);
			
	    	String respcode = ncasRes.get("RESPCODE"); // RESPCODE
	    	
	    	//NCAS 응답코드 저장
	    	logVO.setNcasResultCode(respcode);
		}catch(HttpClientErrorException adcbExc){
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
		
		}catch(HttpServerErrorException adcbExc) {
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
			
		}catch(UnknownHttpStatusCodeException adcbExc) {
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
			
		}catch (ResourceAccessException adcbExc) {
			
			// connect, read time out
			if(adcbExc.getMessage().indexOf("Read") > 0) {
				throw new CommonException(EnAdcbOmsCode.NCAS_READ_TIMEOUT, adcbExc.getMessage());
			}else {
				throw new CommonException(EnAdcbOmsCode.NCAS_CONNECT_TIMEOUT, adcbExc.getMessage());
			}
		
		}catch(CommonException adcbExc) {
			
			throw adcbExc;
			
		}catch (Exception adcbExc) {
			
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
		}
		
		
		String respcode = ncasRes.get("RESPCODE"); // RESPCODE
		String res_msg = ncasRes.get("RESPMSG"); // 처리 결과 내용
		
    	//고객정보가 없거나 번호 이동된  사용자 차단 - 해지된 사용자는 고객정보 없음으로 나옴
    	//70 : 고객정보 없음  
		if("70".equals(respcode) ) {
			throw new CommonException(EnAdcbOmsCode.NCAS_70, res_msg);
		}

		// 71 : SKT로 번호이동   -> boku에게 105 매핑코드밖에 줄 수가 없어서..
		if("71".equals(respcode)) {
			throw new CommonException(EnAdcbOmsCode.NCAS_70.status(), EnAdcbOmsCode.NCAS_70.mappingCode(),  EnAdcbOmsCode.NCAS_71.value(), res_msg);
		}
		
		// 76 : KTF로 번호이동 -> boku에게 105 매핑코드밖에 줄 수가 없어서..
		if("76".equals(respcode)) {
			throw new CommonException(EnAdcbOmsCode.NCAS_70.status(), EnAdcbOmsCode.NCAS_70.mappingCode(),  EnAdcbOmsCode.NCAS_76.value(), res_msg);
		}
		
		if(!"00".equals(respcode)) {
			throw new CommonException(EnAdcbOmsCode.NCAS_API.status(), EnAdcbOmsCode.NCAS_API.mappingCode(),  EnAdcbOmsCode.NCAS_API.value() + respcode, res_msg);
		}
		
		
		
		
		String msisdn = paramMap.get("msisdn").toString(); // 소비자의 전체 MSISDN
		
		String account_status = ""; // 계정 상태 표시(예: 활성, 비활성)
		if(!"N".equals(ncasRes.get("UNIT_LOSS_YN_CODE")) || !"A".equals(ncasRes.get("CTN_STUS_CODE"))) { // 분실여부 || CTN상태 체크
			account_status = "inactive"; // 비활성
		}else{
			account_status = "active"; // 활성
		}

		
		// 소비자가 통신사 청구서를 사용할 자격이 있는 경우 true, 그렇지 않은 경우 false
		boolean eligibility = false;
		String fee_type = ncasRes.get("FEE_TYPE"); //요금제 타입
		int blockctn = 0;
		int blockfeetype = 0;
		int testPhoneCnt = 0;
		try {
			//CTN 관리자 차단여부 확인
	    	if(ctn.length() == 12){
	    		blockctn = commonDAO.getBlockCTN(ctn);
	    	}else {
	    		blockctn = 1;
	    	}
	    	
	    	//요금제 관리자 차단여부 확인
	    	if(!"".equals(fee_type)){
	    		blockfeetype = commonDAO.getBlockFeeType(fee_type);
	    	}else {
	    		blockfeetype = 1;
	    	}
		}catch(DataAccessException adcbExc){
			/*SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));*/
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
			
		}catch(ConnectException adcbExc) {
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
		}catch(CommonException common) {
			throw common;
		}catch (Exception adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
		}
		
		if(blockctn == 0 && blockfeetype == 0 && commonService.userEligibilityCheck(paramMap, logVO)) {
			eligibility = true;
		}
		

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
		
		String spend_limittype = "calendar"; // 한도초기화는 매월1일이기때문에 calendar로 고정
		
		String iccid = ncasRes.getOrDefault("USIM_ICCID_NO", "");
//		String imei = ncasRes.getOrDefault("IMEI", "");
		String imsi = ncasRes.getOrDefault("IMSI", "");
		
		boolean purchase = false;
		Calendar cal = Calendar.getInstance();
		paramMap.put("current_month", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		cal.add(Calendar.MONTH, -1);
		paramMap.put("last_month", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		cal.add(Calendar.MONTH, 1);
		paramMap.put("day2", new SimpleDateFormat("yyyyMMdd").format(cal.getTime())+"235959");
		cal.add(Calendar.DATE, -30);
		paramMap.put("day1", new SimpleDateFormat("yyyyMMdd").format(cal.getTime())+"000000");
		
		try {
			// 30일 이내 구매이력 체크
			int purchaseCnt = accountProfileDAO.getPurchase(paramMap);
			if(purchaseCnt > 0) {
				purchase = true;
			}
		}catch(DataAccessException adcbExc){
			//SQLException se = (SQLException) adcbExc.getRootCause();
			//logVO.setRsCode(Integer.toString(se.getErrorCode()));
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
			
		}catch(ConnectException adcbExc) {
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
		}catch (Exception adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
		}
		
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("msisdn", msisdn);
		resMap.put("account-type", account_type);
		resMap.put("eligibility", eligibility);
		resMap.put("account-status", account_status);
		resMap.put("spend-limittype", spend_limittype);
		resMap.put("iccid", iccid);
//		resMap.put("imei", imei);
		resMap.put("imsi", imsi);
		resMap.put("purchase", purchase);
		resMap.put("result", commonService.getSuccessResult());
		
		return resMap;
	}
	
	

		
		
}
	
	
	



