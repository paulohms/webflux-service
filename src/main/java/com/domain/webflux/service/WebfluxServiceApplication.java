package com.domain.webflux.service;

import com.domain.webflux.service.model.PubSubMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Sinks;

@SpringBootApplication
public class WebfluxServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxServiceApplication.class, args);
	}

	// pub
	@Bean
	public Sinks.Many<PubSubMessage> sink() {
		return Sinks.many().multicast()
				.onBackpressureBuffer(1000);
	}

}
