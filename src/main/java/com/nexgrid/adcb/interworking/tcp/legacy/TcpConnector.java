package com.nexgrid.adcb.interworking.tcp.legacy;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgrid.adcb.interworking.tcp.util.SequenceNoManager;

public class TcpConnector implements Runnable{

	public static final Logger logger = LoggerFactory.getLogger(TcpConnector.class);
	
	private static SequenceNoManager seqNoMgr;
	
	private String name = null;
	private String serverIp = null;
	private int serverPort = 0;
	private Socket socket = null;
	private int conId = 0;
	private boolean isConnected = false;
	private boolean isRun = true;
	
	private long lastSendTime = System.currentTimeMillis();
	
	private Object sendTimelock = new Object();
	
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	
	
}
