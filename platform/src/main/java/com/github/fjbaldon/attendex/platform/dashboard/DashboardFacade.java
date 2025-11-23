package com.github.fjbaldon.attendex.platform.dashboard;

import com.github.fjbaldon.attendex.platform.analytics.AnalyticsFacade;
import com.github.fjbaldon.attendex.platform.analytics.dto.OrganizationSummaryDto;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.dashboard.dto.DashboardDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardFacade {

    private final EventFacade eventFacade;
    private final AnalyticsFacade analyticsFacade;
    private final CaptureFacade captureFacade;

    @Transactional(readOnly = true)
    public DashboardDto getOrganizerDashboard(Long organizationId) {
        OrganizationSummaryDto orgSummary = analyticsFacade.getOrganizationSummary(organizationId);
        long liveEntries = captureFacade.countEntriesSince(organizationId, Instant.now().minus(1, ChronoUnit.HOURS));

        var stats = new DashboardDto.DashboardStatsDto(
                orgSummary.totalEvents(),
                orgSummary.totalAttendees(),
                orgSummary.totalScanners(),
                liveEntries
        );

        var upcomingEvents = eventFacade.findUpcomingEvents(organizationId, PageRequest.of(0, 5, Sort.by("startDate").ascending())).stream()
                .map(e -> new DashboardDto.UpcomingEventDto(e.id(), e.name(), e.startDate()))
                .collect(Collectors.toList());

        var recentEvents = analyticsFacade.findRecentEventSummaries(organizationId, PageRequest.of(0, 5)).stream()
                .map(summary -> new DashboardDto.RecentEventStatsDto(
                        summary.eventId(),
                        summary.eventName(),
                        summary.rosterCount(),
                        summary.entryCount()
                ))
                .collect(Collectors.toList());

        var recentActivity = captureFacade.getRecentActivity(organizationId);

        return new DashboardDto(stats, upcomingEvents, recentEvents, recentActivity);
    }
}
