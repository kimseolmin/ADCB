package com.nexgrid.adcb.interworking.rbp.legacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RbpMessageSender extends Thread{
	
	private static final Logger loger = LoggerFactory.getLogger(RbpMessageSender.class);
	
	private boolean isRun = true;
	private RbpConnector rbpConn = null;

}
