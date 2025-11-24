package com.github.fjbaldon.attendex.platform.analytics;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

interface OrganizationSummaryRepository extends CrudRepository<OrganizationSummary, Long> {

    // FIX: Atomic insert to prevent race conditions during CSV import
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        INSERT INTO analytics_organization_summary (organization_id, total_events, total_attendees, total_scanners)
        VALUES (:id, 0, 0, 0)
        ON CONFLICT (organization_id) DO NOTHING
    """)
    void insertIfNotExists(@Param("id") Long id);

    @Modifying
    @Query("UPDATE OrganizationSummary o SET o.totalEvents = o.totalEvents + 1 WHERE o.organizationId = :id")
    void incrementEventCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE OrganizationSummary o SET o.totalAttendees = o.totalAttendees + 1 WHERE o.organizationId = :id")
    void incrementAttendeeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE OrganizationSummary o SET o.totalScanners = o.totalScanners + 1 WHERE o.organizationId = :id")
    void incrementScannerCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE OrganizationSummary o SET o.totalAttendees = CASE WHEN o.totalAttendees > 0 THEN o.totalAttendees - 1 ELSE 0 END WHERE o.organizationId = :id")
    void decrementAttendeeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE OrganizationSummary o SET o.totalAttendees = CASE WHEN o.totalAttendees >= :count THEN o.totalAttendees - :count ELSE 0 END WHERE o.organizationId = :id")
    void decrementAttendeeCountBy(@Param("id") Long id, @Param("count") int count);
}
