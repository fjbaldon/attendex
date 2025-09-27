package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.organization.id = :organizationId AND :date BETWEEN e.startDate AND e.endDate")
    List<Event> findActiveEventsByOrganizationIdAndDate(Long organizationId, LocalDate date);

    List<Event> findAllByOrganizationId(Long organizationId);

    boolean existsByOrganizerId(Long organizerId);
}
