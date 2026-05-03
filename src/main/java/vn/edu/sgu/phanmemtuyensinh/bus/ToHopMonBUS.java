package vn.edu.sgu.phanmemtuyensinh.bus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import vn.edu.sgu.phanmemtuyensinh.dal.ToHopMonDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ToHopMon;

public class ToHopMonBUS {
    private static final Pattern MA_TO_HOP_DETAIL_PATTERN = Pattern.compile("([A-Za-z0-9]+)\\(([^)]+)\\)");
    private static final Pattern MA_TO_HOP_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]{1,4}$");

    private final ToHopMonDAO dao;
    private String lastError = "";
    private String lastImportSummary = "";

    public ToHopMonBUS() {
        dao = new ToHopMonDAO();
    }

    // Lấy danh sách để hiển thị lên bảng (Table) ở giao diện
    public List<ToHopMon> getAll() {
        return dao.getAll();
    }

    // Logic xử lý trước khi thêm mới
    public boolean add(ToHopMon toHop) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        if (!validateToHopMon(toHop, true)) {
            return false;
        }
        return dao.add(toHop);
    }

    // Logic xử lý trước khi cập nhật
    public boolean update(ToHopMon toHop) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        if (toHop == null) {
            lastError = "Dữ liệu tổ hợp môn không hợp lệ";
            return false;
        }

        String maToHop = safe(toHop.getMaToHop()).toUpperCase(Locale.ROOT);
        ToHopMon existing = dao.getByMaToHop(maToHop);
        if (existing != null && existing.getIdToHop() != toHop.getIdToHop()) {
            lastError = "Mã tổ hợp '" + maToHop + "' đã tồn tại";
            return false;
        }

        if (!validateToHopMon(toHop, false)) {
            return false;
        }

        boolean ok = dao.update(toHop);
        if (!ok && safe(lastError).isBlank()) {
            lastError = "Cập nhật thất bại";
        }
        return ok;
    }

    // Logic xử lý trước khi xóa
    public boolean delete(int idToHop) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        return dao.delete(idToHop);
    }

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public String getLastImportSummary() {
        return lastImportSummary == null ? "" : lastImportSummary;
    }

    public List<ToHopMon> importDanhSachToHop(String filePath) throws IOException {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".xlsx")) {
            return importFromExcel(filePath);
        }
        return importFromTextLike(filePath);
    }

    private List<ToHopMon> importFromExcel(String filePath) throws IOException {
        List<ToHopMon> result = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                List<String> cells = new ArrayList<>();
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    String value = safe(formatter.formatCellValue(row.getCell(i)));
                    if (!value.isBlank()) {
                        cells.add(value);
                    }
                }

                ToHopMon parsed = parseToHopFromCells(cells);
                if (parsed != null) {
                    result.add(parsed);
                }
            }
        }

        return deduplicateByMaToHop(result);
    }

    private List<ToHopMon> importFromTextLike(String filePath) throws IOException {
        List<ToHopMon> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trim = line.trim();
                if (trim.isEmpty() || trim.startsWith("---") || trim.toLowerCase().startsWith("stt")) {
                    continue;
                }

                String[] parts = trim.contains("\t") ? trim.split("\\t+") : trim.split("\\s{2,}");
                List<String> cells = new ArrayList<>();
                for (String part : parts) {
                    String value = safe(part);
                    if (!value.isBlank()) {
                        cells.add(value);
                    }
                }

                ToHopMon parsed = parseToHopFromCells(cells);
                if (parsed != null) {
                    result.add(parsed);
                }
            }
        }

        return deduplicateByMaToHop(result);
    }

    private ToHopMon parseToHopFromCells(List<String> cells) {
        if (cells == null || cells.isEmpty()) {
            return null;
        }

        List<String> cleanCells = new ArrayList<>();
        for (String cell : cells) {
            String value = safe(cell);
            if (!value.isBlank()) {
                cleanCells.add(value);
            }
        }
        if (cleanCells.isEmpty() || looksLikeHeaderRow(cleanCells)) {
            return null;
        }

        String maToHop = "";
        int maIndex = -1;
        String detail = "";
        String tenToHop = "";
        String[] mons = new String[0];

        for (int i = 0; i < cleanCells.size(); i++) {
            String cell = cleanCells.get(i);
            if (cell.matches("^[A-Za-z0-9]+\\([^)]+\\)$")) {
                detail = cell;
            }
            if (maToHop.isBlank() && isMaToHopCode(cell)) {
                maToHop = cell.trim().toUpperCase(Locale.ROOT);
                maIndex = i;
            }
        }

        if (maToHop.isBlank() && !detail.isBlank()) {
            Matcher matcher = MA_TO_HOP_DETAIL_PATTERN.matcher(detail);
            if (matcher.find()) {
                maToHop = matcher.group(1).trim().toUpperCase(Locale.ROOT);
            }
        }

        // Format cột chuẩn: idtohop | matohop | mon1 | mon2 | mon3 | tentohop
        if (maIndex != -1 && maIndex + 3 < cleanCells.size()) {
            String m1 = normalizeMonCode(cleanCells.get(maIndex + 1));
            String m2 = normalizeMonCode(cleanCells.get(maIndex + 2));
            String m3 = normalizeMonCode(cleanCells.get(maIndex + 3));
            if (!m1.isBlank() && !m2.isBlank() && !m3.isBlank()) {
                mons = new String[] {m1, m2, m3};
                if (maIndex + 4 < cleanCells.size()) {
                    tenToHop = cleanCells.get(maIndex + 4);
                }
            }
        }

        // Format cũ: A00(TO-1,LI-1,HO-1)
        if (mons.length < 3 && !detail.isBlank()) {
            String[] parsed = parseMonTuMaToHop(detail);
            if (parsed.length >= 3) {
                mons = new String[] {
                    normalizeMonCode(parsed[0]),
                    normalizeMonCode(parsed[1]),
                    normalizeMonCode(parsed[2])
                };
            }
        }

        // Fallback từ cột môn dạng text nếu chưa parse đủ 3 môn
        if (mons.length < 3) {
            List<String> monFromText = new ArrayList<>();
            for (String cell : cleanCells) {
                if (cell.equalsIgnoreCase(maToHop)
                        || cell.equalsIgnoreCase(detail)
                        || cell.matches("^-?\\d+(\\.\\d+)?$")) {
                    continue;
                }

                if (cell.contains(",")) {
                    if (tenToHop.isBlank()) {
                        tenToHop = cell;
                    }
                    String[] parts = cell.split(",");
                    for (String part : parts) {
                        String mapped = normalizeMonCode(part);
                        if (!mapped.isBlank()) {
                            monFromText.add(mapped);
                        }
                    }
                    continue;
                }

                String mapped = normalizeMonCode(cell);
                if (!mapped.isBlank()) {
                    monFromText.add(mapped);
                } else if (tenToHop.isBlank()) {
                    tenToHop = cell;
                }
            }

            if (monFromText.size() >= 3) {
                mons = new String[] {monFromText.get(0), monFromText.get(1), monFromText.get(2)};
            }
        }

        if (maToHop.isBlank() || mons.length < 3) {
            return null;
        }

        if (tenToHop.isBlank() && !detail.isBlank()) {
            tenToHop = detail;
        }

        ToHopMon toHopMon = new ToHopMon();
        toHopMon.setMaToHop(maToHop);
        toHopMon.setMon1(trimMax(normalizeMonCode(mons[0]), 10));
        toHopMon.setMon2(trimMax(normalizeMonCode(mons[1]), 10));
        toHopMon.setMon3(trimMax(normalizeMonCode(mons[2]), 10));
        toHopMon.setTenToHop(trimMax(tenToHop, 100));
        return toHopMon;
    }

    private String[] parseMonTuMaToHop(String maToHopChiTiet) {
        Matcher matcher = MA_TO_HOP_DETAIL_PATTERN.matcher(maToHopChiTiet);
        if (!matcher.find()) {
            return new String[0];
        }

        String inside = matcher.group(2);
        String[] phanTu = inside.split(",");
        if (phanTu.length < 3) {
            return new String[0];
        }

        String[] mons = new String[3];
        for (int i = 0; i < 3; i++) {
            String item = phanTu[i].trim();
            String[] monVaHeSo = item.split("-");
            if (monVaHeSo.length == 0 || monVaHeSo[0].trim().isEmpty()) {
                return new String[0];
            }
            mons[i] = monVaHeSo[0].trim();
        }
        return mons;
    }

    public int importAndSaveToDatabase(String filePath) throws IOException {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            lastImportSummary = lastError;
            return 0;
        }

        List<ToHopMon> toHopList = importDanhSachToHop(filePath);
        int countSuccess = 0;
        int countFailed = 0;
        List<String> sampleErrors = new ArrayList<>();

        for (ToHopMon toHop : toHopList) {
            if (!validateToHopMon(toHop, false)) {
                countFailed++;
                if (sampleErrors.size() < 8) {
                    sampleErrors.add(getLastError());
                }
                continue;
            }

            ToHopMon existing = dao.getByMaToHop(toHop.getMaToHop());
            boolean ok;
            if (existing != null) {
                toHop.setIdToHop(existing.getIdToHop());
                ok = dao.update(toHop);
            } else {
                ok = dao.add(toHop);
            }

            if (ok) {
                countSuccess++;
            } else {
                countFailed++;
                if (sampleErrors.size() < 8) {
                    sampleErrors.add("Không lưu được tổ hợp " + toHop.getMaToHop());
                }
            }
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Tổng đọc: ").append(toHopList.size())
                .append(" | Thành công: ").append(countSuccess)
                .append(" | Thất bại: ").append(countFailed);
        if (!sampleErrors.isEmpty()) {
            summary.append("\nMột số lỗi:\n- ").append(String.join("\n- ", sampleErrors));
        }
        lastImportSummary = summary.toString();
        return countSuccess;
    }

    private List<ToHopMon> deduplicateByMaToHop(List<ToHopMon> rawList) {
        List<ToHopMon> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ToHopMon item : rawList) {
            if (item == null || safe(item.getMaToHop()).isBlank()) {
                continue;
            }
            if (seen.contains(item.getMaToHop())) {
                continue;
            }
            result.add(item);
            seen.add(item.getMaToHop());
        }
        return result;
    }

    private boolean validateToHopMon(ToHopMon toHop, boolean checkDuplicate) {
        if (toHop == null) {
            lastError = "Dữ liệu tổ hợp môn không hợp lệ";
            return false;
        }

        String maToHop = safe(toHop.getMaToHop()).toUpperCase();
        String mon1 = normalizeMonCode(safe(toHop.getMon1()));
        String mon2 = normalizeMonCode(safe(toHop.getMon2()));
        String mon3 = normalizeMonCode(safe(toHop.getMon3()));
        String ten = safe(toHop.getTenToHop());

        if (maToHop.isBlank()) {
            lastError = "Mã tổ hợp không được để trống";
            return false;
        }
        if (mon1.isBlank() || mon2.isBlank() || mon3.isBlank()) {
            lastError = "Mỗi tổ hợp phải có đủ 3 môn";
            return false;
        }
        if (checkDuplicate && dao.getByMaToHop(maToHop) != null) {
            lastError = "Mã tổ hợp đã tồn tại";
            return false;
        }

        toHop.setMaToHop(trimMax(maToHop, 45));
        toHop.setMon1(trimMax(mon1, 10));
        toHop.setMon2(trimMax(mon2, 10));
        toHop.setMon3(trimMax(mon3, 10));
        toHop.setTenToHop(trimMax(ten, 100));
        lastError = "";
        return true;
    }

    private String normalizeMonCode(String code) {
        String raw = safe(code);
        if (raw.isBlank()) {
            return "";
        }

        String c = raw.toUpperCase(Locale.ROOT);
        if (c.matches("^NK[0-9]{1,2}$")) {
            return c;
        }
        if (c.matches("^(TO|LI|HO|SI|SU|DI|VA|N1|TI|KTPL|CNCN|CNNN|KHTN|KHXH|DOC|GD)$")) {
            return c;
        }

        String key = toAsciiKey(raw).replaceAll("[^A-Z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        String compact = key.replace(" ", "");

        if (compact.equals("TOAN")) {
            return "TO";
        }
        if (c.equals("LY")) {
            return "LI";
        }
        if (compact.contains("VATLI") || compact.contains("VATLY") || compact.equals("LI")) {
            return "LI";
        }
        if (c.equals("SINH")) {
            return "SI";
        }
        if (compact.equals("SINHHOC") || compact.equals("SINH")) {
            return "SI";
        }
        if (compact.equals("HOA") || compact.equals("HOAHOC")) {
            return "HO";
        }
        if (compact.equals("LICHSU") || compact.equals("SU")) {
            return "SU";
        }
        if (c.equals("DIA")) {
            return "DI";
        }
        if (compact.equals("DIALI") || compact.equals("DIALY") || compact.equals("DIA")) {
            return "DI";
        }
        if (c.equals("NGUVAN") || c.equals("VAN")) {
            return "VA";
        }
        if (compact.equals("NGUVAN") || compact.equals("VAN")) {
            return "VA";
        }
        if (c.equals("NN") || c.equals("ANH")) {
            return "N1";
        }
        if (compact.contains("TIENG") || compact.equals("NGOAINGU") || compact.equals("ANH")) {
            return "N1";
        }
        if (c.equals("TIN")) {
            return "TI";
        }
        if (compact.equals("TINHOC") || compact.equals("TIN")) {
            return "TI";
        }
        if (c.equals("GDCD")) {
            return "KTPL";
        }
        if (compact.contains("GDKTPL") || compact.contains("GDKTPL") || compact.equals("GDCD")) {
            return "KTPL";
        }
        if (compact.contains("CONGNGHECONGNGHIEP")) {
            return "CNCN";
        }
        if (compact.contains("CONGNGHENONGNGHIEP")) {
            return "CNNN";
        }
        if (compact.contains("KHOAHOCTUNHIEN") || compact.equals("KHTN")) {
            return "KHTN";
        }
        if (compact.contains("KHOAHOCXAHOI") || compact.equals("KHXH")) {
            return "KHXH";
        }
        if (compact.contains("HINHHOA") || compact.contains("VEMYTHUAT")) {
            return "NK3";
        }
        if (compact.contains("TRANGTRI")) {
            return "NK4";
        }
        if (compact.contains("XUONGAM") || compact.contains("THAMAM") || compact.contains("TIETTAU")) {
            return "NK6";
        }
        if (compact.contains("HAT") || compact.contains("NHACCU")) {
            return "NK5";
        }
        if (compact.contains("TDTT") || compact.contains("THEDUCTHETHAO")) {
            return "NK7";
        }
        if (compact.contains("NANGKHIEU1") || compact.equals("NK1")) {
            return "NK1";
        }
        if (compact.contains("NANGKHIEU2") || compact.equals("NK2")) {
            return "NK2";
        }
        if (compact.contains("NANGKHIEU")) {
            return "NK1";
        }

        if (c.matches("^[A-Z0-9]{1,10}$")) {
            return c;
        }
        return "";
    }

    private boolean isMaToHopCode(String value) {
        String v = safe(value).toUpperCase(Locale.ROOT);
        return MA_TO_HOP_CODE_PATTERN.matcher(v).matches();
    }

    private boolean looksLikeHeaderRow(List<String> cells) {
        String joined = toAsciiKey(String.join(" ", cells));
        if (joined.contains("STT") && joined.contains("TO HOP")) {
            return true;
        }
        if (joined.contains("IDTOHOP") || joined.contains("MATOHOP")) {
            return true;
        }
        return joined.contains("MON CHI TIET") || (joined.contains("MON1") && joined.contains("MON2"));
    }

    private String toAsciiKey(String value) {
        String normalized = Normalizer.normalize(safe(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('Đ', 'D')
                .replace('đ', 'd');
        return normalized.toUpperCase(Locale.ROOT);
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
    
    public ToHopMon getByMaToHop(String maToHop)
    {
        return dao.getByMaToHop(maToHop);
    }
}