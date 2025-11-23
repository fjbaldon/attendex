package com.github.fjbaldon.attendex.platform.report;

import com.github.fjbaldon.attendex.platform.capture.CaptureFacade;
import com.github.fjbaldon.attendex.platform.capture.dto.EntryDetailsDto;
import com.github.fjbaldon.attendex.platform.event.EventFacade;
import com.github.fjbaldon.attendex.platform.event.dto.EventDto;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportFacade {

    private final CaptureFacade captureFacade;
    private final EventFacade eventFacade;
    private final TemplateEngine templateEngine;

    @Transactional(readOnly = true)
    public byte[] generateArrivalsPdf(Long organizationId, Long eventId) {
        // 1. Fetch Data
        EventDto event = eventFacade.findEventById(eventId, organizationId);
        List<EntryDetailsDto> entries = captureFacade.findAllEntriesForEvent(organizationId, eventId);

        // 2. Prepare Thymeleaf Context
        Context context = new Context();
        context.setVariable("eventName", event.name());
        context.setVariable("totalCount", entries.size());
        context.setVariable("generatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")));
        context.setVariable("entries", entries);

        // 3. Render HTML
        String htmlContent = templateEngine.process("report-arrivals", context);

        // 4. Convert to PDF
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode(); // Faster, less strict on HTML
            builder.withHtmlContent(htmlContent, "");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}
