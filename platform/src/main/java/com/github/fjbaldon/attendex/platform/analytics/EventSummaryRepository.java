package com.github.fjbaldon.attendex.platform.analytics;

import org.springframework.data.repository.CrudRepository;

interface EventSummaryRepository extends CrudRepository<EventSummary, Long> {
}
