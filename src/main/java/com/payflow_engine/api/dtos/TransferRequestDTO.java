package com.payflow_engine.api.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequestDTO(
        @NotNull(message = "O ID do pagador é obrigatório")
        Long payerId,

        @NotNull(message = "O ID do recebedor é obrigatório")
        Long payeeId,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor mínimo para transferências é 0.01")
        BigDecimal value
) {
}
