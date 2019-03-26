package com.nexgrid.adcb.common.error.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.LogUtil;



/**
 * HTTP Status 에러 페이지 
 * 
 * @author Daehong
 */
@Controller
public class ErrorPageController {
	
	
	private Logger log = LoggerFactory.getLogger(getClass());	
	
	@Autowired
	private CommonService commonService;
	
	//@Autowired
	//private MessageSourceAccessor messageSource;
	
//	@Autowired
//	private MappingJackson2JsonView jsonView;
	
	
	
	
	/**
	 * @param url
	 * @return mav
	 * @throws Exception
	 * @summary 에러처리 컨트롤러
	 */
	/**
	 * @param url
	 * @return
	 * @throws Exception
	 * @summury 404 url 처리 에러
	 */
	@RequestMapping("/error/{url}")
	@ResponseBody
	public Map<String, Object> errorController(@PathVariable String url, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//Create a seqId and reqTime and serviceType concurrently with declaration
		LogVO logVO = new LogVO("INVAILD");
		logVO.setFlow("[SVC] --> [ADCB]");
		
		//Service Start Log Print
		LogUtil.startServiceLog(logVO, request);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		paramMap.put("sCode", Integer.parseInt(url));
		paramMap.put("eCode", EnAdcbOmsCode.INVALID_URL_KEY.value());
		paramMap.put("apiResultCode", EnAdcbOmsCode.INVALID_URL_KEY.mappingCode());
		
		Map<String, Object> errMap = new LinkedHashMap<String, Object>();
		
		Map<String, Object> result = CommonException.checkException(paramMap, logVO.getSeqId(), logVO.getFlow());
		errMap.put("result", result);
		
		response.setStatus(Integer.parseInt(url));
		
		LogUtil.EndServiceLog(errMap, logVO);
		return errMap;
	}

}

