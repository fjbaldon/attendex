package com.github.fjbaldon.attendex.backend.security;

import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final OrganizerRepository organizerRepository;
    private final ScannerRepository scannerRepository;

    public UserDetailsServiceImpl(OrganizerRepository organizerRepository, ScannerRepository scannerRepository) {
        this.organizerRepository = organizerRepository;
        this.scannerRepository = scannerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First, try to find an organizer
        return organizerRepository.findByUsername(username)
                .map(organizer -> new User(
                        organizer.getUsername(),
                        organizer.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER"))
                ))
                // If not found, try to find a scanner
                .orElseGet(() -> scannerRepository.findByUsername(username)
                        .map(scanner -> new User(
                                scanner.getUsername(),
                                scanner.getPassword(),
                                List.of(new SimpleGrantedAuthority("ROLE_SCANNER"))
                        ))
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username)));
    }
}
