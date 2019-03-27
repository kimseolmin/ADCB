package com.nexgrid.adcb.interworking.rcsg.legacy;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rcsg.sync.RcsgSyncManager;
import com.nexgrid.adcb.interworking.rcsg.util.RcsgMessageConverter;
import com.nexgrid.adcb.util.Init;

public class RcsgMessageReceiver extends Thread{
	
	public static final Logger logger = LoggerFactory.getLogger(RcsgMessageReceiver.class);
	
	private boolean isRun = true;
	private RcsgConnector rcsgConnector = null;
	private RcsgMessageConverter msgConverter = null;
	
	public RcsgMessageReceiver(RcsgConnector rcsgConnector) {
		this.rcsgConnector = rcsgConnector;
		this.msgConverter = new RcsgMessageConverter();
	}

	@Override
	public void run() {
		byte buffer[] = null;
		
		while(isRun) {
			try {
				if(rcsgConnector.getSocket().getInputStream().available() != 0) {
					buffer = new byte[rcsgConnector.getSocket().getInputStream().available()];
					rcsgConnector.getSocket().getInputStream().read(buffer, 0, rcsgConnector.getSocket().getInputStream().available());
				
					receiveMsg(new String(buffer));
				}else {
					Thread.sleep(10);
				}
				
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void destroy() {
		isRun = false;
	}
	
	
	
	/**
	 * message 수신 (RCSG에서 받은 String 형태의 message를 map형태로 변환 -> 해당 응답을 기다리는 쓰레드를 찾아 map전달 후 notify()) 
	 * @param resMsg RCSG에서 받은 String 형태의 message
	 */
	public void receiveMsg(String resMsg){
		String seqId = null;
		String seqNo = null;
		String msgGbn = null;
		String opCode = null;
		
		String logSeq = "";
		String resLog = "RCSG Response Data: ";
		
		try {
			Map<String, String> resMap = msgConverter.parseReturnMessage(resMsg);
			if(resMap != null) { // header형식에 이상이 없는 경우
				seqNo = resMap.get("SEQUENCE_NO");
				msgGbn = resMap.get("MESSAGE_GBN");
				opCode = resMap.get("OP_CODE");
			}
			
			// return일 경우
			if(Init.readConfig.getRcsg_msg_gbn_return().equals(msgGbn)) {
				resLog = "RCSG Response Data: ";
				seqId = RcsgSyncManager.getInstance().free(seqNo, resMap);
				logSeq = "[" + seqId + "] ";
				logger.info(logSeq + resLog + resMsg);
				logger.info(logSeq + new String(new char[resLog.length()]).replace("\0", " ") + resMap);
				
			}else if(Init.readConfig.getRcsg_msg_gbn_invoke().equals(msgGbn)) { // rcsg server로부터 health check인 경우
				
				resLog = "RCSG Health Check Response";
				rcsgConnector.setLogVO(new LogVO("healthCheck"));
				logSeq = "[" + rcsgConnector.getLogVO().getSeqId() + "] ";
				logger.info(logSeq + resLog + " IP: " + rcsgConnector.getServerIp());
				logger.info(logSeq + resLog + " PORT: " + rcsgConnector.getServerPort());
				resLog += " Data: ";
				logger.info(logSeq + resLog + resMsg);
				logger.info(logSeq + new String(new char[resLog.length()]).replace("\0", " ") + resMap);
				
				// 연결상태 확인일 경우에만.
				if(Init.readConfig.getRcsg_opcode_con_qry().equals(opCode)) {
					rcsgConnector.returnHealthCheck(resMap);
				}
			}
			
		}catch (Exception e) {
			logSeq = "[" + rcsgConnector.getName() + " Recevier] ";
			logger.info(logSeq + "/********************** RCSG Response Error **********************/");
			logger.info(logSeq + resLog + resMsg);
			logger.info(logSeq + "Error: " + e.getMessage());
			logger.info(logSeq + "/****************************************************************/");
			
		}
		
		
	}
	
	

}
