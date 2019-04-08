package com.nexgrid.adcb.api.submitMT.controller;

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

import com.nexgrid.adcb.api.submitMT.service.SubmitMTService;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.LogUtil;

@RestController
public class SubmitMTController {

	@Autowired
	SubmitMTService submitMTService;
	
	@Autowired
	private CommonService commonService;
	
	private Logger logger = LoggerFactory.getLogger(SubmitMTController.class);
	
	/**
	 * SubmitMT API
	 * @param request
	 * @param response
	 * @param paramMap
	 */
	@RequestMapping(value="/submitMT", produces = "application/json; charset=utf8",  method = RequestMethod.POST)
	public void submitMT(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) Map<String, Object> paramMap){
		
		//For OMS, ServiceLog
		LogVO logVO = new LogVO("SubmitMT");
		
		//Service Start Log Print
		LogUtil.startServiceLog(logVO, request, paramMap);
		
		//Usage Data in Source
		//Map<String, Object> paramMap = new HashMap<String, Object>();
				
		//Return Value
		Map<String, Object> dataMap = new HashMap<String, Object>();
		
		//set flow
		logVO.setFlow("[SVC] --> [ADCB]");
		
		try {
			
			// reqBody check
			submitMTService.reqBodyCheck(paramMap, logVO);
			
		}
		catch(CommonException commonEx) {
			
			logVO.setResultCode(commonEx.getOmsErrCode());
			logVO.setApiResultCode(commonEx.getResReasonCode());
			
			dataMap.put("result", commonEx.sendException());
			paramMap.put("HTTP_STATUS", commonEx.getStatusCode());
			response.setStatus(commonEx.getStatusCode());
			
			logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
			logger.error("[" + logVO.getSeqId() + "] Error Message : " + commonEx.getLogMsg());
			logger.error("[" + logVO.getSeqId() + "]", commonEx);
			
		}
		catch(Exception ex){
			
			paramMap.put("sCode", HttpStatus.INTERNAL_SERVER_ERROR);
			paramMap.put("eCode", EnAdcbOmsCode.INVALID_ERROR.value());
			paramMap.put("apiResultCode", EnAdcbOmsCode.INVALID_ERROR.mappingCode());
			paramMap.put("HTTP_STATUS", HttpStatus.INTERNAL_SERVER_ERROR.value());
			
			logVO.setResultCode(EnAdcbOmsCode.INVALID_ERROR.value());
			logVO.setApiResultCode(EnAdcbOmsCode.INVALID_ERROR.mappingCode());
			
			Map<String, Object> result = CommonException.checkException(paramMap);
			dataMap.put("result", result);
			
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			
			logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
			logger.error("[" + logVO.getSeqId() + "] Error Message : " + result.get("message"));
			logger.error("[" + logVO.getSeqId() + "]" + ex);
						
		}finally {
			
			
			try {
				//Test일때만
				response.setStatus(200);
				response.setContentType("application/json");
				response.getWriter().print(new ObjectMapper().writeValueAsString(dataMap));
				response.getWriter().flush();
				response.getWriter().close();
				
				logVO.setResTime();
				commonService.omsLogWrite(logVO);
				
				// SLA Insert
				commonService.slaInsert(paramMap, logVO);
				
				
			}catch (Exception ex) {
				logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
				logger.error("[" + logVO.getSeqId() + "]" + ex);
			}finally {
				
				LogUtil.EndServiceLog(dataMap, logVO);
			}
			
			
		}

	}
}
