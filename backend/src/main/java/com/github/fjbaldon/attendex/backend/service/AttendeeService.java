package com.github.fjbaldon.attendex.backend.service;

import com.github.fjbaldon.attendex.backend.dto.AttendeeImportResponse;
import com.github.fjbaldon.attendex.backend.dto.AttendeeRequest;
import com.github.fjbaldon.attendex.backend.dto.AttendeeResponse;
import com.github.fjbaldon.attendex.backend.model.Attendee;
import com.github.fjbaldon.attendex.backend.repository.AttendeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;

    @Transactional
    public AttendeeResponse createAttendee(AttendeeRequest request) {
        Attendee attendee = new Attendee();
        attendee.setSchoolIdNumber(request.getSchoolIdNumber());
        attendee.setFirstName(request.getFirstName());
        attendee.setMiddleInitial(request.getMiddleInitial());
        attendee.setLastName(request.getLastName());
        attendee.setCourse(request.getCourse());
        attendee.setYearLevel(request.getYearLevel());

        Attendee savedAttendee = attendeeRepository.save(attendee);
        return toAttendeeResponse(savedAttendee);
    }

    @Transactional(readOnly = true)
    public Page<AttendeeResponse> getAllAttendees(Pageable pageable) {
        return attendeeRepository.findAll(pageable)
                .map(this::toAttendeeResponse);
    }

    @Transactional
    public AttendeeResponse updateAttendee(Long attendeeId, AttendeeRequest request) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new EntityNotFoundException("Attendee with ID " + attendeeId + " not found"));

        attendee.setSchoolIdNumber(request.getSchoolIdNumber());
        attendee.setFirstName(request.getFirstName());
        attendee.setMiddleInitial(request.getMiddleInitial());
        attendee.setLastName(request.getLastName());
        attendee.setCourse(request.getCourse());
        attendee.setYearLevel(request.getYearLevel());

        Attendee updatedAttendee = attendeeRepository.save(attendee);
        return toAttendeeResponse(updatedAttendee);
    }

    @Transactional
    public void deleteAttendee(Long attendeeId) {
        if (!attendeeRepository.existsById(attendeeId)) {
            throw new EntityNotFoundException("Attendee with ID " + attendeeId + " not found");
        }
        attendeeRepository.deleteById(attendeeId);
    }

    @Transactional
    public AttendeeImportResponse importAttendeesFromCsv(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty");
        }

        List<Attendee> attendeesToSave = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get();
            CSVParser csvParser = CSVParser.parse(fileReader, csvFormat);

            for (CSVRecord csvRecord : csvParser) {
                try {
                    AttendeeRequest request = new AttendeeRequest();
                    request.setSchoolIdNumber(csvRecord.get("schoolIdNumber"));
                    request.setFirstName(csvRecord.get("firstName"));
                    request.setLastName(csvRecord.get("lastName"));
                    if (csvRecord.isMapped("middleInitial") && !csvRecord.get("middleInitial").isEmpty()) {
                        request.setMiddleInitial(csvRecord.get("middleInitial").charAt(0));
                    }
                    if (csvRecord.isMapped("course")) {
                        request.setCourse(csvRecord.get("course"));
                    }
                    if (csvRecord.isMapped("yearLevel") && !csvRecord.get("yearLevel").isEmpty()) {
                        request.setYearLevel(Integer.parseInt(csvRecord.get("yearLevel")));
                    }

                    Attendee attendee = new Attendee();
                    attendee.setSchoolIdNumber(request.getSchoolIdNumber());
                    attendee.setFirstName(request.getFirstName());
                    attendee.setMiddleInitial(request.getMiddleInitial());
                    attendee.setLastName(request.getLastName());
                    attendee.setCourse(request.getCourse());
                    attendee.setYearLevel(request.getYearLevel());

                    attendeesToSave.add(attendee);
                    successfulImports++;
                } catch (Exception e) {
                    failedImports++;
                    errors.add("Error on row " + csvRecord.getRecordNumber() + ": " + e.getMessage());
                }
            }

            attendeeRepository.saveAll(attendeesToSave);

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }

        return AttendeeImportResponse.builder()
                .message("CSV processing complete.")
                .successfulImports(successfulImports)
                .failedImports(failedImports)
                .errors(errors)
                .build();
    }

    private AttendeeResponse toAttendeeResponse(Attendee attendee) {
        return AttendeeResponse.builder()
                .id(attendee.getId())
                .schoolIdNumber(attendee.getSchoolIdNumber())
                .firstName(attendee.getFirstName())
                .middleInitial(attendee.getMiddleInitial())
                .lastName(attendee.getLastName())
                .course(attendee.getCourse())
                .yearLevel(attendee.getYearLevel())
                .build();
    }
}
