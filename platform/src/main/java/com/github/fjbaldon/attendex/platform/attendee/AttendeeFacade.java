package com.github.fjbaldon.attendex.platform.attendee;

import com.github.fjbaldon.attendex.platform.attendee.dto.AttendeeDto;
import com.github.fjbaldon.attendex.platform.attendee.dto.CreateAttendeeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class AttendeeFacade {

    private final AttendeeRepository attendeeRepository;

    @Transactional
    public AttendeeDto createAttendee(Long organizationId, CreateAttendeeDto dto) {
        Assert.isTrue(!attendeeRepository.existsByOrganizationIdAndIdentity(organizationId, dto.identity()),
                "An attendee with this identity already exists in your organization.");

        Attendee attendee = Attendee.create(
                organizationId,
                dto.identity(),
                dto.firstName(),
                dto.lastName(),
                dto.attributes()
        );

        Attendee saved = attendeeRepository.save(attendee);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<AttendeeDto> findAttendees(Long organizationId, Pageable pageable) {
        return attendeeRepository.findAllByOrganizationId(organizationId, pageable)
                .map(this::toDto);
    }

    private AttendeeDto toDto(Attendee attendee) {
        return new AttendeeDto(
                attendee.getId(),
                attendee.getIdentity(),
                attendee.getFirstName(),
                attendee.getLastName(),
                attendee.getAttributes(),
                attendee.getCreatedAt()
        );
    }
}
