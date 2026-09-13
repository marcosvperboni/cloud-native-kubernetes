package com.marcosperboni.cloudnative.inventory.interfaces.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcosperboni.cloudnative.inventory.application.InventoryService;
import com.marcosperboni.cloudnative.inventory.application.dto.InventoryRequest;
import com.marcosperboni.cloudnative.inventory.application.dto.InventoryResponse;
import com.marcosperboni.cloudnative.inventory.application.dto.ReserveByProductRequest;
import com.marcosperboni.cloudnative.inventory.application.dto.ReserveStockRequest;
import com.marcosperboni.cloudnative.inventory.domain.InsufficientStockException;
import com.marcosperboni.cloudnative.inventory.domain.ResourceNotFoundException;
import com.marcosperboni.cloudnative.inventory.infrastructure.ProductServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private InventoryService inventoryService;

	@Test
	void findAllReturnsOk() throws Exception {
		InventoryResponse response = aResponse();
		when(inventoryService.findAll()).thenReturn(List.of(response));

		mockMvc.perform(get("/api/inventory"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(response.id().toString()));
	}

	@Test
	void findByIdReturnsOkWhenFound() throws Exception {
		InventoryResponse response = aResponse();
		when(inventoryService.findById(response.id())).thenReturn(response);

		mockMvc.perform(get("/api/inventory/{id}", response.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productId").value(response.productId().toString()));
	}

	@Test
	void findByIdReturnsNotFoundWhenMissing() throws Exception {
		UUID id = UUID.randomUUID();
		when(inventoryService.findById(id)).thenThrow(new ResourceNotFoundException("Inventory item not found: " + id));

		mockMvc.perform(get("/api/inventory/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void createReturnsCreatedWithLocationHeader() throws Exception {
		InventoryRequest request = new InventoryRequest(UUID.randomUUID(), 100, "WH-01");
		InventoryResponse response = aResponse();
		when(inventoryService.create(any(InventoryRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").value(response.id().toString()));
	}

	@Test
	void createReturnsBadRequestWhenProductIdMissing() throws Exception {
		String invalidBody = """
				{"quantityAvailable": 10, "warehouseLocation": "WH-01"}""";

		mockMvc.perform(post("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.productId").exists());
	}

	@Test
	void createReturnsBadRequestWhenQuantityAvailableNegative() throws Exception {
		InventoryRequest request = new InventoryRequest(UUID.randomUUID(), -1, "WH-01");

		mockMvc.perform(post("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.quantityAvailable").exists());
	}

	@Test
	void createReturnsNotFoundWhenProductDoesNotExist() throws Exception {
		InventoryRequest request = new InventoryRequest(UUID.randomUUID(), 10, "WH-01");
		when(inventoryService.create(any(InventoryRequest.class)))
				.thenThrow(new ResourceNotFoundException("Product not found: " + request.productId()));

		mockMvc.perform(post("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	void createReturnsBadGatewayWhenProductServiceUnavailable() throws Exception {
		InventoryRequest request = new InventoryRequest(UUID.randomUUID(), 10, "WH-01");
		when(inventoryService.create(any(InventoryRequest.class)))
				.thenThrow(new ProductServiceUnavailableException("down"));

		mockMvc.perform(post("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadGateway());
	}

	@Test
	void updateReturnsOkWhenFound() throws Exception {
		InventoryResponse response = aResponse();
		InventoryRequest request = new InventoryRequest(response.productId(), 60, "WH-03");
		when(inventoryService.update(eq(response.id()), any(InventoryRequest.class))).thenReturn(response);

		mockMvc.perform(put("/api/inventory/{id}", response.id())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());
	}

	@Test
	void updateReturnsNotFoundWhenMissing() throws Exception {
		UUID id = UUID.randomUUID();
		InventoryRequest request = new InventoryRequest(UUID.randomUUID(), 60, "WH-03");
		when(inventoryService.update(eq(id), any(InventoryRequest.class)))
				.thenThrow(new ResourceNotFoundException("Inventory item not found: " + id));

		mockMvc.perform(put("/api/inventory/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteReturnsNoContentWhenFound() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(delete("/api/inventory/{id}", id))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteReturnsNotFoundWhenMissing() throws Exception {
		UUID id = UUID.randomUUID();
		org.mockito.Mockito.doThrow(new ResourceNotFoundException("Inventory item not found: " + id))
				.when(inventoryService).delete(id);

		mockMvc.perform(delete("/api/inventory/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void reserveReturnsOkWhenEnoughStock() throws Exception {
		InventoryResponse response = aResponse();
		ReserveStockRequest request = new ReserveStockRequest(10);
		when(inventoryService.reserveStock(eq(response.id()), anyInt())).thenReturn(response);

		mockMvc.perform(post("/api/inventory/{id}/reserve", response.id())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());
	}

	@Test
	void reserveReturnsConflictWhenInsufficientStock() throws Exception {
		UUID id = UUID.randomUUID();
		ReserveStockRequest request = new ReserveStockRequest(1000);
		when(inventoryService.reserveStock(eq(id), anyInt()))
				.thenThrow(new InsufficientStockException("Insufficient stock"));

		mockMvc.perform(post("/api/inventory/{id}/reserve", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
	}

	@Test
	void reserveReturnsNotFoundWhenItemMissing() throws Exception {
		UUID id = UUID.randomUUID();
		ReserveStockRequest request = new ReserveStockRequest(10);
		when(inventoryService.reserveStock(eq(id), anyInt()))
				.thenThrow(new ResourceNotFoundException("Inventory item not found: " + id));

		mockMvc.perform(post("/api/inventory/{id}/reserve", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	void reserveReturnsBadRequestWhenQuantityMissing() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(post("/api/inventory/{id}/reserve", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void reserveByProductReturnsOkWhenEnoughStock() throws Exception {
		InventoryResponse response = aResponse();
		ReserveByProductRequest request = new ReserveByProductRequest(response.productId(), 10);
		when(inventoryService.reserveStockByProduct(eq(response.productId()), anyInt())).thenReturn(response);

		mockMvc.perform(post("/api/inventory/reserve-by-product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productId").value(response.productId().toString()));
	}

	@Test
	void reserveByProductReturnsNotFoundWhenNoInventoryForProduct() throws Exception {
		UUID productId = UUID.randomUUID();
		ReserveByProductRequest request = new ReserveByProductRequest(productId, 10);
		when(inventoryService.reserveStockByProduct(eq(productId), anyInt()))
				.thenThrow(new ResourceNotFoundException("No inventory found for product: " + productId));

		mockMvc.perform(post("/api/inventory/reserve-by-product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	void reserveByProductReturnsConflictWhenInsufficientStock() throws Exception {
		UUID productId = UUID.randomUUID();
		ReserveByProductRequest request = new ReserveByProductRequest(productId, 1000);
		when(inventoryService.reserveStockByProduct(eq(productId), anyInt()))
				.thenThrow(new InsufficientStockException("Insufficient stock"));

		mockMvc.perform(post("/api/inventory/reserve-by-product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
	}

	@Test
	void reserveByProductReturnsBadRequestWhenProductIdMissing() throws Exception {
		mockMvc.perform(post("/api/inventory/reserve-by-product")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"quantity": 10}"""))
				.andExpect(status().isBadRequest());
	}

	private InventoryResponse aResponse() {
		return new InventoryResponse(UUID.randomUUID(), UUID.randomUUID(), 100, 0, "WH-01", Instant.now());
	}
}
