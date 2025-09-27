package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByOrganizationId(Long organizationId);

    Optional<Role> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Role> findByNameAndOrganizationId(String name, Long organizationId);

    boolean existsByNameAndOrganizationId(String name, Long organizationId);
}
