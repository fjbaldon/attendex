package com.github.fjbaldon.attendex.platform.capture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

interface EntryRepository extends CrudRepository<Entry, Long> {
    boolean existsBySessionIdAndAttendeeId(Long sessionId, Long attendeeId);

    // This query is slightly more complex as it needs to check the organizationId,
    // which is not directly on the Entry entity. We can achieve this with a subquery
    // or a join, but a subquery is often cleaner in this context.
    // For now, we will assume a simpler model and add this logic later if needed,
    // as it might violate our cross-module join rule if not handled carefully.
    // Let's assume for now that we can query by a field that should exist.
    // A better implementation would involve the EntryCreatedEvent populating an analytics table.

    // For now, let's add a placeholder that we will fix with the analytics module.
    // We will need to add organizationId to the Entry entity for this to work directly.
    long countByOrganizationIdAndSyncTimestampAfter(Long organizationId, Instant timestamp);

    Page<Entry> findBySessionIdIn(List<Long> sessionIds, Pageable pageable);
}
