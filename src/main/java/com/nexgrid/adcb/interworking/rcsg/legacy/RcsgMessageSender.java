package com.nexgrid.adcb.interworking.rcsg.legacy;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgrid.adcb.interworking.rcsg.util.RcsgMessageConverter;
import com.nexgrid.adcb.interworking.util.MessageQueue;
import com.nexgrid.adcb.util.Init;

public class RcsgMessageSender extends Thread{
	
	private static final Logger logger = LoggerFactory.getLogger(RcsgMessageSender.class);
	
	private boolean isRun = true;
	private RcsgConnector rcsgConnector = null;
	private RcsgMessageConverter msgConverter = null;
	private MessageQueue msgQueue = null;

	public RcsgMessageSender(RcsgConnector rcsgConnector) {
		this.rcsgConnector = rcsgConnector;
		this.msgConverter = new RcsgMessageConverter();
		this.msgQueue = new MessageQueue();
	}
	
	
	
	/**
	 * queue에 요청 메시지 넣음
	 * @param reqMap
	 */
	public void putMessage(ConcurrentHashMap<String, String> reqMap) {
		this.msgQueue.put(reqMap);
		logger.debug("[RCSG msg put] : [" + reqMap.toString() + "]" );	
	}



	@Override
	public void run() {
		ConcurrentHashMap<String, String> reqMap = null;
		while(isRun) {
			reqMap = (ConcurrentHashMap<String, String>) this.msgQueue.pop();
			
			if(reqMap != null) {
				try {
					sendMsg(reqMap);
				}catch(Exception e) {
					logger.error ("Internal Etc Error RCSG msgQueue.Pop : ", e);
				}
			}else {
				try {
					Thread.sleep(10);
				}catch (Exception e) {
					logger.error ("Internal Etc Error RCSG msgQueue.Pop : ", e);
				}
			}
		}
	}
	
	
	
	@Override
	public void destroy() {
		isRun = false;
	}
	
	
	
	/**
	 * RCSG에 message 전송 (map으로 된 요청데이터를 String으로 변환 후 전송)
	 * @param reqMap
	 * @throws Exception
	 */
	public void sendMsg(ConcurrentHashMap<String, String > reqMap) throws Exception{
		String msgGbn = reqMap.get("MESSAGE_GBN");
		String opCode = reqMap.get("OP_CODE");
		
		int seqNo = Integer.parseInt(reqMap.get("SEQUENCE_NO"));
		
		// health check 구분
		String logSeq = "[" + rcsgConnector.getLogVO().getSeqId() + "] ";
		String reqLog = "";
		if(Init.readConfig.getRcsg_opcode_con_qry().equals(opCode)) {
			if(Init.readConfig.getRcsg_msg_gbn_invoke().equals(msgGbn)) {
				reqLog = "RCSG Health Check(invoke) Request";
			}else {
				reqLog = "RCSG Health Check(return) Request";
			}
			
		}else {
			reqLog = "RCSG Request";
		}
		
		String invokeMsg = msgConverter.getInvokeMessage(msgGbn, opCode, reqMap);
		
		
		if(invokeMsg == null) {
			return;
		}
		
		byte[] reqByte = invokeMsg.getBytes();
		
		synchronized (rcsgConnector.getSocket().getOutputStream()) {
			
			if(Init.readConfig.getRcsg_msg_gbn_invoke().equals(msgGbn)) {
				logger.info(logSeq + "---------------------------- RCSG START ----------------------------");
				logger.info(logSeq + reqLog + " IP: " + rcsgConnector.getServerIp());
				logger.info(logSeq + reqLog + " PORT: " + rcsgConnector.getServerPort());
			}
			
			reqLog = reqLog + " Data: ";
			logger.info(logSeq + reqLog + reqMap);
			logger.info(logSeq + new String(new char[reqLog.length()]).replace("\0", " ") + invokeMsg);
			rcsgConnector.getLogVO().setRcsgReqTime();
			if(!Init.readConfig.getRcsg_msg_gbn_invoke().equals(msgGbn)) {
				logger.info(logSeq + "---------------------------- RCSG END (Server's health check)----------------------------");
			}
			
			
			rcsgConnector.getSocket().getOutputStream().write(reqByte);
			rcsgConnector.getSocket().getOutputStream().flush();
			
		}
	}
	
	
	
	
}
