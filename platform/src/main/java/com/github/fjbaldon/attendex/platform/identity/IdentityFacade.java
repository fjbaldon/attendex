package com.github.fjbaldon.attendex.platform.identity;

import com.github.fjbaldon.attendex.platform.admin.AdminFacade;
import com.github.fjbaldon.attendex.platform.identity.dto.AuthRequestDto;
import com.github.fjbaldon.attendex.platform.identity.dto.AuthResponseDto;
import com.github.fjbaldon.attendex.platform.identity.dto.PasswordChangeRequestDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
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
    private final OrganizationFacade organizationFacade;
    private final AdminFacade adminFacade;

    public AuthResponseDto login(AuthRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String jwt = jwtService.generateToken(authentication.getName());

        return new AuthResponseDto(jwt);
    }

    public void forceChangePassword(CustomUserDetails user, PasswordChangeRequestDto dto) {
        if ("Steward".equals(user.getRole())) {
            adminFacade.changeStewardPassword(user.getUsername(), dto.newPassword());
        } else {
            organizationFacade.changeUserPassword(user.getUsername(), dto.newPassword());
        }
    }
}
