package com.github.fjbaldon.attendex.backend.repository;

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
}
