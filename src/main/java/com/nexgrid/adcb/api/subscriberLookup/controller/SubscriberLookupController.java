package com.nexgrid.adcb.api.subscriberLookup.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.LogUtil;

@RestController
public class SubscriberLookupController {

	@Autowired
	private CommonService commonService;
	
	private Logger logger = LoggerFactory.getLogger(SubscriberLookupController.class);
	
	
	/**
	 * SubscriberLookup API
	 * @param request
	 * @param response
	 * @param paramMap
	 * @return
	 */
	@RequestMapping(value="/subscriberLookup", method = RequestMethod.POST)
	public Map<String, Object> getSubscriberLookup(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) Map<String, Object> paramMap){
		//For OMS, ServiceLog
		LogVO logVO = new LogVO("SubscriberLookup");
		
		//Service Start Log Print
		LogUtil.startServiceLog(logVO, request, paramMap);
		
		//Return Value
		Map<String, Object> dataMap = new HashMap<String, Object>();
		
		//set flow
		logVO.setFlow("[SVC] --> [ADCB]");
		
		try {
			
			// reqBody check
			commonService.reqBodyCheck(paramMap, logVO);
			
			// NCAS 연동
			commonService.getNcasGetMethod(paramMap, logVO);
			
			//NCAS 연동이 예외 없이 돌아왔을 경우
			dataMap.put("result", commonService.getSuccessResult());
			logVO.setResultCode(EnAdcbOmsCode.SUCCESS.value());
			
		}
		catch(CommonException commonEx) {
			
			logVO.setResultCode(commonEx.getOmsErrCode());
			logVO.setApiResultCode(commonEx.getResReasonCode());
			
			dataMap.put("msisdn", paramMap.get("msisdn"));
			dataMap.put("result", commonEx.sendException());
			response.setStatus(commonEx.getStatusCode());
			
			logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
			logger.error("[" + logVO.getSeqId() + "] Error Message : " + commonEx.getLogMsg());
			logger.error("[" + logVO.getSeqId() + "]", commonEx);
			
		}
		catch(Exception ex){
			
			paramMap.put("sCode", HttpStatus.INTERNAL_SERVER_ERROR);
			paramMap.put("eCode", EnAdcbOmsCode.INVALID_ERROR.value());
			paramMap.put("apiResultCode", EnAdcbOmsCode.INVALID_ERROR.mappingCode());
			
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
			
			logVO.setResTime();
			commonService.omsLogWrite(logVO);
			LogUtil.EndServiceLog(dataMap, logVO);
			
			
		}
		
		//Test일때만
		response.setStatus(200);
		return dataMap;
	}
}
