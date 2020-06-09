package com.nexgrid.adcb.interworking.rcsg.legacy;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rcsg.sync.RcsgSyncManager;
import com.nexgrid.adcb.interworking.rcsg.util.RcsgMessageConverter;
import com.nexgrid.adcb.util.Init;

public class RcsgMessageReceiver extends Thread{ // 2020.05.04_par RBP 소스로 전체 변경

	public static final Logger logger = LoggerFactory.getLogger(RcsgMessageReceiver.class);
	
	private boolean isRun = true;
	private RcsgConnector rcsgConnector = null;
	private RcsgMessageConverter msgConverter = null;
	
	public RcsgMessageReceiver(RcsgConnector rcsgConnector) {
		this.rcsgConnector = rcsgConnector;
		this.msgConverter = new RcsgMessageConverter();
	}

	public class Header {
		//헤더의 MSGTYPE 
		//bind=0  bindack=1  deliver=2  deliverack=3  report=4  reportack=5
		public String PROTOCO_TYPE= "";//3 ,3 
		
		public String VERSION= "";//3  , 6
		
		public String SYSTEMID = "";//6 ,12
		
		public String MESSAGE_TYPE = "";//1 ,13
		
		public String SErIAL_NO = "";//8	, 21
		
		public String OP_CODE = "";//3  	,24
		
		public String LENGTH = "";//3	, 27
	}
	
	final int SMSMO_HEADER_LENGTH = 28;

	public Header headerProsess(byte[] headerMessage){
		ByteBuffer buff = ByteBuffer.allocate(SMSMO_HEADER_LENGTH);
		buff.clear();
		buff.order(ByteOrder.BIG_ENDIAN);
		buff.put(headerMessage);
		buff.flip();
		
		Header message  = new Header();
		
		
		message.PROTOCO_TYPE = readString(buff, 0, 3);				
		message.VERSION = readString( buff, 3, 3);				
		message.SYSTEMID = readString( buff, 6,6);				
		message.MESSAGE_TYPE =  readString( buff, 12,1);
		
		message.SErIAL_NO =  readString( buff, 13,8);		
		message.OP_CODE =  readString( buff, 21,3);		
		message.LENGTH =  readString( buff, 24,4);
		
		
		return message;
	}
	
	

	public String readString(ByteBuffer buff, int offset, int length)
	{
		byte[] byte1 = new byte[length];
		buff.get(byte1);
		
		byte[] byte2 = new byte[length];
		
		int byteIndex = 0;
		for( int i = 0; i < length; ++i)
		{
			if(byte1[i] != 0 && byte1[i] != ' ' )
			{
				byte2[byteIndex] = byte1[i];
				++byteIndex;
			}
		}		
		
		
		for( int i =byteIndex; i < length; ++i)
		{						
			byte2[i] = ' ';					
		}
		
		String strProtocolType = new String( byte2);
		strProtocolType = strProtocolType.trim();
		return strProtocolType;
		
	}
	
	
	@Override
	public void run() {
		byte buffer[] = null;
		
		while(isRun) {
			try {
				if(rcsgConnector.getSocket().getInputStream().available() != 0)
				{
					
//					buffer = new byte[rcsgConnector.getSocket().getInputStream().available()];
//					rcsgConnector.getSocket().getInputStream().read(buffer, 0, rcsgConnector.getSocket().getInputStream().available());
					
					byte []bufferHeader = new byte[SMSMO_HEADER_LENGTH];
					int headReaded = 0;
					
					int temp = rcsgConnector.getSocket().getInputStream().read(bufferHeader, headReaded, SMSMO_HEADER_LENGTH - headReaded);				
					if( temp <= 0)
					{
						return;
					}
					headReaded += temp;
					while(headReaded < SMSMO_HEADER_LENGTH)
					{
						temp = rcsgConnector.getSocket().getInputStream().read(bufferHeader, headReaded, SMSMO_HEADER_LENGTH - headReaded);
						if( temp <= 0)
						{
							return;
						}
						headReaded += temp;
					}
					
										
					//buffer = new byte[rcsgConnector.getSocket().getInputStream().available()];
					
					int readedSize = 0;						
					Header	header = headerProsess(bufferHeader);
						//log.info(LogUtil.getFormatedLogHeader() +"Readed Header :[" + new String(headerMessage) + "]" );
					int bodyLength = Integer.parseInt(header.LENGTH);
					byte[] bufferBody = new byte[bodyLength];


					int bodyReaded = 0;						
					
					temp = rcsgConnector.getSocket().getInputStream().read(bufferBody, bodyReaded, bodyLength - bodyReaded);	
					if( temp <= 0)
					{
						return;
					}
					bodyReaded = bodyReaded + temp;
					//bodyReaded += temp;
					while(bodyReaded < bodyLength)
					{
						
						temp = rcsgConnector.getSocket().getInputStream().read(bufferBody, bodyReaded, bodyLength - bodyReaded);	
						if( temp <= 0)
						{
							return;
						}
						bodyReaded += temp;
					}
				
					receiveMsg(new String(bufferHeader)+new String(bufferBody));
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
				logger.info(logSeq + resLog + resMap);
				logger.info(logSeq + new String(new char[resLog.length()]).replace("\0", " ") + resMsg);
				logger.info(logSeq + "---------------------------- RCSG END ----------------------------");
				
			}else if(Init.readConfig.getRcsg_msg_gbn_invoke().equals(msgGbn)) { // rcsg server로부터 health check인 경우
				
				logger.info(logSeq + "---------------------------- RCSG START (Server's health check)----------------------------");
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
			logger.info(logSeq + "Error: ",e);
			logger.info(logSeq + "/****************************************************************/");
			
		}

	}
	
	
	
	
}
