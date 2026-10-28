package com.booking.engine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReversalRequest {

    @NotBlank
    @Size(max = 500)
    private String reason;

    @NotBlank
    @Size(max = 100)
    private String idempotencyKey;
}
