package com.github.fjbaldon.attendex.backend.controller;

import com.github.fjbaldon.attendex.backend.dto.CustomFieldDefinitionDto;
import com.github.fjbaldon.attendex.backend.dto.CustomFieldDefinitionRequest;
import com.github.fjbaldon.attendex.backend.security.CustomUserDetails;
import com.github.fjbaldon.attendex.backend.service.CustomFieldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/custom-fields")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_ORGANIZATION')")
public class CustomFieldController {

    private final CustomFieldService customFieldService;

    @GetMapping
    public ResponseEntity<List<CustomFieldDefinitionDto>> getDefinitions(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(customFieldService.getDefinitionsByOrganization(user.getOrganizationId()));
    }

    @PostMapping
    public ResponseEntity<CustomFieldDefinitionDto> createDefinition(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CustomFieldDefinitionRequest request) {
        CustomFieldDefinitionDto newField = customFieldService.createDefinition(request, user.getOrganizationId());
        return new ResponseEntity<>(newField, HttpStatus.CREATED);
    }

    @DeleteMapping("/{fieldId}")
    public ResponseEntity<Void> deleteDefinition(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long fieldId) {
        customFieldService.deleteDefinition(fieldId, user.getOrganizationId());
        return ResponseEntity.noContent().build();
    }
}
