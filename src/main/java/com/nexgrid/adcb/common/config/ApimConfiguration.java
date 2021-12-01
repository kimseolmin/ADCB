package com.nexgrid.adcb.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nexgrid.apim.module.ApimContext;

@Configuration
public class ApimConfiguration {

	@Bean
	public ApimContext apimContext(){
		return ApimContext.getInstance();
	}

}
