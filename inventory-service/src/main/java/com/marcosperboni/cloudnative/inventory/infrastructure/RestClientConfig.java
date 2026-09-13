package com.marcosperboni.cloudnative.inventory.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient productServiceRestClient(RestClient.Builder restClientBuilder,
			@Value("${product-service.base-url}") String baseUrl) {
		ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect()
				.build(HttpClientSettings.defaults()
						.withConnectTimeout(Duration.ofSeconds(3))
						.withReadTimeout(Duration.ofSeconds(5)));
		return restClientBuilder
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.build();
	}
}
