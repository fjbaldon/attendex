package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.model.Scanner;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScannerRepository extends JpaRepository<Scanner, Long> {
    @EntityGraph(attributePaths = {"organization"})
    Optional<Scanner> findByEmail(String email);

    List<Scanner> findAllByOrganizationId(Long organizationId);

    boolean existsByEmailAndOrganizationId(String email, Long organizationId);

    Optional<Scanner> findByIdAndOrganizationId(Long id, Long organizationId);
}
