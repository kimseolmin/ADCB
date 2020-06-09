package com.nexgrid.adcb.interworking.rbp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rbp.legacy.RbpConnector;
import com.nexgrid.adcb.interworking.rbp.message.EnRbpResultCode;
import com.nexgrid.adcb.interworking.rbp.message.EnRbpReturnCanCelPart;
import com.nexgrid.adcb.interworking.rbp.message.EnRbpReturnCancel;
import com.nexgrid.adcb.interworking.rbp.message.EnRbpReturnCharge;
import com.nexgrid.adcb.interworking.rbp.message.EnRbpReturnSelectLimit;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.Init;


@Component
public class RbpClientService {

	private static final Logger logger= LoggerFactory.getLogger(RbpClientService.class);
	
	private List<RbpConnector> connList = null;
	
	public RbpClientService() {
		init();
	}
	
	
	/**
	 * primary, secondary connector 생성
	 */
	public void init() {
		connList = new ArrayList<>();
		
		String[] serverIp = new String[2];
		int[] serverPort = new int[2];
		
		serverIp[0] = Init.readConfig.getRbp_primary_ip();
		serverPort[0] = Integer.parseInt(Init.readConfig.getRbp_primary_port());
		serverIp[1] = Init.readConfig.getRbp_secondary_ip();
		serverPort[1] = Integer.parseInt(Init.readConfig.getRbp_secondary_port());
		
		Thread[] threads = new Thread[serverIp.length];
		//Thread[] threads = new Thread[1];
		
		for(int i=0; i<threads.length; i++) {
			RbpConnector rbpConn = new RbpConnector(serverIp[i], serverPort[i]);
			rbpConn.setName("RBP_CONNECTOR_" + i);
			
			connList.add(rbpConn);
			
			threads[i] = new Thread(rbpConn);
			threads[i].setName(rbpConn.getName());
			threads[i].start();
			
			try {
				Thread.sleep(100);
			}catch (Exception e){
				logger.error("RCSG init Error : ", e); // 2020.05.04_par추가
			}
		}
		
	}
	
	
	/**
	 * RBP에 message 전송
	 * @param logVO logVO의 seqId로 요청 thread의 응답이 제대로 왔는지 확인 가능
	 * @param opCode 001:연결상태확인, 111:한도조회, 114:한도즉시차감, 116:결제취소, 117:부분취소
	 * @param reqMap RBP 요청 데이터
	 * @return RBP 응답 데이터
	 * @throws Exception
	 */
	public Map<String, String> doRequest(LogVO logVO, String opCode, Map<String, Object> paramMap) throws Exception{
		ConcurrentHashMap<String, String> reqMap = new ConcurrentHashMap<String, String>(); 
		Map<String, String> resMap = null;
		
		reqMap.putAll((Map<String, String>)paramMap.get("Req_" + opCode)); // ConcurrentHashMap으로 변경(2020.05.04_par)
		
		for(RbpConnector rbpConn : connList) {
			if(rbpConn.isConnected()) {
				try {
					rbpConn.setLogVO(logVO);
					resMap = rbpConn.sendMsg(Init.readConfig.getRbp_msg_gbn_invoke(), opCode, reqMap);
				}catch(CommonException common) {
					throw common;
				}
				catch(Exception e) {
					logger.error("[" + logVO.getSeqId() + "]", e); // 2020.05.04_par추가
					logVO.setFlow("[ADCB] --> [RBP]");
					throw new CommonException(EnAdcbOmsCode.RBP_INVALID_ERROR, e.getMessage());
				}
			}
			
			
			
			if(resMap != null) {
				logVO.setFlow("[ADCB] <-- [RBP]");
				logVO.setRbpResTime();
				
				// 응답 형식 정상여부 체크 (필수 응답값 없으면 exception)
				if(Init.readConfig.getRbp_opcode_select().equals(opCode)) {	// 한도조회
					for(EnRbpReturnSelectLimit e: EnRbpReturnSelectLimit.values()) {
						if(!resMap.containsKey(e.toString())) {
							throw new CommonException(EnAdcbOmsCode.RBP_RES_BODY_KEY);
						}
					}
				}else if(Init.readConfig.getRbp_opcode_charge().equals(opCode)) { // 즉시차감
					for(EnRbpReturnCharge e: EnRbpReturnCharge.values()) {
						if(!resMap.containsKey(e.toString())) {
							throw new CommonException(EnAdcbOmsCode.RBP_RES_BODY_KEY);
						}
					}
				}else if(Init.readConfig.getRbp_opcode_cancel().equals(opCode)) { // 차감취소
					for(EnRbpReturnCancel e : EnRbpReturnCancel.values()) {
						if(!resMap.containsKey(e.toString())) {
							throw new CommonException(EnAdcbOmsCode.RBP_RES_BODY_KEY);
						}
					}
				}else if(Init.readConfig.getRbp_opcode_cancel_part().equals(opCode)) { // 부분취소
					for(EnRbpReturnCanCelPart e : EnRbpReturnCanCelPart.values()) {
						if(!resMap.containsKey(e.toString())) {
							throw new CommonException(EnAdcbOmsCode.RBP_RES_BODY_KEY);
						}
					}
				}
				
				
				
				// RESULT값 정상 아닐 경우
				String result = resMap.get("RESULT");
				logVO.setRbpResultCode(result);
				
				if(!"0000".equals(resMap.get("RESULT"))) {
					logger.info("[" + logVO.getSeqId() + "] RBP RESULT=" + result + "(" + opCode  + ")");
					
					// RBP 연동 결과 paramMap에 저장
					paramMap.put("Res_" + opCode, resMap);
					for(EnRbpResultCode e : EnRbpResultCode.values()) {
						// 에러코드를 찾아서 매핑한다.
						if(e.getDefaultValue().equals(result)) {
							if("AccountProfile".equals(logVO.getApiType()) && e.getDiffResult()){ // charge API result와 다르게 reasonCode=0 (OK), eligibility=false로 _1910_PAR 추가
								resMap.put("adcbDiffResult", "true");
								
								return resMap;
							} 
							
							if("".equals(e.getOpCode())) { 
								throw new CommonException(e.getStatus(), e.getMappingCode(), EnAdcbOmsCode.RBP_API.value() + e.getDefaultValue(), e.getResMsg());
							}else { // RBP의 결과를 boku의 Reason코드로 매핑 시 opCode에 따라 다른 reasonCode를 줘야 하는 경우
								if(e.getOpCode().equals(opCode)) {
									throw new CommonException(e.getStatus(), e.getMappingCode(), EnAdcbOmsCode.RBP_API.value() + e.getDefaultValue(), e.getResMsg());
								}
							}
						}
					}
					
					// 정의되지 않은 RESULT가 왔을 경우
					throw new CommonException(EnRbpResultCode.RS_INVALID.getStatus(), EnRbpResultCode.RS_INVALID.getMappingCode(), EnAdcbOmsCode.RBP_API.value() + result, EnRbpResultCode.RS_INVALID.getResMsg());
				}
				
				
				
				break;
			}
		}
		
		if(resMap == null) {
			logVO.setFlow("[ADCB] --> [RBP]");
			throw new CommonException(EnAdcbOmsCode.RBP_RES_TIMEOUT);
		}
		
		
		return resMap;
		
		
		
	}
	
	
	
	
	

}