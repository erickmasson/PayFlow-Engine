package com.payflow_engine.api.dtos;

import java.math.BigDecimal;

public record NotificationEventDTO(
        Long transactionId,
        String payerEmail,
        String payeeEmail,
        BigDecimal amount
) {
}
