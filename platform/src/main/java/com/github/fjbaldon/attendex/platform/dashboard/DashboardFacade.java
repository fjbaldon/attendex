package com.github.fjbaldon.attendex.platform.dashboard;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.dashboard.dto.DashboardDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardFacade {

    private final OrganizationFacade organizationFacade;
    private final AttendeeFacade attendeeFacade;
    private final EventFacade eventFacade;
    private final CaptureFacade captureFacade;

    @Transactional(readOnly = true)
    public DashboardDto getOrganizerDashboard(Long organizationId) {
        long totalEvents = eventFacade.countEvents(organizationId);
        long totalAttendees = attendeeFacade.countAttendees(organizationId);
        long totalScanners = organizationFacade.countScanners(organizationId);
        long liveEntries = captureFacade.countEntriesSince(organizationId, Instant.now().minus(1, ChronoUnit.HOURS));

        var stats = new DashboardDto.DashboardStatsDto(totalEvents, totalAttendees, totalScanners, liveEntries);

        var upcomingEvents = eventFacade.findUpcomingEvents(organizationId, Pageable.ofSize(5)).stream()
                .map(e -> new DashboardDto.UpcomingEventDto(e.id(), e.name(), e.startDate()))
                .collect(Collectors.toList());

        var recentEvents = eventFacade.findRecentEvents(organizationId, Pageable.ofSize(5)).stream()
                .map(e -> {
                    long rosterCount = eventFacade.countRosterByEventId(e.id());
                    long entryCount = captureFacade.countEntriesByEventId(e.id());
                    return new DashboardDto.RecentEventStatsDto(e.id(), e.name(), rosterCount, entryCount);
                })
                .collect(Collectors.toList());


        return new DashboardDto(stats, upcomingEvents, recentEvents);
    }
}
