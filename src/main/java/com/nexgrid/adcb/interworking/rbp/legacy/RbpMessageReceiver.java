package com.nexgrid.adcb.interworking.rbp.legacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rbp.util.RbpMessageConverter;

public class RbpMessageReceiver extends Thread{

	public static final Logger logger = LoggerFactory.getLogger(RbpMessageReceiver.class);
	
	private boolean isRun = true;
	private RbpConnector rbpConnector = null;
	private RbpMessageConverter msgConverter = null;
	private LogVO logVO = null;
	
	public RbpMessageReceiver(RbpConnector rbpConnector, LogVO logVO) {
		this.rbpConnector = rbpConnector;
		this.msgConverter = new RbpMessageConverter();
		this.logVO = logVO;
	}

	
	
	@Override
	public void run() {
		byte buffer[] = null;
		
		while(isRun) {
			try {
				
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}



	@Override
	public void destroy() {
		isRun = false;
	}
	
	
	
	
	
	
}
