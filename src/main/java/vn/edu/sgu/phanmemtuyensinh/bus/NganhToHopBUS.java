package vn.edu.sgu.phanmemtuyensinh.bus;

import vn.edu.sgu.phanmemtuyensinh.dal.NganhToHopDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.NganhDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.ToHopMonDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NganhToHop;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.Nganh;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ToHopMon;
import vn.edu.sgu.phanmemtuyensinh.utils.DolechTable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class NganhToHopBUS {
    private final NganhToHopDAO dao = new NganhToHopDAO();
    private final NganhDAO nganhDAO = new NganhDAO();
    private final ToHopMonDAO toHopMonDAO = new ToHopMonDAO();
    private String lastError = "";
    private String lastImportSummary = "";

    public String getLastImportSummary() {
        return lastImportSummary;
    }

    public void importAndSaveToDatabase(String filePath) throws Exception {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            throw new Exception("Không có quyền thực hiện: " + lastError);
        }
        int success = 0;
        int failed = 0;
        int skippedEmpty = 0;
        StringBuilder failDetail = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Bỏ qua dòng tiêu đề

                Cell cellMaNganh = row.getCell(1); // Column B: MANGANH
                Cell cellMaToHop = row.getCell(5); // Column F: TEN_TO_HOP (chứa mã B03, C01...)

                String maNganh = getCellValueAsString(cellMaNganh);
                String maToHop = getCellValueAsString(cellMaToHop);
                
                String tenNganhChuan = getCellValueAsString(row.getCell(2)); // Cột C
                String laToHopGoc = getCellValueAsString(row.getCell(6)); // Cột G

                // Fallback nếu cột F trống, lấy từ cột D (MA_TO_HOP: B03(TO-3, VA-3, SI-1))
                if (maToHop.isEmpty()) {
                    String rawToHop = getCellValueAsString(row.getCell(3));
                    if (rawToHop.contains("(")) {
                        maToHop = rawToHop.substring(0, rawToHop.indexOf('(')).trim();
                    } else {
                        maToHop = rawToHop.trim();
                    }
                }

                if (maNganh.isEmpty() || maToHop.isEmpty()) {
                    skippedEmpty++;
                    continue;
                }

                NganhToHop nth = new NganhToHop();
                nth.setMaNganh(maNganh);
                nth.setMaToHop(maToHop);
                nth.setTenNganhChuan(tenNganhChuan);
                nth.setTenToHop(maToHop);
                nth.setLaToHopGoc(laToHopGoc);
                
                // Update xt_nganh toHopGoc if this row is Gốc
                if ("Gốc".equalsIgnoreCase(laToHopGoc) || "Goc".equalsIgnoreCase(laToHopGoc)) {
                    Nganh nganh = nganhDAO.getByMaNganh(maNganh);
                    if (nganh != null) {
                        nganh.setToHopGoc(maToHop);
                        nganhDAO.update(nganh);
                    }
                }

                // Các hệ số có thể parse từ chuỗi ở cột D nếu cần, nhưng hiện tại lấy mặc định là 1 (null -> 1)
                nth.setHsMon1(null);
                nth.setHsMon2(null);
                nth.setHsMon3(null);
                
                String doLechStr = getCellValueAsString(row.getCell(7)); // Cột H: Độ lệch
                if (!doLechStr.isEmpty()) {
                    try {
                        nth.setDoLech(new BigDecimal(doLechStr));
                    } catch (Exception e) {}
                }

                // Kiểm tra xem đã tồn tại chưa
                NganhToHop existing = dao.getByTbKeys(buildTbKeys(maNganh, maToHop));
                boolean result;
                if (existing != null) {
                    nth.setId(existing.getId());
                    result = update(nth);
                } else {
                    result = add(nth);
                }

                if (result) {
                    success++;
                } else {
                    failed++;
                    if (failDetail.length() < 1000) {
                        failDetail.append("Dòng ").append(row.getRowNum() + 1)
                                .append(": ").append(lastError).append("\n");
                    }
                }
            }
        }
        lastImportSummary = String.format("Thành công: %d dòng\nThất bại: %d dòng\nBỏ qua dòng trống: %d\n\nChi tiết lỗi:\n%s", 
                success, failed, skippedEmpty, failDetail.toString());
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public List<NganhToHop> getAll() {
        return dao.getAll();
    }

    public List<NganhToHop> getByMaNganh(String maNganh) {
        return dao.getByMaNganh(maNganh);
    }

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public BigDecimal previewDoLech(String maNganh, String maToHop) {
        String ma = safeUpper(maNganh);
        String th = safeUpper(maToHop);
        if (ma.isEmpty() || th.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Nganh nganh = nganhDAO.getByMaNganh(ma);
        if (nganh == null || nganh.getToHopGoc() == null || nganh.getToHopGoc().isBlank()) {
            return BigDecimal.ZERO;
        }
        return DolechTable.getDoLech(nganh.getToHopGoc(), th);
    }

    public boolean add(NganhToHop nth) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        if (!validateAndNormalize(nth, true)) {
            return false;
        }
        return dao.add(nth);
    }

    public boolean update(NganhToHop nth) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        if (!validateAndNormalize(nth, false)) {
            return false;
        }
        return dao.update(nth);
    }

    public boolean delete(int id) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        return dao.delete(id);
    }

    private boolean validateAndNormalize(NganhToHop nth, boolean isAdd) {
        lastError = "";
        if (nth == null) {
            lastError = "Dữ liệu rỗng";
            return false;
        }

        String maNganh = safeUpper(nth.getMaNganh());
        String maToHop = safeUpper(nth.getMaToHop());
        if (maNganh.isEmpty()) {
            lastError = "Mã ngành không được để trống";
            return false;
        }
        if (maToHop.isEmpty()) {
            lastError = "Mã tổ hợp không được để trống";
            return false;
        }

        Nganh nganh = nganhDAO.getByMaNganh(maNganh);
        if (nganh == null) {
            lastError = "Không tìm thấy ngành với mã: " + maNganh;
            return false;
        }

        ToHopMon th = toHopMonDAO.getByMaToHop(maToHop);
        if (th == null) {
            lastError = "Không tìm thấy tổ hợp môn với mã: " + maToHop;
            return false;
        }

        // Normalize core fields
        nth.setMaNganh(maNganh);
        nth.setMaToHop(maToHop);
        nth.setTbKeys(buildTbKeys(maNganh, maToHop));
        
        // Tự động cập nhật tên chuẩn từ danh mục gốc
        nth.setTenNganhChuan(nganh.getTenNganh());
        nth.setTenToHop(th.getTenToHop());

        // Always align subject list with xt_tohop_monthi
        nth.setThMon1(safeUpper(th.getMon1()));
        nth.setThMon2(safeUpper(th.getMon2()));
        nth.setThMon3(safeUpper(th.getMon3()));

        // Default weights
        nth.setHsMon1(normalizeWeight(nth.getHsMon1()));
        nth.setHsMon2(normalizeWeight(nth.getHsMon2()));
        nth.setHsMon3(normalizeWeight(nth.getHsMon3()));

        // Subject flags
        resetSubjectFlags(nth);
        applySubjectFlag(nth, nth.getThMon1());
        applySubjectFlag(nth, nth.getThMon2());
        applySubjectFlag(nth, nth.getThMon3());

        // dolech based on nganh's tohop goc only if not explicitly set
        if (nth.getDoLech() == null) {
            BigDecimal doLech = BigDecimal.ZERO;
            if (nganh.getToHopGoc() != null && !nganh.getToHopGoc().isBlank()) {
                doLech = DolechTable.getDoLech(nganh.getToHopGoc(), maToHop);
            }
            nth.setDoLech(doLech);
        }

        // Unique key check
        int currentId = isAdd ? -1 : nth.getId();
        if (!isAdd && currentId <= 0) {
            lastError = "Thiếu ID để cập nhật";
            return false;
        }
        if (dao.existsTbKeysExceptId(nth.getTbKeys(), isAdd ? -1 : currentId)) {
            lastError = "Mapping ngành-tổ hợp đã tồn tại (trùng khóa: " + nth.getTbKeys() + ")";
            return false;
        }

        return true;
    }

    private Integer normalizeWeight(Integer w) {
        if (w == null || w <= 0) {
            return 1;
        }
        return w;
    }

    private String buildTbKeys(String maNganh, String maToHop) {
        return safeUpper(maNganh) + "_" + safeUpper(maToHop);
    }

    private void resetSubjectFlags(NganhToHop nth) {
        nth.setN1(0);
        nth.setTo(0);
        nth.setLi(0);
        nth.setHo(0);
        nth.setSi(0);
        nth.setVa(0);
        nth.setSu(0);
        nth.setDi(0);
        nth.setTi(0);
        nth.setGdcd(0);
        nth.setKtpl(0);
        nth.setCncn(0);
        nth.setCnnn(0);
        nth.setKhac(0);
    }

    private void applySubjectFlag(NganhToHop nth, String monCode) {
        String m = safeUpper(monCode);
        if (m.isEmpty()) {
            return;
        }
        switch (m) {
            case "N1" -> nth.setN1(1);
            case "TO" -> nth.setTo(1);
            case "LI" -> nth.setLi(1);
            case "HO" -> nth.setHo(1);
            case "SI" -> nth.setSi(1);
            case "VA" -> nth.setVa(1);
            case "SU" -> nth.setSu(1);
            case "DI" -> nth.setDi(1);
            case "TI" -> nth.setTi(1);
            case "GDCD" -> nth.setGdcd(1);
            case "KTPL" -> nth.setKtpl(1);
            case "CNCN" -> nth.setCncn(1);
            case "CNNN" -> nth.setCnnn(1);
            default -> nth.setKhac(1);
        }
    }

    private String safeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }
}
