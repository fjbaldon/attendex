package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService {
    private final OrganizerRepository organizerRepository;
    private final ScannerRepository scannerRepository;

    @Transactional(readOnly = true)
    public UserDetails findUserByEmail(String email) throws UsernameNotFoundException {
        return organizerRepository.findByEmail(email)
                .map(organizer -> new CustomUserDetails(
                        organizer.getEmail(),
                        organizer.getPassword(),
                        getAuthorities(organizer),
                        organizer.getOrganization().getId(),
                        organizer.isEnabled(),
                        organizer.isForcePasswordChange()
                ))
                .orElseGet(() -> scannerRepository.findByEmail(email)
                        .map(scanner -> new CustomUserDetails(
                                scanner.getEmail(),
                                scanner.getPassword(),
                                getAuthorities(scanner),
                                scanner.getOrganization().getId(),
                                scanner.isEnabled(),
                                scanner.isForcePasswordChange()
                        ))
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email)));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Object user) {
        boolean forceChange;
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user instanceof com.github.fjbaldon.attendex.backend.model.Organizer o) {
            forceChange = o.isForcePasswordChange();
            authorities.add(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
        } else if (user instanceof com.github.fjbaldon.attendex.backend.model.Scanner s) {
            forceChange = s.isForcePasswordChange();
            authorities.add(new SimpleGrantedAuthority("ROLE_SCANNER"));
        } else {
            return authorities;
        }

        if (forceChange) {
            authorities.add(new SimpleGrantedAuthority("FORCE_PASSWORD_CHANGE"));
        }

        return authorities;
    }
}
