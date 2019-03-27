package com.nexgrid.adcb.util;

import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.nexgrid.adcb.common.vo.LogVO;


public class SendUtil {
	
	
	/**
	 * HTTP 연동 API 요청
	 * @param httpMethod
	 * @param headers
	 * @param reqBody
	 * @param url
	 * @param division HTTP 연동 API 구분
	 * @param connTimeout
	 * @param readTimeout
	 * @param logVO
	 * @return
	 * @throws Exception
	 */
	public static ResponseEntity<String> requestUrl(HttpMethod httpMethod, HttpHeaders headers, String reqBody, String url, String division, int connTimeout, int readTimeout, LogVO logVO)
			throws Exception{
		// TODO Auto-generated method stub
//		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
		
		ResponseEntity<String> responseEntity = null;
		String strBody = null;
		
			URI uri = URI.create(url);
			if (reqBody != null) {
				strBody = new String(reqBody.getBytes("utf-8"), "utf-8");
				headers.set("Content-Length", Integer.toString(strBody.length()));
			}
			
			LogUtil.printAPIReqData(httpMethod, headers, strBody, url, division, logVO);
			
			HttpEntity<String> entity = new HttpEntity<String>(strBody, headers);
			RestTemplate restTemplate;
			
			restTemplate = getRestTemplate(connTimeout, readTimeout);
			responseEntity = restTemplate.exchange(uri, httpMethod, entity, String.class);
			
			LogUtil.printAPIResData(logVO, division, responseEntity);
	
			
		return responseEntity;
	}

	
	
	/**
	 * 타임아웃 설정
	 * @param connTimeout
	 * @param readTimeout
	 * @return
	 */
	static public RestTemplate getRestTemplate(int connTimeout, int readTimeout) {
		
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
		SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
		rf.setConnectTimeout(connTimeout);
        rf.setReadTimeout(readTimeout);
        
        return restTemplate;
	}
	
	


}

