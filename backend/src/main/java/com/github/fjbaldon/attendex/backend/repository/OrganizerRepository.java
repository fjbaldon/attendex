package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.model.Organizer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
    @EntityGraph(attributePaths = {"organization", "role"})
    Optional<Organizer> findByEmail(String email);

    boolean existsByEmailAndOrganizationId(String email, Long organizationId);

    boolean existsByRoleId(Long roleId);

    List<Organizer> findAllByOrganizationId(Long organizationId);

    Optional<Organizer> findByIdAndOrganizationId(Long id, Long organizationId);

    long countByOrganizationIdAndRole_Name(Long organizationId, String roleName);
}
