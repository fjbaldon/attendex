package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

interface ScannerRepository extends PagingAndSortingRepository<Scanner, Long>, CrudRepository<Scanner, Long> {
    Optional<Scanner> findByEmail(String email);

    boolean existsByEmailAndOrganizationId(String email, Long organizationId);

    long countByOrganizationId(Long organizationId);
}
