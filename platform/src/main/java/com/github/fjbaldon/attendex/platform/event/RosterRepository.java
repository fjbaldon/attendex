package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;

interface RosterRepository extends PagingAndSortingRepository<RosterEntry, RosterEntryId>, CrudRepository<RosterEntry, RosterEntryId> {

    @Query("SELECT re.id.attendeeId FROM RosterEntry re WHERE re.id.eventId = :eventId")
    Page<Long> findAttendeeIdsByEventId(Long eventId, Pageable pageable);

    long countByEventId(Long eventId);

    @Override
    boolean existsById(@NonNull RosterEntryId id);
}
