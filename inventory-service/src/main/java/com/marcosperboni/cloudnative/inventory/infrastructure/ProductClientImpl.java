package com.marcosperboni.cloudnative.inventory.infrastructure;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class ProductClientImpl implements ProductClient {

	private final RestClient restClient;

	public ProductClientImpl(RestClient productServiceRestClient) {
		this.restClient = productServiceRestClient;
	}

	@Override
	public boolean productExists(UUID productId) {
		try {
			restClient.get()
					.uri("/api/products/{id}", productId)
					.retrieve()
					.toBodilessEntity();
			return true;
		} catch (HttpClientErrorException.NotFound ex) {
			return false;
		} catch (RestClientException ex) {
			throw new ProductServiceUnavailableException(
					"Product service unavailable while checking product " + productId, ex);
		}
	}
}
