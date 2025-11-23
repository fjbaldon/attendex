package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

interface EventRepository extends PagingAndSortingRepository<Event, Long>, CrudRepository<Event, Long> {

    Page<Event> findAllByOrganizationId(Long organizationId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.organizationId = :organizationId AND " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Event> searchByOrganizationId(@Param("organizationId") Long organizationId, @Param("query") String query, Pageable pageable);

    Optional<Event> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<Event> findByOrganizationIdAndStartDateAfterOrderByStartDateAsc(Long organizationId, Instant date, Pageable pageable);
}
