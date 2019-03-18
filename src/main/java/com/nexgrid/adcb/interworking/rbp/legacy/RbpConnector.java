package com.nexgrid.adcb.interworking.rbp.legacy;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgrid.adcb.interworking.util.SequenceNoManager;

public class RbpConnector implements Runnable{
	
	public static final Logger logger = LoggerFactory.getLogger(RbpConnector.class);
	
	private static SequenceNoManager seqNoManager;
	
	private String name = null;
	private String serverIp = null;
	private int serverPort = 0;
	private Socket socket = null;
	private int conId = 0;
	private boolean isConnected = false;
	private boolean isRun = true;
	
	private long lastSendTime = System.currentTimeMillis();
	
	private Object sendTimelock = new Object();
	
	private RbpMessageSender msgSender;
	private RbpMessageReceiver msgReceiver;
	private Timer healthCheckTimer;
	
	static {
		seqNoManager = new SequenceNoManager();
		
		try {
			
		}catch(Exception e) {
			logger.error("RBP Sequence File Open Error : Error_Msg=" + e.getMessage(), e);
		}
	}
	
	public RbpConnector(String ip, int port) {
		this.serverIp = ip;
		this.serverPort = port;
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

	
	
	
	// health check timer 설정
	private void initHealthChecker() {
		healthCheckTimer = new Timer();
		//healthCheckTimer.scheduleAtFixedRate(, delay, period);
	}
	
	
	
	// health check를 위한 TimerTask 클래스
	class HealthChecker extends TimerTask{

		@Override
		public void run() {
			// 정기적으로 health check
			
		}
		
	}
	
	
	
	// health check
	private void invokeHealthCheck() {
		try {
			// 마지막 sendTime에서 10초가 지났을 경우 
			// (health check는 10초마다 한번씩, 다른 요청이 있었을 경우 마지막 보낸 시간에서 10초)
			if(lastSendTime < System.currentTimeMillis() - 1000 * 10) {
				Map<String, String> reqMap = new HashMap<String, String>();
				reqMap.put("CON_ID", conId + "");
				//Map<String, String> req
			}
		}catch (Exception e) {
			logger.error("[Health Check " + getName() + "] RCSG Error : " + e.getMessage(), e);
		}
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	
	
}
