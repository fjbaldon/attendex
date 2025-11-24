package com.github.fjbaldon.attendex.platform.attendee;

import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class AttendeeIngestService {

    private final AttendeeRepository attendeeRepository;
    private final OrganizationFacade organizationFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AttendeeDto createAttendee(Long organizationId, CreateAttendeeDto dto) {
        validateIdentityFormat(organizationId, dto.identity());
        try {
            Attendee attendee = Attendee.create(organizationId, dto.identity(), dto.firstName(), dto.lastName(), dto.attributes());
            Attendee saved = attendeeRepository.save(attendee);
            eventPublisher.publishEvent(new AttendeeCreatedEvent(saved.getId(), saved.getOrganizationId()));
            return toDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("An attendee with this identity already exists in your organization.");
        }
    }

    @Transactional
    public AttendeeDto updateAttendee(Long organizationId, Long attendeeId, UpdateAttendeeDto dto) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .filter(a -> a.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new EntityNotFoundException("Attendee not found"));
        attendee.update(dto.firstName(), dto.lastName(), dto.attributes());
        return toDto(attendee);
    }

    @Transactional
    public void deleteAttendee(Long organizationId, Long attendeeId) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .filter(a -> a.getOrganizationId().equals(organizationId))
                .filter(Attendee::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Attendee not found"));

        attendee.markAsDeleted();
        attendeeRepository.save(attendee);

        eventPublisher.publishEvent(new AttendeeDeletedEvent(organizationId));
    }

    @Transactional
    public void deleteAttendees(Long organizationId, List<Long> attendeeIds) {
        if (attendeeIds == null || attendeeIds.isEmpty()) {
            return;
        }

        int deletedCount = attendeeRepository.softDeleteBatch(organizationId, attendeeIds);

        if (deletedCount > 0) {
            eventPublisher.publishEvent(new AttendeeBatchDeletedEvent(organizationId, deletedCount));
        }
    }

    private void validateIdentityFormat(Long organizationId, String identity) {
        OrganizationDto org = organizationFacade.findOrganizationById(organizationId);
        String regex = org.identityFormatRegex();
        if (regex != null && !regex.isBlank() && !identity.matches(regex)) {
            throw new IllegalArgumentException("Identity '" + identity + "' does not match the required format.");
        }
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
