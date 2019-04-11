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
	
	
	
	/**
	 * queue에 요청 메시지 넣음
	 * @param reqMap
	 */
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


	
	@Override
	public void destroy() {
		isRun = false;
	}
	
	
	
	/**
	 * RBP에 message 전송 (map으로 된 요청데이터를 String으로 변환 후 전송)
	 * @param reqMap
	 * @throws Exception
	 */
	public void sendMsg(Map<String, String > reqMap) throws Exception{
		String msgGbn = reqMap.get("MESSAGE_GBN");
		String opCode = reqMap.get("OP_CODE");
		
		int seqNo = Integer.parseInt(reqMap.get("SEQUENCE_NO"));
		
		// health check 구분
		String logSeq = "[" + rbpConnector.getLogVO().getSeqId() + "] ";
		String reqLog = "";
		if(Init.readConfig.getRbp_opcode_con_qry().equals(opCode)) {
			if(Init.readConfig.getRbp_msg_gbn_invoke().equals(msgGbn)) {
				reqLog = "RBP Health Check(invoke) Request";
			}else {
				reqLog = "RBP Health Check(return) Request";
			}
			
		}else {
			reqLog = "RBP Request";
		}
		
		
		String invokeMsg = msgConverter.getInvokeMessage(msgGbn, opCode, reqMap);
		
		
		if(invokeMsg == null) {
			return;
		}
		
		byte[] reqByte = invokeMsg.getBytes();
		
		synchronized (rbpConnector.getSocket().getOutputStream()) {
			
			if(Init.readConfig.getRbp_msg_gbn_invoke().equals(msgGbn)) {
				logger.info(logSeq + "---------------------------- RBP START ----------------------------");
				logger.info(logSeq + reqLog + " IP: " + rbpConnector.getServerIp());
				logger.info(logSeq + reqLog + " PORT: " + rbpConnector.getServerPort());
			}
			
			reqLog = reqLog + " Data: ";
			logger.info(logSeq + reqLog + reqMap);
			logger.info(logSeq + new String(new char[reqLog.length()]).replace("\0", " ") + invokeMsg);
			rbpConnector.getLogVO().setRbpReqTime();
			if(!Init.readConfig.getRbp_msg_gbn_invoke().equals(msgGbn)) {
				logger.info(logSeq + "---------------------------- RBP END (Server's health check)----------------------------");
			}
			
			rbpConnector.getSocket().getOutputStream().write(reqByte);
			rbpConnector.getSocket().getOutputStream().flush();
			
		}
		
		
	}
	
	
	
	
	
	
	
	
	

}
