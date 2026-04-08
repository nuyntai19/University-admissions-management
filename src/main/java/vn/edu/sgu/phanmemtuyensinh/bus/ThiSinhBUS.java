package vn.edu.sgu.phanmemtuyensinh.bus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;
import vn.edu.sgu.phanmemtuyensinh.dal.DiemThiXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen;

public class ThiSinhBUS {

    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern DATASET_CODE_PATTERN = Pattern.compile("^TS_\\d{1,10}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final ThiSinhDAO dao = new ThiSinhDAO();
    private String lastError = "";
    private String lastImportSummary = "";
    private final DiemThiXetTuyenDAO diemDAO = new DiemThiXetTuyenDAO();

    public List<ThiSinh> getAll() {
        return dao.getAll();
    }

    public ThiSinh getByCccd(String cccd) {
        return dao.getByCccd(cccd);
    }

    public ThiSinh getById(int idThiSinh) {
        return dao.getById(idThiSinh);
    }

    public List<ThiSinh> getPage(int page, int pageSize) {
        return dao.getPage(page, pageSize);
    }

    public long countAll() {
        return dao.countAll();
    }

    public List<ThiSinh> searchByHoTen(String hoTen) {
        return dao.searchByHoTen(hoTen);
    }

    public List<ThiSinh> searchByKeyword(String keyword, int page, int pageSize) {
        return dao.searchByKeyword(keyword, page, pageSize);
    }

    public long countByKeyword(String keyword) {
        return dao.countByKeyword(keyword);
    }

    public boolean add(ThiSinh ts) {
        if (!validateThiSinh(ts, true)) {
            return false;
        }
        return dao.add(ts);
    }

    public boolean update(ThiSinh ts) {
        if (!validateThiSinh(ts, false)) {
            return false;
        }
        return dao.update(ts);
    }

    public boolean delete(int idThiSinh) {
        return dao.delete(idThiSinh);
    }

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public int importAndSaveToDatabase(String filePath) throws IOException {
        List<ThiSinh> list;
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".xlsx")) {
            list = importFromExcel(filePath);
        } else {
            list = importFromTextLike(filePath);
        }

        int imported = 0;
        int failed = 0;
        int diemSyncFailed = 0;
        for (ThiSinh ts : list) {
            if (add(ts)) {
                if (!syncDiemThiRecord(ts)) {
                    diemSyncFailed++;
                }
                imported++;
            } else {
                failed++;
            }
        }
        lastImportSummary = "Tổng đọc: " + list.size()
                + " | Thành công: " + imported
                + " | Thất bại: " + failed
                + " | Lỗi đồng bộ điểm thi: " + diemSyncFailed;
        return imported;
    }

    public String getLastImportSummary() {
        return lastImportSummary == null ? "" : lastImportSummary;
    }

    private boolean syncDiemThiRecord(ThiSinh ts) {
        if (ts == null || safe(ts.getCccd()).isEmpty()) {
            return false;
        }

        DiemThiXetTuyen existing = diemDAO.getByCccd(ts.getCccd());
        if (existing != null) {
            existing.setSoBaoDanh(ts.getSoBaoDanh());
            return diemDAO.update(existing);
        }

        DiemThiXetTuyen diem = new DiemThiXetTuyen();
        diem.setCccd(ts.getCccd());
        diem.setSoBaoDanh(ts.getSoBaoDanh());
        return diemDAO.add(diem);
    }

    private List<ThiSinh> importFromExcel(String filePath) throws IOException {
        List<ThiSinh> result = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String cccd = formatter.formatCellValue(row.getCell(1)).trim();
                String hoTen = formatter.formatCellValue(row.getCell(2)).trim();
                String soBaoDanh = formatter.formatCellValue(row.getCell(2)).trim();
                String ngaySinh = formatter.formatCellValue(row.getCell(3)).trim();
                String gioiTinh = formatter.formatCellValue(row.getCell(4)).trim();
                String doiTuong = formatter.formatCellValue(row.getCell(5)).trim();
                String khuVuc = formatter.formatCellValue(row.getCell(6)).trim();
                
                String maMonNn = row.getCell(16) != null ? formatter.formatCellValue(row.getCell(16)).trim() : "";
                String chuongTrinhHoc = row.getCell(21) != null ? formatter.formatCellValue(row.getCell(21)).trim() : "";
                
                String danToc = row.getCell(row.getLastCellNum() - 3) != null 
                    ? formatter.formatCellValue(row.getCell(row.getLastCellNum() - 3)).trim() : "";
                String maDanToc = row.getCell(row.getLastCellNum() - 2) != null 
                    ? formatter.formatCellValue(row.getCell(row.getLastCellNum() - 2)).trim() : "";
                String noiSinh = row.getCell(row.getLastCellNum() - 1) != null 
                    ? formatter.formatCellValue(row.getCell(row.getLastCellNum() - 1)).trim() : "";

                ThiSinh ts = new ThiSinh();
                ts.setCccd(cccd);
                ts.setSoBaoDanh(soBaoDanh);
                fillName(ts, hoTen);
                ts.setNgaySinh(ngaySinh);
                ts.setGioiTinh(gioiTinh);
                ts.setDoiTuong(doiTuong);
                ts.setKhuVuc(khuVuc);
                                ts.setMaMonNn(maMonNn);
                                ts.setChuongTrinhHoc(chuongTrinhHoc);
                ts.setDanToc(danToc);
                ts.setMaDanToc(maDanToc);
                ts.setNoiSinh(noiSinh);
                ts.setPassword("123456");
                ts.setUpdatedAt(LocalDate.now().toString());

                if (validateThiSinh(ts, true)) {
                    result.add(ts);
                }
            }
        }

        return result;
    }

    private List<ThiSinh> importFromTextLike(String filePath) throws IOException {
        List<ThiSinh> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trim = line.trim();
                if (trim.isEmpty() || trim.startsWith("---") || trim.toLowerCase().startsWith("stt")) {
                    continue;
                }

                String[] parts = trim.contains("\t") ? trim.split("\\t+") : trim.split("\\s{2,}");
                if (parts.length < 4) {
                    continue;
                }

                String cccd = parts[1].trim();
                String hoTen = parts[2].trim();
                String ngaySinh = parts[3].trim();
                String gioiTinh = parts.length > 4 ? parts[4].trim() : "";
                String doiTuong = parts.length > 5 ? parts[5].trim() : "";
                String khuVuc = parts.length > 6 ? parts[6].trim() : "";
                
                String maMonNn = parts.length > 16 ? parts[16].trim() : "";
                String chuongTrinhHoc = parts.length > 21 ? parts[21].trim() : "";
                
                String danToc = parts.length > 2 ? parts[parts.length - 3].trim() : "";
                String maDanToc = parts.length > 1 ? parts[parts.length - 2].trim() : "";
                String noiSinh = parts.length > 0 ? parts[parts.length - 1].trim() : "";
                ts.setMaMonNn(maMonNn);
                ts.setChuongTrinhHoc(chuongTrinhHoc);

                ThiSinh ts = new ThiSinh();
                ts.setCccd(cccd);
                fillName(ts, hoTen);
                ts.setNgaySinh(ngaySinh);
                ts.setGioiTinh(gioiTinh);
                ts.setDoiTuong(doiTuong);
                ts.setKhuVuc(khuVuc);
                ts.setDanToc(danToc);
                ts.setMaDanToc(maDanToc);
                ts.setNoiSinh(noiSinh);
                ts.setPassword("123456");
                ts.setUpdatedAt(LocalDate.now().toString());

                if (validateThiSinh(ts, true)) {
                    result.add(ts);
                }
            }
        }
        return result;
    }

    private void fillName(ThiSinh ts, String fullName) {
        if (fullName == null || fullName.isBlank()) {
            ts.setHo("");
            ts.setTen("");
            return;
        }

        String[] tokens = fullName.trim().split("\\s+");
        if (tokens.length == 1) {
            ts.setHo(tokens[0]);
            ts.setTen(tokens[0]);
            return;
        }

        ts.setTen(tokens[tokens.length - 1]);
        ts.setHo(fullName.substring(0, fullName.length() - ts.getTen().length()).trim());
    }

    private boolean validateThiSinh(ThiSinh ts, boolean checkDuplicate) {
        if (ts == null) {
            lastError = "Dữ liệu thí sinh không hợp lệ!";
            return false;
        }

        String cccd = safe(ts.getCccd());
        String soBaoDanh = safe(ts.getSoBaoDanh());
        String ho = safe(ts.getHo());
        String ten = safe(ts.getTen());
        String email = safe(ts.getEmail());
        String dienThoai = safe(ts.getDienThoai());
        String ngaySinh = safe(ts.getNgaySinh());
        String gioiTinh = safe(ts.getGioiTinh());
        String noiSinh = safe(ts.getNoiSinh());
        String doiTuong = safe(ts.getDoiTuong());
        String khuVuc = safe(ts.getKhuVuc());
        String password = safe(ts.getPassword());
        String updatedAt = safe(ts.getUpdatedAt());

        if (cccd.isEmpty()) {
            lastError = "CCCD không được để trống!";
            return false;
        }
        if (!isAcceptedCandidateCode(cccd)) {
            lastError = "CCCD không hợp lệ (chấp nhận 12 số hoặc mã TS_xxxx)!";
            return false;
        }
        if (ho.isEmpty() || ten.isEmpty()) {
            lastError = "Họ và tên không được để trống!";
            return false;
        }
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            lastError = "Email không đúng định dạng!";
            return false;
        }
        if (!dienThoai.isEmpty() && !PHONE_PATTERN.matcher(dienThoai).matches()) {
            lastError = "Số điện thoại không hợp lệ!";
            return false;
        }
        if (!ngaySinh.isEmpty() && !isValidDate(ngaySinh)) {
            lastError = "Ngày sinh không hợp lệ (dd/MM/yyyy hoặc yyyy-MM-dd)!";
            return false;
        }
        if (checkDuplicate && dao.getByCccd(cccd) != null) {
            lastError = "CCCD đã tồn tại trong hệ thống!";
            return false;
        }

        ts.setCccd(trimMax(cccd, 20));
        ts.setSoBaoDanh(trimMax(soBaoDanh, 45));
        ts.setHo(trimMax(ho, 100));
        ts.setTen(trimMax(ten, 100));
        ts.setNgaySinh(trimMax(ngaySinh, 45));
        ts.setDienThoai(trimMax(dienThoai, 20));
        ts.setPassword(trimMax(password.isEmpty() ? "123456" : password, 100));
        ts.setGioiTinh(trimMax(gioiTinh, 10));
        ts.setEmail(trimMax(email, 100));
        ts.setNoiSinh(trimMax(noiSinh, 45));
        ts.setDoiTuong(trimMax(doiTuong, 45));
        ts.setKhuVuc(trimMax(khuVuc, 45));
        ts.setUpdatedAt(trimMax(updatedAt, 45));
        lastError = "";
        return true;
    }

    private boolean isValidDate(String text) {
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate.parse(text, formatter);
                return true;
            } catch (DateTimeParseException ignored) {
            }
        }
        return false;
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
}
