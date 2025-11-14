package com.github.fjbaldon.attendex.platform.analytics;

import com.github.fjbaldon.attendex.platform.analytics.dto.AttributeBreakdownDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsFacade {

    private final AttributeBreakdownRepository repository;

    @Transactional(readOnly = true)
    public AttributeBreakdownDto getAttributeBreakdown(Long eventId, String attributeName) {
        List<AttributeBreakdown> results = repository.findAllByEventIdAndAttributeName(eventId, attributeName);
        var items = results.stream()
                .map(item -> new AttributeBreakdownDto.BreakdownItem(item.getAttributeValue(), item.getAttendeeCount()))
                .collect(Collectors.toList());
        return new AttributeBreakdownDto(attributeName, items);
    }
}
