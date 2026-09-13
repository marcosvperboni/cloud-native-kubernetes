package com.marcosperboni.cloudnative.order.infrastructure;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.marcosperboni.cloudnative.order.domain.DownstreamServiceUnavailableException;
import com.marcosperboni.cloudnative.order.domain.ResourceNotFoundException;

@Component
public class ProductClientImpl implements ProductClient {

	private final RestClient restClient;

	public ProductClientImpl(@Value("${product-service.base-url}") String baseUrl) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(3_000);
		requestFactory.setReadTimeout(5_000);
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.build();
	}

	@Override
	public ProductInfo getProduct(UUID productId) {
		try {
			return restClient.get()
					.uri("/api/products/{id}", productId)
					.retrieve()
					.body(ProductInfo.class);
		} catch (HttpClientErrorException.NotFound e) {
			throw new ResourceNotFoundException("Product not found with id: " + productId);
		} catch (RestClientException e) {
			throw new DownstreamServiceUnavailableException("product-service is unavailable: " + e.getMessage());
		}
	}

}
