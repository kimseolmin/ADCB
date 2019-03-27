package com.nexgrid.adcb.interworking.rcsg.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;



public class RcsgKeyGenerator {
	
	private static RcsgKeyGenerator generator;
	private DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private long currTimeMillis;
	private long currSeq;
	private Random randomSeq;
	private String systemId;
	private String SystemSeq;
	
	
	private RcsgKeyGenerator(String systemId) {
		
		this.systemId = systemId;
		this.randomSeq = new Random();
		
		String systemAlias;
		try {
			systemAlias = InetAddress.getLocalHost().getHostName();
		}catch (UnknownHostException e) {
			systemAlias = "INTERFACE" + String.format("%02d", this.randomSeq.nextInt(100));
					
		}
	}
	
	
	
	/**
	 * multi-thread로 동시접근되는 것을 막고
	 * @param systemId
	 * @return
	 */
	public static synchronized RcsgKeyGenerator getInstance(String systemId) {
		if(generator == null) {
			generator = new RcsgKeyGenerator(systemId);
		}
		return generator;
	}
	
	
	
	/**
	 * multi-thread로 동시접근되는 것을 막고 RCSG의 BR_ID 생성
	 * @return
	 */
	public String generateKey() {
		// Business Request ID 로서 YYYYMMDDHHmmssSSS + 시스템코드 6자리 + 일련번호
		// YYYYMMDDHHmmssSSS: 1/1000 초단위 값을 기록한다.
		// 시스템코드 6자리: 연동 헤더정보에 SRC SYSTEMID를 기록한다.
		// 일련번호: 00~99 까지의 순차적번호를 사용한다.(즉 1/1000초 에 전송할 수 있는 최대 개수는 100개로 제한된다.)
		
		String str = "";
		
		synchronized (generator) {
			long tempTimeMillis = System.currentTimeMillis();
			if (this.currTimeMillis == tempTimeMillis)
			{
				this.currSeq++;
			}
			else
			{
				this.currTimeMillis = tempTimeMillis;
				this.currSeq = 1;
			}
			String currTimeMillisStr = df.format(new Date(currTimeMillis)); 
			str = currTimeMillisStr + this.systemId + String.format("%02d", currSeq);
		}
		
		return str;
	}

}
