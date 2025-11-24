package com.github.fjbaldon.attendex.platform.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final Long id;
    private final Long organizationId;
    private final boolean forcePasswordChange;
    private final String role;

    public CustomUserDetails(
            Long id,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Long organizationId,
            boolean enabled,
            boolean forcePasswordChange,
            String role
    ) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
        this.organizationId = organizationId;
        this.forcePasswordChange = forcePasswordChange;
        this.role = role;
    }
}
