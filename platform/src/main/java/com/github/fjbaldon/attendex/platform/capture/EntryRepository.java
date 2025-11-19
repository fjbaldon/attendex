package com.github.fjbaldon.attendex.platform.capture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.Instant;
import java.util.List;

interface EntryRepository extends PagingAndSortingRepository<Entry, Long>, JpaRepository<Entry, Long> {
    boolean existsBySessionIdAndAttendeeId(Long sessionId, Long attendeeId);

    long countByOrganizationIdAndSyncTimestampAfter(Long organizationId, Instant timestamp);

    Page<Entry> findBySessionIdIn(List<Long> sessionIds, Pageable pageable);
}
