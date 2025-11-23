package com.github.fjbaldon.attendex.platform.organization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

interface ScannerRepository extends PagingAndSortingRepository<Scanner, Long>, CrudRepository<Scanner, Long> {
    Optional<Scanner> findByEmail(String email);

    boolean existsByEmailAndOrganizationId(String email, Long organizationId);

    Page<Scanner> findAllByOrganizationId(Long organizationId, Pageable pageable);

    @Override
    @NonNull
    List<Scanner> findAllById(@NonNull Iterable<Long> ids);
}
