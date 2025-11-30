package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

interface RosterRepository extends PagingAndSortingRepository<RosterEntry, RosterEntryId>, CrudRepository<RosterEntry, RosterEntryId> {

    @Query("SELECT re.id.attendeeId FROM RosterEntry re WHERE re.id.eventId = :eventId")
    Page<Long> findAttendeeIdsByEventId(@Param("eventId") Long eventId, Pageable pageable);

    @Query("SELECT re.id.attendeeId FROM RosterEntry re WHERE re.id.eventId = :eventId AND re.id.attendeeId IN :attendeeIds")
    Page<Long> findAttendeeIdsByEventIdAndAttendeeIdIn(@Param("eventId") Long eventId, @Param("attendeeIds") List<Long> attendeeIds, Pageable pageable);

    @Query("""
        SELECT re.id.attendeeId
        FROM RosterEntry re
        JOIN com.github.fjbaldon.attendex.platform.attendee.Attendee a ON re.id.attendeeId = a.id
        WHERE re.id.eventId = :eventId
        AND (
            LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(a.identity) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<Long> searchAttendeeIdsByEventId(@Param("eventId") Long eventId, @Param("query") String query, Pageable pageable);

    @Query("""
        SELECT re.id.attendeeId
        FROM RosterEntry re
        JOIN com.github.fjbaldon.attendex.platform.attendee.Attendee a ON re.id.attendeeId = a.id
        WHERE re.id.eventId = :eventId
        AND re.id.attendeeId IN :attendeeIds
        AND (
            LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(a.identity) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<Long> searchAttendeeIdsByEventIdAndAttendeeIdIn(@Param("eventId") Long eventId, @Param("attendeeIds") List<Long> attendeeIds, @Param("query") String query, Pageable pageable);

    @Query("""
                SELECT new com.github.fjbaldon.attendex.platform.event.RosterSyncProjection(
                    re.id.attendeeId,
                    a.identity,
                    a.firstName,
                    a.lastName
                )
                FROM RosterEntry re
                JOIN com.github.fjbaldon.attendex.platform.attendee.Attendee a ON re.id.attendeeId = a.id
                WHERE re.id.eventId = :eventId
                AND a.deletedAt IS NULL
            """)
    Page<RosterSyncProjection> findRosterProjectionsByEventId(@Param("eventId") Long eventId, Pageable pageable);

    @Override
    boolean existsById(@NonNull RosterEntryId id);

    long countByIdEventId(Long eventId);

    @Query("SELECT re.id.attendeeId FROM RosterEntry re WHERE re.id.eventId = :eventId")
    Set<Long> findAllAttendeeIdsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT re.event FROM RosterEntry re WHERE re.id.attendeeId = :attendeeId AND re.event.deletedAt IS NULL ORDER BY re.event.startDate DESC")
    List<Event> findEventsByAttendeeId(@Param("attendeeId") Long attendeeId);
}
