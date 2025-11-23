package com.github.fjbaldon.attendex.platform.identity;

import com.github.fjbaldon.attendex.platform.identity.dto.AuthRequestDto;
import com.github.fjbaldon.attendex.platform.identity.dto.AuthResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
class IdentityController {

    private final IdentityFacade identityFacade;

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody AuthRequestDto request) {
        return identityFacade.login(request);
    }
}
