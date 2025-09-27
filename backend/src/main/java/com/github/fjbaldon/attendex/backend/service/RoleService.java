package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.RoleDto;
import com.github.fjbaldon.attendex.backend.dto.RoleRequest;
import com.github.fjbaldon.attendex.backend.model.Organization;
import com.github.fjbaldon.attendex.backend.model.Role;
import com.github.fjbaldon.attendex.backend.repository.OrganizerRepository;
import com.github.fjbaldon.attendex.backend.repository.RoleRepository;
import com.github.fjbaldon.attendex.backend.repository.ScannerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final OrganizerRepository organizerRepository;
    private final ScannerRepository scannerRepository;


    @Transactional
    public RoleDto createRole(RoleRequest request, Long organizationId) {
        if (roleRepository.existsByNameAndOrganizationId(request.getName(), organizationId)) {
            throw new IllegalArgumentException("A role with this name already exists in your organization.");
        }
        Organization orgReference = new Organization();
        orgReference.setId(organizationId);

        Role role = Role.builder()
                .name(request.getName())
                .permissions(request.getPermissions())
                .organization(orgReference)
                .build();

        return toDto(roleRepository.save(role));
    }

    @Transactional(readOnly = true)
    public RoleDto getRoleById(Long roleId, Long organizationId) {
        Role role = roleRepository.findByIdAndOrganizationId(roleId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found in your organization."));
        return toDto(role);
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getRolesByOrganization(Long organizationId) {
        return roleRepository.findByOrganizationId(organizationId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleDto updateRole(Long roleId, RoleRequest request, Long organizationId) {
        Role role = roleRepository.findByIdAndOrganizationId(roleId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found in your organization."));

        role.setName(request.getName());
        role.setPermissions(request.getPermissions());

        return toDto(roleRepository.save(role));
    }

    public void deleteRole(Long roleId, Long organizationId) {
        Role role = roleRepository.findByIdAndOrganizationId(roleId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found in your organization."));

        boolean isRoleInUse = organizerRepository.existsByRoleId(roleId) || scannerRepository.existsByRoleId(roleId);
        if (isRoleInUse) {
            throw new IllegalStateException("Cannot delete role: it is currently assigned to one or more users.");
        }

        if ("Admin".equalsIgnoreCase(role.getName())) {
            throw new IllegalStateException("The default 'Admin' role cannot be deleted.");
        }

        roleRepository.delete(role);
    }

    private RoleDto toDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(role.getPermissions())
                .build();
    }
}
