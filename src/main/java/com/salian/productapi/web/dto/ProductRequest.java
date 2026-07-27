package com.salian.productapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Payload for creating or replacing a product")
public record ProductRequest(

        @Schema(example = "SKU-1001")
        @NotBlank
        @Size(max = 64)
        String sku,

        @Schema(example = "Mechanical Keyboard")
        @NotBlank
        @Size(max = 255)
        String name,

        @Schema(example = "87-key hot-swappable keyboard")
        @Size(max = 2000)
        String description,

        @Schema(example = "129.99")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        @Schema(example = "25")
        @NotNull
        @Min(0)
        Integer quantity) {
}
