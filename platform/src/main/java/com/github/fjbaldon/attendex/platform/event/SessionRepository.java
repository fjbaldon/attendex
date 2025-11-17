package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

interface SessionRepository extends CrudRepository<Session, Long> {

    @Query("SELECT s FROM Session s JOIN FETCH s.event WHERE s.id IN :sessionIds")
    List<Session> findSessionsWithEventByIdIn(Set<Long> sessionIds);

    @Query("SELECT s.id FROM Session s WHERE s.event.id = :eventId AND s.intent = :intent")
    List<Long> findSessionIdsByEventIdAndIntent(Long eventId, String intent);
}
