package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant; // Import Instant
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

    List<Event> findAllByOrganizationId(Long organizationId);

    boolean existsByOrganizerId(Long organizerId);

    long countByOrganizationId(Long organizationId);
}
