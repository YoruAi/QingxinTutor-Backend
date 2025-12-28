package com.yoru.qingxintutor.pojo.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {
    @NotBlank(message = "Item is required")
    @Size(max = 100, message = "Item must be between 1 and 100 characters")
    private String item;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be between 1 and 100")
    @Max(value = 100, message = "Quantity must be between 1 and 100")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be between 0 and 2000")
    @DecimalMax(value = "2000.00", message = "Price must be between 0 and 2000")
    @Digits(integer = 4, fraction = 2, message = "Price must have at most 2 decimal places and up to 4 digits before decimal")
    private BigDecimal price;
}
