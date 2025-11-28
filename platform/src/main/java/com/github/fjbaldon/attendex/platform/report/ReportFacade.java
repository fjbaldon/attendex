package com.github.fjbaldon.attendex.platform.report;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.attendee.AttributeDto;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.capture.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.event.EventDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.event.SessionDto;
import com.github.fjbaldon.attendex.platform.notification.EmailService;
import com.github.fjbaldon.attendex.platform.organization.OrganizationDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportFacade {

    private final CaptureFacade captureFacade;
    private final EventFacade eventFacade;
    private final OrganizationFacade organizationFacade;
    private final AttendeeFacade attendeeFacade;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    // Cache logo to prevent disk IO on every request
    private static String cachedBase64Logo = null;

    private String getBase64Logo() {
        if (cachedBase64Logo != null) return cachedBase64Logo;
        try {
            ClassPathResource imageResource = new ClassPathResource("logo.png");
            byte[] imageBytes = imageResource.getInputStream().readAllBytes();
            cachedBase64Logo = java.util.Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.warn("Could not load logo.png", e);
            return "";
        }
        return cachedBase64Logo;
    }

    @Async
    @Transactional(readOnly = true)
    public void generateAndEmailReportPdf(Long organizationId, Long eventId, Long sessionId, String type, Map<String, String> attributes, String userEmail) {
        try {
            byte[] pdfBytes = generateReportPdf(organizationId, eventId, sessionId, type, attributes);
            String subject = "Attendance Report: " + eventId;
            String body = """
                    <p>Hello,</p>
                    <p>The attendance report you requested is attached.</p>
                    <p>This report was generated automatically by AttendEx.</p>
                    """;
            String filename = "Attendance_Report_" + eventId + ".pdf";
            emailService.sendReportEmail(userEmail, subject, body, filename, pdfBytes);
        } catch (Exception e) {
            log.error("Failed to generate or email report for Event {}", eventId, e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateReportPdf(Long organizationId, Long eventId, Long sessionId, String type, Map<String, String> attributes) {
        // 1. Fetch Data
        List<Long> attendeeIds = attendeeFacade.findAttendeeIdsByFilters(organizationId, attributes);

        // FIX: Handle "All" type correctly by converting to null
        String intentParam = (type == null || type.equalsIgnoreCase("All")) ? null : type;

        // Pass sessionId (can be null) to filter entries
        List<EntryDetailsDto> entries = captureFacade.findFilteredEntries(organizationId, eventId, sessionId, intentParam, attendeeIds);

        EventDto event = eventFacade.findEventById(eventId, organizationId);
        OrganizationDto organization = organizationFacade.findOrganizationById(organizationId);

        // Filter out attributes that are being used as filters to prevent column duplication in the PDF
        List<String> attributeHeaders = attendeeFacade.findAttributes(organizationId).stream()
                .map(AttributeDto::name)
                .filter(name -> attributes == null || !attributes.containsKey(name))
                .sorted()
                .toList();

        // 2. Generate CSS based on column count
        int staticColumns = 5; // Identity, First, Last, Status, Time
        int totalColumns = staticColumns + attributeHeaders.size();
        String customCss = generateSmartCss(totalColumns);

        // 3. Dynamic Title
        String baseTitle;
        if (sessionId != null) {
            // Find session name from the Event object's session list
            String sessionName = event.sessions().stream()
                    .filter(s -> s.id().equals(sessionId))
                    .map(SessionDto::activityName)
                    .findFirst()
                    .orElse("Unknown Session");
            baseTitle = sessionName + " Log";
        } else {
            baseTitle = switch (type != null ? type : "All") {
                case "Arrival" -> "Arrivals Log";
                case "Departure" -> "Departures Log";
                default -> "Full Attendance Log";
            };
        }

        StringBuilder reportTitle = new StringBuilder(baseTitle);
        if (attributes != null && !attributes.isEmpty()) {
            String filterDesc = attributes.entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));
            reportTitle.append(" (Filter: ").append(filterDesc).append(")");
        }

        // 4. Context Build
        Context context = new Context();
        context.setVariable("eventName", event.name());
        context.setVariable("organizationName", organization.name());
        context.setVariable("totalCount", entries.size());
        context.setVariable("generatedDate", LocalDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a 'UTC'")));
        context.setVariable("entries", entries);
        context.setVariable("logoData", "data:image/png;base64," + getBase64Logo());
        context.setVariable("reportTitle", reportTitle.toString());
        context.setVariable("attributeHeaders", attributeHeaders);
        context.setVariable("customCss", customCss);

        String htmlContent = templateEngine.process("attendance-report", context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private String generateSmartCss(int totalColumns) {
        String pageSize = "A4 portrait";
        String fontSize = "9pt";
        String cellPadding = "6px";

        if (totalColumns > 7) {
            pageSize = "A4 landscape";
        }
        if (totalColumns > 12) {
            fontSize = "7pt";
            cellPadding = "3px";
        }
        if (totalColumns > 16) {
            fontSize = "6pt";
            cellPadding = "2px";
        }

        return String.format("""
            @page {
                size: %s;
                margin: 15mm 15mm;
                @bottom-right {
                    content: "Page " counter(page) " of " counter(pages);
                    font-family: sans-serif;
                    font-size: 8pt;
                    color: #888;
                }
            }
            body {
                font-family: sans-serif;
                color: #333;
                font-size: %s;
            }
            table.data-table th, table.data-table td {
                padding: %s;
            }
            """, pageSize, fontSize, cellPadding);
    }
}
