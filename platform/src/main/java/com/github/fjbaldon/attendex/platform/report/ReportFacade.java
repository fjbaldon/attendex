package com.github.fjbaldon.attendex.platform.report;

import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.capture.dto.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.event.dto.EventDto;
import com.github.fjbaldon.attendex.platform.organization.OrganizationFacade;
import com.github.fjbaldon.attendex.platform.organization.dto.OrganizationDto;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportFacade {

    private final CaptureFacade captureFacade;
    private final EventFacade eventFacade;
    private final OrganizationFacade organizationFacade;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional(readOnly = true)
    public byte[] generateArrivalsPdf(Long organizationId, Long eventId) {
        EventDto event = eventFacade.findEventById(eventId, organizationId);
        OrganizationDto organization = organizationFacade.findOrganizationById(organizationId);
        List<EntryDetailsDto> entries = captureFacade.findAllEntriesForEvent(organizationId, eventId);

        String base64Logo = "";
        try {
            ClassPathResource imageResource = new ClassPathResource("logo.png");
            byte[] imageBytes = imageResource.getInputStream().readAllBytes();
            base64Logo = Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            System.err.println("Warning: Could not load logo.png for PDF report: " + e.getMessage());
        }

        Context context = new Context();
        context.setVariable("eventName", event.name());
        context.setVariable("organizationName", organization.name());
        context.setVariable("totalCount", entries.size());
        context.setVariable("generatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")));
        context.setVariable("entries", entries);
        context.setVariable("logoData", "data:image/png;base64," + base64Logo);

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
