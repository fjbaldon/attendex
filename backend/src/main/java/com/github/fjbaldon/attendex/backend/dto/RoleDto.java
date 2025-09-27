package com.github.fjbaldon.attendex.backend.dto;

import com.github.fjbaldon.attendex.backend.model.Permission;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleDto {
    private Long id;
    private String name;
    private Set<Permission> permissions;
}
