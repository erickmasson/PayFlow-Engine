package com.payflow_engine.api.controllers;

import com.payflow_engine.api.dtos.TransferRequestDTO;
import com.payflow_engine.api.dtos.TransferResponseDTO;
import com.payflow_engine.domain.services.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/transfers")
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransferResponseDTO> transfer(@Valid @RequestBody TransferRequestDTO request){
        TransferResponseDTO transfer = service.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transfer);
    }
}