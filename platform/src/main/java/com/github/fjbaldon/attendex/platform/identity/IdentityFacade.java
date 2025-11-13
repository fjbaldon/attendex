package com.github.fjbaldon.attendex.platform.identity;

import com.github.fjbaldon.attendex.platform.identity.dto.AuthRequestDto;
import com.github.fjbaldon.attendex.platform.identity.dto.AuthResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityFacade {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponseDto login(AuthRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String jwt = jwtService.generateToken(authentication.getName());

        return new AuthResponseDto(jwt);
    }
}
