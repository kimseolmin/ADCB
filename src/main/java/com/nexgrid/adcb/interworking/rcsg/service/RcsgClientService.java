package com.nexgrid.adcb.interworking.rcsg.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rcsg.legacy.RcsgConnector;
import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgResultCode;
import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgReturnCharge;
import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgReturnSelectLimit;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.Init;


@Component
public class RcsgClientService {
	
	private static final Logger logger= LoggerFactory.getLogger(RcsgClientService.class);

	private List<RcsgConnector> connList = null;
	
	public RcsgClientService() {
		init();
	}
	
	
	/**
	 * primary, secondary connector 생성
	 */
	public void init() {
		connList = new ArrayList<>();
		
		String[] serverIp = new String[2];
		int[] serverPort = new int[2];
		
		serverIp[0] = Init.readConfig.getRcsg_primary_ip();
		serverPort[0] = Integer.parseInt(Init.readConfig.getRcsg_primary_port());
		serverIp[1] = Init.readConfig.getRcsg_secondary_ip();
		serverPort[1] = Integer.parseInt(Init.readConfig.getRcsg_secondary_port());
		
		Thread[] threads = new Thread[serverIp.length];
		//Thread[] threads = new Thread[1];
		
		for(int i=0; i<threads.length; i++) {
			RcsgConnector rcsgConn = new RcsgConnector(serverIp[i], serverPort[i]);
			rcsgConn.setName("RCSG_CONNECTOR_" + i);
			
			connList.add(rcsgConn);
			
			threads[i] = new Thread(rcsgConn);
			threads[i].setName(rcsgConn.getName());
			threads[i].start();
			
			try {
				Thread.sleep(100);
			}catch (Exception e){
				
			}
		}
		
	}
	
	
	
	/**
	 * RCSG에 message 전송
	 * @param logVO logVO의 seqId로 요청 thread의 응답이 제대로 왔는지 확인 가능
	 * @param opCode 001:연결상태확인, 111:한도조회, 114:한도즉시차감, 116:결제취소
	 * @param reqMap RCSG 요청 데이터
	 * @return RCSG 응답 데이터
	 * @throws Exception
	 */
	public Map<String, String> doRequest(LogVO logVO, String opCode, Map<String, String> reqMap) throws Exception{
		
		Map<String, String> resMap = null;
		
		for(RcsgConnector rcsgConn : connList) {
			if(rcsgConn.isConnected()) {
				try {
					rcsgConn.setLogVO(logVO);
					resMap = rcsgConn.sendMsg(Init.readConfig.getRcsg_msg_gbn_invoke(), opCode, reqMap);
				}catch(CommonException common) {
					throw common;
				}
				catch(Exception e) {
					logVO.setFlow("[ADCB] --> [RCSG]");
					throw new CommonException(EnAdcbOmsCode.RCSG_INVALID_ERROR, e.getMessage());
				}
			}
			
			
			if(resMap != null) {
				logVO.setFlow("[ADCB] <-- [RCSG]");
				logVO.setRcsgResTime();
				// 응답 형식 정상여부 체크 (필수 응답값 없으면 exception)
				if(Init.readConfig.getRcsg_opcode_select().equals(opCode)) {
					for(EnRcsgReturnSelectLimit e: EnRcsgReturnSelectLimit.values()) {
						if(!resMap.containsKey(e.toString())) {
							throw new CommonException(EnAdcbOmsCode.RCSG_RES_BODY_KEY);
						}
					}
				}else if(Init.readConfig.getRcsg_opcode_charge().equals(opCode)) {
					for(EnRcsgReturnCharge e: EnRcsgReturnCharge.values()) {
						if(!resMap.containsKey(e.toString())) {
							throw new CommonException(EnAdcbOmsCode.RCSG_RES_BODY_KEY);
						}
					}
				}
				
				
				
				// RESULT값 정상 아닐 경우
				String result = resMap.get("RESULT");
				logVO.setRcsgResultCode(result);
				if(!"0000".equals(resMap.get("RESULT"))) {
					for(EnRcsgResultCode e : EnRcsgResultCode.values()) {
						// 에러코드를 찾아서 매핑한다.
						if(e.getDefaultValue().equals(result)) {
							if("".equals(e.getOpCode())) { 
								throw new CommonException(e.getStatus(), e.getMappingCode(), EnAdcbOmsCode.RCSG_API.value() + e.getDefaultValue(), e.getResMsg());
							}else { // RCSG의 결과를 boku의 Reason코드로 매핑 시 opCode에 따라 다른 reasonCode를 줘야 하는 경우
								if(e.getOpCode().equals(opCode)) {
									throw new CommonException(e.getStatus(), e.getMappingCode(), EnAdcbOmsCode.RCSG_API.value() + e.getDefaultValue(), e.getResMsg());
								}
							}
						}
					}
					
					// 정의되지 않은 RESULT가 왔을 경우
					throw new CommonException(EnRcsgResultCode.RS_INVALID.getStatus(), EnRcsgResultCode.RS_INVALID.getMappingCode(), EnAdcbOmsCode.RCSG_API.value() + result, EnRcsgResultCode.RS_INVALID.getResMsg());
				}
				
				break;
			}
		}
		
		if(resMap == null) {
			logVO.setFlow("[ADCB] --> [RCSG]");
			throw new CommonException(EnAdcbOmsCode.RCSG_RES_TIMEOUT);
		}
		
		return resMap;
	}
	
}
