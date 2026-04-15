package vn.edu.sgu.phanmemtuyensinh.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GenerateToHopExcel2026 {
    private static final String SOURCE_URL = "https://thuvienphapluat.vn/phap-luat/ho-tro-phap-luat/cac-khoi-thi-dai-hoc-2026-moi-nhat-xem-chi-tiet-to-hop-cac-khoi-thi-dai-hoc-moi-nhat-nam-2026-485999-255791.html";
    private static final Path OUTPUT_FILE = Path.of("data", "tohop_mon_2026_import.xlsx");

    private static final Pattern TABLE_ROW_PATTERN = Pattern.compile(
            "<tr>\\s*<td[^>]*>(.*?)</td>\\s*<td[^>]*>(.*?)</td>\\s*<td[^>]*>(.*?)</td>\\s*</tr>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]{1,4}$");

    private record ToHopRow(String maToHop, String mon1, String mon2, String mon3, String tenToHop) {
    }

    public static void main(String[] args) throws Exception {
        String html = fetchHtml(SOURCE_URL);
        List<ToHopRow> rows = extractRows(html);

        if (rows.isEmpty()) {
            throw new IllegalStateException("Khong trich duoc du lieu to hop tu trang nguon");
        }

        writeExcel(rows, OUTPUT_FILE);
        System.out.println("Da tao file: " + OUTPUT_FILE.toAbsolutePath());
        System.out.println("So dong du lieu: " + rows.size());
    }

    private static String fetchHtml(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Khong the tai du lieu tu URL. HTTP status: " + response.statusCode());
        }
        return response.body();
    }

    private static List<ToHopRow> extractRows(String html) {
        Matcher matcher = TABLE_ROW_PATTERN.matcher(html);
        Map<String, ToHopRow> uniqueRows = new LinkedHashMap<>();

        while (matcher.find()) {
            String codeCell = htmlToText(matcher.group(2));
            String detailCell = htmlToText(matcher.group(3));

            if (codeCell.isBlank() || detailCell.isBlank()) {
                continue;
            }
            if (isHeader(codeCell, detailCell)) {
                continue;
            }

            List<String> codes = splitCodes(codeCell);
            if (codes.isEmpty()) {
                continue;
            }

            String[] monCodes = extractMonCodes(detailCell);
            if (monCodes.length < 3) {
                continue;
            }

            for (String code : codes) {
                uniqueRows.putIfAbsent(
                        code,
                        new ToHopRow(code, monCodes[0], monCodes[1], monCodes[2], trimMax(detailCell, 100))
                );
            }
        }

        return new ArrayList<>(uniqueRows.values());
    }

    private static boolean isHeader(String codeCell, String detailCell) {
        String code = toAsciiKey(codeCell);
        String detail = toAsciiKey(detailCell);
        return code.contains("TO HOP") || code.contains("STT") || detail.contains("MON CHI TIET");
    }

    private static List<String> splitCodes(String rawCodeCell) {
        List<String> codes = new ArrayList<>();
        String normalized = rawCodeCell.toUpperCase(Locale.ROOT).replace(';', ',').replace('/', ',');

        for (String block : normalized.split(",")) {
            String token = block.trim();
            if (token.isEmpty()) {
                continue;
            }

            for (String part : token.split("\\s+")) {
                String candidate = part.trim();
                if (CODE_PATTERN.matcher(candidate).matches()) {
                    codes.add(candidate);
                }
            }
        }
        return codes;
    }

    private static String[] extractMonCodes(String detail) {
        List<String> subjects = new ArrayList<>();
        String normalized = detail
                .replace(" và ", ", ")
                .replace(";", ",")
                .replace("|", ",");

        String[] parts = normalized.split(",");
        for (int i = 0; i < parts.length; i++) {
            String code = mapSubject(parts[i], i + 1);
            if (!code.isBlank()) {
                subjects.add(code);
            }
            if (subjects.size() >= 3) {
                break;
            }
        }

        if (subjects.size() < 3) {
            return new String[0];
        }
        return new String[] {subjects.get(0), subjects.get(1), subjects.get(2)};
    }

    private static String mapSubject(String raw, int order) {
        String key = toAsciiKey(raw).replaceAll("[^A-Z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        String compact = key.replace(" ", "");

        if (compact.isEmpty()) {
            return "";
        }

        if (compact.equals("TOAN")) return "TO";
        if (compact.equals("VATLI") || compact.equals("VATLY") || compact.equals("LY") || compact.equals("LI")) return "LI";
        if (compact.equals("HOA") || compact.equals("HOAHOC")) return "HO";
        if (compact.equals("SINH") || compact.equals("SINHHOC")) return "SI";
        if (compact.equals("LICHSU") || compact.equals("SU")) return "SU";
        if (compact.equals("DIALI") || compact.equals("DIALY") || compact.equals("DIA")) return "DI";
        if (compact.equals("NGUVAN") || compact.equals("VAN")) return "VA";

        if (compact.contains("TIENG") || compact.contains("NGOAINGU")) return "N1";
        if (compact.equals("GDKTPL") || compact.equals("GDKTPL") || compact.equals("GDKT&PL") || compact.equals("GDCD")) return "KTPL";
        if (compact.equals("TIN") || compact.equals("TINHOC")) return "TI";
        if (compact.contains("CONGNGHECONGNGHIEP")) return "CNCN";
        if (compact.contains("CONGNGHENONGNGHIEP")) return "CNNN";
        if (compact.contains("KHOAHOCTUNHIEN") || compact.equals("KHTN")) return "KHTN";
        if (compact.contains("KHOAHOCXAHOI") || compact.equals("KHXH")) return "KHXH";

        if (compact.contains("HINHHOA") || compact.contains("VEMYTHUAT")) return "NK3";
        if (compact.contains("TRANGTRI")) return "NK4";
        if (compact.contains("XUONGAM") || compact.contains("THAMAM") || compact.contains("TIETTAU")) return "NK6";
        if (compact.contains("HAT") || compact.contains("NHACCU") || compact.contains("NHAC")) return "NK5";
        if (compact.contains("TDTT") || compact.contains("THEDUCTHETHAO")) return "NK7";

        if (compact.contains("NANGKHIEU1") || compact.equals("NK1")) return "NK1";
        if (compact.contains("NANGKHIEU2") || compact.equals("NK2")) return "NK2";
        if (compact.contains("NANGKHIEU")) {
            int idx = Math.max(1, Math.min(order, 10));
            return "NK" + idx;
        }

        if (compact.equals("DOCHIEU") || compact.contains("DOCDIENCAM") || compact.contains("KECHUYEN")) return "NK1";

        return "KHAC";
    }

    private static void writeExcel(List<ToHopRow> rows, Path outputFile) throws IOException {
        if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ToHopMon2026");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("idtohop");
            header.createCell(1).setCellValue("matohop");
            header.createCell(2).setCellValue("mon1");
            header.createCell(3).setCellValue("mon2");
            header.createCell(4).setCellValue("mon3");
            header.createCell(5).setCellValue("tentohop");

            int rowIndex = 1;
            int id = 1;
            for (ToHopRow rowData : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(id++);
                row.createCell(1).setCellValue(rowData.maToHop());
                row.createCell(2).setCellValue(rowData.mon1());
                row.createCell(3).setCellValue(rowData.mon2());
                row.createCell(4).setCellValue(rowData.mon3());
                row.createCell(5).setCellValue(rowData.tenToHop());
            }

            int noteStart = rowIndex + 1;
            sheet.createRow(noteStart).createCell(0).setCellValue("GHI CHU");
            sheet.createRow(noteStart + 1).createCell(0).setCellValue("Du lieu duoc trich tu bai viet tong hop cac khoi thi dai hoc 2026.");
            sheet.createRow(noteStart + 2).createCell(0).setCellValue("Nguon: " + SOURCE_URL);
            sheet.createRow(noteStart + 3).createCell(0).setCellValue("Ngay tao file: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            sheet.createRow(noteStart + 4).createCell(0).setCellValue("File dung de Import Excel cho man hinh Quan ly To hop mon.");

            for (int i = 0; i <= 5; i++) {
                sheet.autoSizeColumn(i);
            }

            try (OutputStream os = Files.newOutputStream(outputFile)) {
                workbook.write(os);
            }
        }
    }

    private static String htmlToText(String html) {
        if (html == null) {
            return "";
        }
        String text = html
                .replaceAll("(?i)<br\\s*/?>", ", ")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"");

        text = decodeNumericEntities(text);
        return text.replaceAll("\\s+", " ").trim();
    }

    private static String decodeNumericEntities(String text) {
        String decoded = text;

        Matcher hexMatcher = Pattern.compile("&#x([0-9A-Fa-f]+);").matcher(decoded);
        StringBuffer hexBuffer = new StringBuffer();
        while (hexMatcher.find()) {
            int codePoint = Integer.parseInt(hexMatcher.group(1), 16);
            hexMatcher.appendReplacement(hexBuffer, Matcher.quoteReplacement(String.valueOf((char) codePoint)));
        }
        hexMatcher.appendTail(hexBuffer);

        Matcher decMatcher = Pattern.compile("&#([0-9]+);").matcher(hexBuffer.toString());
        StringBuffer decBuffer = new StringBuffer();
        while (decMatcher.find()) {
            int codePoint = Integer.parseInt(decMatcher.group(1));
            decMatcher.appendReplacement(decBuffer, Matcher.quoteReplacement(String.valueOf((char) codePoint)));
        }
        decMatcher.appendTail(decBuffer);

        return decBuffer.toString();
    }

    private static String toAsciiKey(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('Đ', 'D')
                .replace('đ', 'd');
        return normalized.toUpperCase(Locale.ROOT).trim();
    }

    private static String trimMax(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, maxLength);
    }
}
