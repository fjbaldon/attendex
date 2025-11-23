package com.github.fjbaldon.attendex.platform.capture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface EntryRepository extends PagingAndSortingRepository<Entry, Long>, JpaRepository<Entry, Long> {

    boolean existsByAttendeeIdAndSessionId(Long attendeeId, Long sessionId);

    boolean existsByScanUuid(String scanUuid);

    long countByOrganizationIdAndSyncTimestampAfter(Long organizationId, Instant timestamp);

    Page<Entry> findBySessionIdIn(List<Long> sessionIds, Pageable pageable);

    List<Entry> findByEventIdOrderByScanTimestampDesc(Long eventId);

    long countByEventId(Long eventId);

    @Query("SELECT e.sessionId, COUNT(e) FROM Entry e WHERE e.eventId = :eventId AND e.sessionId IS NOT NULL GROUP BY e.sessionId")
    List<Object[]> countByEventIdGroupBySessionId(@Param("eventId") Long eventId);

    @Query("SELECT e.scannerId, COUNT(e) FROM Entry e WHERE e.eventId = :eventId GROUP BY e.scannerId")
    List<Object[]> countByEventIdGroupByScannerId(@Param("eventId") Long eventId);

    @Query("SELECT MIN(e.scanTimestamp) FROM Entry e WHERE e.eventId = :eventId")
    Optional<Instant> findFirstScanByEventId(@Param("eventId") Long eventId);

    @Query("SELECT MAX(e.scanTimestamp) FROM Entry e WHERE e.eventId = :eventId")
    Optional<Instant> findLastScanByEventId(@Param("eventId") Long eventId);
}
