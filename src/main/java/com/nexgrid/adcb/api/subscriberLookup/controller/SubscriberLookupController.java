package com.nexgrid.adcb.api.subscriberLookup.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nexgrid.adcb.api.subscriberLookup.service.SubscriberLookupService;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.LogUtil;

@RestController
public class SubscriberLookupController {

	@Autowired
	private CommonService commonService;
	
	@Autowired
	private SubscriberLookupService subscriberLookupService;
	
	private Logger logger = LoggerFactory.getLogger(SubscriberLookupController.class);
	
	
	/**
	 * SubscriberLookup API
	 * @param request
	 * @param response
	 * @param paramMap
	 * @return
	 */
	@RequestMapping(value="/subscriberLookup", method = RequestMethod.POST)
	public void getSubscriberLookup(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) Map<String, Object> paramMap){
		//For OMS, ServiceLog
		LogVO logVO = new LogVO("SubscriberLookup");
		
		//Service Start Log Print
		LogUtil.startServiceLog(logVO, request, paramMap == null ? null : paramMap.toString());
		
		//Return Value
		Map<String, Object> dataMap = new HashMap<String, Object>();
		
		//set flow
		logVO.setFlow("[SVC] --> [ADCB]");
		
		try {
			
			// 작업공지
			commonService.maintenanceCheck();
			
			// header check
			commonService.contentTypeCheck(request, logVO);
			
			// reqBody check
			commonService.reqBodyCheck(paramMap, logVO);
			
			// NCAS 연동
			subscriberLookupService.doSubscriberLookup(paramMap, logVO);
			
			//NCAS 연동이 예외 없이 돌아왔을 경우
			dataMap.put("result", commonService.getSuccessResult());
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
			
			dataMap.put("result", commonEx.sendException());
			paramMap.put("http_status", commonEx.getStatusCode());
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
