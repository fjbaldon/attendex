package com.github.fjbaldon.attendex.platform;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithArchitectureTests {
    ApplicationModules modules = ApplicationModules.of(PlatformApplication.class);

    @Test
    void verifyModularity() {
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        new Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml()
                .writeAggregatingDocument();
    }
}
