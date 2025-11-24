package com.github.fjbaldon.attendex.platform.analytics;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

interface ScannerSummaryRepository extends CrudRepository<ScannerSummary, Long> {

    List<ScannerSummary> findAllByEventId(Long eventId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        INSERT INTO analytics_scanner_summary (scanner_id, event_id, entry_count)
        VALUES (:scannerId, :eventId, 1)
        ON CONFLICT (scanner_id)
        DO UPDATE SET entry_count = analytics_scanner_summary.entry_count + 1
    """)
    void incrementCount(@Param("scannerId") Long scannerId, @Param("eventId") Long eventId);
}
