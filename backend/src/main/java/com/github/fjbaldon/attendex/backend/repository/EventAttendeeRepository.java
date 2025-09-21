package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.model.EventAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {
    @Modifying
    @Query("DELETE FROM EventAttendee ea WHERE ea.event.id = :eventId AND ea.attendee.id = :attendeeId")
    void deleteByEventIdAndAttendeeId(Long eventId, Long attendeeId);
}
