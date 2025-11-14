package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

interface SessionRepository extends CrudRepository<Session, Long> {
    @Query("SELECT s FROM Session s WHERE s.id IN :sessionIds")
    List<Session> findSessionsWithEventByIdIn(Set<Long> sessionIds);
}
