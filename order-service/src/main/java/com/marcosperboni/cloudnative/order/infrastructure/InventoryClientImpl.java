package com.marcosperboni.cloudnative.order.infrastructure;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.marcosperboni.cloudnative.order.domain.DownstreamServiceUnavailableException;
import com.marcosperboni.cloudnative.order.domain.InsufficientStockException;
import com.marcosperboni.cloudnative.order.domain.ResourceNotFoundException;

/**
 * Reserves stock in inventory-service. This assumes inventory-service exposes a
 * {@code POST /api/inventory/reserve-by-product} convenience endpoint keyed by product id
 * (its inventory item id is an internal detail order-service does not need to know). If that
 * endpoint doesn't exist yet on inventory-service's side, this class still compiles and its
 * unit-tested caller (OrderServiceImpl) mocks this port directly, so it does not block
 * order-service's own build or tests.
 */
@Component
public class InventoryClientImpl implements InventoryClient {

	private final RestClient restClient;

	public InventoryClientImpl(@Value("${inventory-service.base-url}") String baseUrl) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(3_000);
		requestFactory.setReadTimeout(5_000);
		this.restClient = RestClient.builder()
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.build();
	}

	@Override
	public void reserveStock(UUID productId, int quantity) {
		try {
			restClient.post()
					.uri("/api/inventory/reserve-by-product")
					.body(new ReserveStockRequest(productId, quantity))
					.retrieve()
					.toBodilessEntity();
		} catch (HttpClientErrorException.NotFound e) {
			throw new ResourceNotFoundException("No inventory found for product: " + productId);
		} catch (HttpClientErrorException.Conflict e) {
			throw new InsufficientStockException("Insufficient stock for product " + productId + " to reserve " + quantity + " unit(s)");
		} catch (RestClientException e) {
			throw new DownstreamServiceUnavailableException("inventory-service is unavailable: " + e.getMessage());
		}
	}

	private record ReserveStockRequest(UUID productId, int quantity) {
	}

}
