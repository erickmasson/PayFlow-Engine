package com.payflow_engine.api.dtos;

import com.payflow_engine.domain.entities.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponseDTO(
        Long transactionId,
        Long payerId,
        Long payeeId,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt
) {
    public static TransferResponseDTO fromEntity(Transaction transaction){
        return new TransferResponseDTO(
                transaction.getId(),
                transaction.getPayerWallet().getUser().getId(),
                transaction.getPayeeWallet().getUser().getId(),
                transaction.getAmount(),
                transaction.getStatus().name(),
                transaction.getCreatedAt()
        );
    }
}
