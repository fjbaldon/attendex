package com.github.fjbaldon.attendex.platform.identity;

import com.github.fjbaldon.attendex.platform.admin.AdminFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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
            return new User(
                    authDto.email(),
                    authDto.password(),
                    Collections.singletonList(new SimpleGrantedAuthority(authDto.role()))
            );
        }

        Optional<com.github.fjbaldon.attendex.platform.organization.dto.UserAuthDto> organizerAuth = organizationFacade.findOrganizerAuthByEmail(email);

        if (organizerAuth.isPresent()) {
            var authDto = organizerAuth.get();
            return new User(
                    authDto.email(),
                    authDto.password(),
                    Collections.singletonList(new SimpleGrantedAuthority(authDto.role()))
            );
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
