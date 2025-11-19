package com.github.fjbaldon.attendex.platform.identity;

import com.github.fjbaldon.attendex.platform.admin.AdminFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(authDto.role()));
            if (authDto.forcePasswordChange()) {
                authorities.add(new SimpleGrantedAuthority("FORCE_PASSWORD_CHANGE"));
            }
            return new CustomUserDetails(
                    null,
                    authDto.email(),
                    authDto.password(),
                    authorities,
                    null,
                    true,
                    authDto.forcePasswordChange(),
                    "Steward"
            );
        }

        Optional<com.github.fjbaldon.attendex.platform.organization.dto.UserAuthDto> userAuth = organizationFacade.findUserAuthByEmail(email);
        if (userAuth.isPresent()) {
            var authDto = userAuth.get();
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(authDto.role()));
            if (authDto.forcePasswordChange()) {
                authorities.add(new SimpleGrantedAuthority("FORCE_PASSWORD_CHANGE"));
            }
            String domainRole = authDto.role().equals("ROLE_ORGANIZER") ? "Organizer" : "Scanner";

            return new CustomUserDetails(
                    authDto.id(),
                    authDto.email(),
                    authDto.password(),
                    authorities,
                    authDto.organizationId(),
                    authDto.enabled(),
                    authDto.forcePasswordChange(),
                    domainRole
            );
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
