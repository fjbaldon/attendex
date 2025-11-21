package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

interface SessionRepository extends CrudRepository<Session, Long> {

    @Query("SELECT s FROM Session s JOIN FETCH s.event WHERE s.id IN :sessionIds")
    List<Session> findSessionsWithEventByIdIn(Set<Long> sessionIds);

    @Query("SELECT s.id FROM Session s WHERE s.event.id = :eventId AND s.intent = :intent")
    List<Long> findSessionIdsByEventIdAndIntent(Long eventId, String intent);

    @Query(value = """
                SELECT s.*
                FROM event_session s
                JOIN event_event e ON s.event_id = e.id
                WHERE e.id = :eventId
                AND :timestamp BETWEEN
                    (s.target_time - (e.grace_minutes_before * interval '1 minute'))
                    AND
                    (s.target_time + (e.grace_minutes_after * interval '1 minute'))
                ORDER BY s.target_time
                LIMIT 1
            """, nativeQuery = true)
    Optional<Session> findActiveSession(@Param("eventId") Long eventId, @Param("timestamp") Instant timestamp);
}
