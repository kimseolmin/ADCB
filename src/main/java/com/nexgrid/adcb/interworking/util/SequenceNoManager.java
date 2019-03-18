package com.nexgrid.adcb.interworking.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;


public class SequenceNoManager {
	
	private String				filePath	= null;
	private String				fileName	= null;
	private int					sn			= 0;
	private int					cursor		= 0;
	private int					saveFrq		= 10000;
	private int					bufSize		= 1000;
	private int					blockSize	= 512;
	
	private RandomAccessFile file;

	public int getCursor() {
		return cursor;
	}

	public int getBlockSize() {
		return blockSize;
	}
	
	public void open(String filePath, String fileName) throws Exception {
		this.filePath = filePath;
		this.fileName = fileName;
		
		byte[] snByte = new byte[4];
		
		if(!new File(filePath).isDirectory()) {
			new File(filePath).mkdirs();
		}
		
		String fullPath = filePath + "/" + fileName;
		if(!new File(fullPath).isFile()) {
			makeFile();
			ByteConverter.writeInt(snByte, 0, sn);
			file = new RandomAccessFile(fullPath, "rw");
			
		}else {
			file = new RandomAccessFile(fullPath, "rw");
			
			for(cursor = bufSize - 1; cursor >= 0; cursor--) {
				file.seek(cursor * blockSize);
				file.read(snByte);
				
				int tmpSn = ByteConverter.readInt(snByte, 0);
				if(tmpSn != 0) {
					sn = tmpSn + saveFrq - 1;
					cursor++;
					
					if(cursor >= bufSize) {
						cursor = 0;
						file.close();
						new File(fullPath).delete();
						makeFile();
						file = new RandomAccessFile(fullPath, "rw");
					}
					break;
				}
			}
			
			if(cursor < 0) {
				cursor = 0;
			}
		}
	}
	
	
	public void makeFile() {
		try {
			FileOutputStream out = new FileOutputStream(filePath + "/" + fileName);
			PrintWriter writer = new PrintWriter(out);
			
			char[] b = new char[blockSize];
			for(int i=0; i<bufSize; i++) {
				writer.print(b);
			}
			writer.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public synchronized int getNext() throws Exception {
		sn++;
		if(sn == 100000000) {
			sn = 1;
		}
		
		if(saveFrq == 1 || sn % saveFrq == 1) { // XX0000 일때마다 저장
			if(cursor >= bufSize) {
				cursor = 0;
				new File(filePath + "/" + fileName).delete();
				makeFile();
				
			}
			byte[] b = new byte[4];
			ByteConverter.writeInt(b, 0, sn);
			RandomAccessFile file = new RandomAccessFile(filePath + "/" + fileName, "rw");
			
			byte[] tmp = new byte[508];
			file.seek(cursor * blockSize);
			file.write(b);
			file.write(tmp);
			file.close();
			cursor++;
		}
		
		return sn;
	}
	
	
	
}
