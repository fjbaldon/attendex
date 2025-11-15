package com.github.fjbaldon.attendex.platform.dashboard;

import com.github.fjbaldon.attendex.platform.analytics.AnalyticsFacade;
import com.github.fjbaldon.attendex.platform.analytics.dto.OrganizationSummaryDto;
import com.github.fjbaldon.attendex.platform.dashboard.dto.DashboardDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardFacade {

    private final EventFacade eventFacade;
    private final AnalyticsFacade analyticsFacade;

    @Transactional(readOnly = true)
    public DashboardDto getOrganizerDashboard(Long organizationId) {
        OrganizationSummaryDto orgSummary = analyticsFacade.getOrganizationSummary(organizationId);

        var stats = new DashboardDto.DashboardStatsDto(
                orgSummary.totalEvents(),
                orgSummary.totalAttendees(),
                orgSummary.totalScanners(),
                0
        );

        var upcomingEvents = eventFacade.findUpcomingEvents(organizationId, Pageable.ofSize(5)).stream()
                .map(e -> new DashboardDto.UpcomingEventDto(e.id(), e.name(), e.startDate()))
                .collect(Collectors.toList());

        var recentEvents = analyticsFacade.findRecentEventSummaries(organizationId, Pageable.ofSize(5)).stream()
                .map(summary -> new DashboardDto.RecentEventStatsDto(
                        summary.eventId(),
                        summary.eventName(),
                        summary.rosterCount(),
                        summary.entryCount()
                ))
                .collect(Collectors.toList());

        return new DashboardDto(stats, upcomingEvents, recentEvents);
    }
}
