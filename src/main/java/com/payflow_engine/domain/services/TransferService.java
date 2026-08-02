package com.payflow_engine.domain.services;

import com.payflow_engine.api.dtos.NotificationEventDTO;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final RabbitTemplate rabbitTemplate;

    public TransferService(UserRepository userRepository, TransactionRepository transactionRepository, WalletRepository walletRepository, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public TransferResponseDTO transfer (TransferRequestDTO request){
        if(request.payerId().equals(request.payeeId())){
            throw new IllegalArgumentException("Pagador e Recebedor não podem ser a mesma pessoa.");
        }

        User payer = userRepository.findById(request.payerId()).orElseThrow(() -> new IllegalArgumentException("Pagador não encontrado."));

        User payee = userRepository.findById(request.payeeId()).orElseThrow(() -> new IllegalArgumentException("Recebedor não encontrado."));

        if(payer.getUserType() == UserType.LOJISTA){
            throw new IllegalStateException("Lojistas não estão autorizados a realizar transferências.");
        }

        Wallet firstLock = payer.getWallet().getId() < payee.getWallet().getId() ? payer.getWallet() : payee.getWallet();
        Wallet secondLock = payer.getWallet().getId() < payee.getWallet().getId() ? payer.getWallet() : payee.getWallet();

        walletRepository.findByIdForUpdate(firstLock.getId());
        walletRepository.findByIdForUpdate(secondLock.getId());

        if(payer.getWallet().getBalance().compareTo(request.value()) < 0){
            throw new IllegalStateException("Saldo insuficiente.");
        }

        payer.getWallet().setBalance(payer.getWallet().getBalance().subtract(request.value()));
        payee.getWallet().setBalance(payee.getWallet().getBalance().add(request.value()));

        walletRepository.save(payer.getWallet());
        walletRepository.save(payee.getWallet());

        Transaction transaction = new Transaction();
        transaction.setPayerWallet(payer.getWallet());
        transaction.setPayeeWallet(payee.getWallet());
        transaction.setAmount(request.value());
        transaction.setStatus(TransactionStatus.SUCCESS);

        transaction = transactionRepository.save(transaction);

        // ---- mensageria
        NotificationEventDTO eventDTO = new NotificationEventDTO(
                transaction.getId(),
                payer.getEmail(),
                payee.getEmail(),
                transaction.getAmount()
        );

        rabbitTemplate.convertAndSend("notification.exchange", "notification.routing.key", eventDTO);
        System.out.println("Evento de notificação publicado na fila!");
        //----- fim mensageria


        return TransferResponseDTO.fromEntity(transaction);
    }
}
