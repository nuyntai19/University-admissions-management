package vn.edu.sgu.phanmemtuyensinh.bus;

import vn.edu.sgu.phanmemtuyensinh.dal.BangQuyDoiDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BangQuyDoiBUS {
    private final BangQuyDoiDAO dao = new BangQuyDoiDAO();
    private String lastError = "";

    /**
     * Import dữ liệu bách phân vị ĐGNL từ file văn bản (.txt)
     * Hỗ trợ định dạng bảng | STT | Phân vị | ... || Phân vị | THPT | ... |
     */
    public String importDGNLFromTextFile(String filePath) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return "Lỗi phân quyền: " + lastError;
        }

        int count = 0;
        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get(filePath));
            String currentToHop = "";
            java.util.regex.Pattern toHopPattern = java.util.regex.Pattern.compile("TỔ HỢP ([A-Z0-9]+)");

            for (String line : lines) {
                line = line.trim();
                java.util.regex.Matcher m = toHopPattern.matcher(line);
                if (m.find()) {
                    currentToHop = m.group(1);
                    continue;
                }

                if (line.startsWith("|") && line.contains("||")) {
                    String[] parts = line.split("\\|\\|");
                    if (parts.length == 2) {
                        String left = parts[0].substring(1).trim();
                        String right = parts[1].substring(0, parts[1].length() - 1).trim();
                        String[] leftCols = left.split("\\|");
                        String[] rightCols = right.split("\\|");

                        if (leftCols.length >= 3 && rightCols.length >= 3) {
                            try {
                                String phanViStr = leftCols[0].trim().replace("%", "");
                                String dgnlMax = leftCols[1].trim();
                                String dgnlMin = leftCols[2].trim();
                                String thptMax = rightCols[1].trim().replace(",", ".");
                                String thptMin = rightCols[2].trim().replace(",", ".");

                                BangQuyDoi bqd = new BangQuyDoi();
                                bqd.setDPhuongThuc("ĐGNL");
                                bqd.setDToHop(currentToHop.isEmpty() ? "CHUNG" : currentToHop);
                                bqd.setDPhanVi(phanViStr + "%");
                                bqd.setDDiemA(new BigDecimal(dgnlMin));
                                bqd.setDDiemB(new BigDecimal(dgnlMax));
                                bqd.setDDiemC(new BigDecimal(thptMin));
                                bqd.setDDiemD(new BigDecimal(thptMax));
                                
                                if (add(bqd)) count++;
                            } catch (Exception e) { /* Bỏ qua dòng header hoặc rác */ }
                        }
                    }
                }
            }
            return "Import thành công " + count + " bản ghi bách phân vị ĐGNL từ file text!";
        } catch (Exception e) {
            return "Lỗi khi đọc file: " + e.getMessage();
        }
    }

    public List<BangQuyDoi> getAll() {
        return dao.getAll();
    }

    public List<BangQuyDoi> getByPhuongThuc(String phuongThuc) {
        if (phuongThuc == null || phuongThuc.isBlank()) {
            return getAll();
        }
        return dao.getByPhuongThuc(phuongThuc.trim());
    }

    public BangQuyDoi getByMaQuyDoi(String maQuyDoi) {
        return dao.getByMaQuyDoi(maQuyDoi);
    }

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public boolean add(BangQuyDoi bqd) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        if (!validateAndNormalize(bqd, true)) {
            return false;
        }
        return dao.add(bqd);
    }

    public boolean update(BangQuyDoi bqd) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        if (!validateAndNormalize(bqd, false)) {
            return false;
        }
        return dao.update(bqd);
    }

    public boolean delete(int idQd) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        return dao.delete(idQd);
    }

    /**
     * Quy đổi điểm theo phương pháp bách phân vị + nội suy tuyến tính.
     */
    public BigDecimal quyDoiNoiSuy(String phuongThuc, String toHop, String mon, BigDecimal x) {
        return quyDoiNoiSuyCached(phuongThuc, toHop, mon, x, null);
    }

    /**
     * Phiên bản tối ưu: Sử dụng Map cache để tra cứu cực nhanh.
     * @param cache Map được nhóm theo key: PHUONGTHUC_TOHOP_MON
     */
    public BigDecimal quyDoiNoiSuyCached(String phuongThuc, String toHop, String mon, BigDecimal x, Map<String, List<BangQuyDoi>> cache) {
        lastError = "";
        if (x == null) return null;
        
        String pt = norm(phuongThuc);
        String th = emptyToNull(norm(toHop));
        String m = emptyToNull(norm(mon));

        BangQuyDoi interval = null;
        if (cache != null) {
            // Build key để tra cứu nhanh trong Map
            String key = pt + (th != null ? "_" + th : "") + (m != null ? "_" + m : "");
            List<BangQuyDoi> subList = cache.get(key);
            
            if (subList != null) {
                for (BangQuyDoi bqd : subList) {
                    if (x.compareTo(bqd.getDDiemA()) >= 0 && x.compareTo(bqd.getDDiemB()) <= 0) {
                        interval = bqd;
                        break;
                    }
                }
            }
        } else {
            // Fallback DB
            interval = dao.findIntervalExclusiveLower(pt, th, m, x);
            if (interval == null) interval = dao.findIntervalInclusive(pt, th, m, x);
        }

        if (interval == null) {
            lastError = "Không tìm thấy khoảng quy đổi phù hợp";
            return null;
        }

        BigDecimal a = interval.getDDiemA();
        BigDecimal b = interval.getDDiemB();
        BigDecimal c = interval.getDDiemC();
        BigDecimal d = interval.getDDiemD();
        
        if (a == null || b == null || c == null || d == null || b.compareTo(a) == 0) return d;

        MathContext mc = MathContext.DECIMAL64;
        BigDecimal ratio = x.subtract(a, mc).divide(b.subtract(a, mc), 10, RoundingMode.HALF_UP);
        BigDecimal y = c.add(ratio.multiply(d.subtract(c, mc), mc), mc);
        return y.setScale(5, RoundingMode.HALF_UP);
    }

    private boolean validateAndNormalize(BangQuyDoi bqd, boolean isAdd) {
        lastError = "";
        if (bqd == null) {
            lastError = "Dữ liệu rỗng";
            return false;
        }

        String pt = norm(bqd.getDPhuongThuc());
        String th = norm(bqd.getDToHop());
        String mon = norm(bqd.getDMon());
        String pv = norm(bqd.getDPhanVi());

        if (pt.isEmpty()) {
            lastError = "d_phuongthuc không được để trống";
            return false;
        }
        if (pv.isEmpty()) {
            lastError = "d_phanvi không được để trống";
            return false;
        }

        if (bqd.getDDiemA() == null || bqd.getDDiemB() == null || bqd.getDDiemC() == null || bqd.getDDiemD() == null) {
            lastError = "Thiếu dữ liệu mốc quy đổi (a,b,c,d)";
            return false;
        }
        if (bqd.getDDiemB().compareTo(bqd.getDDiemA()) <= 0) {
            lastError = "Yêu cầu d_diema < d_diemb";
            return false;
        }

        // Normalize optional fields: store null for blank
        bqd.setDPhuongThuc(pt);
        bqd.setDToHop(th.isEmpty() ? null : th);
        bqd.setDMon(mon.isEmpty() ? null : mon);
        bqd.setDPhanVi(pv);

        // Derived key for consistency
        String derivedKey = buildMaQuyDoi(pt, bqd.getDToHop(), bqd.getDMon(), pv);
        bqd.setDMaQuyDoi(derivedKey);

        // Uniqueness guard (best-effort, DB also has unique constraint)
        BangQuyDoi existing = dao.getByMaQuyDoi(derivedKey);
        if (existing != null) {
            if (isAdd) {
                lastError = "Bản ghi quy đổi đã tồn tại (trùng khóa: " + derivedKey + ")";
                return false;
            }
            if (existing.getIdQd() != bqd.getIdQd()) {
                lastError = "Bản ghi quy đổi đã tồn tại (trùng khóa: " + derivedKey + ")";
                return false;
            }
        }

        return true;
    }

    private String buildMaQuyDoi(String phuongThuc, String toHop, String mon, String phanVi) {
        StringBuilder sb = new StringBuilder();
        sb.append(norm(phuongThuc));
        if (toHop != null && !toHop.isBlank()) {
            sb.append('_').append(norm(toHop));
        }
        if (mon != null && !mon.isBlank()) {
            sb.append('_').append(norm(mon));
        }
        sb.append('_').append(norm(phanVi));
        return sb.toString();
    }

    private String norm(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    /**
     * Import toàn bộ bảng quy đổi V-SAT 8 môn (đúng theo Công văn 339/KTĐGQG-PTCCKTĐG).
     * Bản ghi đã tồn tại sẽ được bỏ qua (không update).
     */
    public String importVSATFromDataFile() {
        // Dữ liệu V-SAT: {monCode, phanVi, a, b, c, d}
        // Nguồn: Quy doi diem thi V-SAT 2025.txt
        String[][] vsatData = {
            // --- Toán (TO) ---
            {"TO","3%",   "132",   "150",   "8.5",  "10"},
            {"TO","5%",   "128.5", "132",   "8.1",  "8.5"},
            {"TO","10%",  "122.5", "128.5", "7.75", "8.1"},
            {"TO","20%",  "114.5", "122.5", "7.0",  "7.75"},
            {"TO","30%",  "108",   "114.5", "6.6",  "7.0"},
            {"TO","40%",  "102.5", "108",   "6.25", "6.6"},
            {"TO","50%",  "97",    "102.5", "6.0",  "6.25"},
            {"TO","60%",  "91",    "97",    "5.6",  "6.0"},
            {"TO","70%",  "85",    "91",    "5.25", "5.6"},
            {"TO","80%",  "77",    "85",    "5.0",  "5.25"},
            {"TO","90%",  "68",    "77",    "4.5",  "5.0"},
            {"TO",">90%", "6",     "68",    "1.5",  "4.5"},
            // --- Vật lí (LI) ---
            {"LI","3%",   "123",   "147",   "9.5",  "10"},
            {"LI","5%",   "118.5", "123",   "9.25", "9.5"},
            {"LI","10%",  "112.5", "118.5", "9.0",  "9.25"},
            {"LI","20%",  "105",   "112.5", "8.5",  "9.0"},
            {"LI","30%",  "99.5",  "105",   "8.0",  "8.5"},
            {"LI","40%",  "94.5",  "99.5",  "7.75", "8.0"},
            {"LI","50%",  "90",    "94.5",  "7.5",  "7.75"},
            {"LI","60%",  "85",    "90",    "7.25", "7.5"},
            {"LI","70%",  "80",    "85",    "6.75", "7.25"},
            {"LI","80%",  "74",    "80",    "6.35", "6.75"},
            {"LI","90%",  "66.5",  "74",    "5.75", "6.35"},
            {"LI",">90%", "17",    "66.5",  "3.05", "5.75"},
            // --- Hóa học (HO) ---
            {"HO","3%",   "129",   "150",   "9.5",  "10"},
            {"HO","5%",   "124.5", "129",   "9.25", "9.5"},
            {"HO","10%",  "117",   "124.5", "8.75", "9.25"},
            {"HO","20%",  "107.5", "117",   "8.25", "8.75"},
            {"HO","30%",  "100.5", "107.5", "7.75", "8.25"},
            {"HO","40%",  "94",    "100.5", "7.25", "7.75"},
            {"HO","50%",  "88",    "94",    "6.75", "7.25"},
            {"HO","60%",  "81.5",  "88",    "6.25", "6.75"},
            {"HO","70%",  "75.5",  "81.5",  "5.75", "6.25"},
            {"HO","80%",  "68.5",  "75.5",  "5.25", "5.75"},
            {"HO","90%",  "59.5",  "68.5",  "4.6",  "5.25"},
            {"HO",">90%", "20",    "59.5",  "1.35", "4.6"},
            // --- Sinh học (SI) ---
            {"SI","3%",   "130.5", "150",   "9.0",  "9.75"},
            {"SI","5%",   "126.5", "130.5", "8.75", "9.0"},
            {"SI","10%",  "120.5", "126.5", "8.34", "8.75"},
            {"SI","20%",  "112.5", "120.5", "7.85", "8.34"},
            {"SI","30%",  "105.5", "112.5", "7.5",  "7.85"},
            {"SI","40%",  "100",   "105.5", "7.25", "7.5"},
            {"SI","50%",  "94.5",  "100",   "6.85", "7.25"},
            {"SI","60%",  "88.5",  "94.5",  "6.5",  "6.85"},
            {"SI","70%",  "82.5",  "88.5",  "6.25", "6.5"},
            {"SI","80%",  "76",    "82.5",  "5.85", "6.25"},
            {"SI","90%",  "66.5",  "76",    "5.25", "5.85"},
            {"SI",">90%", "26.5",  "66.5",  "2.8",  "5.25"},
            // --- Lịch sử (SU) ---
            {"SU","3%",   "133.5", "150",   "9.75", "10"},
            {"SU","5%",   "131",   "133.5", "9.5",  "9.75"},
            {"SU","10%",  "126.5", "131",   "9.25", "9.5"},
            {"SU","20%",  "120.5", "126.5", "9.0",  "9.25"},
            {"SU","30%",  "115",   "120.5", "8.5",  "9.0"},
            {"SU","40%",  "110",   "115",   "8.25", "8.5"},
            {"SU","50%",  "105.5", "110",   "8.0",  "8.25"},
            {"SU","60%",  "101",   "105.5", "7.75", "8.0"},
            {"SU","70%",  "95.5",  "101",   "7.5",  "7.75"},
            {"SU","80%",  "88.5",  "95.5",  "7.0",  "7.5"},
            {"SU","90%",  "79.5",  "88.5",  "6.35", "7.0"},
            {"SU",">90%", "36.5",  "79.5",  "2.95", "6.35"},
            // --- Địa lí (DI) ---
            {"DI","3%",   "124",   "141",   "10",   "10"},
            {"DI","5%",   "120.5", "124",   "10",   "10"},
            {"DI","10%",  "115.5", "120.5", "9.75", "10"},
            {"DI","20%",  "108.5", "115.5", "9.25", "9.75"},
            {"DI","30%",  "103",   "108.5", "9.0",  "9.25"},
            {"DI","40%",  "98.5",  "103",   "8.75", "9.0"},
            {"DI","50%",  "94",    "98.5",  "8.5",  "8.75"},
            {"DI","60%",  "89.5",  "94",    "8.25", "8.5"},
            {"DI","70%",  "84.5",  "89.5",  "7.75", "8.25"},
            {"DI","80%",  "79",    "84.5",  "7.25", "7.75"},
            {"DI","90%",  "71",    "79",    "6.5",  "7.25"},
            {"DI",">90%", "31",    "71",    "3.0",  "6.5"},
            // --- Tiếng Anh (N1_THI) ---
            {"N1_THI","3%",   "131",   "150",   "7.75", "9.75"},
            {"N1_THI","5%",   "127.5", "131",   "7.5",  "7.75"},
            {"N1_THI","10%",  "120.5", "127.5", "7.0",  "7.5"},
            {"N1_THI","20%",  "112",   "120.5", "6.5",  "7.0"},
            {"N1_THI","30%",  "105",   "112",   "6.0",  "6.5"},
            {"N1_THI","40%",  "98.5",  "105",   "5.75", "6.0"},
            {"N1_THI","50%",  "92",    "98.5",  "5.5",  "5.75"},
            {"N1_THI","60%",  "85.5",  "92",    "5.25", "5.5"},
            {"N1_THI","70%",  "78.5",  "85.5",  "5.0",  "5.25"},
            {"N1_THI","80%",  "70.5",  "78.5",  "4.5",  "5.0"},
            {"N1_THI","90%",  "60",    "70.5",  "4.0",  "4.5"},
            {"N1_THI",">90%", "20.5",  "60",    "1.25", "4.0"},
            // --- Ngữ văn (VA) ---
            {"VA","3%",   "129.5", "146",   "9.25", "9.75"},
            {"VA","5%",   "127.5", "129.5", "9.0",  "9.25"},
            {"VA","10%",  "124",   "127.5", "9.0",  "9.0"},
            {"VA","20%",  "119.5", "124",   "8.75", "9.0"},
            {"VA","30%",  "115.5", "119.5", "8.5",  "8.75"},
            {"VA","40%",  "112.5", "115.5", "8.25", "8.5"},
            {"VA","50%",  "109",   "112.5", "8.0",  "8.25"},
            {"VA","60%",  "106",   "109",   "7.75", "8.0"},
            {"VA","70%",  "102",   "106",   "7.5",  "7.75"},
            {"VA","80%",  "97",    "102",   "7.25", "7.5"},
            {"VA","90%",  "90",    "97",    "6.75", "7.25"},
            {"VA",">90%", "5",     "90",    "3.5",  "6.75"},
        };

        int success = 0, skipped = 0, failed = 0;
        for (String[] row : vsatData) {
            BangQuyDoi bqd = new BangQuyDoi();
            bqd.setDPhuongThuc("V-SAT");
            bqd.setDToHop(null);
            bqd.setDMon(row[0]);
            bqd.setDPhanVi(row[1]);
            bqd.setDDiemA(new BigDecimal(row[2]));
            bqd.setDDiemB(new BigDecimal(row[3]));
            bqd.setDDiemC(new BigDecimal(row[4]));
            bqd.setDDiemD(new BigDecimal(row[5]));

            // Build key
            String key = "V-SAT_" + row[0] + "_" + row[1];
            bqd.setDMaQuyDoi(key);

            // Skip if exists
            if (dao.getByMaQuyDoi(key) != null) {
                skipped++;
                continue;
            }

            if (dao.add(bqd)) {
                success++;
            } else {
                failed++;
            }
        }

        return String.format("Import V-SAT hoàn tất!\n\n✅ Thêm mới: %d bản ghi\n⏭️ Bỏ qua (đã có): %d bản ghi\n❌ Thất bại: %d bản ghi\n\nTổng cộng 8 môn × 12 khoảng = 96 bản ghi.", success, skipped, failed);
    }

    /**
     * Import danh sách quy đổi điểm ngoại ngữ từ file Excel.
     * CCCD trong file có thể là TS_xxx → tra bảng thí sinh để lấy CCCD thật.
     * Dữ liệu lưu vào bảng DiemCong (diemCC = điểm quy đổi chứng chỉ).
     * Trả về chuỗi tóm tắt kết quả.
     */
    public String importTiengAnhFromExcel(String filePath) {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return "Lỗi phân quyền: " + lastError;
        }

        vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO thiSinhDAO =
                new vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO();
        java.util.Map<String, String> sbdToCccd = thiSinhDAO.getAllSbdToCccdMap();

        vn.edu.sgu.phanmemtuyensinh.dal.DiemCongXetTuyenDAO dcDao =
                new vn.edu.sgu.phanmemtuyensinh.dal.DiemCongXetTuyenDAO();

        int success = 0, failed = 0, skipNoSbd = 0;
        org.apache.poi.ss.usermodel.DataFormatter fmt =
                new org.apache.poi.ss.usermodel.DataFormatter();

        try (java.io.FileInputStream fis = new java.io.FileInputStream(filePath);
             org.apache.poi.ss.usermodel.Workbook wb =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {

            // Tìm sheet chứa dữ liệu quy đổi
            org.apache.poi.ss.usermodel.Sheet sheet = null;
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                String name = wb.getSheetName(s).toLowerCase().replaceAll("[^a-z0-9]", "");
                if (name.contains("quydo") || name.contains("import") || name.contains("tieng")) {
                    sheet = wb.getSheetAt(s); break;
                }
            }
            if (sheet == null) sheet = wb.getSheetAt(0);

            // Tìm header row
            int headerIdx = -1;
            java.util.Map<String, Integer> hmap = new java.util.HashMap<>();
            for (int i = 0; i <= Math.min(5, sheet.getLastRowNum()); i++) {
                org.apache.poi.ss.usermodel.Row r = sheet.getRow(i);
                if (r == null) continue;
                java.util.Map<String, Integer> tmp = new java.util.HashMap<>();
                for (int c = 0; c < r.getLastCellNum(); c++) {
                    String k = normalizeHdr(fmt.formatCellValue(r.getCell(c)));
                    if (!k.isBlank()) tmp.put(k, c);
                }
                if (tmp.containsKey("cccd") || tmp.containsKey("tt") || tmp.containsKey("chungchi")) {
                    headerIdx = i; hmap = tmp; break;
                }
            }
            if (headerIdx < 0) return "Không tìm thấy header trong file!";

            // DEBUG: in ra header map để kiểm tra
            System.out.println("=== HEADER MAP (Tiếng Anh) ===");
            for (java.util.Map.Entry<String, Integer> entry : hmap.entrySet()) {
                System.out.println("  Key: '" + entry.getKey() + "' -> Col: " + entry.getValue());
            }

            for (int i = headerIdx + 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                String rawCccd = cellVal(row, hmap, fmt, "cccd");
                if (rawCccd.isEmpty()) continue;

                // Fix CCCD TS_xxx → CCCD thật
                String cccd = rawCccd;
                if (cccd.toUpperCase().startsWith("TS_")) {
                    String real = sbdToCccd.get(cccd.toUpperCase());
                    if (real != null) cccd = real;
                    else { skipNoSbd++; continue; }
                }

                // Excel: "Chứng chỉ ngoại ngữ" -> normalized: "chungchingoaingu"
                String tenChungChi = cellVal(row, hmap, fmt,
                        "chungchingoaingu", "chungchi", "chungchingoaingu");
                // Excel: "Điểm/ Bậc chứng chỉ" -> normalized: "diembacchungchi"
                String mucDat = cellVal(row, hmap, fmt,
                        "diembacchungchi", "diemchungchi", "bacchungchi");
                // Excel: "Điểm Quy đổi" -> normalized: "diemquydoi"
                BigDecimal diemQuyDoi = parseBig(cellVal(row, hmap, fmt,
                        "diemquydoi", "diemquy"));
                // Excel: "Điểm cộng" -> normalized: "diemcong"
                BigDecimal diemCong = parseBig(cellVal(row, hmap, fmt,
                        "diemcong"));

                // DEBUG: in 1 dòng đầu
                if (i == headerIdx + 1) {
                    System.out.println("=== ROW 1 DATA ===");
                    System.out.println("  CCCD=" + cccd + " ChungChi=" + tenChungChi
                        + " MucDat=" + mucDat + " QuyDoi=" + diemQuyDoi + " DiemCong=" + diemCong);
                }

                vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen dc =
                        new vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen();
                dc.setTsCccd(cccd);
                dc.setChungChi(tenChungChi);            // chung_chi_ngoai_ngu
                dc.setMucDatDuoc(mucDat);               // diem_chung_chi
                dc.setDiemQuyDoiChungChi(diemQuyDoi);   // diem_quy_doi_chung_chi
                dc.setCoChungChi(tenChungChi != null && !tenChungChi.isEmpty());
                dc.setDiemCC(diemCong != null ? diemCong : BigDecimal.ZERO);
                dc.setDiemUtxt(BigDecimal.ZERO);
                dc.setDiemTong(dc.getDiemCC());
                dc.setDcKeys("CC_" + cccd);

                if (dcDao.saveOrUpdate(dc)) {
                    success++;
                }
                else failed++;
            }
        } catch (Exception e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return "Lỗi: " + e.getMessage();
        }

        return String.format("Import Ngoại ngữ hoàn tất!\n✅ Thành công: %d\n⏭️ Bỏ qua (không tìm thấy SBD): %d\n❌ Thất bại: %d",
                success, skipNoSbd, failed);
    }

    private String normalizeHdr(String s) {
        if (s == null) return "";
        try {
            String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("").toLowerCase().replaceAll("đ", "d").replaceAll("[^a-z0-9]", "");
        } catch (Exception e) {
            return s.toLowerCase().replaceAll("[^a-z0-9]", "");
        }
    }

    private String cellVal(org.apache.poi.ss.usermodel.Row row,
                           java.util.Map<String, Integer> hmap,
                           org.apache.poi.ss.usermodel.DataFormatter fmt,
                           String... keys) {
        for (String k : keys) {
            Integer idx = hmap.get(normalizeHdr(k));
            if (idx != null) {
                String v = fmt.formatCellValue(row.getCell(idx)).trim();
                if (!v.isEmpty()) return v;
            }
        }
        return "";
    }

    private BigDecimal parseBig(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.replace(",", ".")); } catch (Exception e) { return null; }
    }
}
