package com.example.financial_engine_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CustomerServiceProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient customerServiceRestClient(
            RestClient.Builder builder,
            CustomerServiceProperties properties) {
        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }
}
