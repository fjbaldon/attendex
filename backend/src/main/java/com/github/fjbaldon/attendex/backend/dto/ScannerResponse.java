package com.github.fjbaldon.attendex.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScannerResponse {
    private Long id;
    private String username;
}
