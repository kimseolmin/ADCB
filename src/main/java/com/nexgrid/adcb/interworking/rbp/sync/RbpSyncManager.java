package com.nexgrid.adcb.interworking.rbp.sync;

import java.util.Hashtable;
import java.util.Map;

public class RbpSyncManager {

	private Hashtable<String, RbpSyncObject> table = null;
	private static RbpSyncManager syncManager = null;
	
	public static synchronized RbpSyncManager getInstance() {
		if(syncManager == null) {
			syncManager = new RbpSyncManager();
		}
		
		return syncManager;
	}
	
	
	private RbpSyncManager() {
		table = new Hashtable<>();
	}
	
	
	public synchronized void put(String seqNo, RbpSyncObject syncObj) {
		table.put(seqNo, syncObj);
	}
	
	
	public synchronized RbpSyncObject get(String seqNo) {
		return table.get(seqNo);
	}
	
	
	
	// 처리완료 메시지가 오게 되면 대기중인 쓰레드를 찾아 대기를 풀어줌
	public String free(String seqNo, Map<String,String> resMap) throws Exception{
		String seqId = null;
		RbpSyncObject rObject = table.get(seqNo);
		
		if(rObject == null) {
			return null;
		}
		
		seqId = new String(rObject.getSeqId());
		
		rObject.setResponseMap(resMap);
		rObject.setNotify();
		table.remove(seqNo);
		
		return seqId;
		
		
	}
	
}
