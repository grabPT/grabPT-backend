package com.grabpt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siot.IamportRestClient.IamportClient;

@Configuration
public class ImPortConfig {

	@Value("${import.api.key}")
	private String apiKey;

	@Value("${import.api.secret}")
	private String secretKey;

	@Bean
	public IamportClient iamportClient() {
		return new IamportClient(apiKey, secretKey);
	}
}
