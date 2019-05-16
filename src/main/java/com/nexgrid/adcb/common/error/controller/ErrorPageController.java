package com.nexgrid.adcb.common.error.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.LogUtil;



/**
 * HTTP Status 에러 페이지 
 * 
 * @author Daehong
 */
@Controller
public class ErrorPageController {
	
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private Logger logger = LoggerFactory.getLogger(ErrorPageController.class);
	
	@Autowired
	private CommonService commonService;
	
	

	

	/**
	 * @param url
	 * @return
	 * @throws Exception
	 * @summury 404 url 처리 에러
	 */
	@RequestMapping("/error/{url}")
	@ResponseBody
	public Map<String, Object> errorController(@PathVariable String url, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//Create a seqId and reqTime and serviceType concurrently with declaration
		LogVO logVO = new LogVO("INVAILD");
		logVO.setFlow("[SVC] --> [ADCB]");
		
		//Service Start Log Print
		LogUtil.startServiceLog(logVO, request);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("sCode", Integer.parseInt(url));
		paramMap.put("eCode", EnAdcbOmsCode.INVALID_URL_KEY.value());
		paramMap.put("apiResultCode", EnAdcbOmsCode.INVALID_URL_KEY.mappingCode());
		
		Map<String, Object> errMap = new LinkedHashMap<String, Object>();
		
		Map<String, Object> result = CommonException.checkException(paramMap);
		errMap.put("result", result);
		
		response.setStatus(Integer.parseInt(url));
		
		LogUtil.EndServiceLog(errMap, logVO);
		return errMap;
	}
	
	
	@RequestMapping("/errors/415")
	@ResponseBody
	public Map<String, Object> errorController415(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String reqUrl = request.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH).toString(); 
		
		//Create a seqId and reqTime and serviceType concurrently with declaration
		LogVO logVO = null;
		if(reqUrl.indexOf("profile") > 0) {
			logVO = new LogVO("AccountProfile");
		}else if(reqUrl.indexOf("charge") > 0) {
			logVO = new LogVO("Charge");
		}else if(reqUrl.indexOf("paymentStatus") > 0) {
			logVO = new LogVO("PaymentStatus");
		}else if(reqUrl.indexOf("refund") > 0) {
			logVO = new LogVO("Refund");
		}else if(reqUrl.indexOf("reverse") > 0) {
			logVO = new LogVO("Reverse");
		}else if(reqUrl.indexOf("submitMT") > 0) {
			logVO = new LogVO("SubmitMT");
		}else if(reqUrl.indexOf("subscriberLookup") > 0) {
			logVO = new LogVO("SubscriberLookup");
		}

		logVO.setFlow("[SVC] --> [ADCB]");
		logVO.setReqTime();
		
		//Service Start Log Print
		LogUtil.startServiceLog(logVO, request);
		
		//Return Value
		Map<String, Object> dataMap = new HashMap<String, Object>();
		
		try {
			commonService.contentTypeCheck(request, logVO);
		}catch(CommonException commonEx) {
			
			logVO.setResultCode(commonEx.getOmsErrCode());
			logVO.setApiResultCode(commonEx.getResReasonCode());

			dataMap.put("result", commonEx.sendException());
			response.setStatus(commonEx.getStatusCode());
			
			logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
			logger.error("[" + logVO.getSeqId() + "] Error Message : " + commonEx.getLogMsg());
			logger.error("[" + logVO.getSeqId() + "]", commonEx);
			
		}finally {
			LogUtil.EndServiceLog(dataMap, logVO);
			logVO.setResTime();
			commonService.omsLogWrite(logVO);
		}
		
		return dataMap;
	}
	
	
	


}

