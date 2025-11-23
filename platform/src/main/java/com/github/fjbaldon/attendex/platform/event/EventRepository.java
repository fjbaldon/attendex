package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface EventRepository extends PagingAndSortingRepository<Event, Long>, CrudRepository<Event, Long> {

    @Query(value = "SELECT e FROM Event e LEFT JOIN FETCH e.sessions WHERE e.organizationId = :organizationId AND e.deletedAt IS NULL",
            countQuery = "SELECT COUNT(e) FROM Event e WHERE e.organizationId = :organizationId AND e.deletedAt IS NULL")
    Page<Event> findAllByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.organizationId = :organizationId AND " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Event> searchByOrganizationId(@Param("organizationId") Long organizationId, @Param("query") String query, Pageable pageable);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.sessions WHERE e.id = :id AND e.organizationId = :organizationId")
    Optional<Event> findByIdAndOrganizationId(@Param("id") Long id, @Param("organizationId") Long organizationId);

    Page<Event> findByOrganizationIdAndStartDateAfterOrderByStartDateAsc(Long organizationId, Instant date, Pageable pageable);

    @Override
    @NonNull
    List<Event> findAllById(@NonNull Iterable<Long> ids);
}
