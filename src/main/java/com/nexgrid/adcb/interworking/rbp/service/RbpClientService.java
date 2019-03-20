package com.nexgrid.adcb.interworking.rbp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rbp.legacy.RbpConnector;
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
		
		//Thread[] threads = new Thread[serverIp.length];
		Thread[] threads = new Thread[1];
		
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
				}catch(Exception e) {
					logger.error("[" + logVO.getSeqId() + "] RBP Request Error : Name=" + rbpConn.getName() + ", Error_Msg" + e.getMessage(), e );
				}
			}
			
			if(resMap != null) {
				break;
			}
		}
		
		if(resMap == null) {
			logger.error("[" + logVO.getSeqId() + "] RBP Error (not connected)");
		}
		
		
		return resMap;
		
		
		
	}
	
	
	
	
	

}