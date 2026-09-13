package com.marcosperboni.cloudnative.inventory.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ProductClientImplTest {

	private final RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8081");
	private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
	private final ProductClientImpl client = new ProductClientImpl(builder.build());

	@Test
	void returnsTrueWhenProductExists() {
		UUID productId = UUID.randomUUID();
		server.expect(requestTo("http://localhost:8081/api/products/" + productId))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

		assertThat(client.productExists(productId)).isTrue();
		server.verify();
	}

	@Test
	void returnsFalseWhenProductNotFound() {
		UUID productId = UUID.randomUUID();
		server.expect(requestTo("http://localhost:8081/api/products/" + productId))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));

		assertThat(client.productExists(productId)).isFalse();
		server.verify();
	}

	@Test
	void throwsProductServiceUnavailableOnServerError() {
		UUID productId = UUID.randomUUID();
		server.expect(requestTo("http://localhost:8081/api/products/" + productId))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

		assertThatThrownBy(() -> client.productExists(productId))
				.isInstanceOf(ProductServiceUnavailableException.class);
		server.verify();
	}
}
