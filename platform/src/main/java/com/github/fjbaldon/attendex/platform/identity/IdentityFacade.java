package com.github.fjbaldon.attendex.platform.identity;

import com.github.fjbaldon.attendex.platform.admin.AdminFacade;
import com.github.fjbaldon.attendex.platform.common.security.CustomUserDetails;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class IdentityFacade {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OrganizationFacade organizationFacade;
    private final AdminFacade adminFacade;
    private final ApplicationEventPublisher eventPublisher;

    public AuthResponseDto login(AuthRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);

        eventPublisher.publishEvent(new UserLoggedInEvent(request.email(), getIpAddress()));

        return new AuthResponseDto(jwt);
    }

    public void forceChangePassword(CustomUserDetails user, PasswordChangeRequestDto dto) {
        if ("Steward".equals(user.getRole())) {
            adminFacade.changeStewardPassword(user.getUsername(), dto.newPassword());
        } else {
            organizationFacade.changeUserPassword(user.getUsername(), dto.newPassword());
        }
        eventPublisher.publishEvent(new UserPasswordChangedEvent(user.getUsername(), getIpAddress()));
    }

    private String getIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            return (ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip;
        }
        return "N/A";
    }
}
