package com.github.fjbaldon.attendex.platform.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

interface EventRepository extends PagingAndSortingRepository<Event, Long>, CrudRepository<Event, Long> {

    Page<Event> findAllByOrganizationId(Long organizationId, Pageable pageable);

    Optional<Event> findByIdAndOrganizationId(Long id, Long organizationId);
}
