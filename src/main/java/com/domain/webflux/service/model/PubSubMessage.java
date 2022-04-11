package com.domain.webflux.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PubSubMessage {
    String key;
    String value;
}
