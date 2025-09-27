package com.github.fjbaldon.attendex.backend.repository;

import com.github.fjbaldon.attendex.backend.dto.AnalyticsBreakdownDto;
import com.github.fjbaldon.attendex.backend.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

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
}
