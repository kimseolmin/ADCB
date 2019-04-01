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
		String rand02 = String.format("%04d", a);

		seq = dTime + rand01 + rand02;
		return seq;
	}

	/**
	 * @return String
	 * @summury 현재 시간 반환
	 */
	public static String getCurrentTimeMilli() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.KOREA);
		Date currentTime = new Date();
		String dTime = formatter.format(currentTime);

		return dTime;
	}

	/**
	 * @return String
	 * @summury 현재 시간 반환 (초단위 절삭)
	 */
	public static String getCurrentTime() {
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

		} catch (Exception e) {
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

		} catch (Exception e) {
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
		} else {
			return str;
		}
	}

	public static String makeStackTrace(Exception t) {
		if (t == null)
			return "";
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(bout));
			bout.flush();
			String error = new String(bout.toByteArray());

			return error;
		} catch (Exception ex) {
			return "";
		}
	}

	static public void myLog(Logger log, LogVO logVO, String str) {

		StringBuilder sb = new StringBuilder();

		sb.append("[").append(logVO == null ? "" : logVO.getSeqId()).append("]").append(" ").append(str);

		log.debug(sb.toString());
	}



	/**
	 * @Method Name : hexToBytes 문자열의 각 글자들의 ascii 값으로 byte 배열로
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
				buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
			}
			return buffer;
		}

	}

	/**
	 * @Method Name : bytesToHex byte 배열의 각각의 값을 해당하는 ascii 문자로 변환해서 문자열로 반환
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
					str = str + "0" + java.lang.Integer.toHexString(data[i] & 0xFF);
				else
					str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
			}
			return str.toUpperCase();
		}
	}

	public static boolean hasSpecialCharacter(String str) {

		if (str.isEmpty()) {
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
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ')
				return true;
		}
		return false;
	}

	public static boolean maxCheck(String str, int maxLen) {

		if (str.isEmpty()) {
			return false;
		}

		if (str.length() > maxLen) {
			return true;
		}

		return false;

	}

	// CTN을 12자리로 만들어 준다.
	public static String getNcas444(String ctn) {
		
		// boku가 국가코드를 넣어서 주는 경우
		ctn = ctn.substring(0, 2).equals("82") ? ctn.replaceFirst("82", "0") : ctn;
		int leng = ctn.length();
		switch (leng) {
		case 10:
			ctn = ctn.substring(0, 3) + "00" + ctn.substring(3, 10);
			break;
		case 11:
			ctn = ctn.substring(0, 3) + "0" + ctn.substring(3, 11);
			break;
		default:
			break;
		}
		return ctn;
	}

	// CTN을 11자리로 만들어 준다.
	public static String getCtn344(String ctn) {
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

	// 문자열 좌우 공백 제거. Null인 경우 "" 리턴
	public static String checkTrim(String sData) {
		if (sData == null) {
			return "";
		}
		return sData.trim();
	}

	// 만나이 계산
	// ssn : 주민등록번호 13자리 (예: "999999-1234567" or "9999991234567")
	public static int calculateManAge(String sub_birth_pers_id, String sub_sex_pers_id) throws Exception {

		if (sub_birth_pers_id.length() < 6 || sub_sex_pers_id.length() < 1) {
			return 0;
		}

		String today = ""; // 오늘 날짜
		int manAge = 0; // 만 나이

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

		today = formatter.format(new Date()); // 시스템 날짜를 가져와서 yyyyMMdd 형태로 변환

		// today yyyyMMdd
		int todayYear = Integer.parseInt(today.substring(0, 4));
		int todayMonth = Integer.parseInt(today.substring(4, 6));
		int todayDay = Integer.parseInt(today.substring(6, 8));

		int ssnYear = Integer.parseInt(sub_birth_pers_id.substring(0, 2));
		int ssnMonth = Integer.parseInt(sub_birth_pers_id.substring(2, 4));
		int ssnDay = Integer.parseInt(sub_birth_pers_id.substring(4, 6));

		if (sub_sex_pers_id.equals("0") || sub_sex_pers_id.equals("9")) {
			ssnYear += 1800;
		} else if (sub_sex_pers_id.equals("1") || sub_sex_pers_id.equals("2") || sub_sex_pers_id.equals("5")
				|| sub_sex_pers_id.equals("6")) {
			ssnYear += 1900;
		} else { // 3, 4, 7, 8
			ssnYear += 2000;
		}

		manAge = todayYear - ssnYear;

		if (todayMonth < ssnMonth) { // 생년월일 "월"이 지났는지 체크
			manAge--;
		} else if (todayMonth == ssnMonth) { // 생년월일 "일"이 지났는지 체크
			if (todayDay < ssnDay) {
				manAge--; // 생일 안지났으면 (만나이 - 1)
			}
		}

		return manAge;
	}

	public static String lPad(String pData, int length) {
		return lPad(pData, length, ' ');
	}

	public static String lPad(String pData, int length, char fillChar) {
		return fmt(pData, 1, length, fillChar);
	}
	
	/**
	 * String 형의 자료를 입력받아 길이만큼 문자를 채워서 리턴하는 method
	 *
	 * inputData : 원데이타 align : 0 - left, 1 - right (어느쪽에 원 데이타를 둘건지...) fillSize : 늘리고자하는 길이 fillChar : 채울 문자
	 *
	 * 예) : fmt("string", 1, 10, '0') => "0000string"
	 */
	private static String fmt(String data, int align, int fillSize, char fillChar)
	{

		if (data == null)
		{
			data = "";
		}

		byte[] bytes = data.getBytes();

		int len = bytes.length;
		// System.out.println("crc 길이2 : "+len);
		if (len < fillSize)
		{ // 모자라는 길이만큼 채울 문자열을 만든다.
			if (align == 0)
			{
				StringBuffer strbuf = new StringBuffer(data);
				for (int i = len; i < fillSize; i++)
				{
					strbuf.append(fillChar);
				}
				return strbuf.toString();
			}
			else
			{
				StringBuffer strbuf = new StringBuffer();
				for (int i = len; i < fillSize; i++)
				{
					strbuf.append(fillChar);
				}
				strbuf.append(data);
				return strbuf.toString();
			}
		}
		else
		{ // 원하는 길이보다 크면 잘라 보낸다.
			if (align == 0)
			{
				return new String(bytes, 0, fillSize);
			}
			else
			{
				return new String(bytes, len - fillSize, fillSize);
			}
		}
	}

}
