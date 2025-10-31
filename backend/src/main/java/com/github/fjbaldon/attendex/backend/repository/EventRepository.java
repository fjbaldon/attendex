package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT DISTINCT e FROM Event e JOIN e.timeSlots ts WHERE e.organization.id = :organizationId AND " +
            "(ts.startTime <= :dayEnd AND ts.endTime >= :dayStart)")
    List<Event> findEventsForDay(
            @Param("organizationId") Long organizationId,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd
    );

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.timeSlots WHERE e.organization.id = :organizationId")
    List<Event> findAllByOrganizationIdWithTimeSlots(@Param("organizationId") Long organizationId);

    List<Event> findByOrganizationIdAndStartDateAfterOrderByStartDateAsc(Long organizationId, Instant date, Pageable pageable);

    List<Event> findByOrganizationIdAndEndDateBeforeOrderByEndDateDesc(Long organizationId, Instant date, Pageable pageable);

    boolean existsByOrganizerId(Long organizerId);

    long countByOrganizationId(Long organizationId);
}
