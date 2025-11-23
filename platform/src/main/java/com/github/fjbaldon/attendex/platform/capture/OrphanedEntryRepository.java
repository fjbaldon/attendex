package com.github.fjbaldon.attendex.platform.capture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

interface OrphanedEntryRepository extends PagingAndSortingRepository<OrphanedEntry, Long>, CrudRepository<OrphanedEntry, Long> {
    Page<OrphanedEntry> findAllByOrganizationId(Long organizationId, Pageable pageable);
    Optional<OrphanedEntry> findByIdAndOrganizationId(Long id, Long organizationId);
}
