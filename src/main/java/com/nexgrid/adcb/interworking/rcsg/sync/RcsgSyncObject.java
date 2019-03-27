package com.nexgrid.adcb.interworking.rcsg.sync;

import java.util.Map;

public class RcsgSyncObject {
	
	private Map<String, String> resMap = null;
	private String seqId = null;
	private long createTime = 0L;
	
	public RcsgSyncObject(String seqId) {
		this.seqId = seqId;
		this.createTime = System.currentTimeMillis();
	}

	public String getSeqId() {
		return seqId;
	}
	
	
	/**
	 * waitTime 밀리세컨드만큼 waiting한 후 resume
	 * @param waitTime
	 * @throws InterruptedException
	 */
	public void setWait(long waitTime) throws InterruptedException{
		synchronized (this) {
			if(resMap == null) {
				this.wait(waitTime);
			}
		}
	}
	
	
	
	/**
	 * waiting하고 있는 쓰레드를 resume 한다.
	 * @throws InterruptedException
	 */
	public void setNotify() throws InterruptedException{
		synchronized (this) {
			this.notify();
		}
	}
	

	
	/**
	 * RCSG 응답 데이터 할당
	 * @param resMap
	 */
	public void setResponseMap(Map<String, String> resMap) {
		synchronized (this) {
			this.resMap = resMap;
		}
	}

	
	public Map<String, String> getResMap() {
		return resMap;
	}

	public long getCreateTime() {
		return createTime;
	}

	
	
	/**
	 * waiting 되어진 시간을 할당한다.
	 * @param createTime
	 */
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
}
