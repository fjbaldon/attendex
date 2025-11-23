package com.github.fjbaldon.attendex.platform.analytics;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface AttributeBreakdownRepository extends CrudRepository<AttributeBreakdown, Long> {
    Optional<AttributeBreakdown> findByEventIdAndAttributeNameAndAttributeValue(Long eventId, String name, String value);

    List<AttributeBreakdown> findAllByEventIdAndAttributeName(Long eventId, String attributeName);

    @Modifying
    @Query(nativeQuery = true, value = """
        DELETE FROM analytics_attributebreakdown ab
        USING event_event e
        WHERE ab.event_id = e.id
        AND e.organization_id = :orgId
        AND ab.attribute_name = :name
    """)
    void deleteStatsForAttribute(@Param("orgId") Long orgId, @Param("name") String name);
}

