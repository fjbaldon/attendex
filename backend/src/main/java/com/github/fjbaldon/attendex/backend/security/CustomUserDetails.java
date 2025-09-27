package com.github.fjbaldon.attendex.backend.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final Long organizationId;
    private final boolean forcePasswordChange;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long organizationId, boolean enabled, boolean forcePasswordChange) {
        super(username, password, enabled, true, true, true, authorities);
        this.organizationId = organizationId;
        this.forcePasswordChange = forcePasswordChange;
    }
}
