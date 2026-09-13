package com.marcosperboni.cloudnative.product.infrastructure;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.marcosperboni.cloudnative.product.application.dto.ProductResponse;

@EnableCaching
@Configuration
public class RedisCacheConfig {

	@Bean
	public RedisCacheConfiguration cacheConfiguration() {
		ObjectMapper redisObjectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		Jackson2JsonRedisSerializer<ProductResponse> serializer = new Jackson2JsonRedisSerializer<>(
				redisObjectMapper, ProductResponse.class);

		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofSeconds(60))
				.disableCachingNullValues()
				.serializeKeysWith(
						RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
	}

}
