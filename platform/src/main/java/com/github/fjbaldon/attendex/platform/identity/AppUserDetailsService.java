package com.github.fjbaldon.attendex.platform.identity;

import com.github.fjbaldon.attendex.platform.admin.AdminFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class AppUserDetailsService implements UserDetailsService {

    private final AdminFacade adminFacade;
    private final OrganizationFacade organizationFacade;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<com.github.fjbaldon.attendex.platform.admin.dto.UserAuthDto> stewardAuth = adminFacade.findStewardAuthByEmail(email);
        if (stewardAuth.isPresent()) {
            var authDto = stewardAuth.get();
            return new CustomUserDetails(
                    authDto.email(),
                    authDto.password(),
                    Collections.singletonList(new SimpleGrantedAuthority(authDto.role())),
                    null,
                    true
            );
        }

        Optional<com.github.fjbaldon.attendex.platform.organization.dto.UserAuthDto> userAuth = organizationFacade.findUserAuthByEmail(email);
        if (userAuth.isPresent()) {
            var authDto = userAuth.get();
            return new CustomUserDetails(
                    authDto.email(),
                    authDto.password(),
                    Collections.singletonList(new SimpleGrantedAuthority(authDto.role())),
                    authDto.organizationId(),
                    authDto.enabled()
            );
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
