package com.github.fjbaldon.attendex.platform.admin;

import com.github.fjbaldon.attendex.platform.admin.dto.StewardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stewards")
@RequiredArgsConstructor
class AdminController {

    private final AdminFacade adminFacade;

    @GetMapping
    public List<StewardDto> getAllStewards() {
        return adminFacade.findAllStewards();
    }
}
