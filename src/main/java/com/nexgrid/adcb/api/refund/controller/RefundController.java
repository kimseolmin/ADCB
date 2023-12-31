package com.nexgrid.adcb.api.refund.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nexgrid.adcb.api.refund.service.RefundService;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.LogUtil;

@RestController
public class RefundController {
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private RefundService refundService;
	
	private Logger logger = LoggerFactory.getLogger(RefundController.class);
	
	
	@RequestMapping(value="/refund", method = RequestMethod.POST)
	public Map<String ,Object> refund(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) Map<String, Object> paramMap){
		
		//For OMS, ServiceLog
		LogVO logVO = new LogVO("Refund");
		
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
			refundService.reqBodyCheck(paramMap, logVO);
			
			// 요청 중복 확인
			boolean duplicateYn = refundService.reqDuplicateCheck(paramMap, logVO);
			if(duplicateYn) { //해당 요청 아이디에 대한 응답을 줬을 경우 같은 응답을 다시 준다.
				
				dataMap = (Map<String, Object>)paramMap.get("duplicateRes");
				Map<String, Object> result = (Map<String, Object>)dataMap.get("result");
				logVO.setApiResultCode(result.get("reasonCode").toString());
				logVO.setResultCode(EnAdcbOmsCode.DUPLICAT_RESULT.value());
			}else {
				
				// 최초 요청 데이터 저장
				refundService.insertRefundReq(paramMap, logVO);
				
				// 부분 환불처리 및 환불 유효기간 체크 & 결제 정보 저장
				refundService.partialRefundCheck(paramMap, logVO);
				
				// refund (RBP, RCSG 연동)
				refundService.refund(paramMap, logVO);
				
				// 취소 성공 시 SMS List paramMap에 저장
				commonService.addCancelSuccessSMS(paramMap);
				
				// 예외없이 왔을 경우 BOKU에게 성공 msg 전송
				paramMap.put("http_status", HttpStatus.OK.value());
				dataMap.put("result", commonService.getSuccessResult());
				logVO.setApiResultCode(EnAdcbOmsCode.SUCCESS.mappingCode());
				logVO.setResultCode(EnAdcbOmsCode.SUCCESS.value());
			}
			
			
			
		}
		catch(CommonException commonEx) {
			
			logVO.setResultCode(commonEx.getOmsErrCode());
			logVO.setApiResultCode(commonEx.getResReasonCode());
			
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
			
			Map<String, Object> result = CommonException.checkException(paramMap);
			dataMap.put("result", result);

			paramMap.put("http_status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			
			logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
			logger.error("[" + logVO.getSeqId() + "] Error Message : " + result.get("message"));
			logger.error("[" + logVO.getSeqId() + "]" + ex);
						
		}finally {
			
			try {
				// BOKU에게 응답
				logVO.setFlow("[ADCB] --> [SVC]");
				
				logVO.setResTime();

				// OMS Write
				commonService.omsLogWrite(logVO);
				
				if(paramMap.containsKey("duplicateRes") || EnAdcbOmsCode.CHARGE_DUPLICATE_REQ.value().equals(logVO.getResultCode())) { // 중복 요청일 경우
					if(paramMap.containsKey("http_status")) {
						response.setStatus( ((BigDecimal)paramMap.get("http_status")).intValue());
					}
					logger.info("[" + logVO.getSeqId() + "] Response Data : " + dataMap);
					return dataMap;
					
				}else { // 중복 요청이 아닐 경우에만 응답을 준 후  EAI, SLA, SMS를 처리한다. (BOKU가 최대 응답속도를 1초로 제한을 뒀기 때문.)
					
					// RPP, RCSG 연동 관련 에러의 경우 (2020.01.28_par_RCSG도 추가)
					if(EnAdcbOmsCode.RBP_API.value().substring(0, 2).equals(logVO.getResultCode().substring(0, 2))
							|| EnAdcbOmsCode.RCSG_API.value().substring(0, 2).equals(logVO.getResultCode().substring(0, 2))) {
	
						// RPP, RCSG 연동 관련 에러의 경우에는 BOKU에게는 성공으로 줘야 함.
						dataMap = new HashMap<>();
						dataMap.put("result", commonService.getSuccessResult());
						
						// RPP, RCSG 연동 관련 에러의 경우에는 BOKU에게는 성공으로 주기 때문에  OMS ResultCode도 성공으로 남긴다.
						logVO.setResultCode(EnAdcbOmsCode.SUCCESS.value());
						logVO.setApiResultCode(EnAdcbOmsCode.SUCCESS.mappingCode());
					}
					
					dataMap.put("issuerRefundId", logVO.getSeqId());
					logger.info("[" + logVO.getSeqId() + "] Response Data : " + dataMap);
					
					response.setContentType("application/json");
					response.getWriter().print(new ObjectMapper().writeValueAsString(dataMap));
					response.getWriter().flush();
					response.getWriter().close();
					
					// paramMap에 BOKU에게 준 응답값 저장
					paramMap.put("bokuRes", dataMap);
					
					// 요청 전문이 정상일 경우에만
					if(!"2".equals(logVO.getApiResultCode())) {
						
						// BOKU에게 응답준 결과 DB update
						refundService.updateRefundInfo(paramMap, logVO);
						
						// SLA Insert
						commonService.insertSLA(paramMap, logVO);
					}
					
					// Refund API가 성공이거나 RBP, RCSG 연동 관련 에러가 났을 경우
					if(EnAdcbOmsCode.SUCCESS.value().equals(logVO.getResultCode()) 
							|| EnAdcbOmsCode.RBP_API.value().substring(0, 2).equals(logVO.getResultCode().substring(0, 2))
							|| EnAdcbOmsCode.RCSG_API.value().substring(0, 2).equals(logVO.getResultCode().substring(0, 2))) {
						
						// 환불 처리 누적 금액 & 환불후 잔액 UPDATE
						commonService.setBalance(paramMap, logVO);
						
						// EAI
						refundService.insertEAI(paramMap, logVO);
					}
					

				}
				

				
			}catch (Exception ex) {
				logger.error("[" + logVO.getSeqId() + "] Error Flow : " + logVO.getFlow());
				logger.error("[" + logVO.getSeqId() + "]" + ex);
			}finally {
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

}
