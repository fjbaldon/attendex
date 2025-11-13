package com.github.fjbaldon.attendex.platform.attendee;

import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
import com.github.fjbaldon.attendex.platform.attendee.dto.CreateAttendeeDto;
import com.github.fjbaldon.attendex.platform.identity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendees")
@RequiredArgsConstructor
class AttendeeController {

    private final AttendeeFacade attendeeFacade;

    @PostMapping
    public ResponseEntity<AttendeeDto> createAttendee(
            @Valid @RequestBody CreateAttendeeDto request,
            @AuthenticationPrincipal CustomUserDetails user) {

        AttendeeDto attendee = attendeeFacade.createAttendee(user.getOrganizationId(), request);
        return new ResponseEntity<>(attendee, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<AttendeeDto> getAttendees(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails user) {

        return attendeeFacade.findAttendees(user.getOrganizationId(), pageable);
    }
}
