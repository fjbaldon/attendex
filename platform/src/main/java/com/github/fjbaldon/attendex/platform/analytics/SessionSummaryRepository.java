package com.github.fjbaldon.attendex.platform.analytics;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

interface SessionSummaryRepository extends CrudRepository<SessionSummary, Long> {

    List<SessionSummary> findAllByEventId(Long eventId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        INSERT INTO analytics_session_summary (session_id, event_id, entry_count)
        VALUES (:sessionId, :eventId, 1)
        ON CONFLICT (session_id)
        DO UPDATE SET entry_count = analytics_session_summary.entry_count + 1
    """)
    void incrementCount(@Param("sessionId") Long sessionId, @Param("eventId") Long eventId);
}
