package com.github.fjbaldon.attendex.platform.dashboard;

import com.github.fjbaldon.attendex.platform.analytics.AnalyticsFacade;
import com.github.fjbaldon.attendex.platform.analytics.OrganizationSummaryDto;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.capture.DashboardDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.DailyRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardFacade {

    private final EventFacade eventFacade;
    private final AnalyticsFacade analyticsFacade;
    private final CaptureFacade captureFacade;
    private final OrganizationFacade organizationFacade;

    // --- ORGANIZER DASHBOARD ---

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

    // --- ADMIN (STEWARD) DASHBOARD ---

    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboard() {
        var stats = new AdminDashboardDto.AdminDashboardStatsDto(
                analyticsFacade.countTotalOrganizations(),
                analyticsFacade.countOrganizationsByLifecycle("ACTIVE"),
                analyticsFacade.countOrganizationsBySubscriptionType("TRIAL"),
                analyticsFacade.countOrganizationsByLifecycle("SUSPENDED")
        );

        var expiring = organizationFacade.findExpiringSubscriptions(Instant.now().plus(30, ChronoUnit.DAYS), PageRequest.of(0, 5))
                .stream()
                .map(org -> new AdminDashboardDto.OrganizationSummaryDto(org.id(), org.name(), org.subscriptionExpiresAt()))
                .collect(Collectors.toList());

        var recent = organizationFacade.findRecentRegistrations(PageRequest.of(0, 5))
                .stream()
                .map(org -> new AdminDashboardDto.OrganizationSummaryDto(org.id(), org.name(), org.createdAt()))
                .collect(Collectors.toList());

        var attention = organizationFacade.findOrganizationsByLifecycle(List.of("INACTIVE", "SUSPENDED"), PageRequest.of(0, 5))
                .stream()
                .map(org -> new AdminDashboardDto.OrganizationSummaryDto(org.id(), org.name(), null))
                .collect(Collectors.toList());

        return new AdminDashboardDto(stats, expiring, recent, attention);
    }

    @Transactional(readOnly = true)
    public List<DailyRegistration> getRegistrationActivity(String range) {
        long days = switch (range) {
            case "30d" -> 30;
            case "7d" -> 7;
            default -> 90;
        };
        Instant startDate = Instant.now().minus(days, ChronoUnit.DAYS);
        return organizationFacade.getDailyRegistrations(startDate);
    }
}
