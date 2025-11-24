package com.github.fjbaldon.attendex.platform.attendee;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
class AttendeeQueryService {

    private final AttendeeRepository attendeeRepository;

    @Transactional(readOnly = true)
    public Page<AttendeeDto> findAttendees(Long organizationId, String query, Map<String, String> attributeFilters, Pageable pageable) {
        Specification<Attendee> spec = AttendeeSpecification.withFilters(organizationId, query, attributeFilters);
        return attendeeRepository.findAll(spec, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<AttendeeDto> findAttendeeById(Long attendeeId, Long organizationId) {
        return attendeeRepository.findById(attendeeId)
                .filter(attendee -> attendee.getOrganizationId().equals(organizationId))
                .filter(Attendee::isActive)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<AttendeeDto> findAttendeesByIds(List<Long> attendeeIds) {
        Iterable<Attendee> attendees = attendeeRepository.findAllById(attendeeIds);
        return StreamSupport.stream(attendees.spliterator(), false).map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> findAttendeeIdsByFilters(Long organizationId, Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }
        List<Long> matchingIds = null;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            List<Long> idsForThisFilter = attendeeRepository.findIdsByAttributeValue(organizationId, entry.getKey(), entry.getValue());
            if (matchingIds == null) {
                matchingIds = idsForThisFilter;
            } else {
                matchingIds.retainAll(idsForThisFilter);
            }
            if (matchingIds.isEmpty()) {
                return matchingIds;
            }
        }
        return matchingIds;
    }

    @Transactional(readOnly = true)
    public List<Long> findIdsByCriteria(Long organizationId, String query, Map<String, String> attributeFilters) {
        Specification<Attendee> spec = AttendeeSpecification.withFilters(organizationId, query, attributeFilters);

        return attendeeRepository.findAll(spec)
                .stream()
                .map(Attendee::getId)
                .collect(Collectors.toList());
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
