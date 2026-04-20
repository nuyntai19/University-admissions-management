package vn.edu.sgu.phanmemtuyensinh.bus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import vn.edu.sgu.phanmemtuyensinh.dal.NganhDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.Nganh;

public class NganhBUS {
    private static final Pattern MA_NGANH_PATTERN = Pattern.compile("^[0-9]{7,8}(CLC)?$", Pattern.CASE_INSENSITIVE);

    private NganhDAO dao = new NganhDAO();
    private String lastError = "";
    private String lastImportSummary = "";

    public List<Nganh> getAll() {
        return dao.getAll();
    }

    public Nganh getById(int idNganh) {
        return dao.getById(idNganh);
    }

    public Nganh getByMaNganh(String maNganh) {
        return dao.getByMaNganh(maNganh);
    }

    public List<Nganh> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAll();
        }
        return dao.searchByKeyword(keyword.trim());
    }

    public boolean add(Nganh nganh) {
        if (!validateNganh(nganh, true)) {
            return false;
        }
        return dao.add(nganh);
    }

    public boolean update(Nganh nganh) {
        if (!validateNganh(nganh, false)) {
            return false;
        }
        return dao.update(nganh);
    }

    public boolean delete(int idNganh) {
        return dao.delete(idNganh);
    }

    public boolean clearAndResetId() {
        return dao.clearAndResetId();
    }

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public int importAndSaveToDatabase(String filePath) throws IOException {
        List<ImportRecord> records = filePath.toLowerCase().endsWith(".xlsx")
                ? importFromExcel(filePath)
                : importFromTextLike(filePath);

        int success = 0;
        int failed = 0;
        List<String> sampleErrors = new ArrayList<>();

        for (ImportRecord record : records) {
            if (!validateNganh(record.nganh, false)) {
                failed++;
                if (sampleErrors.size() < 8) {
                    sampleErrors.add("Dòng " + record.lineNo + ": " + getLastError());
                }
                continue;
            }

            Nganh existing = dao.getByMaNganh(record.nganh.getMaNganh());
            boolean ok;
            if (existing != null) {
                Nganh merged = mergeExisting(existing, record);
                ok = dao.update(merged);
            } else {
                ok = dao.add(record.nganh);
            }

            if (ok) {
                success++;
            } else {
                failed++;
                if (sampleErrors.size() < 8) {
                    sampleErrors.add("Dòng " + record.lineNo + ": Lỗi lưu dữ liệu");
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
        String lowerPath = filePath.toLowerCase();
        ImportMode forcedMode = detectImportModeByFileName(lowerPath);

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                ImportRecord record = parseFlexibleExcelRow(row, formatter, forcedMode);
                if (record == null || record.nganh.getMaNganh() == null || record.nganh.getMaNganh().isBlank()) {
                    continue;
                }
                result.add(new ImportRecord(i + 1, record.mode, record.nganh));
            }
        }

        return result;
    }

    private List<ImportRecord> importFromTextLike(String filePath) throws IOException {
        List<ImportRecord> result = new ArrayList<>();
        String lowerPath = filePath.toLowerCase();
        ImportMode forcedMode = detectImportModeByFileName(lowerPath);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trim = line.trim();
                if (trim.isEmpty() || trim.startsWith("---") || trim.toLowerCase().startsWith("idnganh")
                        || trim.toLowerCase().contains("mã ctđt") || trim.toLowerCase().contains("mã xét tuyển")) {
                    continue;
                }

                String[] parts = trim.contains("\t") ? trim.split("\\t+") : trim.split("\\s{2,}");
                if (parts.length < 2) {
                    continue;
                }

                ImportRecord record = parseFlexibleTextParts(parts, forcedMode);
                if (record == null || record.nganh.getMaNganh() == null || record.nganh.getMaNganh().isBlank()) {
                    continue;
                }
                result.add(new ImportRecord(lineNo, record.mode, record.nganh));
            }
        }

        return result;
    }

    private ImportRecord parseFlexibleExcelRow(Row row, DataFormatter formatter, ImportMode forcedMode) {
        List<String> cells = new ArrayList<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String value = safe(formatter.formatCellValue(row.getCell(i)));
            if (!value.isBlank()) {
                cells.add(value);
            }
        }
        return parseFlexibleCells(cells, forcedMode);
    }

    private ImportRecord parseFlexibleTextParts(String[] parts, ImportMode forcedMode) {
        List<String> cells = new ArrayList<>();
        for (String p : parts) {
            String value = safe(p);
            if (!value.isBlank()) {
                cells.add(value);
            }
        }
        return parseFlexibleCells(cells, forcedMode);
    }

    private ImportRecord parseFlexibleCells(List<String> cells, ImportMode forcedMode) {
        if (cells == null || cells.isEmpty()) {
            return null;
        }

        int maIdx = findMaNganhIndex(cells);
        if (maIdx < 0) {
            return null;
        }

        Nganh nganh = new Nganh();
        nganh.setMaNganh(cells.get(maIdx));
        nganh.setTenNganh(maIdx + 1 < cells.size() ? cells.get(maIdx + 1) : "");

        ImportMode mode = forcedMode;
        if (mode == ImportMode.AUTO) {
            mode = detectModeByCells(cells, maIdx);
        }

        if (mode == ImportMode.CHI_TIEU) {
            String number = findFirstNumericAfter(cells, maIdx + 2);
            nganh.setChiTieu(parseInt(number));
        } else if (mode == ImportMode.NGUONG) {
            String number = findFirstNumericAfter(cells, maIdx + 2);
            nganh.setDiemSan(parseDecimal(number));
        } else {
            nganh.setToHopGoc(trimMax(getAt(cells, maIdx + 2), 3));
            nganh.setChiTieu(parseInt(getAt(cells, maIdx + 3)));
            nganh.setDiemSan(parseDecimal(getAt(cells, maIdx + 4)));
            nganh.setDiemTrungTuyen(parseDecimal(getAt(cells, maIdx + 5)));
            nganh.setTuyenThang(trimMax(getAt(cells, maIdx + 6), 1));
            nganh.setDgnl(trimMax(getAt(cells, maIdx + 7), 1));
            nganh.setThpt(trimMax(getAt(cells, maIdx + 8), 1));
            nganh.setVsat(trimMax(getAt(cells, maIdx + 9), 1));
            nganh.setSlXtt(parseInt(getAt(cells, maIdx + 10)));
            nganh.setSlDgnl(parseInt(getAt(cells, maIdx + 11)));
            nganh.setSlVsat(parseInt(getAt(cells, maIdx + 12)));
            nganh.setSlThpt(trimMax(getAt(cells, maIdx + 13), 45));
        }

        return new ImportRecord(0, mode, nganh);
    }

    private Nganh mergeExisting(Nganh existing, ImportRecord record) {
        Nganh merged = new Nganh();
        merged.setIdNganh(existing.getIdNganh());
        merged.setMaNganh(record.nganh.getMaNganh());
        merged.setTenNganh(!safe(record.nganh.getTenNganh()).isBlank() ? record.nganh.getTenNganh() : existing.getTenNganh());

        if (record.mode == ImportMode.CHI_TIEU) {
            merged.setToHopGoc(existing.getToHopGoc());
            merged.setChiTieu(record.nganh.getChiTieu());
            merged.setDiemSan(existing.getDiemSan());
            merged.setDiemTrungTuyen(existing.getDiemTrungTuyen());
            merged.setTuyenThang(existing.getTuyenThang());
            merged.setDgnl(existing.getDgnl());
            merged.setThpt(existing.getThpt());
            merged.setVsat(existing.getVsat());
            merged.setSlXtt(existing.getSlXtt());
            merged.setSlDgnl(existing.getSlDgnl());
            merged.setSlVsat(existing.getSlVsat());
            merged.setSlThpt(existing.getSlThpt());
        } else if (record.mode == ImportMode.NGUONG) {
            merged.setToHopGoc(existing.getToHopGoc());
            merged.setChiTieu(existing.getChiTieu());
            merged.setDiemSan(record.nganh.getDiemSan());
            merged.setDiemTrungTuyen(existing.getDiemTrungTuyen());
            merged.setTuyenThang(existing.getTuyenThang());
            merged.setDgnl(existing.getDgnl());
            merged.setThpt(existing.getThpt());
            merged.setVsat(existing.getVsat());
            merged.setSlXtt(existing.getSlXtt());
            merged.setSlDgnl(existing.getSlDgnl());
            merged.setSlVsat(existing.getSlVsat());
            merged.setSlThpt(existing.getSlThpt());
        } else {
            merged.setToHopGoc(record.nganh.getToHopGoc());
            merged.setChiTieu(record.nganh.getChiTieu());
            merged.setDiemSan(record.nganh.getDiemSan());
            merged.setDiemTrungTuyen(record.nganh.getDiemTrungTuyen());
            merged.setTuyenThang(record.nganh.getTuyenThang());
            merged.setDgnl(record.nganh.getDgnl());
            merged.setThpt(record.nganh.getThpt());
            merged.setVsat(record.nganh.getVsat());
            merged.setSlXtt(record.nganh.getSlXtt());
            merged.setSlDgnl(record.nganh.getSlDgnl());
            merged.setSlVsat(record.nganh.getSlVsat());
            merged.setSlThpt(record.nganh.getSlThpt());
        }

        return merged;
    }

    private ImportMode detectImportModeByFileName(String lowerPath) {
        if (lowerPath.contains("chi tieu")) {
            return ImportMode.CHI_TIEU;
        }
        if (lowerPath.contains("nguong")) {
            return ImportMode.NGUONG;
        }
        return ImportMode.AUTO;
    }

    private ImportMode detectModeByCells(List<String> cells, int maIdx) {
        if (cells.size() - maIdx <= 4) {
            String numeric = findFirstNumericAfter(cells, maIdx + 2);
            if (numeric.contains(".")) {
                return ImportMode.NGUONG;
            }
            return ImportMode.CHI_TIEU;
        }
        return ImportMode.FULL;
    }

    private int findMaNganhIndex(List<String> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (MA_NGANH_PATTERN.matcher(cells.get(i)).matches()) {
                return i;
            }
        }
        return -1;
    }

    private String findFirstNumericAfter(List<String> cells, int start) {
        for (int i = Math.max(0, start); i < cells.size(); i++) {
            String candidate = cells.get(i).replace(",", ".");
            if (candidate.matches("^-?\\d+(\\.\\d+)?$")) {
                return cells.get(i);
            }
        }
        return "";
    }

    private String getAt(List<String> cells, int index) {
        if (cells == null || index < 0 || index >= cells.size()) {
            return "";
        }
        return cells.get(index);
    }

    private boolean validateNganh(Nganh nganh, boolean checkDuplicate) {
        if (nganh == null) {
            lastError = "Dữ liệu ngành không hợp lệ";
            return false;
        }

        String maNganh = normalizeMaNganh(nganh.getMaNganh());
        String tenNganh = normalizeTenNganh(nganh.getTenNganh());

        if (maNganh.isBlank()) {
            lastError = "Mã ngành không được để trống";
            return false;
        }
        if (tenNganh.isBlank()) {
            lastError = "Tên ngành không được để trống";
            return false;
        }

        if (checkDuplicate && dao.getByMaNganh(maNganh) != null) {
            lastError = "Mã ngành đã tồn tại";
            return false;
        }

        if (nganh.getChiTieu() < 0) {
            lastError = "Chỉ tiêu phải là số nguyên lớn hơn hoặc bằng 0";
            return false;
        }

        if (!isNonNegativeDecimal(nganh.getDiemSan())) {
            lastError = "Điểm sàn phải là số không âm hoặc để trống";
            return false;
        }

        if (!isNonNegativeDecimal(nganh.getDiemTrungTuyen())) {
            lastError = "Điểm trúng tuyển phải là số không âm hoặc để trống";
            return false;
        }

        String tuyenThang = normalizeFlag(nganh.getTuyenThang());
        String dgnl = normalizeFlag(nganh.getDgnl());
        String thpt = normalizeFlag(nganh.getThpt());
        String vsat = normalizeFlag(nganh.getVsat());

        if (!isBinaryFlag(tuyenThang)) {
            lastError = "Cờ N_TuyểnThẳng chỉ nhận giá trị 0 hoặc 1";
            return false;
        }
        if (!isBinaryFlag(dgnl)) {
            lastError = "Cờ N_DGNL chỉ nhận giá trị 0 hoặc 1";
            return false;
        }
        if (!isBinaryFlag(thpt)) {
            lastError = "Cờ N_THPT chỉ nhận giá trị 0 hoặc 1";
            return false;
        }
        if (!isBinaryFlag(vsat)) {
            lastError = "Cờ N_VSAT chỉ nhận giá trị 0 hoặc 1";
            return false;
        }

        nganh.setMaNganh(trimMax(maNganh, 45));
        nganh.setTenNganh(trimMax(tenNganh, 100));
        nganh.setToHopGoc(trimMax(safe(nganh.getToHopGoc()), 3));
        nganh.setTuyenThang(tuyenThang);
        nganh.setDgnl(dgnl);
        nganh.setThpt(thpt);
        nganh.setVsat(vsat);
        nganh.setSlThpt(trimMax(safe(nganh.getSlThpt()), 45));
        lastError = "";
        return true;
    }

    private String normalizeMaNganh(String value) {
        String text = safe(value).replaceAll("\\s+", "").toUpperCase();
        return text.replace("CLC", "CLC");
    }

    private String normalizeTenNganh(String value) {
        String text = safe(value).replaceAll("\\s+", " ").trim();
        if (text.isBlank()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String part : text.split(" ")) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(capitalizeWord(part));
        }
        return result.toString();
    }

    private String capitalizeWord(String word) {
        if (word == null || word.isBlank()) {
            return "";
        }
        if (word.length() == 1) {
            return word.toUpperCase();
        }
        if (word.equals(word.toUpperCase())) {
            return word;
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    private boolean isNonNegativeDecimal(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) >= 0;
    }

    private boolean isBinaryFlag(String value) {
        return value == null || value.isBlank() || "0".equals(value) || "1".equals(value);
    }

    private String normalizeFlag(String value) {
        String normalized = safe(value);
        if (normalized.isBlank()) {
            return "";
        }
        return normalized.length() > 1 ? normalized.substring(0, 1) : normalized;
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        if (row == null || index < 0) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(index)).trim();
    }

    private int parseInt(String value) {
        String text = safe(value);
        if (text.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private BigDecimal parseDecimal(String value) {
        String text = safe(value);
        if (text.isBlank()) {
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

    private String trimMax(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String getPart(String[] parts, int idx) {
        if (parts == null || idx < 0 || idx >= parts.length || parts[idx] == null) {
            return "";
        }
        return parts[idx].trim();
    }

    private static class ImportRecord {
        private final int lineNo;
        private final ImportMode mode;
        private final Nganh nganh;

        private ImportRecord(int lineNo, ImportMode mode, Nganh nganh) {
            this.lineNo = lineNo;
            this.mode = mode;
            this.nganh = nganh;
        }
    }

    private enum ImportMode {
        AUTO,
        FULL,
        CHI_TIEU,
        NGUONG
    }
}
