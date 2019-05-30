package com.nexgrid.adcb.api.charge.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nexgrid.adcb.api.charge.service.ChargeService;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.LogUtil;

@RestController
public class ChargeController {

	@Autowired
	private CommonService commonService;
	
	@Autowired
	private ChargeService chargeService;
	
	private Logger logger = LoggerFactory.getLogger(ChargeController.class);
	
	
	/**
	 * Charge API
	 * @param request
	 * @param response
	 * @param paramMap
	 * @return
	 */
	@RequestMapping(value="/charge", method = RequestMethod.POST)
	public Map<String ,Object> charge(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) Map<String, Object> paramMap){
		
		//For OMS, ServiceLog
		LogVO logVO = new LogVO("Charge");
		
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
			chargeService.reqBodyCheck(paramMap, logVO);
			
			// 요청 중복 확인
			boolean duplicateYn = chargeService.reqDuplicateCheck(paramMap, logVO);
			
			if(duplicateYn) { //해당 요청 아이디에 대한 응답을 줬을 경우 같은 응답을 다시 준다.
				dataMap = (Map<String, Object>)paramMap.get("duplicateRes");
				Map<String, Object> result = (Map<String, Object>)dataMap.get("result");
				logVO.setApiResultCode(result.get("reasonCode").toString());
			}else {
				// 최초 요청 데이터 저장
				chargeService.insertChargeReq(paramMap, logVO);
				
				// NCAS 연동
				commonService.getNcasGetMethod(paramMap, logVO);
				
				// NCAS 연동 값 -> 청구자격 체크
				commonService.userEligibilityCheck(paramMap, logVO);
				
				// charge
				chargeService.charge(paramMap, logVO);
				
				// 결제 성공 SMS 전송 정보 paramMap에 저장.
				chargeService.addChargeSuccessSMS(paramMap);
				
				// 예외없이 왔을 경우 성공 MSG
				paramMap.put("http_status", HttpStatus.OK.value());
				dataMap.put("result", commonService.getSuccessResult());
				logVO.setApiResultCode(EnAdcbOmsCode.SUCCESS.mappingCode());
			}
			logVO.setResultCode(EnAdcbOmsCode.SUCCESS.value());
			
			
			
		}
		catch(CommonException commonEx) {
			
			logVO.setResultCode(commonEx.getOmsErrCode());
			logVO.setApiResultCode(commonEx.getResReasonCode());
			
			dataMap.put("issuerPaymentId", logVO.getSeqId());
			dataMap.put("result", commonEx.sendException());
			
			// body값이 없는 상태로 요청이 온 경우
			if(paramMap == null) {
				paramMap = new HashMap<String, Object>();
			}
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
			
			logVO.setResultCode(EnAdcbOmsCode.INVALID_ERROR.value());
			logVO.setApiResultCode(EnAdcbOmsCode.INVALID_ERROR.mappingCode());
			
			dataMap.put("issuerPaymentId", logVO.getSeqId());
			Map<String, Object> result = CommonException.checkException(paramMap);
			dataMap.put("result", result);

			paramMap.put("http_status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			
			logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
			logger.error("[" + logVO.getSeqId() + "] Error Message : " + result.get("message"));
			logger.error("[" + logVO.getSeqId() + "]" + ex);
						
		}finally {
			
			try {
				logVO.setResTime();

				// OMS Write
				commonService.omsLogWrite(logVO);
				
				// BOKU에게 응답
				logVO.setFlow("[ADCB] --> [SVC]");
				if((paramMap != null && paramMap.containsKey("duplicateRes")) || EnAdcbOmsCode.CHARGE_DUPLICATE_REQ.value().equals(logVO.getResultCode())) { // 중복 요청일 경우
					if(paramMap.containsKey("http_status")) {
						response.setStatus( ((BigDecimal)paramMap.get("http_status")).intValue());
					}
					
					return dataMap;
					
				}else { // 중복 요청이 아닐 경우에만 응답을 준 후  SMS, EAI, SLA를 처리한다. (BOKU가 최대 응답속도를 1초로 제한을 뒀기 때문.)
					dataMap.put("issuerPaymentId", logVO.getSeqId());
					
					//ObjectMapper mapper = new ObjectMapper();
					response.setContentType("application/json");
					response.getWriter().print(new ObjectMapper().writeValueAsString(dataMap));
					response.getWriter().flush();
					response.getWriter().close();
					
					// paramMap에 BOKU에게 준 응답값 저장
					paramMap.put("bokuRes", dataMap);
					
					// 요청 전문이 정상일 경우에만.
					if(!"2".equals(logVO.getApiResultCode())) {
						
						// BOKU에게 응답준 결과 DB update
						chargeService.updateChargeInfo(paramMap, logVO);
						
						// SLA Insert
						commonService.insertSLA(paramMap, logVO);
					}
					
					
					// 청구 API가 성공일 경우에만 EAI
					if(EnAdcbOmsCode.SUCCESS.value().equals(logVO.getResultCode())) {
						chargeService.insertEAI(paramMap, logVO);
					}
					

				}
				
			}catch (Exception ex) {
				logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
				logger.error("[" + logVO.getSeqId() + "]" + ex);
			}finally {
				logger.info("[" + logVO.getSeqId() + "] Response Data : " + dataMap);
				// SMS : paramMap에 SMS 정보가 저장이 되어 있으면 전송.
				if(paramMap.containsKey("smsList")) {
					try {
						commonService.insertSmsList(paramMap, logVO);
					}catch (Exception ex) {
						logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
						logger.error("[" + logVO.getSeqId() + "]" + ex);
					}
				}
				
				LogUtil.EndServiceLog(logVO);
			}
			

			
			
			
		}
		
		
		return null;
	}
	
	
	
	@RequestMapping(value="/ASCN/{msisdn}", method = RequestMethod.DELETE)
	public Map<String ,Object> test(HttpServletRequest request, HttpServletResponse response, @PathVariable("msisdn") String msisdn){
		
		//Return Value
		Map<String, Object> dataMap = new HashMap<String, Object>();
		
		try {
			dataMap.put("result", commonService.getSuccessResult());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		return dataMap;
	
	}
	
	
	
	@RequestMapping(value="/testESB/{msisdn}", method = RequestMethod.GET)
	public Map<String ,Object> test2(HttpServletRequest request, HttpServletResponse response, @PathVariable("msisdn") String msisdn){
		
		//Parameter Value
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("msisdn", msisdn);
		LogVO logVO = new LogVO("testESB");
		
		//Return Value
		Map<String, Object> dataMap = new HashMap<String, Object>();
		
		try {
			
			// NCAS 연동
			commonService.getNcasGetMethod(paramMap, logVO);
			chargeService.doEsbMps208(paramMap, logVO);
			dataMap.put("result", commonService.getSuccessResult());
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		return dataMap;
	
	}
	
}
