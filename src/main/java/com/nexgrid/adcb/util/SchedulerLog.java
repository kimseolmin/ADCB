package com.nexgrid.adcb.util;

import java.util.Date;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;



public class SchedulerLog extends TimerTask {
	
	static private long lastTime = 0;
	
	//oms log => log4j2 
	private static org.apache.logging.log4j.core.Logger omsLog = (org.apache.logging.log4j.core.Logger)LogManager.getLogger("oms");
	private static final Logger logger = LoggerFactory.getLogger(SchedulerLog.class);
	
	//Date now;
	
	public void run() {
		
		Date date = new Date();
		long currTime = date.getTime();
		
		
		if ((currTime - lastTime) >= (60 * 1000)) {
			
			//oms가 찍히지 않더라도 빈파일을 생성해주기 위함.
			omsLog.info("");
			logger.info(">>>>>>>>>>>>>>>>>>>>>>>> Keep Alive <<<<<<<<<<<<<<<<<<<<<<<<<");
			
			lastTime = currTime;
		}
		
	}
}
