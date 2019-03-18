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
	private LogVO logVO = null;
	
	public RbpMessageSender(RbpConnector rbpConnector, LogVO logVO) {
		this.rbpConnector = rbpConnector;
		this.msgConverter = new RbpMessageConverter();
		this.msgQueue = new MessageQueue();
		this.logVO = logVO;
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
					
				}catch(Exception e) {
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
		
		String logSeq = "[" + logVO.getSeqId() + "] ";
		logger.info(logSeq + "RBP Request(Map): " + reqMap.toString());
		
		String invokeMsg = msgConverter.getInvokeMessage(msgGbn, opCode, reqMap);
		logger.info(logSeq + "RBP Request(Invoke Message): " + invokeMsg);
		
		if(invokeMsg == null) {
			return;
		}
		
		byte[] reqByte = invokeMsg.getBytes();
		
		
	}
	
	
	
	
	
	
	
	
	

}
