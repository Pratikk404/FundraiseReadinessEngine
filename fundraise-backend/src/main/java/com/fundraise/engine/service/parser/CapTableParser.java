package com.fundraise.engine.service.parser;

import com.fundraise.engine.entity.EquityEvent;
import com.fundraise.engine.entity.ShareClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses CSV and XLSX cap table files into structured equity events and share classes.
 *
 * Expected CSV/XLSX format:
 * Round, Instrument Type, Shares Issued, Price Per Share, Date, Share Class, Total Shares (Class)
 * Pre-Seed, COMMON, 7000000, 1.00, 2023-04-01, Ordinary, 7000000
 * Seed, PREFERRED_A, 2000000, 10.00, 2024-01-15, Preferred_A, 2000000
 * ESOP Pool, ESOP_POOL, 1000000, 0.00, 2024-01-15, ESOP Pool, 1000000
 */
@Component
@Slf4j
public class CapTableParser {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMAT_ALT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMAT_ALT2 = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Parse a cap table file (CSV or XLSX)
     */
    public ParsedCapTable parse(Path filePath, String filename) {
        String ext = getFileExtension(filename).toLowerCase();

        return switch (ext) {
            case "csv" -> parseCsv(filePath);
            case "xlsx", "xls" -> parseXlsx(filePath);
            default -> throw new IllegalArgumentException("Unsupported file format: " + ext);
        };
    }

    /**
     * Parse CSV cap table
     */
    private ParsedCapTable parseCsv(Path filePath) {
        List<EquityEvent> events = new ArrayList<>();
        List<ShareClass> shareClasses = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, ShareClass> shareClassMap = new HashMap<>();
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(filePath)))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> headerIndex = buildHeaderIndex(headers);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                totalRows++;

                try {
                    String[] values = parseCsvLine(line);
                    EquityEvent event = parseEventRow(values, headerIndex);
                    if (event != null) {
                        events.add(event);
                    }

                    // Extract share class if present
                    String className = getColumnValue(values, headerIndex, "Share Class");
                    String totalSharesStr = getColumnValue(values, headerIndex, "Total Shares", "Shares (Class)", "Class Shares");
                    if (className != null && totalSharesStr != null) {
                        shareClassMap.computeIfAbsent(className, name ->
                                ShareClass.builder()
                                        .className(name)
                                        .totalShares(parseLong(totalSharesStr))
                                        .build()
                        );
                    }
                } catch (Exception e) {
                    warnings.add("Row " + totalRows + ": " + e.getMessage());
                    log.warn("Failed to parse CSV row {}: {}", totalRows, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file: " + e.getMessage(), e);
        }

        shareClasses.addAll(shareClassMap.values());

        log.info("CSV parsed: {} events, {} share classes, {} warnings",
                events.size(), shareClasses.size(), warnings.size());

        return ParsedCapTable.builder()
                .equityEvents(events)
                .shareClasses(shareClasses)
                .totalRowsParsed(totalRows)
                .warnings(warnings)
                .build();
    }

    /**
     * Parse XLSX cap table using Apache POI
     */
    private ParsedCapTable parseXlsx(Path filePath) {
        List<EquityEvent> events = new ArrayList<>();
        List<ShareClass> shareClasses = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, ShareClass> shareClassMap = new HashMap<>();
        int totalRows = 0;

        try (InputStream is = Files.newInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("XLSX file has no data");
            }

            // Parse header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("XLSX file has no header row");
            }

            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                String header = getCellStringValue(headerRow.getCell(i)).trim();
                headerIndex.put(header.toLowerCase(), i);
            }

            // Parse data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                totalRows++;

                try {
                    EquityEvent event = parseEventRowFromSheet(row, headerIndex);
                    if (event != null) {
                        events.add(event);
                    }

                    // Extract share class
                    String className = getCellFromHeader(row, headerIndex, "Share Class");
                    String totalSharesStr = getCellFromHeader(row, headerIndex, "Total Shares", "Shares (Class)", "Class Shares");
                    if (className != null && totalSharesStr != null) {
                        shareClassMap.computeIfAbsent(className, name ->
                                ShareClass.builder()
                                        .className(name)
                                        .totalShares(parseLong(totalSharesStr))
                                        .build()
                        );
                    }
                } catch (Exception e) {
                    warnings.add("Row " + (i + 1) + ": " + e.getMessage());
                    log.warn("Failed to parse XLSX row {}: {}", i + 1, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read XLSX file: " + e.getMessage(), e);
        }

        shareClasses.addAll(shareClassMap.values());

        log.info("XLSX parsed: {} events, {} share classes, {} warnings",
                events.size(), shareClasses.size(), warnings.size());

        return ParsedCapTable.builder()
                .equityEvents(events)
                .shareClasses(shareClasses)
                .totalRowsParsed(totalRows)
                .warnings(warnings)
                .build();
    }

    private EquityEvent parseEventRow(String[] values, Map<String, Integer> headerIndex) {
        String roundName = getColumnValue(values, headerIndex, "Round", "Round Name");
        String instrumentTypeStr = getColumnValue(values, headerIndex, "Instrument Type", "Type");
        String sharesStr = getColumnValue(values, headerIndex, "Shares Issued", "Shares");
        String priceStr = getColumnValue(values, headerIndex, "Price Per Share", "Price", "Price/Share");
        String dateStr = getColumnValue(values, headerIndex, "Date", "Event Date");

        if (instrumentTypeStr == null || sharesStr == null) {
            return null; // Skip rows without essential data
        }

        EquityEvent.InstrumentType instrumentType = parseInstrumentType(instrumentTypeStr);

        return EquityEvent.builder()
                .roundName(roundName)
                .instrumentType(instrumentType)
                .sharesIssued(parseLong(sharesStr))
                .pricePerShare(priceStr != null ? new BigDecimal(priceStr) : BigDecimal.ZERO)
                .eventDate(dateStr != null ? parseDate(dateStr) : LocalDate.now())
                .build();
    }

    private EquityEvent parseEventRowFromSheet(Row row, Map<String, Integer> headerIndex) {
        String roundName = getCellFromHeader(row, headerIndex, "Round", "Round Name");
        String instrumentTypeStr = getCellFromHeader(row, headerIndex, "Instrument Type", "Type");
        String sharesStr = getCellFromHeader(row, headerIndex, "Shares Issued", "Shares");
        String priceStr = getCellFromHeader(row, headerIndex, "Price Per Share", "Price", "Price/Share");
        String dateStr = getCellFromHeader(row, headerIndex, "Date", "Event Date");

        if (instrumentTypeStr == null || sharesStr == null) {
            return null;
        }

        EquityEvent.InstrumentType instrumentType = parseInstrumentType(instrumentTypeStr);

        return EquityEvent.builder()
                .roundName(roundName)
                .instrumentType(instrumentType)
                .sharesIssued(parseLong(sharesStr))
                .pricePerShare(priceStr != null ? new BigDecimal(priceStr) : BigDecimal.ZERO)
                .eventDate(dateStr != null ? parseDate(dateStr) : LocalDate.now())
                .build();
    }

    private EquityEvent.InstrumentType parseInstrumentType(String type) {
        if (type == null) return EquityEvent.InstrumentType.COMMON;

        return switch (type.trim().toUpperCase().replace(" ", "_").replace("-", "_")) {
            case "COMMON", "ORDINARY" -> EquityEvent.InstrumentType.COMMON;
            case "PREFERRED_A", "PREF_A", "PREFERRED" -> EquityEvent.InstrumentType.PREFERRED_A;
            case "PREFERRED_B", "PREF_B" -> EquityEvent.InstrumentType.PREFERRED_B;
            case "ESOP_POOL", "ESOP", "OPTIONS" -> EquityEvent.InstrumentType.ESOP_POOL;
            case "CONVERTIBLE_NOTE", "NOTE" -> EquityEvent.InstrumentType.CONVERTIBLE_NOTE;
            case "SAFE" -> EquityEvent.InstrumentType.SAFE;
            default -> {
                log.warn("Unknown instrument type '{}', defaulting to COMMON", type);
                yield EquityEvent.InstrumentType.COMMON;
            }
        };
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return LocalDate.now();

        dateStr = dateStr.trim();

        // Try ISO format first
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException ignored) {}

        // Try dd/MM/yyyy
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT_ALT);
        } catch (DateTimeParseException ignored) {}

        // Try MM/dd/yyyy
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT_ALT2);
        } catch (DateTimeParseException ignored) {}

        log.warn("Could not parse date '{}', using current date", dateStr);
        return LocalDate.now();
    }

    private Map<String, Integer> buildHeaderIndex(String[] headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            index.put(headers[i].trim().toLowerCase(), i);
        }
        return index;
    }

    private String getColumnValue(String[] values, Map<String, Integer> headerIndex, String... possibleHeaders) {
        for (String header : possibleHeaders) {
            Integer colIndex = headerIndex.get(header.toLowerCase());
            if (colIndex != null && colIndex < values.length) {
                String val = values[colIndex].trim();
                return val.isEmpty() ? null : val;
            }
        }
        return null;
    }

    private String getCellFromHeader(Row row, Map<String, Integer> headerIndex, String... possibleHeaders) {
        for (String header : possibleHeaders) {
            Integer colIndex = headerIndex.get(header.toLowerCase());
            if (colIndex != null) {
                Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null) {
                    String val = getCellStringValue(cell);
                    if (val != null && !val.trim().isEmpty()) {
                        return val.trim();
                    }
                }
            }
        }
        return null;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());

        return values.toArray(new String[0]);
    }

    private long parseLong(String value) {
        if (value == null) return 0;
        try {
            return Long.parseLong(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex + 1) : "";
    }
}
