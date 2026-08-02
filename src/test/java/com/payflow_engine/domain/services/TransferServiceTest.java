package com.payflow_engine.domain.services;

import com.payflow_engine.api.dtos.TransferRequestDTO;
import com.payflow_engine.api.dtos.TransferResponseDTO;
import com.payflow_engine.domain.entities.Transaction;
import com.payflow_engine.domain.entities.User;
import com.payflow_engine.domain.entities.Wallet;
import com.payflow_engine.domain.enums.TransactionStatus;
import com.payflow_engine.domain.enums.UserType;
import com.payflow_engine.domain.repositories.TransactionRepository;
import com.payflow_engine.domain.repositories.UserRepository;
import com.payflow_engine.domain.repositories.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private TransferService transferService;

    private User payer;
    private User payee;

    @BeforeEach
    void setUp() {
        payer = new User();
        payer.setId(1L);
        payer.setEmail("joao@email.com");
        payer.setUserType(UserType.CLIENTE);

        Wallet payerWallet = new Wallet();
        payerWallet.setId(1L);
        payerWallet.setBalance(new BigDecimal("500.00"));
        payerWallet.setUser(payer);
        payer.setWallet(payerWallet);

        payee = new User();
        payee.setId(2L);
        payee.setEmail("maria@email.com");
        payee.setUserType(UserType.LOJISTA);

        Wallet payeeWallet = new Wallet();
        payeeWallet.setId(2L);
        payeeWallet.setBalance(new BigDecimal("100.00"));
        payeeWallet.setUser(payee); // <--- A CORREÇÃO ESTÁ AQUI!
        payee.setWallet(payeeWallet);
    }

    @Test
    @DisplayName("Deve realizar transferência com sucesso e publicar no RabbitMQ")
    void shouldTransferSuccessfully() {
        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(payee));

        Transaction mockSavedTransaction = new Transaction();
        mockSavedTransaction.setId(99L);
        mockSavedTransaction.setPayerWallet(payer.getWallet());
        mockSavedTransaction.setPayeeWallet(payee.getWallet());
        mockSavedTransaction.setAmount(request.value());
        mockSavedTransaction.setStatus(TransactionStatus.SUCCESS);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockSavedTransaction);

        TransferResponseDTO response = transferService.transfer(request);

        assertEquals(new BigDecimal("400.00"), payer.getWallet().getBalance());
        assertEquals(new BigDecimal("200.00"), payee.getWallet().getBalance());

        verify(walletRepository, times(2)).save(any(Wallet.class));

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));

        assertNotNull(response.transactionId());
        assertEquals("SUCCESS", response.status());
    }

    @Test
    @DisplayName("Não deve permitir transferência quando pagador é LOJISTA")
    void shouldThrowExceptionWhenPayerIsLojista() {
        payer.setUserType(UserType.LOJISTA);
        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("50.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(payee));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            transferService.transfer(request);
        });

        assertEquals("Lojistas não estão autorizados a realizar transferências.", exception.getMessage());

        verify(transactionRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Não deve permitir transferência sem saldo suficiente")
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("600.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(payee));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            transferService.transfer(request);
        });

        assertEquals("Saldo insuficiente.", exception.getMessage());
    }
}