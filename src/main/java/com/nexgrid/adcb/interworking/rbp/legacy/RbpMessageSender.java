package com.nexgrid.adcb.interworking.rbp.legacy;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rbp.util.RbpMessageConverter;
import com.nexgrid.adcb.interworking.util.MessageQueue;
import com.nexgrid.adcb.util.Init;


public class RbpMessageSender extends Thread{
	
	private static final Logger logger = LoggerFactory.getLogger(RbpMessageSender.class);
	
	private boolean isRun = true;
	private RbpConnector rbpConnector = null;
	private RbpMessageConverter msgConverter = null;
	private MessageQueue msgQueue = null;
	
	public RbpMessageSender(RbpConnector rbpConnector) {
		this.rbpConnector = rbpConnector;
		this.msgConverter = new RbpMessageConverter();
		this.msgQueue = new MessageQueue();
	}
	
	
	
	// queue에 요청 메시지 넣음
	public void putMessage(Map<String, String> reqMap) {
		this.msgQueue.put(reqMap);
		logger.debug("[RBP msg put] : [" + reqMap.toString() + "]" );	
	}



	@Override
	public void run() {
		Map<String, String> reqMap = null;
		while(isRun) {
			reqMap = (Map<String, String>) this.msgQueue.pop();
			
			if(reqMap != null) {
				try {
					sendMsg(reqMap);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}else {
				try {
					Thread.sleep(10);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	
	}


	
	// thread 종료
	@Override
	public void destroy() {
		isRun = false;
	}
	
	
	
	// RBP에 message 전송
	public void sendMsg(Map<String, String > reqMap) throws Exception{
		String msgGbn = reqMap.get("MESSAGE_GBN");
		String opCode = reqMap.get("OP_CODE");
		
		int seqNo = Integer.parseInt(reqMap.get("SEQUENCE_NO"));
		
		// health check 구분
		String logSeq = "";
		if(Init.readConfig.getRbp_opcode_con_qry().equals(opCode)) {
			if(Init.readConfig.getRbp_msg_gbn_invoke().equals(msgGbn)) {
				logSeq = "[Health Check (invoke)] ";
			}else {
				logSeq = "[Health Check (return)] ";
			}
			
		}else {
			logSeq = "[" + rbpConnector.getLogVO().getSeqId() + "] ";
		}
		logger.info(logSeq + "RBP Request Map: " + reqMap.toString());
		
		String invokeMsg = msgConverter.getInvokeMessage(msgGbn, opCode, reqMap);
		
		
		if(invokeMsg == null) {
			return;
		}
		
		byte[] reqByte = invokeMsg.getBytes();
		
		synchronized (rbpConnector.getSocket().getOutputStream()) {
			rbpConnector.getSocket().getOutputStream().write(reqByte);
			rbpConnector.getSocket().getOutputStream().flush();
			
			logger.info(logSeq + "RBP Request Invoke Message: " + invokeMsg);
		}
		
		
	}
	
	
	
	
	
	
	
	
	

}
