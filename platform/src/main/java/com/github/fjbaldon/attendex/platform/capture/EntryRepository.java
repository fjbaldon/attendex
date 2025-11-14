package com.github.fjbaldon.attendex.platform.capture;

import org.springframework.data.repository.CrudRepository;

interface EntryRepository extends CrudRepository<Entry, Long> {
    boolean existsBySessionIdAndAttendeeId(Long sessionId, Long attendeeId);
}
