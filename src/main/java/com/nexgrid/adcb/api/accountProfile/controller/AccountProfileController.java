package com.nexgrid.adcb.api.accountProfile.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nexgrid.adcb.api.accountProfile.service.AccountProfileService;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.LogUtil;

@RestController
public class AccountProfileController {
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private AccountProfileService accountProfileService;
	
	private Logger logger = LoggerFactory.getLogger(AccountProfileController.class);

	
	/**
	 * AccountProfile API
	 * @param request
	 * @param response
	 * @param paramMap AccountProfile API 필수 파라미터
	 * @return
	 */
	@RequestMapping(value="/profile", produces = "application/json; charset=utf8",  method = RequestMethod.POST)
	public void getAccountProfile(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) Map<String, Object> paramMap){
		
		//For OMS, ServiceLog
		LogVO logVO = new LogVO("AccountProfile");
		
		//Service Start Log Print
		LogUtil.startServiceLog(logVO, request, paramMap == null ? null : paramMap.toString());
		
		//Usage Data in Source
		//Map<String, Object> paramMap = new HashMap<String, Object>();
				
		//Return Value
		Map<String, Object> dataMap = new HashMap<String, Object>();
		
		//set flow
		logVO.setFlow("[SVC] --> [ADCB]");
		
		try {
			
			// header check
			commonService.contentTypeCheck(request, logVO);
			
			// reqBody check
			commonService.reqBodyCheck(paramMap, logVO);
			
			
			// NCAS 연동 후 -> boku 결과 값
			dataMap = accountProfileService.getAccountProfile(paramMap, logVO);
			paramMap.put("http_status", HttpStatus.OK.value());
			logVO.setResultCode(EnAdcbOmsCode.SUCCESS.value());
			logVO.setApiResultCode(EnAdcbOmsCode.SUCCESS.mappingCode());
			
		}
		catch(CommonException commonEx) {
			
			logVO.setResultCode(commonEx.getOmsErrCode());
			logVO.setApiResultCode(commonEx.getResReasonCode());
			
			// body값이 없는 상태로 요청이 온 경우
			if(paramMap == null) {
				paramMap = new HashMap<String, Object>();
			}else {
				dataMap.put("msisdn", paramMap.get("msisdn"));
			}
			paramMap.put("http_status", commonEx.getStatusCode());
			
			
			dataMap.put("result", commonEx.sendException());
			
			response.setStatus(commonEx.getStatusCode());
			
			logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
			logger.error("[" + logVO.getSeqId() + "] Error Message : " + commonEx.getLogMsg());
			logger.error("[" + logVO.getSeqId() + "]", commonEx);
			
		}
		catch(Exception ex){
			
			paramMap.put("sCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
			paramMap.put("eCode", EnAdcbOmsCode.INVALID_ERROR.value());
			paramMap.put("apiResultCode", EnAdcbOmsCode.INVALID_ERROR.mappingCode());
			paramMap.put("http_status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			
			logVO.setResultCode(EnAdcbOmsCode.INVALID_ERROR.value());
			logVO.setApiResultCode(EnAdcbOmsCode.INVALID_ERROR.mappingCode());
			
			dataMap.put("msisdn", paramMap.get("msisdn"));
			
			Map<String, Object> result = CommonException.checkException(paramMap);
			dataMap.put("result", result);
			
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			
			logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
			logger.error("[" + logVO.getSeqId() + "] Error Message : " + result.get("message"));
			logger.error("[" + logVO.getSeqId() + "]" + ex);
						
		}finally {
			
			
			try {
				response.setContentType("application/json");
				response.getWriter().print(new ObjectMapper().writeValueAsString(dataMap));
				response.getWriter().flush();
				response.getWriter().close();
				
				logVO.setResTime();
				commonService.omsLogWrite(logVO);
				
				// SLA Insert
				commonService.insertSLA(paramMap, logVO);
				
				
			}catch (Exception ex) {
				logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
				logger.error("[" + logVO.getSeqId() + "]" + ex);
			}finally {
				
				LogUtil.EndServiceLog(dataMap, logVO);
			}
			
			
		}
		
	}
	
}
