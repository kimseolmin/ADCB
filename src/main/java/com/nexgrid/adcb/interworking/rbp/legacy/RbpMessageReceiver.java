package com.nexgrid.adcb.interworking.rbp.legacy;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rbp.sync.RbpSyncManager;
import com.nexgrid.adcb.interworking.rbp.util.RbpMessageConverter;
import com.nexgrid.adcb.util.Init;

public class RbpMessageReceiver extends Thread{

	public static final Logger logger = LoggerFactory.getLogger(RbpMessageReceiver.class);
	
	private boolean isRun = true;
	private RbpConnector rbpConnector = null;
	private RbpMessageConverter msgConverter = null;
	
	public RbpMessageReceiver(RbpConnector rbpConnector) {
		this.rbpConnector = rbpConnector;
		this.msgConverter = new RbpMessageConverter();
	}

	
	
	@Override
	public void run() {
		byte buffer[] = null;
		
		while(isRun) {
			try {
				if(rbpConnector.getSocket().getInputStream().available() != 0) {
					buffer = new byte[rbpConnector.getSocket().getInputStream().available()];
					rbpConnector.getSocket().getInputStream().read(buffer, 0, rbpConnector.getSocket().getInputStream().available());
				
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
	 * message 수신 (RBP에서 받은 String 형태의 message를 map형태로 변환 -> 해당 응답을 기다리는 쓰레드를 찾아 map전달 후 notify()) 
	 * @param resMsg RBP에서 받은 String 형태의 message
	 */
	public void receiveMsg(String resMsg){
		String seqId = null;
		String seqNo = null;
		String msgGbn = null;
		String opCode = null;
		
		String logSeq = "";
		String resLog = "RBP Response Data: ";
		
		
		try {
			Map<String, String> resMap = msgConverter.parseReturnMessage(resMsg);
			if(resMap != null) { // header형식에 이상이 없는 경우
				seqNo = resMap.get("SEQUENCE_NO");
				msgGbn = resMap.get("MESSAGE_GBN");
				opCode = resMap.get("OP_CODE");
				
			}
			
			// return일 경우
			if(Init.readConfig.getRbp_msg_gbn_return().equals(msgGbn)) {
				resLog = "RBP Response Data: ";
				seqId = RbpSyncManager.getInstance().free(seqNo, resMap);
				logSeq = "[" + seqId + "] ";
				logger.info(logSeq + resLog + resMsg);
				logger.info(logSeq + new String(new char[resLog.length()]).replace("\0", " ") + resMap);
				logger.info(logSeq + "---------------------------- RBP END ----------------------------");
				
			}else if(Init.readConfig.getRbp_msg_gbn_invoke().equals(msgGbn)) { // rbp server로부터 health check인 경우
				
				logger.info(logSeq + "---------------------------- RBP START ----------------------------");
				resLog = "RBP Health Check Response";
				rbpConnector.setLogVO(new LogVO("healthCheck"));
				logSeq = "[" + rbpConnector.getLogVO().getSeqId() + "] ";
				logger.info(logSeq + resLog + " IP: " + rbpConnector.getServerIp());
				logger.info(logSeq + resLog + " PORT: " + rbpConnector.getServerPort());
				resLog += " Data: ";
				logger.info(logSeq + resLog + resMsg);
				logger.info(logSeq + new String(new char[resLog.length()]).replace("\0", " ") + resMap);
				
				// 연결상태 확인일 경우에만.
				if(Init.readConfig.getRbp_opcode_con_qry().equals(opCode)) {
					rbpConnector.returnHealthCheck(resMap);
				}
			}
		}catch (Exception e) {
			logSeq = "[" + rbpConnector.getName() + " Recevier] ";
			logger.info(logSeq + "/********************** RBP Response Error **********************/");
			logger.info(logSeq + resLog + resMsg);
			logger.info(logSeq + "Error: " + e.getMessage());
			logger.info(logSeq + "/****************************************************************/");
			
		}

	}
	
	
	
	
}
