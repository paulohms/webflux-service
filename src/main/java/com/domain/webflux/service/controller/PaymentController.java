package com.domain.webflux.service.controller;

import com.domain.webflux.service.model.Payment;
import com.domain.webflux.service.publisher.PaymentPublisher;
import com.domain.webflux.service.repository.InMemoryDatabase;
import com.domain.webflux.service.repository.PaymentRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Mono - (0,1, error)
 * Flux - (0,N, error)
 */

@RestController
@RequestMapping("/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentPublisher paymentPublisher;

    @PostMapping
    public Mono<Payment> createPayment(@RequestBody final NewPaymentInput input) {
        final String userId = input.getUserId();
        log.info("Processing payment {}", userId);
        return this.paymentRepository.createPayment(userId)
                .flatMap(payment -> this.paymentPublisher.onPaymentCreate(payment))
                .flatMap(payment ->
                        Flux.interval(Duration.ofSeconds(1))
                                .doOnNext(it -> log.info("Next tick - {}", it))
                                .flatMap(tick -> this.paymentRepository.getPayment(userId))
                                .filter(it -> Payment.PaymentStatus.APPROVED == it.getStatus())
                                .next()
                )
                .doOnNext(next -> log.info("Payment processed {}", userId))
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .doAfterRetry(signal -> log.info("Execution failed... retrying {}", signal.totalRetries())));
    }

    @GetMapping("/users")
    public Flux<Payment> findAllById(@RequestParam String ids) {
        final List<String> _ids = Arrays.asList(ids.split(","));
        log.info("Collecting {} payments", _ids.size());
        return Flux.fromIterable(_ids)
                .flatMap(id -> this.paymentRepository.getPayment(id));
    }

    @GetMapping("/ids")
    public Mono<String> getIds() {
        return Mono.fromCallable(() -> {
                    return String.join(",", InMemoryDatabase.DATABASE.keySet());
                })
                .subscribeOn(Schedulers.parallel());
    }

    @Data
    public static class NewPaymentInput {
        private String userId;
    }

}
