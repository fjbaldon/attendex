package com.github.fjbaldon.attendex.platform.analytics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface EventSummaryRepository extends CrudRepository<EventSummary, Long> {
    List<EventSummary> findByOrganizationId(Long organizationId, Pageable pageable);

    @Modifying
    @Query("UPDATE EventSummary e SET e.entryCount = e.entryCount + 1 WHERE e.eventId = :eventId")
    void incrementEntryCount(@Param("eventId") Long eventId);

    @Modifying
    @Query("UPDATE EventSummary e SET e.rosterCount = e.rosterCount + 1 WHERE e.eventId = :eventId")
    void incrementRosterCount(@Param("eventId") Long eventId);
}
