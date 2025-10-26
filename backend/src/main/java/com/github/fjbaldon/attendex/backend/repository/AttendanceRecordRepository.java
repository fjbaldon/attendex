// backend/src/main/java/com/github/fjbaldon/attendex/backend/repository/AttendanceRecordRepository.java
package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.dto.AnalyticsBreakdownDto;
import com.github.fjbaldon.attendex.backend.dto.DailyActivityDto;
import com.github.fjbaldon.attendex.backend.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByEventId(Long eventId);

    @Query("SELECT COUNT(DISTINCT ar.attendee.id) FROM AttendanceRecord ar WHERE ar.event.id = :eventId")
    long countDistinctAttendeesByEventId(Long eventId);

    @Query("SELECT CAST(ar.checkInTimestamp AS LocalDate), COUNT(ar.id) FROM AttendanceRecord ar WHERE ar.event.id = :eventId GROUP BY CAST(ar.checkInTimestamp AS LocalDate) ORDER BY CAST(ar.checkInTimestamp AS LocalDate)")
    List<Object[]> countCheckInsByDate(Long eventId);

    @Query(value =
            "SELECT " +
                    "    att.custom_fields ->> :customFieldKey AS groupName, " +
                    "    COUNT(DISTINCT ar.attendee_id) AS count " +
                    "FROM " +
                    "    attendance_record ar " +
                    "JOIN " +
                    "    attendee att ON ar.attendee_id = att.id " +
                    "WHERE " +
                    "    ar.event_id = :eventId " +
                    "    AND att.custom_fields ->> :customFieldKey IS NOT NULL " +
                    "GROUP BY " +
                    "    groupName " +
                    "ORDER BY " +
                    "    count DESC, groupName ASC",
            nativeQuery = true)
    List<AnalyticsBreakdownDto> countAttendeesByCustomField(Long eventId, String customFieldKey);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar JOIN ar.event e WHERE e.organization.id = :organizationId AND ar.checkInTimestamp > :timestamp")
    long countByOrganizationIdAndCheckInTimestampAfter(Long organizationId, Instant timestamp);

    @Query(value =
            "SELECT " +
                    "    CAST(ar.check_in_timestamp AS DATE) as date, " +
                    "    COUNT(ar.id) as count " +
                    "FROM " +
                    "    attendance_record ar " +
                    "JOIN " +
                    "    event e ON ar.event_id = e.id " +
                    "WHERE " +
                    "    e.organization_id = :organizationId AND ar.check_in_timestamp >= :startDate " +
                    "GROUP BY " +
                    "    date " +
                    "ORDER BY " +
                    "    date ASC",
            nativeQuery = true)
    List<DailyActivityDto> findDailyActivitySince(Long organizationId, Instant startDate);
}
