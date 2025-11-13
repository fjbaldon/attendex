package com.github.fjbaldon.attendex.platform.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AdminDataInitializer implements ApplicationRunner {

    private final AdminFacade adminFacade;

    @Override
    public void run(ApplicationArguments args) {
        adminFacade.createDefaultStewardIfNeeded();
    }
}
