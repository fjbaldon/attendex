package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.analytics.dto.AttributeBreakdownDto;
import com.github.fjbaldon.attendex.platform.analytics.dto.EventSummaryDto;
import com.github.fjbaldon.attendex.platform.analytics.dto.OrganizationSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AnalyticsFacade {

    private final AttributeBreakdownRepository attributeBreakdownRepository;
    private final OrganizationSummaryRepository organizationSummaryRepository;
    private final EventSummaryRepository eventSummaryRepository;

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
    public List<EventSummaryDto> findRecentEventSummaries(Long organizationId, Pageable pageable) {
        // This would be a proper query in a full implementation
        Iterable<EventSummary> summaries = eventSummaryRepository.findAll();
        return StreamSupport.stream(summaries.spliterator(), false) // Use StreamSupport for Iterable
                .filter(summary -> summary.getOrganizationId().equals(organizationId))
                .limit(pageable.getPageSize())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private OrganizationSummaryDto toDto(OrganizationSummary summary) {
        return new OrganizationSummaryDto(
                summary.getTotalEvents(),
                summary.getTotalAttendees(),
                summary.getTotalScanners()
        );
    }

    private EventSummaryDto toDto(EventSummary summary) {
        return new EventSummaryDto(
                summary.getEventId(),
                summary.getOrganizationId(),
                summary.getRosterCount(),
                summary.getEntryCount(),
                "Event Name Placeholder"
        );
    }
}
