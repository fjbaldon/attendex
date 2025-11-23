package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.analytics.dto.AttributeBreakdownDto;
import com.github.fjbaldon.attendex.platform.analytics.dto.EventStatsDto;
import com.github.fjbaldon.attendex.platform.analytics.dto.EventSummaryDto;
import com.github.fjbaldon.attendex.platform.analytics.dto.OrganizationSummaryDto;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsFacade {

    private final AttributeBreakdownRepository attributeBreakdownRepository;
    private final OrganizationSummaryRepository organizationSummaryRepository;
    private final EventSummaryRepository eventSummaryRepository;
    private final CaptureFacade captureFacade;
    private final OrganizationFacade organizationFacade;

    @Transactional(readOnly = true)
    public AttributeBreakdownDto getAttributeBreakdown(Long eventId, String attributeName) {
        List<AttributeBreakdown> results = attributeBreakdownRepository.findAllByEventIdAndAttributeName(eventId, attributeName);
        var items = results.stream()
                .map(item -> new AttributeBreakdownDto.BreakdownItem(item.getAttributeValue(), item.getAttendeeCount()))
                .collect(Collectors.toList());
        return new AttributeBreakdownDto(attributeName, items);
    }

    @Transactional(readOnly = true)
    public OrganizationSummaryDto getOrganizationSummary(Long organizationId) {
        return organizationSummaryRepository.findById(organizationId)
                .map(this::toDto)
                .orElse(new OrganizationSummaryDto(0, 0, 0));
    }

    @Transactional(readOnly = true)
    public Optional<EventSummaryDto> findEventSummary(Long eventId) {
        return eventSummaryRepository.findById(eventId).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public long countTotalOrganizations() {
        return organizationSummaryRepository.count();
    }

    @Transactional(readOnly = true)
    public long countOrganizationsByLifecycle(String lifecycle) {
        return organizationFacade.countByLifecycle(lifecycle);
    }

    @Transactional(readOnly = true)
    public long countOrganizationsBySubscriptionType(String subscriptionType) {
        return organizationFacade.countBySubscriptionType(subscriptionType);
    }

    @Transactional(readOnly = true)
    public List<EventSummaryDto> findRecentEventSummaries(Long organizationId, Pageable pageable) {
        return eventSummaryRepository.findByOrganizationId(organizationId, pageable).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventStatsDto getEventStats(Long organizationId, Long eventId) {
        return captureFacade.getEventStats(organizationId, eventId);
    }

    private OrganizationSummaryDto toDto(OrganizationSummary summary) {
        return new OrganizationSummaryDto(
                summary.getTotalEvents(),
                summary.getTotalAttendees(),
                summary.getTotalScanners()
        );
    }

    private EventSummaryDto toDto(EventSummary summary) {
        double attendanceRate = (summary.getRosterCount() > 0)
                ? ((double) summary.getEntryCount() / summary.getRosterCount()) * 100.0
                : 0.0;

        return new EventSummaryDto(
                summary.getEventId(),
                summary.getOrganizationId(),
                summary.getRosterCount(),
                summary.getEntryCount(),
                summary.getEventName(),
                attendanceRate
        );
    }
}
