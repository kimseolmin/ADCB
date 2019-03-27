package com.nexgrid.adcb.interworking.util;

import java.util.Vector;

public class MessageQueue extends Vector{
	
	private int maxSize = 1000;
	
	public MessageQueue(){
		
	}
	
	public MessageQueue(int maxSize) {
		setMaxSize(maxSize);
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
	/**
	 * queue에 메시지 담는다.
	 * @param message
	 * @return queue의 용량을 초과하여 메시지를 못 넣는다면 false.
	 */
	public synchronized boolean put(Object message) {
		if(this.size() <= maxSize) {
			return super.add(message);
		}
		
		return false;
	}
	
	
	

	/**
	 * queue에서 메시지를 꺼내온다.
	 * @return 비어있으면 null을 반환.
	 */
	public synchronized Object pop() {
		if(this.isEmpty()) {
			return null;
		}else {
			return this.remove(0);
		}
	
	}

}
