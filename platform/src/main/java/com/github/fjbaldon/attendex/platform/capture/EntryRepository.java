package com.github.fjbaldon.attendex.platform.capture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface EntryRepository extends PagingAndSortingRepository<Entry, Long>, JpaRepository<Entry, Long> {

    List<Entry> findTop5ByOrganizationIdOrderByScanTimestampDesc(Long organizationId);

    boolean existsByAttendeeIdAndSessionId(Long attendeeId, Long sessionId);

    boolean existsByAttendeeIdAndEventIdAndSessionIdIsNull(Long attendeeId, Long eventId);

    long countByOrganizationIdAndSyncTimestampAfter(Long organizationId, Instant timestamp);

    @Query("SELECT e FROM Entry e WHERE e.sessionId IN :sessionIds " +
            "AND (:query IS NULL OR (" +
            "   e.snapshotFirstName ILIKE CONCAT('%', CAST(:query AS string), '%') OR " +
            "   e.snapshotLastName ILIKE CONCAT('%', CAST(:query AS string), '%') OR " +
            "   e.snapshotIdentity ILIKE CONCAT('%', CAST(:query AS string), '%')" +
            "))")
    Page<Entry> findBySessionIdsAndQuery(
            @Param("sessionIds") List<Long> sessionIds,
            @Param("query") String query,
            Pageable pageable
    );

    long countByEventId(Long eventId);

    @Query("SELECT e.sessionId, COUNT(e) FROM Entry e WHERE e.eventId = :eventId AND e.sessionId IS NOT NULL GROUP BY e.sessionId")
    List<Object[]> countByEventIdGroupBySessionId(@Param("eventId") Long eventId);

    @Query("SELECT e.scannerId, COUNT(e) FROM Entry e WHERE e.eventId = :eventId GROUP BY e.scannerId")
    List<Object[]> countByEventIdGroupByScannerId(@Param("eventId") Long eventId);

    @Query("SELECT MIN(e.scanTimestamp) FROM Entry e WHERE e.eventId = :eventId")
    Optional<Instant> findFirstScanByEventId(@Param("eventId") Long eventId);

    @Query("SELECT MAX(e.scanTimestamp) FROM Entry e WHERE e.eventId = :eventId")
    Optional<Instant> findLastScanByEventId(@Param("eventId") Long eventId);

    @Query("SELECT e FROM Entry e LEFT JOIN com.github.fjbaldon.attendex.platform.event.Session s ON e.sessionId = s.id " +
            "WHERE e.eventId = :eventId " +
            "AND (:intent IS NULL OR s.intent = :intent) " +
            "AND (:attendeeIds IS NULL OR e.attendeeId IN :attendeeIds) " +
            "ORDER BY e.scanTimestamp DESC")
    List<Entry> findEntriesForReport(
            @Param("eventId") Long eventId,
            @Param("intent") String intent,
            @Param("attendeeIds") List<Long> attendeeIds
    );

    @Query(value = """
        SELECT CAST(scan_timestamp AS DATE) as date, COUNT(id) as count
        FROM capture_entry
        WHERE organization_id = :organizationId
        AND scan_timestamp >= :startDate
        GROUP BY date
        ORDER BY date
    """, nativeQuery = true)
    List<DailyEntryCount> findDailyEntriesSince(
            @Param("organizationId") Long organizationId,
            @Param("startDate") Instant startDate
    );

    @Query("SELECT e.scanUuid FROM Entry e WHERE e.scanUuid IN :uuids")
    List<String> findExistingScanUuids(@Param("uuids") List<String> uuids);

    @Modifying
    @Query(nativeQuery = true, value = """
        UPDATE capture_entry e
        SET punctuality = CASE
            WHEN EXTRACT(EPOCH FROM (e.scan_timestamp - :targetTime)) / 60 < -:graceBefore THEN 'EARLY'
            WHEN EXTRACT(EPOCH FROM (e.scan_timestamp - :targetTime)) / 60 > :graceAfter THEN 'LATE'
            ELSE 'PUNCTUAL'
        END
        WHERE e.session_id = :sessionId
    """)
    void recalculatePunctualityForSession(
            @Param("sessionId") Long sessionId,
            @Param("targetTime") Instant targetTime,
            @Param("graceBefore") int graceBefore,
            @Param("graceAfter") int graceAfter
    );

    List<Entry> findByAttendeeId(Long attendeeId);
}
