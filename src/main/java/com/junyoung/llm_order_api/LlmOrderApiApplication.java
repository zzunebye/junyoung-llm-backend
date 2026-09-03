package com.junyoung.llm_order_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.junyoung.llm_order_api.distance.MapsProperties;

@EnableConfigurationProperties(MapsProperties.class)
@SpringBootApplication
public class LlmOrderApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LlmOrderApiApplication.class, args);
	}

}
