package vn.edu.sgu.phanmemtuyensinh.bus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import vn.edu.sgu.phanmemtuyensinh.dal.DiemThiXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen;

public class DiemThiXetTuyenBUS {
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern DATASET_CODE_PATTERN = Pattern.compile("^TS_\\d{1,10}$", Pattern.CASE_INSENSITIVE);

    private final DiemThiXetTuyenDAO dao = new DiemThiXetTuyenDAO();
    private String lastError = "";
    private String lastImportSummary = "";

    public List<DiemThiXetTuyen> getAll() {
        return dao.getAll();
    }

    public DiemThiXetTuyen getByCccd(String cccd) {
        return dao.getByCccd(cccd);
    }

    public DiemThiXetTuyen getById(int idDiemThi) {
        return dao.getById(idDiemThi);
    }

    public List<DiemThiXetTuyen> getPage(int page, int pageSize) {
        return dao.getPage(page, pageSize);
    }

    public long countAll() {
        return dao.countAll();
    }

    public List<DiemThiXetTuyen> getByPhuongThuc(String phuongThuc) {
        return dao.getByPhuongThuc(phuongThuc);
    }

    public List<DiemThiXetTuyen> searchByKeyword(String keyword, int page, int pageSize) {
        return dao.searchByKeyword(keyword, page, pageSize);
    }

    public long countByKeyword(String keyword) {
        return dao.countByKeyword(keyword);
    }

    public boolean add(DiemThiXetTuyen diem) {
        if (!validateDiemThi(diem, true)) {
            return false;
        }
        return dao.add(diem);
    }

    public boolean update(DiemThiXetTuyen diem) {
        if (!validateDiemThi(diem, false)) {
            return false;
        }
        return dao.update(diem);
    }

    public boolean delete(int idDiemThi) {
        return dao.delete(idDiemThi);
    }

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public int importAndSaveToDatabase(String filePath) throws IOException {
        List<ImportRecord> records;
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".xlsx")) {
            records = importFromExcel(filePath);
        } else {
            records = importFromTextLike(filePath);
        }

        int success = 0;
        int failed = 0;
        List<String> sampleErrors = new ArrayList<>();

        for (ImportRecord record : records) {
            if (!validateDiemThi(record.diem, false)) {
                failed++;
                if (sampleErrors.size() < 8) {
                    sampleErrors.add("Dòng " + record.lineNo + ": " + getLastError());
                }
                continue;
            }

            DiemThiXetTuyen existing = dao.getByCccd(record.diem.getCccd());
            boolean ok;
            if (existing != null) {
                record.diem.setIdDiemThi(existing.getIdDiemThi());
                ok = dao.update(record.diem);
            } else {
                ok = dao.add(record.diem);
            }

            if (ok) {
                success++;
            } else {
                failed++;
                if (sampleErrors.size() < 8) {
                    sampleErrors.add("Dòng " + record.lineNo + ": Lỗi lưu dữ liệu vào cơ sở dữ liệu");
                }
            }
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Tổng đọc: ").append(records.size())
                .append(" | Thành công: ").append(success)
                .append(" | Thất bại: ").append(failed);
        if (!sampleErrors.isEmpty()) {
            summary.append("\nMột số lỗi:\n- ").append(String.join("\n- ", sampleErrors));
        }
        lastImportSummary = summary.toString();
        return success;
    }

    public String getLastImportSummary() {
        return lastImportSummary == null ? "" : lastImportSummary;
    }

    private List<ImportRecord> importFromExcel(String filePath) throws IOException {
        List<ImportRecord> result = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerMap = buildHeaderMap(headerRow, formatter);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                DiemThiXetTuyen diem = parseExcelRow(row, headerMap, formatter);
                if (isBlank(safe(diem.getCccd()))) {
                    continue;
                }
                result.add(new ImportRecord(i + 1, diem));
            }
        }

        return result;
    }

    private List<ImportRecord> importFromTextLike(String filePath) throws IOException {
        List<ImportRecord> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trim = line.trim();
                if (trim.isEmpty() || trim.startsWith("---") || trim.toLowerCase().startsWith("stt")) {
                    continue;
                }

                String[] parts = trim.contains("\t") ? trim.split("\\t+") : trim.split("\\s{2,}");
                if (parts.length < 2) {
                    continue;
                }

                DiemThiXetTuyen diem = parseTextParts(parts);
                if (isBlank(safe(diem.getCccd()))) {
                    continue;
                }
                result.add(new ImportRecord(lineNo, diem));
            }
        }

        return result;
    }

    private DiemThiXetTuyen parseExcelRow(Row row, Map<String, Integer> headerMap, DataFormatter formatter) {
        DiemThiXetTuyen d = new DiemThiXetTuyen();

        d.setCccd(readHeaderCell(row, headerMap, formatter, "cccd", "cancuoc", "cmnd", "maso"));
        d.setSoBaoDanh(readHeaderCell(row, headerMap, formatter, "sobaodanh", "sbd", "mathisinh"));
        d.setPhuongThuc(readHeaderCell(row, headerMap, formatter, "dphuongthuc", "phuongthuc", "ptxt"));

        if (isBlank(safe(d.getCccd()))) {
            d.setCccd(readCell(row, 1, formatter));
        }
        if (isBlank(safe(d.getSoBaoDanh()))) {
            d.setSoBaoDanh(readCell(row, 1, formatter));
        }

        d.setTo(parseDecimal(readHeaderCell(row, headerMap, formatter, "to", "toan")));
        d.setVa(parseDecimal(readHeaderCell(row, headerMap, formatter, "va", "van", "nguvan")));
        d.setLi(parseDecimal(readHeaderCell(row, headerMap, formatter, "li", "ly", "vatly")));
        d.setHo(parseDecimal(readHeaderCell(row, headerMap, formatter, "ho", "hoa")));
        d.setSi(parseDecimal(readHeaderCell(row, headerMap, formatter, "si", "sinh", "sinhhoc")));
        d.setSu(parseDecimal(readHeaderCell(row, headerMap, formatter, "su", "lichsu")));
        d.setDi(parseDecimal(readHeaderCell(row, headerMap, formatter, "di", "dia", "diali")));
        d.setN1Thi(parseDecimal(readHeaderCell(row, headerMap, formatter, "n1thi", "n1", "nn", "ngoaingu")));
        d.setN1Cc(parseDecimal(readHeaderCell(row, headerMap, formatter, "n1cc", "ccnn", "chungchingoaingu")));
        d.setCncn(parseDecimal(readHeaderCell(row, headerMap, formatter, "cncn")));
        d.setCnnn(parseDecimal(readHeaderCell(row, headerMap, formatter, "cnnn")));
        d.setTi(parseDecimal(readHeaderCell(row, headerMap, formatter, "ti", "tin", "tinhoc")));
        d.setKtpl(parseDecimal(readHeaderCell(row, headerMap, formatter, "ktpl", "gdcd", "kinhtephapluat")));
        d.setNl1(parseDecimal(readHeaderCell(row, headerMap, formatter, "nl1", "dgnl")));
        d.setNk1(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk1")));
        d.setNk2(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk2")));

        // Fallback theo định dạng file mẫu khi không có header phù hợp.
        if (allSubjectScoresMissing(d)) {
            d.setTo(parseDecimal(readCell(row, 7, formatter)));
            d.setVa(parseDecimal(readCell(row, 8, formatter)));
            d.setLi(parseDecimal(readCell(row, 9, formatter)));
            d.setHo(parseDecimal(readCell(row, 10, formatter)));
            d.setSi(parseDecimal(readCell(row, 11, formatter)));
            d.setSu(parseDecimal(readCell(row, 12, formatter)));
            d.setDi(parseDecimal(readCell(row, 13, formatter)));
            d.setKtpl(parseDecimal(readCell(row, 17, formatter)));
            d.setTi(parseDecimal(readCell(row, 18, formatter)));
            d.setCncn(parseDecimal(readCell(row, 19, formatter)));
            d.setCnnn(parseDecimal(readCell(row, 20, formatter)));
            d.setN1Thi(parseDecimal(readCell(row, 15, formatter)));
        }

        return d;
    }

    private DiemThiXetTuyen parseTextParts(String[] parts) {
        DiemThiXetTuyen d = new DiemThiXetTuyen();

        d.setCccd(getPart(parts, 1));
        d.setSoBaoDanh(getPart(parts, 1));

        d.setTo(parseDecimal(getPart(parts, 7)));
        d.setVa(parseDecimal(getPart(parts, 8)));
        d.setLi(parseDecimal(getPart(parts, 9)));
        d.setHo(parseDecimal(getPart(parts, 10)));
        d.setSi(parseDecimal(getPart(parts, 11)));
        d.setSu(parseDecimal(getPart(parts, 12)));
        d.setDi(parseDecimal(getPart(parts, 13)));
        d.setKtpl(parseDecimal(getPart(parts, 17)));
        d.setTi(parseDecimal(getPart(parts, 18)));
        d.setCncn(parseDecimal(getPart(parts, 19)));
        d.setCnnn(parseDecimal(getPart(parts, 20)));
        d.setN1Thi(parseDecimal(getPart(parts, 15)));

        return d;
    }

    private boolean validateDiemThi(DiemThiXetTuyen diem, boolean checkDuplicate) {
        if (diem == null) {
            lastError = "Dữ liệu điểm thi không hợp lệ!";
            return false;
        }

        String cccd = safe(diem.getCccd());
        String soBaoDanh = safe(diem.getSoBaoDanh());
        String phuongThuc = safe(diem.getPhuongThuc());

        if (cccd.isEmpty()) {
            lastError = "CCCD không được để trống!";
            return false;
        }
        if (!isAcceptedCandidateCode(cccd)) {
            lastError = "CCCD không hợp lệ (chấp nhận 12 số hoặc mã TS_xxxx)!";
            return false;
        }

        if (!hasAnyScore(diem)) {
            lastError = "Không có điểm hợp lệ để lưu";
            return false;
        }

        if (!checkScore(diem.getTo(), 0, 10, "TO")
                || !checkScore(diem.getLi(), 0, 10, "LI")
                || !checkScore(diem.getHo(), 0, 10, "HO")
                || !checkScore(diem.getSi(), 0, 10, "SI")
                || !checkScore(diem.getSu(), 0, 10, "SU")
                || !checkScore(diem.getDi(), 0, 10, "DI")
                || !checkScore(diem.getVa(), 0, 10, "VA")
                || !checkScore(diem.getN1Thi(), 0, 10, "N1_THI")
                || !checkScore(diem.getN1Cc(), 0, 10, "N1_CC")
                || !checkScore(diem.getCncn(), 0, 10, "CNCN")
                || !checkScore(diem.getCnnn(), 0, 10, "CNNN")
                || !checkScore(diem.getTi(), 0, 10, "TI")
                || !checkScore(diem.getKtpl(), 0, 10, "KTPL")
                || !checkScore(diem.getNk1(), 0, 10, "NK1")
                || !checkScore(diem.getNk2(), 0, 10, "NK2")
                || !checkScore(diem.getNl1(), 0, 1200, "NL1")) {
            return false;
        }

        if (checkDuplicate && dao.getByCccd(cccd) != null) {
            lastError = "CCCD đã tồn tại trong hệ thống!";
            return false;
        }

        diem.setCccd(trimMax(cccd, 20));
        diem.setSoBaoDanh(trimMax(soBaoDanh, 45));
        diem.setPhuongThuc(trimMax(phuongThuc, 10));
        lastError = "";
        return true;
    }

    private boolean checkScore(BigDecimal score, double min, double max, String label) {
        if (score == null) {
            return true;
        }
        double value = score.doubleValue();
        if (value < min || value > max) {
            lastError = "Điểm " + label + " phải trong khoảng " + min + " - " + max;
            return false;
        }
        return true;
    }

    private boolean allSubjectScoresMissing(DiemThiXetTuyen d) {
        return d.getTo() == null
                && d.getVa() == null
                && d.getLi() == null
                && d.getHo() == null
                && d.getSi() == null
                && d.getSu() == null
                && d.getDi() == null;
    }

    private boolean hasAnyScore(DiemThiXetTuyen d) {
        return d.getTo() != null || d.getLi() != null || d.getHo() != null || d.getSi() != null
                || d.getSu() != null || d.getDi() != null || d.getVa() != null || d.getN1Thi() != null
                || d.getN1Cc() != null || d.getCncn() != null || d.getCnnn() != null || d.getTi() != null
                || d.getKtpl() != null || d.getNl1() != null || d.getNk1() != null || d.getNk2() != null;
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) {
            return map;
        }

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String text = normalizeHeader(formatter.formatCellValue(headerRow.getCell(i)));
            if (!text.isBlank()) {
                map.put(text, i);
            }
        }
        return map;
    }

    private String readHeaderCell(Row row, Map<String, Integer> headerMap, DataFormatter formatter, String... aliases) {
        for (String alias : aliases) {
            Integer index = headerMap.get(normalizeHeader(alias));
            if (index != null) {
                return readCell(row, index, formatter);
            }
        }
        return "";
    }

    private String normalizeHeader(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.trim().toLowerCase()
                .replace("đ", "d")
                .replace("á", "a").replace("à", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a")
                .replace("ă", "a").replace("ắ", "a").replace("ằ", "a").replace("ẳ", "a").replace("ẵ", "a").replace("ặ", "a")
                .replace("â", "a").replace("ấ", "a").replace("ầ", "a").replace("ẩ", "a").replace("ẫ", "a").replace("ậ", "a")
                .replace("é", "e").replace("è", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e")
                .replace("ê", "e").replace("ế", "e").replace("ề", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
                .replace("í", "i").replace("ì", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i")
                .replace("ó", "o").replace("ò", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o")
                .replace("ô", "o").replace("ố", "o").replace("ồ", "o").replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
                .replace("ơ", "o").replace("ớ", "o").replace("ờ", "o").replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
                .replace("ú", "u").replace("ù", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u")
                .replace("ư", "u").replace("ứ", "u").replace("ừ", "u").replace("ử", "u").replace("ữ", "u").replace("ự", "u")
                .replace("ý", "y").replace("ỳ", "y").replace("ỷ", "y").replace("ỹ", "y").replace("ỵ", "y");

        return normalized.replaceAll("[^a-z0-9]", "");
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        if (row == null || index < 0) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(index)).trim();
    }

    private BigDecimal parseDecimal(String value) {
        String text = safe(value);
        if (text.isEmpty()) {
            return null;
        }

        if (text.contains(",") && !text.contains(".")) {
            text = text.replace(',', '.');
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String getPart(String[] parts, int idx) {
        if (parts == null || idx < 0 || idx >= parts.length) {
            return "";
        }
        return parts[idx] == null ? "" : parts[idx].trim();
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.equalsIgnoreCase("nan") || normalized.equalsIgnoreCase("null")) {
            return "";
        }
        return normalized;
    }

    private boolean isAcceptedCandidateCode(String cccdOrCode) {
        return CCCD_PATTERN.matcher(cccdOrCode).matches()
                || DATASET_CODE_PATTERN.matcher(cccdOrCode).matches();
    }

    private String trimMax(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class ImportRecord {
        private final int lineNo;
        private final DiemThiXetTuyen diem;

        private ImportRecord(int lineNo, DiemThiXetTuyen diem) {
            this.lineNo = lineNo;
            this.diem = diem;
        }
    }
}
