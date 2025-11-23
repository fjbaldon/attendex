package com.github.fjbaldon.attendex.platform.analytics;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

interface AttributeBreakdownRepository extends CrudRepository<AttributeBreakdown, Long> {
    Optional<AttributeBreakdown> findByEventIdAndAttributeNameAndAttributeValue(Long eventId, String name, String value);

    List<AttributeBreakdown> findAllByEventIdAndAttributeName(Long eventId, String attributeName);
}

