package com.payflow_engine.domain.entities;

import com.payflow_engine.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_wallet_id", nullable = false)
    private Wallet payerWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_wallet_id", nullable = false)
    private Wallet payeeWallet;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotency;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
