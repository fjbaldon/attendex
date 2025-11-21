package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface SessionRepository extends CrudRepository<Session, Long> {

    @Query("SELECT s.id FROM Session s WHERE s.event.id = :eventId AND s.intent = :intent")
    List<Long> findSessionIdsByEventIdAndIntent(Long eventId, String intent);
}
