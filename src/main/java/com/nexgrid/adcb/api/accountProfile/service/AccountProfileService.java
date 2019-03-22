package com.nexgrid.adcb.api.accountProfile.service;

import java.net.ConnectException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.api.accountProfile.dao.AccountProfileDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.StringUtil;

@Service("accountProfileService")
public class AccountProfileService {
	
	@Autowired
	private CommonService commonService;
	
	@Inject
	private AccountProfileDAO accountProfileDAO;

	
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
	public Map<String, Object> getAccountProfile(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
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
		
		// 소비자가 통신사 청구서를 사용할 자격이 있는 경우 true, 그렇지 않은 경우 false
		boolean eligibility = false;
		if(commonService.userEligibilityCheck(paramMap, logVO)) {
			eligibility = true;
		}
		
		String account_status = ""; // 계정 상태 표시(예: 활성, 비활성)
		if(!"N".equals(ncasRes.get("UNIT_LOSS_YN_CODE")) || !"A".equals(ncasRes.get("CTN_STUS_CODE"))) { // 분실여부 || CTN상태 체크
			account_status = "inactive"; // 비활성
		}else{
			account_status = "active"; // 활성
		}
		
		String spend_limittype = "calendar"; // 한도초기화는 매월1일이기때문에 calendar로 고정
		
		String iccid = ncasRes.getOrDefault("USIM_ICCID_NO", "");
		String imei = ncasRes.getOrDefault("IMEI", "");
		String imsi = ncasRes.getOrDefault("IMSI", "");
		
		boolean purchase = false;
		Calendar cal = Calendar.getInstance();
		paramMap.put("current_month", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		cal.add(Calendar.MONTH, -1);
		paramMap.put("last_month", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DATE, -30);
		paramMap.put("before_30", new SimpleDateFormat("yyyyMMdd").format(cal.getTime()));
		
		try {
			// 30일 이내 구매이력 체크
			int purchaseCnt = accountProfileDAO.getPurchase(paramMap);
			if(purchaseCnt > 0) {
				purchase = true;
			}
		}catch(DataAccessException adcbExc){
			SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException("500", "4", "48000000", se.getMessage(), logVO.getFlow());
			
		}catch(ConnectException adcbExc) {
			logVO.setFlow("[ADCB] <-- [DB]");
			throw new CommonException("500", "4", "48000000", adcbExc.getMessage(), logVO.getFlow());
			
		}catch (Exception adcbExc) {
			throw new CommonException("500", "4", "59999999", adcbExc.getMessage(), logVO.getFlow());
		}
		
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("msisdn", msisdn);
		resMap.put("account-type", account_type);
		resMap.put("eligibility", eligibility);
		resMap.put("account-status", account_status);
		resMap.put("spend-limittype", spend_limittype);
		resMap.put("iccid", iccid);
		resMap.put("imei", imei);
		resMap.put("imsi", imsi);
		resMap.put("purchase", purchase);
		resMap.put("result", commonService.getSuccessResult());
		
		return resMap;
	}
	
	

		
		
}
	
	
	



