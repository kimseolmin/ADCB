package com.nexgrid.adcb.interworking.rcsg.legacy;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rcsg.sync.RcsgSyncManager;
import com.nexgrid.adcb.interworking.rcsg.sync.RcsgSyncObject;
import com.nexgrid.adcb.interworking.util.SequenceNoManager;
import com.nexgrid.adcb.util.Init;

public class RcsgConnector implements Runnable{
	
	public static final Logger logger = LoggerFactory.getLogger(RcsgConnector.class);

private static SequenceNoManager seqNoManager;
	
	private String name = null;
	private String serverIp = null;
	private int serverPort = 0;
	private Socket socket = null;
	private int conId = 0;
	private boolean isConnected = false;
	private boolean isRun = true;
	private boolean test = true;
	private LogVO logVO = null;
	
	private long lastSendTime = System.currentTimeMillis();
	
	private Object sendTimelock = new Object();
	
	private RcsgMessageSender msgSender;
	private RcsgMessageReceiver msgReceiver;
	private Timer healthCheckTimer;
	
	static {
		seqNoManager = new SequenceNoManager();
		
		try {
			seqNoManager.open(Init.readConfig.getAdcb_config_path(), Init.readConfig.getRcsg_system_id() + ".ser");
		}catch(Exception e) {
			logger.error("RCSG Sequence File Open Error : Error_Msg=" + e.getMessage(), e);
		}
	}
	
	public RcsgConnector(String ip, int port) {
		this.serverIp = ip;
		this.serverPort = port;
		this.logVO = new LogVO("healthCheck");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Socket getSocket() {
		return socket;
	}

	public boolean isConnected() {
		return isConnected;
	}
	
	public void setLogVO(LogVO logVO) {
		this.logVO = logVO;
	}

	public LogVO getLogVO() {
		return logVO;
	}
	
	public boolean isRun() {
		return isRun;
	}
	
	public String getServerIp() {
		return serverIp;
	}

	public int getServerPort() {
		return serverPort;
	}
	
	
	
	/**
	 * health check timer 설정
	 */
	private void initHealthChecker() {
		healthCheckTimer = new Timer();
		// 10초 후부터 1초 간격으로 실행
		healthCheckTimer.scheduleAtFixedRate(new HealthChecker(), 10000, 1000);
	}
	
	
	
	/**
	 * health check timer 해제
	 */
	private void releaseHealthChecker() {
		if(healthCheckTimer != null) {
			healthCheckTimer.cancel();
			healthCheckTimer = null;
		}
	}
	
	
	
	/**
	 * 서버로부터 healthcheck 응답
	 * @param reqMap
	 */
	public void returnHealthCheck(Map<String, String> reqMap) {
		try {
			// 정상으로 보냄.
			reqMap.put("CON_STS", "1");
			sendMsg(Init.readConfig.getRcsg_msg_gbn_return(), Init.readConfig.getRcsg_opcode_con_qry(), reqMap);
		}catch (Exception e) {
			logger.error("[Health Check Error" + e.getMessage());
		}
	}
	
	
	
	/**
	 * health check를 위한 TimerTask 클래스
	 */
	class HealthChecker extends TimerTask{

		@Override
		public void run() {
			
//			if(test) {
//				// 정기적으로 health check
//				invokeHealthCheck();
//				test = false;
//			}
//			invokeHealthCheck();
			
		}
		
	}
	
	
	
	/**
	 * health check
	 */
	private void invokeHealthCheck() {
		try {
			// 마지막 sendTime에서 10초가 지났을 경우 
			// (health check는 10초마다 한번씩, 다른 요청이 있었을 경우 마지막 보낸 시간에서 10초)
			if(lastSendTime < System.currentTimeMillis() - 1000 * 10) {
				this.logVO = new LogVO("healthCheck");
				Map<String, String> reqMap = new HashMap<String, String>();
				reqMap.put("CON_ID", conId + "");
				Map<String, String> resMap = sendMsg(Init.readConfig.getRcsg_msg_gbn_invoke(), Init.readConfig.getRcsg_opcode_con_qry(), reqMap);
				
				if(resMap == null || !"1".equals(resMap.get("CON_STS"))) {
					isConnected = false;
				}
			}
		}catch (Exception e) {
			logger.error("[Health Check " + getName() + "] RCSG Error : " + e.getMessage(), e);
		}
		
	}
	
	
	
	/**
	 * RCSG에 메시지 전송
	 * @param msgGbn 1:invoke, 2:return
	 * @param opCode 001:연결상태확인, 111:한도조회, 114:한도즉시차감, 116:결제취소
	 * @param reqMap RCSG 요청값
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> sendMsg(String msgGbn, String opCode, Map<String, String> reqMap) throws Exception{
		updateLastSendTime();
		
		reqMap.put("OP_CODE", opCode);
		
		String sequenceNo = "";
		if(Init.readConfig.getRcsg_msg_gbn_invoke().equals(msgGbn)) {
			sequenceNo = seqNoManager.getNext() + "";
			reqMap.put("SEQUENCE_NO", sequenceNo);
			reqMap.put("MESSAGE_GBN", "1");
		}else {
			reqMap.put("MESSAGE_GBN", "2");
		}
		
		String logSeq = "[" + logVO.getSeqId() + "] ";
		logger.debug(logSeq + "RCSG Message Put at sender = " + reqMap);
		
		msgSender.putMessage(reqMap);
		
		if(Init.readConfig.getRcsg_msg_gbn_invoke().equals(msgGbn)) { // 서버의 health check에 대한 응답이 아닐 경우에만
			// 메시지를 수신받을 때까지 기다린다.
			RcsgSyncObject syncObj = new RcsgSyncObject(logVO.getSeqId());
			RcsgSyncManager.getInstance().put(sequenceNo, syncObj);
			
			syncObj.setWait(Long.parseLong(Init.readConfig.getRcsg_receive_time_out()));
			
			//timeout시간 안에 메시지 응답을 받지 못하면 null을 반환
			return syncObj.getResMap();
		}else {
			return reqMap;
		}
	}
	
	
	
	/**
	 * RCSG에 message를 보낸 시간 재설정
	 */
	private void updateLastSendTime() {
		synchronized (sendTimelock) {
			lastSendTime = System.currentTimeMillis();
		}
	}
	
	
	
	/**
	 * message sender & receiver 해제
	 */
	private void releaseStream() {
		if(msgSender != null) {
			msgSender.destroy();
			msgSender = null;
		}
		
		if(msgReceiver != null) {
			msgReceiver.destroy();
			msgReceiver = null;
		}
	}
	
	
	
	@Override
	public void run() {
		while(isRun()) {
			if(!isConnected) {
				try{
					reconnect();
					logger.info("RCSG Connected!!! [serverIp : " + serverIp + ", serverPort : " + serverPort + "]");
				}catch(Exception e) {
					logger.error("RCSG not Connected...... Sleep for " + Init.readConfig.getRcsg_reconnect_sleep_time() + " [serverIp : " + serverIp + ", serverPort : " + serverPort + "]");
					try {
						Thread.sleep(Long.parseLong(Init.readConfig.getRcsg_reconnect_sleep_time()));
					}catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
			
			try {
				Thread.sleep(100);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		try {
			disconnect();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void destory() {
		this.isRun = false;
	}
	
	
	
	/**
	 * 소켓 연결
	 * @throws IOException
	 */
	private void reconnect() throws IOException {
		disconnect();
		
		socket = new Socket();
		socket.setSoTimeout(Integer.parseInt(Init.readConfig.getRcsg_connect_time_out()));
		socket.connect(new InetSocketAddress(serverIp, serverPort));
		isConnected = true;
		
		// conId 설정
		FileInputStream fis = (FileInputStream) socket.getInputStream();
		try {
			Field fd = FileDescriptor.class.getDeclaredField("fd");
			fd.setAccessible(true);
			conId = fd.getInt(fis.getFD());
		}catch(Exception e){
			conId = 0;
		}
		
		// sender & receiver 생성
		msgSender = new RcsgMessageSender(this);
		msgReceiver = new RcsgMessageReceiver(this);
		msgSender.start();
		msgReceiver.start();
		
		// healthCheck timer 설정
		initHealthChecker();
		
		logger.info("RCSG Connected [" + getName() + "] ============================================");
		
	}
	
	
	
	/**
	 * 접속 해제
	 * @throws IOException
	 */
	private void disconnect() throws IOException{
		// healthCheck timer 해제
		releaseHealthChecker();
		
		// sender & receiver 해제
		releaseStream();
		
		if(socket != null) {
			socket.close();
			socket = null;
		}
		
		if(isConnected) {
			logger.error("RCSG Disconnected [" + getName() + "]--------------------------------------");
		}
		
		isConnected = false;
		
	}
	
	
	
	

	
}
