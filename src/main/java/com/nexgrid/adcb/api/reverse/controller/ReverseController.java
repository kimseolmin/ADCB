package com.nexgrid.adcb.api.reverse.controller;

import java.util.Date;
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

import com.nexgrid.adcb.api.reverse.service.ReverseService;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.LogUtil;

@RestController
public class ReverseController {

	private Logger logger = LoggerFactory.getLogger(ReverseController.class);
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private ReverseService reverseService;
	
	
	@RequestMapping(value="/reverse", method = RequestMethod.POST)
	public void refund(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) Map<String, Object> paramMap){
		
		//For OMS, ServiceLog
		LogVO logVO = new LogVO("Reverse");
		
		//Service Start Log Print
		LogUtil.startServiceLog(logVO, request, paramMap);
		
		//Return Value
		Map<String, Object> dataMap = new HashMap<String, Object>();
		
		//set flow
		logVO.setFlow("[SVC] --> [ADCB]");
		
		boolean chargeResponse = false;
		
		try {
			
			// reqBody check
			reverseService.reqBodyCheck(paramMap, logVO);
			
			// 유효한 취소 요청인지 체크
			chargeResponse = reverseService.reverseCheck(paramMap, logVO);
			
			// 취소 대상인 구매의 응답 정보
			dataMap.put("paymentResponse", paramMap.get("paymentResponse"));
			
			// 거래가 성공이어서 차감취소를 해야 할 경우
			if(chargeResponse) { 
				commonService.doRbpCancel(paramMap, logVO);
			}
			
			// 예외없이 왔을 경우 BOKU에게 성공 msg 전송
			paramMap.put("HTTP_STATUS", HttpStatus.OK.value());
			dataMap.put("result", commonService.getSuccessResult());
			logVO.setApiResultCode(EnAdcbOmsCode.SUCCESS.mappingCode());
			logVO.setResultCode(EnAdcbOmsCode.SUCCESS.value());

		}catch(CommonException commonEx) {
			
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
				dataMap.put("issuerReverseId", logVO.getSeqId());
				
				//Test일때만
				response.setStatus(200);
				response.setContentType("application/json");
				response.getWriter().print(new ObjectMapper().writeValueAsString(dataMap));
				response.getWriter().flush();
				response.getWriter().close();
				
				logVO.setResTime();
				commonService.omsLogWrite(logVO);
				
				// RBP연동-차감취소 성공 시 charge_info UPDATE
				if(chargeResponse && EnAdcbOmsCode.SUCCESS.value().equals(logVO.getResultCode())) {
					paramMap.put("REVERSE_DT", new Date());
					commonService.updateChargeInfo(paramMap, logVO);
				}
				
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