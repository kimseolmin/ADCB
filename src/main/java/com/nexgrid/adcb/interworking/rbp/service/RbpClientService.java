package com.nexgrid.adcb.interworking.rbp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rbp.legacy.RbpConnector;
import com.nexgrid.adcb.interworking.rbp.message.EnRbpResultCode;
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
	
	
	// primary, secondary connector 생성
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
				
			}
		}
		
	}
	
	
	// rbp에 message 전송
	public Map<String, String> doRequest(LogVO logVO, String opCode, Map<String, String> reqMap) throws Exception{
		
		Map<String, String> resMap = null;
		
		for(RbpConnector rbpConn : connList) {
			if(rbpConn.isConnected()) {
				try {
					rbpConn.setLogVO(logVO);
					resMap = rbpConn.sendMsg(Init.readConfig.getRbp_msg_gbn_invoke(), opCode, reqMap);
				}catch(CommonException common) {
					throw common;
				}
				catch(Exception e) {
					logVO.setFlow("[ADCB] --> [RBP]");
					throw new CommonException(HttpStatus.INTERNAL_SERVER_ERROR.value(), EnAdcbOmsCode.RBP_INVALID_ERROR.mappingCode(), EnAdcbOmsCode.RBP_INVALID_ERROR.value(), e.getMessage(), logVO.getFlow());
				}
			}
			
			
			
			if(resMap != null) {
				logVO.setFlow("[ADCB] <-- [RBP]");
				logVO.setRbpResTime();
				
				// 응답 형식 정상여부 체크
				if(Init.readConfig.getRbp_opcode_select().equals(opCode)) {
					for(EnRbpReturnSelectLimit e: EnRbpReturnSelectLimit.values()) {
						if(!resMap.containsKey(e.toString())) {
							throw new CommonException(HttpStatus.INTERNAL_SERVER_ERROR.value(), EnAdcbOmsCode.RBP_RES_BODY_KEY.mappingCode(), EnAdcbOmsCode.RBP_RES_BODY_KEY.value(), EnAdcbOmsCode.RBP_RES_BODY_KEY.logMsg(), logVO.getFlow());
						}
					}
				}
				
				
				
				// RESULT값 정상 아닐 경우
				String result = resMap.get("RESULT");
				logVO.setRbpResultCode(result);
				if(!"0000".equals(resMap.get("RESULT"))) {
					for(EnRbpResultCode e : EnRbpResultCode.values()) {
						// 에러코드를 찾아서 매핑한다.
						if(e.getDefaultValue().equals(result)) {
							if("".equals(e.getOpCode())) { 
								throw new CommonException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMappingCode(), EnAdcbOmsCode.RBP_API.value() + e.getDefaultValue(), e.getResMsg(), logVO.getFlow());
							}else { // RBP의 결과를 boku의 Reason코드로 매핑 시 opCode에 따라 다른 reasonCode를 줘야 하는 경우
								if(e.getOpCode().equals(opCode)) {
									throw new CommonException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMappingCode(), EnAdcbOmsCode.RBP_API.value() + e.getDefaultValue(), e.getResMsg(), logVO.getFlow());
								}
							}
						}
					}
					
					// 정의되지 않은 RESULT가 왔을 경우
					throw new CommonException(HttpStatus.INTERNAL_SERVER_ERROR.value(), EnRbpResultCode.RS_INVALID.getMappingCode(), EnAdcbOmsCode.RBP_API.value() + result, EnRbpResultCode.RS_INVALID.getResMsg(), logVO.getFlow());
				}
				
				break;
			}
		}
		
		if(resMap == null) {
			logVO.setFlow("[ADCB] --> [RBP]");
			throw new CommonException(HttpStatus.INTERNAL_SERVER_ERROR.value(), EnAdcbOmsCode.RBP_RES_TIMEOUT.mappingCode(), EnAdcbOmsCode.RBP_RES_TIMEOUT.value(), EnAdcbOmsCode.RBP_RES_TIMEOUT.logMsg(), logVO.getFlow());
		}
		
		
		return resMap;
		
		
		
	}
	
	
	
	
	

}