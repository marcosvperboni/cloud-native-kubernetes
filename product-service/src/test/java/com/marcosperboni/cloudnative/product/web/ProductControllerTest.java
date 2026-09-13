package com.marcosperboni.cloudnative.product.web;

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

import tools.jackson.databind.ObjectMapper;
import com.marcosperboni.cloudnative.product.application.ProductService;
import com.marcosperboni.cloudnative.product.application.dto.ProductRequest;
import com.marcosperboni.cloudnative.product.application.dto.ProductResponse;
import com.marcosperboni.cloudnative.product.domain.ResourceNotFoundException;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProductService productService;

	private ProductResponse sampleResponse(UUID id) {
		Instant now = Instant.now();
		return new ProductResponse(id, "Keyboard", "Mechanical keyboard", new BigDecimal("199.90"), 10, true, now, now);
	}

	@Test
	void findAll_returnsListOfProducts() throws Exception {
		UUID id = UUID.randomUUID();
		when(productService.findAll()).thenReturn(List.of(sampleResponse(id)));

		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", is(id.toString())))
				.andExpect(jsonPath("$[0].name", is("Keyboard")));
	}

	@Test
	void findById_whenFound_returnsProduct() throws Exception {
		UUID id = UUID.randomUUID();
		when(productService.findById(id)).thenReturn(sampleResponse(id));

		mockMvc.perform(get("/api/products/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(id.toString())))
				.andExpect(jsonPath("$.name", is("Keyboard")));
	}

	@Test
	void findById_whenNotFound_returns404() throws Exception {
		UUID id = UUID.randomUUID();
		when(productService.findById(id)).thenThrow(ResourceNotFoundException.forProduct(id));

		mockMvc.perform(get("/api/products/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void create_withValidBody_returns201WithLocation() throws Exception {
		UUID id = UUID.randomUUID();
		ProductResponse response = sampleResponse(id);
		when(productService.create(any(ProductRequest.class))).thenReturn(response);

		ProductRequest request = new ProductRequest("Keyboard", "Mechanical keyboard", new BigDecimal("199.90"), 10);

		mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/products/" + id)))
				.andExpect(jsonPath("$.id", is(id.toString())));
	}

	@Test
	void create_withBlankName_returns400() throws Exception {
		ProductRequest request = new ProductRequest("", "Mechanical keyboard", new BigDecimal("199.90"), 10);

		mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.name").exists());
	}

	@Test
	void create_withNegativePrice_returns400() throws Exception {
		ProductRequest request = new ProductRequest("Keyboard", "Mechanical keyboard", new BigDecimal("-1.00"), 10);

		mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.price").exists());
	}

	@Test
	void update_whenFound_returns200() throws Exception {
		UUID id = UUID.randomUUID();
		ProductResponse response = sampleResponse(id);
		when(productService.update(eq(id), any(ProductRequest.class))).thenReturn(response);

		ProductRequest request = new ProductRequest("Keyboard", "Mechanical keyboard", new BigDecimal("199.90"), 10);

		mockMvc.perform(put("/api/products/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(id.toString())));
	}

	@Test
	void update_whenNotFound_returns404() throws Exception {
		UUID id = UUID.randomUUID();
		when(productService.update(eq(id), any(ProductRequest.class))).thenThrow(ResourceNotFoundException.forProduct(id));

		ProductRequest request = new ProductRequest("Keyboard", "Mechanical keyboard", new BigDecimal("199.90"), 10);

		mockMvc.perform(put("/api/products/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}

	@Test
	void update_withInvalidBody_returns400() throws Exception {
		UUID id = UUID.randomUUID();
		ProductRequest request = new ProductRequest(null, "Mechanical keyboard", new BigDecimal("199.90"), 10);

		mockMvc.perform(put("/api/products/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void delete_whenFound_returns204() throws Exception {
		UUID id = UUID.randomUUID();
		doNothing().when(productService).delete(id);

		mockMvc.perform(delete("/api/products/{id}", id))
				.andExpect(status().isNoContent());

		verify(productService).delete(id);
	}

	@Test
	void delete_whenNotFound_returns404() throws Exception {
		UUID id = UUID.randomUUID();
		doThrow(ResourceNotFoundException.forProduct(id)).when(productService).delete(id);

		mockMvc.perform(delete("/api/products/{id}", id))
				.andExpect(status().isNotFound());
	}

}
