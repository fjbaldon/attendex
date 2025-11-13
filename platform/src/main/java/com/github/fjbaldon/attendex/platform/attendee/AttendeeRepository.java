package com.github.fjbaldon.attendex.platform.attendee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface AttendeeRepository extends PagingAndSortingRepository<Attendee, Long>, CrudRepository<Attendee, Long> {

    Page<Attendee> findAllByOrganizationId(Long organizationId, Pageable pageable);

    boolean existsByOrganizationIdAndIdentity(Long organizationId, String identity);
}
