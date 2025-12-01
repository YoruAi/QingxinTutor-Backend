package com.yoru.qingxintutor.pojo.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechargeRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "The amount must be greater than 0")
    @DecimalMax(value = "2000.00", message = "The amount must be less than 2000")
    @Digits(integer = 4, fraction = 2, message = "Price must have at most 2 decimal places and up to 4 digits before decimal")
    private BigDecimal amount;
}
