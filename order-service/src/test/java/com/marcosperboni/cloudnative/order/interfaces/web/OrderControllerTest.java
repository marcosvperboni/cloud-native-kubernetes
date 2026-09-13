package com.marcosperboni.cloudnative.order.interfaces.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marcosperboni.cloudnative.order.application.OrderService;
import com.marcosperboni.cloudnative.order.application.dto.OrderItemRequest;
import com.marcosperboni.cloudnative.order.application.dto.OrderItemResponse;
import com.marcosperboni.cloudnative.order.application.dto.OrderRequest;
import com.marcosperboni.cloudnative.order.application.dto.OrderResponse;
import com.marcosperboni.cloudnative.order.application.dto.UpdateStatusRequest;
import com.marcosperboni.cloudnative.order.domain.DownstreamServiceUnavailableException;
import com.marcosperboni.cloudnative.order.domain.InvalidOrderException;
import com.marcosperboni.cloudnative.order.domain.OrderStatus;
import com.marcosperboni.cloudnative.order.domain.ResourceNotFoundException;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	// Jackson's test-slice auto-configuration is split into a separate starter in Boot 4;
	// building the mapper directly keeps this test independent of that wiring.
	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@MockitoBean
	private OrderService orderService;

	private OrderResponse sampleResponse(UUID id) {
		Instant now = Instant.now();
		UUID productId = UUID.randomUUID();
		OrderItemResponse item = new OrderItemResponse(productId, 2, new BigDecimal("10.00"), new BigDecimal("20.00"));
		return new OrderResponse(id, "Alice", OrderStatus.PENDING, new BigDecimal("20.00"), List.of(item), now, now);
	}

	private OrderRequest sampleRequest() {
		return new OrderRequest("Alice", List.of(new OrderItemRequest(UUID.randomUUID(), 2)));
	}

	@Test
	void findAll_returnsListOfOrders() throws Exception {
		UUID id = UUID.randomUUID();
		when(orderService.findAll()).thenReturn(List.of(sampleResponse(id)));

		mockMvc.perform(get("/api/orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", is(id.toString())))
				.andExpect(jsonPath("$[0].customerName", is("Alice")));
	}

	@Test
	void findById_whenFound_returnsOrder() throws Exception {
		UUID id = UUID.randomUUID();
		when(orderService.findById(id)).thenReturn(sampleResponse(id));

		mockMvc.perform(get("/api/orders/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(id.toString())));
	}

	@Test
	void findById_whenNotFound_returns404() throws Exception {
		UUID id = UUID.randomUUID();
		when(orderService.findById(id)).thenThrow(ResourceNotFoundException.forOrder(id));

		mockMvc.perform(get("/api/orders/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void create_withValidBody_returns201WithLocation() throws Exception {
		UUID id = UUID.randomUUID();
		OrderResponse response = sampleResponse(id);
		when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(sampleRequest())))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/orders/" + id)))
				.andExpect(jsonPath("$.id", is(id.toString())));
	}

	@Test
	void create_withBlankCustomerName_returns400() throws Exception {
		OrderRequest request = new OrderRequest("", List.of(new OrderItemRequest(UUID.randomUUID(), 1)));

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.customerName").exists());
	}

	@Test
	void create_withEmptyItems_returns400() throws Exception {
		OrderRequest request = new OrderRequest("Alice", List.of());

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.items").exists());
	}

	@Test
	void create_withNegativeQuantity_returns400() throws Exception {
		OrderRequest request = new OrderRequest("Alice", List.of(new OrderItemRequest(UUID.randomUUID(), -1)));

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors['items[0].quantity']").exists());
	}

	@Test
	void create_whenProductNotFound_returns404() throws Exception {
		when(orderService.createOrder(any(OrderRequest.class))).thenThrow(new ResourceNotFoundException("Product not found"));

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(sampleRequest())))
				.andExpect(status().isNotFound());
	}

	@Test
	void create_whenInsufficientStock_returns409() throws Exception {
		when(orderService.createOrder(any(OrderRequest.class))).thenThrow(new InvalidOrderException("Insufficient stock"));

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(sampleRequest())))
				.andExpect(status().isConflict());
	}

	@Test
	void create_whenDownstreamUnavailable_returns502() throws Exception {
		when(orderService.createOrder(any(OrderRequest.class))).thenThrow(new DownstreamServiceUnavailableException("unavailable"));

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(sampleRequest())))
				.andExpect(status().isBadGateway());
	}

	@Test
	void updateStatus_whenLegal_returns200() throws Exception {
		UUID id = UUID.randomUUID();
		OrderResponse response = sampleResponse(id);
		when(orderService.updateStatus(eq(id), any(UpdateStatusRequest.class))).thenReturn(response);

		mockMvc.perform(put("/api/orders/{id}/status", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new UpdateStatusRequest(OrderStatus.CONFIRMED))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(id.toString())));
	}

	@Test
	void updateStatus_whenNotFound_returns404() throws Exception {
		UUID id = UUID.randomUUID();
		when(orderService.updateStatus(eq(id), any(UpdateStatusRequest.class))).thenThrow(ResourceNotFoundException.forOrder(id));

		mockMvc.perform(put("/api/orders/{id}/status", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new UpdateStatusRequest(OrderStatus.CONFIRMED))))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateStatus_whenIllegalTransition_returns409() throws Exception {
		UUID id = UUID.randomUUID();
		when(orderService.updateStatus(eq(id), any(UpdateStatusRequest.class)))
				.thenThrow(InvalidOrderException.illegalTransition(OrderStatus.CANCELLED, OrderStatus.CONFIRMED));

		mockMvc.perform(put("/api/orders/{id}/status", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new UpdateStatusRequest(OrderStatus.CONFIRMED))))
				.andExpect(status().isConflict());
	}

	@Test
	void updateStatus_withNullStatus_returns400() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(put("/api/orders/{id}/status", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void delete_whenFound_returns204() throws Exception {
		UUID id = UUID.randomUUID();
		doNothing().when(orderService).delete(id);

		mockMvc.perform(delete("/api/orders/{id}", id))
				.andExpect(status().isNoContent());

		verify(orderService).delete(id);
	}

	@Test
	void delete_whenNotFound_returns404() throws Exception {
		UUID id = UUID.randomUUID();
		doThrow(ResourceNotFoundException.forOrder(id)).when(orderService).delete(id);

		mockMvc.perform(delete("/api/orders/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void delete_whenConfirmed_returns409() throws Exception {
		UUID id = UUID.randomUUID();
		doThrow(InvalidOrderException.deleteConfirmed()).when(orderService).delete(id);

		mockMvc.perform(delete("/api/orders/{id}", id))
				.andExpect(status().isConflict());
	}

}
