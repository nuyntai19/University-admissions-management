package vn.edu.sgu.phanmemtuyensinh.bus;

import vn.edu.sgu.phanmemtuyensinh.dal.DiemCongXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;

public class DiemCongXetTuyenBUS {
    private DiemCongXetTuyenDAO dao = new DiemCongXetTuyenDAO();
    private vn.edu.sgu.phanmemtuyensinh.dal.DiemThiXetTuyenDAO diemThiDao = new vn.edu.sgu.phanmemtuyensinh.dal.DiemThiXetTuyenDAO();
    private String lastError = "";

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public List<DiemCongXetTuyen> getAll() { return dao.getAll(); }
    public List<DiemCongXetTuyen> getPage(int offset, int limit) { return dao.getPage(offset, limit); }
    public List<DiemCongXetTuyen> getPage(String keyword, int offset, int limit) { return dao.getPage(keyword, offset, limit); }
    public long countAll() { return dao.countAll(); }
    public long countAll(String keyword) { return dao.countAll(keyword); }

    public void tinhToanDiemCongVaUuTien(DiemCongXetTuyen d, String loaiCC, String mucCC, String loaiGiai, String kv, String dt, BigDecimal diemThiGoc) {
        // 1. Xác định mức chứng chỉ
        int mucCCInt = getMucChungChi(loaiCC, mucCC);
        
        // Cập nhật điểm quy đổi và điểm cộng CC
        if (mucCCInt > 0) {
            d.setDiemQuyDoiChungChi(new BigDecimal(mucCCInt == 1 ? "8.0" : mucCCInt == 2 ? "9.0" : "10.0"));
        } else {
            d.setDiemQuyDoiChungChi(null);
        }

        BigDecimal diemCongCC = BigDecimal.ZERO;
        if (mucCCInt > 0) {
            diemCongCC = new BigDecimal(mucCCInt == 1 ? "1.0" : mucCCInt == 2 ? "1.5" : "2.0");
        }

        // Tính điểm môn giải
        BigDecimal diemCongGiai = BigDecimal.ZERO;
        if (loaiGiai != null && loaiGiai.toLowerCase().contains("nhất")) {
            diemCongGiai = diemCongGiai.add(new BigDecimal("2.0"));
        } else if (loaiGiai != null && loaiGiai.toLowerCase().contains("nhì")) {
            diemCongGiai = diemCongGiai.add(new BigDecimal("1.5"));
        } else if (loaiGiai != null && loaiGiai.toLowerCase().contains("ba")) {
            diemCongGiai = diemCongGiai.add(new BigDecimal("1.0"));
        }
        
        d.setDiemCongKhongMon(diemCongCC); // Ta lưu tạm điểm cộng CC vào trường này để hiển thị trên bảng
        d.setDiemCongMonGiai(diemCongGiai);
        
        BigDecimal dCC = diemCongCC.add(diemCongGiai);
        if(dCC.compareTo(new BigDecimal("3.0")) > 0) dCC = new BigDecimal("3.0");
        d.setDiemCC(dCC);

        // 2. Lấy Mức ưu tiên gốc (MĐUT)
        BigDecimal mdutGoc = BigDecimal.ZERO;
        
        // Khu vực
        if (kv != null) {
            String kvUpper = kv.trim().toUpperCase();
            if (kvUpper.equals("KV1") || kvUpper.equals("1")) mdutGoc = mdutGoc.add(new BigDecimal("0.75"));
            else if (kvUpper.equals("KV2-NT") || kvUpper.equals("KV2NT") || kvUpper.equals("2NT")) mdutGoc = mdutGoc.add(new BigDecimal("0.5"));
            else if (kvUpper.equals("KV2") || kvUpper.equals("2")) mdutGoc = mdutGoc.add(new BigDecimal("0.25"));
        }
        
        // Đối tượng
        if (dt != null) {
            String dtTrim = dt.trim();
            if (dtTrim.equals("01") || dtTrim.equals("1") || dtTrim.equals("02") || dtTrim.equals("2") 
                || dtTrim.equals("03") || dtTrim.equals("3") || dtTrim.equals("04") || dtTrim.equals("4")) {
                mdutGoc = mdutGoc.add(new BigDecimal("2.0"));
            } else if (dtTrim.equals("05") || dtTrim.equals("5") || dtTrim.equals("06") || dtTrim.equals("6") 
                       || dtTrim.equals("06A") || dtTrim.equals("07") || dtTrim.equals("7") || dtTrim.equals("07A")) {
                mdutGoc = mdutGoc.add(new BigDecimal("1.0"));
            }
        }

        // 3. Tính ĐUT thực tế theo ngưỡng 22.5
        BigDecimal tongXet = diemThiGoc.add(dCC);
        if (tongXet.compareTo(new BigDecimal("22.5")) < 0) {
            d.setDiemUtxt(mdutGoc);
        } else {
            BigDecimal heSo = new BigDecimal("30").subtract(tongXet).divide(new BigDecimal("7.5"), 4, RoundingMode.HALF_UP);
            d.setDiemUtxt(heSo.multiply(mdutGoc).setScale(2, RoundingMode.HALF_UP));
        }
        d.setDiemTong(d.getDiemCC().add(d.getDiemUtxt()));
    }

    private int getMucChungChi(String loaiCC, String diemStr) {
        if (loaiCC == null || loaiCC.isEmpty() || loaiCC.equalsIgnoreCase("None")) return 0;
        if (diemStr == null || diemStr.isEmpty()) return 0;

        String cc = loaiCC.trim().toLowerCase(java.util.Locale.ROOT);
        double diem = 0;
        try { diem = Double.parseDouble(diemStr.trim().replace(',', '.')); } catch (Exception e) {}
        String diemUpper = diemStr.trim().toUpperCase(java.util.Locale.ROOT);

        if (cc.contains("ielts")) {
            if (diem >= 7.0) return 3;
            if (diem >= 5.5) return 2;
            if (diem >= 4.0) return 1;
        } else if (cc.contains("toefl itp")) {
            if (diem >= 627) return 3;
            if (diem >= 500) return 2;
            if (diem >= 450) return 1;
        } else if (cc.contains("toefl ibt")) {
            if (diem >= 94) return 3;
            if (diem >= 46) return 2;
            if (diem >= 30) return 1;
        } else if (cc.contains("toeic")) {
            if (diem >= 490) return 3;
            if (diem >= 400) return 2;
            if (diem >= 275) return 1;
        } else if (cc.contains("pte")) {
            if (diem >= 76) return 3;
            if (diem >= 59) return 2;
            if (diem >= 43) return 1;
        } else if (cc.contains("linguaskill")) {
            if (diem >= 180) return 3;
            if (diem >= 160) return 2;
            if (diem >= 140) return 1;
        } else if (cc.contains("aptis")) {
            if (diemUpper.equals("C") || diemUpper.equals("C1")) return 3;
            if (diemUpper.equals("B2")) return 2;
            if (diemUpper.equals("B1")) return 1;
        } else if (cc.contains("vstep")) {
            if (diemUpper.contains("5")) return 3;
            if (diemUpper.contains("4")) return 2;
            if (diemUpper.contains("3")) return 1;
        }
        return 0;
    }

    public boolean save(DiemCongXetTuyen d) {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return false;
        }
        return dao.saveOrUpdate(d);
    }

    public boolean delete(int id) {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return false;
        }
        return dao.delete(id);
    }
    
    public Object[] layThongTinThiSinh(String cccd) {
        return dao.getThongTinUuTienByCccd(cccd);
    }

    public List<ThiSinh> timKiemThiSinh(String kw) {
        return dao.searchThiSinh(kw);
    }

    public boolean update(DiemCongXetTuyen d) {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return false;
        }
        return dao.saveOrUpdate(d); // Hibernate merge xử lý cả update
    }

    public DiemCongXetTuyen getById(int id) {
        return dao.getById(id); 
    }   
    
    public BigDecimal layDiemThiGocThucTe(String cccd) {
        vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen dt = diemThiDao.getByCccd(cccd);
        if (dt != null) {
            BigDecimal toan = dt.getTo() != null ? dt.getTo() : BigDecimal.ZERO;
            BigDecimal van = dt.getVa() != null ? dt.getVa() : BigDecimal.ZERO;
            BigDecimal anh = dt.getN1Thi() != null ? dt.getN1Thi() : BigDecimal.ZERO;
            return toan.add(van).add(anh);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Import từ file Excel "Uu tien xet tuyen.xlsx".
     * Cột CCCD có thể là TS_xxx → tra bảng thí sinh lấy CCCD thật.
     * Trả về [thanhCong, thatBai] và ghi lastError nếu có vấn đề.
     */
    public int[] importFromExcel(String filePath) {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return new int[]{0, 0};
        }

        // Pre-load SBD → CCCD
        vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO thiSinhDAO =
                new vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO();
        java.util.Map<String, String> sbdToCccd = thiSinhDAO.getAllSbdToCccdMap();

        int success = 0, failed = 0;
        org.apache.poi.ss.usermodel.DataFormatter formatter =
                new org.apache.poi.ss.usermodel.DataFormatter();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(filePath);
             org.apache.poi.ss.usermodel.Workbook wb =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);

            // Tìm header row
            int headerIdx = -1;
            java.util.Map<String, Integer> hmap = new java.util.HashMap<>();
            for (int i = 0; i <= Math.min(5, sheet.getLastRowNum()); i++) {
                org.apache.poi.ss.usermodel.Row r = sheet.getRow(i);
                if (r == null) continue;
                java.util.Map<String, Integer> tmp = new java.util.HashMap<>();
                for (int c = 0; c < r.getLastCellNum(); c++) {
                    String k = normalizeH(formatter.formatCellValue(r.getCell(c)));
                    if (!k.isBlank()) tmp.put(k, c);
                }
                if (tmp.containsKey("cccd") || tmp.containsKey("tt") || tmp.containsKey("cap")) {
                    headerIdx = i; hmap = tmp; break;
                }
            }
            if (headerIdx < 0) { lastError = "Không tìm thấy header!"; return new int[]{0, 0}; }

            // DEBUG: in ra header map
            System.out.println("=== HEADER MAP (Uu Tien) ===");
            for (java.util.Map.Entry<String, Integer> entry : hmap.entrySet()) {
                System.out.println("  Key: '" + entry.getKey() + "' -> Col: " + entry.getValue());
            }

            for (int i = headerIdx + 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                String rawCccd = cell(row, hmap, formatter, "cccd");
                if (rawCccd.isEmpty()) continue;

                // Fix CCCD TS_xxx → CCCD thật
                String cccd = rawCccd;
                if (cccd.toUpperCase().startsWith("TS_")) {
                    String real = sbdToCccd.get(cccd.toUpperCase());
                    if (real != null) cccd = real;
                    else { failed++; continue; }
                }

                // Excel: "Cấp" -> normalized: "cap"
                String capGiai = cell(row, hmap, formatter, "cap");
                // Excel: "ĐT" -> normalized: "dt"
                String doiTuong = cell(row, hmap, formatter, "dt");
                // Excel: "Mã môn" -> normalized: "mamon"
                String maMon = cell(row, hmap, formatter, "mamon");
                // Excel: "Loại giải" -> normalized: "loaigiai"
                String loaiGiai = cell(row, hmap, formatter, "loaigiai");
                // Excel: "Điểm cộng cho môn đạt giải" -> normalized: "diemcongchomondatgiai"
                BigDecimal diemCongMon = parseBD(cell(row, hmap, formatter,
                        "diemcongchomondatgiai", "diemcongmon"));
                // Excel: "Điểm cộng cho THXT ko có môn đạt giải" -> normalized: "diemcongchothxtkocomondatgiai"
                BigDecimal diemCongKhongMon = parseBD(cell(row, hmap, formatter,
                        "diemcongchothxtkocomondatgiai", "diemcongchothxtkocmondatgiai",
                        "diemcongchothxtkocomond", "diemcongkhongmon"));
                // Excel: "Có C/C" -> normalized: "cocc"
                String coCCStr = cell(row, hmap, formatter, "cocc", "cc");

                // DEBUG: in 1 dòng đầu
                if (i == headerIdx + 1) {
                    System.out.println("=== ROW 1 DATA (Uu Tien) ===");
                    System.out.println("  CCCD=" + cccd + " Cap=" + capGiai + " DT=" + doiTuong
                        + " MaMon=" + maMon + " Giai=" + loaiGiai
                        + " DiemMon=" + diemCongMon + " DiemKoMon=" + diemCongKhongMon
                        + " CoCC=" + coCCStr);
                }

                DiemCongXetTuyen d = new DiemCongXetTuyen();
                d.setTsCccd(cccd);
                d.setCapGiai(capGiai);                   // cap_giai
                d.setDoiTuongGiai(doiTuong);             // doi_tuong_giai
                d.setMaMonGiai(maMon);                   // ma_mon_giai
                d.setLoaiGiai(loaiGiai);                 // loai_giai
                d.setDiemCongMonGiai(diemCongMon);       // diem_cong_mon_giai
                d.setDiemCongKhongMon(diemCongKhongMon); // diem_cong_khong_mon
                d.setCoChungChi(coCCStr != null && (coCCStr.equals("1") || coCCStr.equalsIgnoreCase("có")));

                d.setDiemCC(BigDecimal.ZERO);   // Sẽ tính lại khi xét tuyển
                d.setDiemUtxt(BigDecimal.ZERO); // Sẽ tính lại khi xét tuyển
                d.setDiemTong(BigDecimal.ZERO);
                d.setDcKeys("GIAI_" + cccd);

                if (dao.saveOrUpdate(d)) {
                    success++;
                }
                else failed++;
            }
        } catch (Exception e) {
            lastError = e.getMessage();
            e.printStackTrace();
        }
        return new int[]{success, failed};
    }

    private String normalizeH(String s) {
        if (s == null) return "";
        try {
            String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("").toLowerCase().replaceAll("đ", "d").replaceAll("[^a-z0-9]", "");
        } catch (Exception e) {
            return s.toLowerCase().replaceAll("[^a-z0-9]", "");
        }
    }

    private String cell(org.apache.poi.ss.usermodel.Row row,
                        java.util.Map<String, Integer> hmap,
                        org.apache.poi.ss.usermodel.DataFormatter fmt,
                        String... keys) {
        for (String k : keys) {
            Integer idx = hmap.get(normalizeH(k));
            if (idx != null) {
                String v = fmt.formatCellValue(row.getCell(idx)).trim();
                if (!v.isEmpty()) return v;
            }
        }
        return "";
    }

    private BigDecimal parseBD(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.replace(",", ".")); } catch (Exception e) { return null; }
    }
}