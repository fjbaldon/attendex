package com.github.fjbaldon.attendex.platform.analytics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface EventSummaryRepository extends CrudRepository<EventSummary, Long> {
    List<EventSummary> findByOrganizationId(Long organizationId, Pageable pageable);
}
