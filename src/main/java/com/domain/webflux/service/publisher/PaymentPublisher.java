package com.domain.webflux.service.publisher;

import com.domain.webflux.service.model.Payment;
import com.domain.webflux.service.model.PubSubMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class PaymentPublisher {

    private final Sinks.Many<PubSubMessage> sink;
    private final ObjectMapper objectMapper;

    public Mono<Payment> onPaymentCreate(final Payment payment) {
        return Mono.fromCallable(() -> {
                    final String userId = payment.getUserId();
                    final String data = objectMapper.writeValueAsString(payment);
                    return new PubSubMessage(userId, data);
                })
                .subscribeOn(Schedulers.parallel())
                .doOnNext(message -> this.sink.tryEmitNext(message))
                .thenReturn(payment);
    }

}
