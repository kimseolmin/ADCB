package com.nexgrid.adcb.api.accountProfile.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nexgrid.adcb.api.accountProfile.service.AccountProfileService;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsBack;
import com.nexgrid.adcb.util.EnAdcbOmsFront;
import com.nexgrid.adcb.util.LogUtil;

@RestController
public class AccountProfileController {
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private AccountProfileService accountProfileService;
	
	private Logger logger = LoggerFactory.getLogger(AccountProfileController.class);

	
	@RequestMapping(value="/profile", produces = "application/json; charset=utf8",  method = RequestMethod.POST)
	public Map<String, Object> getAccountProfile(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) Map<String, Object> paramMap) throws Exception {
		
		//For OMS, ServiceLog
		LogVO logVO = new LogVO("AccountProfile");
		
		//Service Start Log Print
		LogUtil.startServiceLog(logVO, request, paramMap);
		
		//Usage Data in Source
		//Map<String, Object> paramMap = new HashMap<String, Object>();
				
		//Return Value
		Map<String, Object> dataMap = new HashMap<String, Object>();
		
		//set flow
		logVO.setFlow("[SVC] --> [ADCB]");
		
		try {
			
			// Content-Type = application/json
			commonService.contentTypeCheck(request, logVO);
			
			// reqBody check
			accountProfileService.reqBodyCheck(paramMap, logVO);
			
			// NCAS 연동
			commonService.getNcasGetMethod(paramMap, logVO);
			
			// NCAS 연동 값 -> boku 결과 값
			dataMap = accountProfileService.getAccountProfile(paramMap, logVO);
			logVO.setResultCode(EnAdcbOmsFront.SUCCESS.getDefaultCode() + EnAdcbOmsBack.SUCCESS.getDefaultCode());
			
		}
		catch(CommonException commonEx) {
			
			logVO.setResultCode(commonEx.getOmsErrCode());
			logVO.setApiResultCode(commonEx.getResReasonCode());
			
			dataMap.put("msisdn", paramMap.get("msisdn"));
			dataMap.put("result", commonEx.sendException());
			response.setStatus(Integer.parseInt(commonEx.getStatusCode()));
			
			logger.error("[" + logVO.getSeqId() + "] Error Flow : " + commonEx.getFlow());
			logger.error("[" + logVO.getSeqId() + "] Error Message : " + commonEx.getLogMsg());
			logger.error("[" + logVO.getSeqId() + "]", commonEx);
			
		}
		catch(Exception ex){
			
			paramMap.put("sCode", "500");
			paramMap.put("eCode", "49999999");
			paramMap.put("apiResultCode", "4");
			
			logVO.setResultCode("49999999");
			logVO.setApiResultCode("4");
			
			dataMap.put("msisdn", paramMap.get("msisdn"));
			Map<String, Object> result = CommonException.checkException(paramMap, logVO.getSeqId(), logVO.getFlow());
			dataMap.put("result", result);
			
			response.setStatus(500);
			paramMap.put("errMsg", result.get("message"));
			
			logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
			logger.error("[" + logVO.getSeqId() + "] Error Message : " + result.get("message"));
			logger.error("[" + logVO.getSeqId() + "]" + ex);
						
		}finally {
			
			logVO.setResTime();
			commonService.omsLogWrite(logVO);
			LogUtil.EndServiceLog(dataMap, logVO);
			
			
		}
		
		
		return dataMap;
	}
	
}
