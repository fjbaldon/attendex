package com.github.fjbaldon.attendex.backend.dto;

import com.github.fjbaldon.attendex.backend.model.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RoleRequest {
    @NotBlank(message = "Role name cannot be blank")
    @Size(max = 50)
    private String name;

    @NotEmpty(message = "A role must have at least one permission")
    private Set<Permission> permissions;
}
