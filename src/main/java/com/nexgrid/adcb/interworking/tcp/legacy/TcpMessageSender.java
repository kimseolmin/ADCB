package com.nexgrid.adcb.interworking.tcp.legacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpMessageSender extends Thread{

	private static final Logger loger = LoggerFactory.getLogger(TcpMessageSender.class);
	
	private boolean isRun = true;
	private TcpConnector tcpConn = null;
	
}
