package com.marcosperboni.cloudnative.product.application;

import java.util.List;
import java.util.UUID;

import com.marcosperboni.cloudnative.product.application.dto.ProductRequest;
import com.marcosperboni.cloudnative.product.application.dto.ProductResponse;

public interface ProductService {

	ProductResponse create(ProductRequest request);

	ProductResponse findById(UUID id);

	List<ProductResponse> findAll();

	ProductResponse update(UUID id, ProductRequest request);

	void delete(UUID id);

}
