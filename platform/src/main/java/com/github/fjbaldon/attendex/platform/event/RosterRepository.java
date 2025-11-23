package com.github.fjbaldon.attendex.platform.event;

import com.github.fjbaldon.attendex.platform.event.dto.RosterSyncProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

interface RosterRepository extends PagingAndSortingRepository<RosterEntry, RosterEntryId>, CrudRepository<RosterEntry, RosterEntryId> {

    @Query("SELECT re.id.attendeeId FROM RosterEntry re WHERE re.id.eventId = :eventId")
    Page<Long> findAttendeeIdsByEventId(@Param("eventId") Long eventId, Pageable pageable);

    @Query("""
                SELECT new com.github.fjbaldon.attendex.platform.event.dto.RosterSyncProjection(
                    re.id.attendeeId,
                    a.identity,
                    a.firstName,
                    a.lastName
                )
                FROM RosterEntry re
                JOIN com.github.fjbaldon.attendex.platform.attendee.Attendee a ON re.id.attendeeId = a.id
                WHERE re.id.eventId = :eventId
            """)
    Page<RosterSyncProjection> findRosterProjectionsByEventId(@Param("eventId") Long eventId, Pageable pageable);

    @Override
    boolean existsById(@NonNull RosterEntryId id);

    long countByIdEventId(Long eventId);
}
