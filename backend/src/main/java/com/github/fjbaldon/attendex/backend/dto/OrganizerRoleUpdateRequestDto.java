package com.github.fjbaldon.attendex.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrganizerRoleUpdateRequestDto {
    @NotNull(message = "Role ID cannot be null")
    private Long roleId;
}
