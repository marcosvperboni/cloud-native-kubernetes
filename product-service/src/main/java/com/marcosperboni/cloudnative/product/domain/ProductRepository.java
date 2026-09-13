package com.marcosperboni.cloudnative.product.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

	Product save(Product product);

	Optional<Product> findById(UUID id);

	List<Product> findAllByOrderByNameAsc();

	boolean existsById(UUID id);

	void deleteById(UUID id);

}
