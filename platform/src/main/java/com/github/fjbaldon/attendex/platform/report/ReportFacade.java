package com.github.fjbaldon.attendex.platform.report;

import com.github.fjbaldon.attendex.platform.attendee.AttendeeFacade;
import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.capture.dto.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.event.dto.EventDto;
import com.github.fjbaldon.attendex.platform.notification.EmailService;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.dto.OrganizationDto;
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

    /**
     * Async method for large reports. Generates PDF in background and emails it.
     */
    @Async
    @Transactional(readOnly = true)
    public void generateAndEmailReportPdf(Long organizationId, Long eventId, String type, Map<String, String> attributes, String userEmail) {
        log.info("Async PDF Generation started for Event {} by User {}", eventId, userEmail);
        try {
            // Re-fetch data inside the async thread to ensure transaction safety
            byte[] pdfBytes = generateReportPdf(organizationId, eventId, type, attributes);

            String subject = "Attendance Report: " + eventId;
            String body = """
                    <p>Hello,</p>
                    <p>The attendance report you requested is attached.</p>
                    <p>This report was generated automatically by AttendEx.</p>
                    """;
            String filename = "Attendance_Report_" + eventId + ".pdf";

            emailService.sendReportEmail(userEmail, subject, body, filename, pdfBytes);
            log.info("Report successfully emailed to {}", userEmail);

        } catch (Exception e) {
            log.error("Failed to generate or email report for Event {}", eventId, e);
        }
    }

    /**
     * Synchronous method to generate PDF bytes.
     */
    @Transactional(readOnly = true)
    public byte[] generateReportPdf(Long organizationId, Long eventId, String type, Map<String, String> attributes) {
        List<Long> attendeeIds = attendeeFacade.findAttendeeIdsByFilters(organizationId, attributes);

        List<EntryDetailsDto> entries = captureFacade.findFilteredEntries(organizationId, eventId, type, attendeeIds);

        EventDto event = eventFacade.findEventById(eventId, organizationId);
        OrganizationDto organization = organizationFacade.findOrganizationById(organizationId);

        String base64Logo = "";
        try {
            ClassPathResource imageResource = new ClassPathResource("logo.png");
            byte[] imageBytes = imageResource.getInputStream().readAllBytes();
            base64Logo = java.util.Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            System.err.println("Warning: Could not load logo.png: " + e.getMessage());
        }

        StringBuilder reportTitle = new StringBuilder();
        if (type != null && !type.isBlank()) {
            reportTitle.append(type).append("s Log");
        } else {
            reportTitle.append("Attendance Log");
        }

        if (attributes != null && !attributes.isEmpty()) {
            String filterDesc = attributes.entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));
            reportTitle.append(" (").append(filterDesc).append(")");
        }

        Context context = new Context();
        context.setVariable("eventName", event.name());
        context.setVariable("organizationName", organization.name());
        context.setVariable("totalCount", entries.size());

        // Use UTC explicitly to avoid server timezone confusion
        String dateStr = LocalDateTime.now(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a 'UTC'"));
        context.setVariable("generatedDate", dateStr);

        context.setVariable("entries", entries);
        context.setVariable("logoData", "data:image/png;base64," + base64Logo);
        context.setVariable("reportTitle", reportTitle.toString());

        String htmlContent = templateEngine.process("report-arrivals", context);

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
}
