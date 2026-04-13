package vn.edu.sgu.phanmemtuyensinh.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DolechTable {
    private static volatile Map<String, Map<String, BigDecimal>> CACHE;
    private static final Object LOCK = new Object();

    private DolechTable() {
    }

    public static BigDecimal getDoLech(String toHopGoc, String toHop) {
        String goc = norm(toHopGoc);
        String th = norm(toHop);
        if (goc.isEmpty() || th.isEmpty() || goc.equals(th)) {
            return BigDecimal.ZERO;
        }

        Map<String, Map<String, BigDecimal>> table = ensureLoaded();
        Map<String, BigDecimal> row = table.get(goc);
        if (row == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal v = row.get(th);
        return v == null ? BigDecimal.ZERO : v;
    }

    private static Map<String, Map<String, BigDecimal>> ensureLoaded() {
        Map<String, Map<String, BigDecimal>> current = CACHE;
        if (current != null) {
            return current;
        }

        synchronized (LOCK) {
            if (CACHE != null) {
                return CACHE;
            }
            CACHE = loadFromDefaultPath();
            return CACHE;
        }
    }

    private static Map<String, Map<String, BigDecimal>> loadFromDefaultPath() {
        Path p = Paths.get("data", "cac cong thuc tinh.txt");
        if (!Files.exists(p)) {
            return new HashMap<>();
        }

        try {
            return parseAsciiTable(p);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private static Map<String, Map<String, BigDecimal>> parseAsciiTable(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        List<String> headerCols = null;
        Map<String, Map<String, BigDecimal>> result = new HashMap<>();

        for (String raw : lines) {
            String line = raw == null ? "" : raw.trim();
            if (!line.startsWith("|") || !line.contains("gốc")) {
                continue;
            }

            // Header line contains "gốc" and list of tohop codes.
            List<String> cells = splitPipeRow(line);
            int gocIdx = indexOfCellContaining(cells, "gốc");
            if (gocIdx >= 0 && gocIdx + 1 < cells.size()) {
                headerCols = new ArrayList<>();
                for (int i = gocIdx + 1; i < cells.size(); i++) {
                    String col = norm(cells.get(i));
                    if (!col.isEmpty()) {
                        headerCols.add(col);
                    }
                }
                break;
            }
        }

        if (headerCols == null || headerCols.isEmpty()) {
            return result;
        }

        for (String raw : lines) {
            String line = raw == null ? "" : raw.trim();
            if (!line.startsWith("|") || !line.contains("Hàng")) {
                continue;
            }

            List<String> cells = splitPipeRow(line);
            // Expected: [STT, Hàng n, <goc>, <val col1>, <val col2>, ...]
            if (cells.size() < 3) {
                continue;
            }

            String goc = norm(cells.get(2));
            if (goc.isEmpty()) {
                continue;
            }

            Map<String, BigDecimal> row = result.computeIfAbsent(goc, k -> new HashMap<>());

            int valuesStart = 3;
            for (int i = 0; i < headerCols.size(); i++) {
                int cellIdx = valuesStart + i;
                if (cellIdx >= cells.size()) {
                    break;
                }

                String txt = cells.get(cellIdx);
                BigDecimal v = parseDecimalOrNull(txt);
                if (v != null) {
                    row.put(headerCols.get(i), v);
                }
            }
        }

        return result;
    }

    private static int indexOfCellContaining(List<String> cells, String needle) {
        for (int i = 0; i < cells.size(); i++) {
            String c = cells.get(i);
            if (c != null && c.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT))) {
                return i;
            }
        }
        return -1;
    }

    private static List<String> splitPipeRow(String line) {
        String trimmed = line;
        if (trimmed.startsWith("|")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("|")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        String[] parts = trimmed.split("\\|");
        List<String> cells = new ArrayList<>(parts.length);
        for (String p : parts) {
            cells.add(p == null ? "" : p.trim());
        }
        return cells;
    }

    private static BigDecimal parseDecimalOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        // Remove spaces and normalize comma decimal separator
        t = t.replace(" ", "").replace(",", ".");
        // Keep leading +
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }
}
