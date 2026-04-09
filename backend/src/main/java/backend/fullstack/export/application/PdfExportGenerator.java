package backend.fullstack.export.application;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import backend.fullstack.checklist.domain.ChecklistInstance;
import backend.fullstack.checklist.domain.ChecklistInstanceItem;
import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.temperature.domain.TemperatureReading;

/**
 * Generates PDF export files from domain data using OpenPDF.
 */
@Component
public class PdfExportGenerator {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.ITALIC);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);

    private static final java.awt.Color HEADER_BG = new java.awt.Color(59, 130, 246);
    private static final java.awt.Color HEADER_FG = java.awt.Color.WHITE;
    private static final java.awt.Color STRIPE_BG = new java.awt.Color(243, 244, 246);

    // ────────────────────────────────────────────────────────────────
    // Temperature logs
    // ────────────────────────────────────────────────────────────────

    public byte[] generateTemperatureLogsPdf(List<TemperatureReading> readings,
                                             LocalDate from, LocalDate to) {
        return createPdf(doc -> {
            addTitle(doc, "Temperature Logs Export");
            addDateRange(doc, from, to);
            addSpacer(doc);

            if (readings.isEmpty()) {
                doc.add(new Paragraph("No temperature readings found for the given period."));
                return;
            }

            PdfPTable table = new PdfPTable(new float[]{0.8f, 2f, 1.5f, 2f, 2f, 1f});
            table.setWidthPercentage(100);
            addHeaders(table, "ID", "Unit", "Temp (°C)", "Recorded At", "Recorded By", "Deviation");

            for (int i = 0; i < readings.size(); i++) {
                TemperatureReading r = readings.get(i);
                boolean stripe = i % 2 == 1;
                addCell(table, str(r.getId()), stripe);
                addCell(table, r.getUnit() != null ? r.getUnit().getName() : "-", stripe);
                addCell(table, str(r.getTemperature()), stripe);
                addCell(table, formatDateTime(r.getRecordedAt()), stripe);
                addCell(table, r.getRecordedBy() != null
                        ? r.getRecordedBy().getFirstName() + " " + r.getRecordedBy().getLastName()
                        : "-", stripe);
                addCell(table, r.isDeviation() ? "Yes" : "No", stripe);
            }

            doc.add(table);
            addRecordCount(doc, readings.size());
        });
    }

    // ────────────────────────────────────────────────────────────────
    // Deviations
    // ────────────────────────────────────────────────────────────────

    public byte[] generateDeviationsPdf(List<Deviation> deviations) {
        return createPdf(doc -> {
            addTitle(doc, "Deviations Export");
            addGeneratedTimestamp(doc);
            addSpacer(doc);

            if (deviations.isEmpty()) {
                doc.add(new Paragraph("No deviations found."));
                return;
            }

            PdfPTable table = new PdfPTable(new float[]{0.6f, 2.5f, 1.2f, 1.2f, 1.5f, 2f, 1.5f});
            table.setWidthPercentage(100);
            addHeaders(table, "ID", "Title", "Status", "Severity", "Module", "Reported By", "Created");

            for (int i = 0; i < deviations.size(); i++) {
                Deviation d = deviations.get(i);
                boolean stripe = i % 2 == 1;
                addCell(table, str(d.getId()), stripe);
                addCell(table, d.getTitle(), stripe);
                addCell(table, d.getStatus() != null ? d.getStatus().name() : "-", stripe);
                addCell(table, d.getSeverity() != null ? d.getSeverity().name() : "-", stripe);
                addCell(table, d.getModuleType() != null ? d.getModuleType().name() : "-", stripe);
                addCell(table, d.getReportedByName() != null ? d.getReportedByName() : "-", stripe);
                addCell(table, formatDateTime(d.getCreatedAt()), stripe);
            }

            doc.add(table);
            addRecordCount(doc, deviations.size());
        });
    }

    // ────────────────────────────────────────────────────────────────
    // Checklists
    // ────────────────────────────────────────────────────────────────

    public byte[] generateChecklistsPdf(List<ChecklistInstance> checklists) {
        return createPdf(doc -> {
            addTitle(doc, "Checklists Export");
            addGeneratedTimestamp(doc);
            addSpacer(doc);

            if (checklists.isEmpty()) {
                doc.add(new Paragraph("No checklists found."));
                return;
            }

            for (ChecklistInstance cl : checklists) {
                Paragraph header = new Paragraph(cl.getTitle(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
                doc.add(header);

                String meta = String.format("Date: %s  |  Status: %s  |  Frequency: %s",
                        formatDate(cl.getDate()),
                        cl.getStatus() != null ? cl.getStatus().name() : "-",
                        cl.getFrequency() != null ? cl.getFrequency().name() : "-");
                doc.add(new Paragraph(meta, SUBTITLE_FONT));
                addSpacer(doc);

                if (!cl.getItems().isEmpty()) {
                    PdfPTable table = new PdfPTable(new float[]{0.5f, 3f, 1f});
                    table.setWidthPercentage(100);
                    addHeaders(table, "#", "Item", "Completed");

                    int idx = 1;
                    for (ChecklistInstanceItem item : cl.getItems()) {
                        boolean stripe = idx % 2 == 0;
                        addCell(table, String.valueOf(idx), stripe);
                        addCell(table, item.getText(), stripe);
                        addCell(table, item.isCompleted() ? "✓" : "✗", stripe);
                        idx++;
                    }
                    doc.add(table);
                }

                addSpacer(doc);
            }

            addRecordCount(doc, checklists.size());
        });
    }

    // ────────────────────────────────────────────────────────────────
    // Shared helpers
    // ────────────────────────────────────────────────────────────────

    private byte[] createPdf(PdfContentWriter writer) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 50, 50);

        try {
            PdfWriter.getInstance(document, baos);

            HeaderFooter footer = new HeaderFooter(
                    new Phrase("Page ", CELL_FONT), true);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setBorder(0);
            document.setFooter(footer);

            document.open();
            writer.write(document);
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate PDF export", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private void addTitle(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, TITLE_FONT);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private void addDateRange(Document doc, LocalDate from, LocalDate to) throws DocumentException {
        String range = "Period: "
                + (from != null ? from.format(D_FMT) : "all")
                + " — "
                + (to != null ? to.format(D_FMT) : "present");
        Paragraph p = new Paragraph(range, SUBTITLE_FONT);
        p.setSpacingAfter(2);
        doc.add(p);
        addGeneratedTimestamp(doc);
    }

    private void addGeneratedTimestamp(Document doc) throws DocumentException {
        Paragraph p = new Paragraph(
                "Generated: " + LocalDateTime.now().format(DT_FMT),
                SUBTITLE_FONT);
        p.setSpacingAfter(2);
        doc.add(p);
    }

    private void addSpacer(Document doc) throws DocumentException {
        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(8);
        doc.add(spacer);
    }

    private void addRecordCount(Document doc, int count) throws DocumentException {
        addSpacer(doc);
        doc.add(new Paragraph("Total records: " + count, SUBTITLE_FONT));
    }

    private void addHeaders(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);

            Font headerCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            headerCellFont.setColor(HEADER_FG);
            cell.setPhrase(new Phrase(h, headerCellFont));

            table.addCell(cell);
        }
    }

    private void addCell(PdfPTable table, String text, boolean stripe) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", CELL_FONT));
        if (stripe) {
            cell.setBackgroundColor(STRIPE_BG);
        }
        cell.setPadding(4);
        table.addCell(cell);
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DT_FMT) : "-";
    }

    private String formatDate(LocalDate d) {
        return d != null ? d.format(D_FMT) : "-";
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "-";
    }

    @FunctionalInterface
    private interface PdfContentWriter {
        void write(Document document) throws DocumentException;
    }
}
