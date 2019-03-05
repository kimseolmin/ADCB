package com.nexgrid.adcb.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;


public class StringUtil {

	/**
	 * @return String
	 * @summury 시퀀스 생성
	 */
	public static String setSeqId() {
		
		String seq = "";
		
		String dTime = StringUtil.getCurrentTimeMilli();
		Random random = new Random();
		
		
		Integer a = random.nextInt(10000);
		
		String rand01 = String.format("%04d", a);
		a = random.nextInt(10000);
		String rand02 = String.format("%04d",  a);
		
		seq = dTime + rand01 + rand02;
		return seq;
	}
	
	
	/**
	 * @return String
	 * @summury 현재 시간 반환
	 */
	public static String getCurrentTimeMilli() {
		SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMMddHHmmssSSS", Locale.KOREA );
		Date currentTime = new Date();
		String dTime = formatter.format ( currentTime );
		
		return dTime;
	}
	
	/**
	 * @return String
	 * @summury 현재 시간 반환 (초단위 절삭)
	 */
	public static String getCurrentTime()
	{	
		String dTime = getCurrentTimeMilli();
		
		return dTime.substring(0, 14);
	}
	
	/**
	 * @param str
	 * @return boolean
	 * @summary 널이나 공백이면 false반환
	 */
	public static boolean nullCheck(String str) {

        if (str == null || "".equals(str.trim())) {
            return false;
        } else {
            return true;
        }
    }
	
	/**
	 * @param str
	 * @return boolean
	 * @summary 숫자가 아닐 경우 false반환
	 */
	public static boolean checkInteger(Object str) {
		
		try {
			
			Integer.parseInt(str.toString());
			
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}

	/**
	 * @param str
	 * @return boolean
	 * @summary 숫자가 아닐 경우 false반환
	 */
	public static boolean checkFloat(Object str) {
		
		try {
			
			Float.parseFloat(str.toString());
			
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}

	/**
	 * @param str
	 * @return String
	 * @summury null이면 공백반환
	 */
	public static String nvl(String str) {
		// TODO Auto-generated method stub
		String nvl = "";
		if (str == null) {
			return nvl;
		}else {
			return str;
		}
	}
	
	public static String makeStackTrace(Exception t){
		if(t == null) return "";
		try{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(bout));
			bout.flush();
			String error = new String(bout.toByteArray());
			
			return error;
		}catch(Exception ex){
		return "";
		}
	}

	static public void myLog(Logger log, LogVO logVO, String str) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("[").append(logVO == null ? "" : logVO.getSeqId()).append("]").append(" ").append(str);
		
		log.debug(sb.toString() );
	}
	
    /**
     * @Method Name : encryptString
     * 암호화할 데이터를 입력받고 암호화한 문자열을 반환
     * @param myKey 암호키
     * @param data      암호화할 데이터
     * @return
     */
    static public String encryptString(String myKey, String data) throws CommonException{
        byte[] encrypted;
        try {
            String iv = myKey + myKey;
            iv = iv.substring(0,16);
            byte[] keyData = iv.getBytes();
            SecretKeySpec KS = new SecretKeySpec(keyData, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, KS, new IvParameterSpec(keyData));
            encrypted = cipher.doFinal(data.getBytes());
        } catch (Exception e) {
        	String flow = "[IPS]";
        	throw new CommonException("500", "500", "49999999", e.getMessage(), flow);
        }
        return bytesToHex(encrypted);
    }
    
    /**
     * @Method Name : decryptString
     * 인코딩된 문자열을 입력받고 복호화해서 원 문자열을 반환
     * @param myKey 암호키
     * @param decStr    디코딩할 데이터
     * @return
     */
    static public String decryptString(String myKey, String decStr) throws CommonException{
        byte[] decrypted = null;
        try {
            byte[] decData = hexToBytes(decStr);
            String iv = myKey + myKey;
            iv = iv.substring(0,16);
            byte[] keyData = iv.getBytes();
            SecretKeySpec KS = new SecretKeySpec(keyData, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, KS, new IvParameterSpec(keyData));
            decrypted = cipher.doFinal(decData);
        } catch (Exception e) {
        	String flow = "[SVC] --> [IPS]";
        	// ip로 복호화가 안됐기 때문에 30400001
        	throw new CommonException("401", "413", "30400001", e.getMessage(), flow);
        }
        return new String(decrypted);
    }
    
    /**
     * @Method Name : hexToBytes
     * 문자열의 각 글자들의 ascii 값으로 byte 배열로
     * @param str
     * @return
     */
    private static byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(
                        str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
 
    }

    /**
     * @Method Name : bytesToHex
     * byte 배열의 각각의 값을 해당하는 ascii 문자로 변환해서 문자열로 반환
     * @param data
     * @return
     */
    private static String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        } else {
            int len = data.length;
            String str = "";
            for (int i = 0; i < len; i++) {
                if ((data[i] & 0xFF) < 16)
                    str = str + "0"
                            + java.lang.Integer.toHexString(data[i] & 0xFF);
                else
                    str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
            }
            return str.toUpperCase();
        }
    }

//	public static String utfEncorder(String msg) throws IOException {
//		// TODO Auto-generated method stub
//		
//		String charSet = finderCharSet(msg);
//		
//		byte[] euckrStringBuffer = msg.getBytes("iso-8859-1");
//		String decodedFromUtf = new String(euckrStringBuffer, "utf-8");
//		
//		return decodedFromUtf;
//	}
	
//	public static String finderCharSet(String str) {
//		byte[] strBuffer = str.getBytes();
//		String charSet = "";
//		String res = "";
//		String token = "";
//		String[] ary = {"euc-kr","utf-8","iso-8859-1","ksc5601","x-windows-949"};
//		for( int i =0 ; i < ary.length; i++){
//			for(int j=0; j < ary.length ; j++){
//				try {
//					for (int ix=0; ix<strBuffer.length; ix++) {
//						token = Integer.toHexString(strBuffer[ix]);
//						//   CommonUtil.println("[" + ix + "] token value : " + token + " len : " + token.length());
//						if (token.length() >= 2) {
//							token = token.substring(token.length()-2);
//						} else {
//							for(int jx=0; jx<2-token.length();jx++)
//								token = "0" + token;
//						}     
//						res += " " + token;
//					}
//					
//					System.out.println( ary[i]+"=>"+ ary[j]+ " \r\n ==> " +new String(str.getBytes(ary[i]),ary[j]));
//				} catch (UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
//		}
//		
//		
//
//		return "";
//	}
    
    
    public static boolean hasSpecialCharacter(String str) {
    	
    	  if (str.isEmpty()){
    		    return false;
    	  }
    		 
		  for (int i = 0; i < str.length(); i++) {
		    if (!Character.isLetterOrDigit(str.charAt(i))) {
		      return true;
		    }
		  }
		 
		 return false;
    }
    
    
    public static boolean spaceCheck(String str) {
        for(int i = 0 ; i < str.length() ; i++)
        {
            if(str.charAt(i) == ' ')
                return true;
        }
        return false;
    }
    
    public static boolean maxCheck(String str, int maxLen) {
    	
    	if(str.isEmpty()) {
    		return false;
    	}
    	
    	if(str.length() > maxLen) {
    		return true;
    	}
    	
    	return false;
    	
    }
    
    
  //CTN을 12자리로 만들어 준다.
  	public static String getNcas444(String ctn){
  		int leng = ctn.length();
  		switch (leng) {
  			case 10:
  			    ctn = ctn.substring(0, 3)+"00"+ctn.substring(3, 10);
  				break;
  			case 11:
  				ctn = ctn.substring(0, 3)+"0"+ctn.substring(3, 11);
  				break;
  			default:
  				break;
  		}
  		return ctn;
  	}
  	
  	
  	
  	//CTN을 11자리로 만들어 준다.
  	public static String getCtn344(String ctn){
  		StringBuffer ctn344 = new StringBuffer();

  		int len = ctn.length();
  		if (len == 10) {
  			ctn344.append(ctn.substring(0, 3));
  			ctn344.append("0");
  			ctn344.append(ctn.substring(3, len));
  		} else if (len == 12) {
  			ctn344.append(ctn.substring(0, 3));
  			ctn344.append(ctn.substring(4, len));
  		} else if (len == 11) {
  			ctn344.append(ctn);
  		}

  		return ctn344.toString();
  	}
  	
  	/**
	 * 
	 * <PRE>
	 * Comment : 문자열 좌우 공백 제거. Null인 경우 "" 리턴
	 *
	 * </PRE>
	 *   @return String
	 *   @param sData
	 *   @return
	 */
	public static String checkTrim(String sData) {
		if (sData == null){
			return "";
		} 
		return sData.trim();
	}



 
	
}

