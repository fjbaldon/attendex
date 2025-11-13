package com.github.fjbaldon.attendex.platform.identity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final Long organizationId;

    public CustomUserDetails(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Long organizationId,
            boolean enabled
    ) {
        super(username, password, enabled, true, true, true, authorities);
        this.organizationId = organizationId;
    }
}
