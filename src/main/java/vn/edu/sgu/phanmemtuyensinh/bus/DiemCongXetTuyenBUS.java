package vn.edu.sgu.phanmemtuyensinh.bus;

import vn.edu.sgu.phanmemtuyensinh.dal.DiemCongXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.NguyenVongXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.ToHopMonDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ToHopMon;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    public List<DiemCongTongHopRow> getTongHopTheoNguyenVongPage(String keyword, int offset, int limit) {
        List<DiemCongTongHopRow> rows = buildTongHopTheoNguyenVong(keyword);
        int from = Math.max(0, Math.min(offset, rows.size()));
        int to = Math.max(from, Math.min(from + limit, rows.size()));
        return rows.subList(from, to);
    }

    public long countTongHopTheoNguyenVong(String keyword) {
        return buildTongHopTheoNguyenVong(keyword).size();
    }

    private List<DiemCongTongHopRow> buildTongHopTheoNguyenVong(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        NguyenVongXetTuyenDAO nvDao = new NguyenVongXetTuyenDAO();
        ToHopMonDAO toHopDao = new ToHopMonDAO();

        Map<String, List<DiemCongXetTuyen>> diemCongByCccd = new HashMap<>();
        for (DiemCongXetTuyen dc : dao.getAll()) {
            if (dc == null || dc.getTsCccd() == null || dc.getTsCccd().isBlank()) {
                continue;
            }
            diemCongByCccd.computeIfAbsent(normalizeCccdKey(dc.getTsCccd()), k -> new ArrayList<>()).add(dc);
        }

        Map<String, ToHopMon> toHopMap = new HashMap<>();
        for (ToHopMon th : toHopDao.getAll()) {
            if (th.getMaToHop() != null) {
                toHopMap.put(th.getMaToHop().trim().toUpperCase(Locale.ROOT), th);
            }
        }

        List<DiemCongTongHopRow> rows = new ArrayList<>();
        List<NguyenVongXetTuyen> nguyenVongList = nvDao.getAll();
        nguyenVongList.sort(Comparator
                .comparing((NguyenVongXetTuyen nv) -> safe(nv.getNvCccd()))
                .thenComparingInt(NguyenVongXetTuyen::getNvTt));

        for (NguyenVongXetTuyen nv : nguyenVongList) {
            String cccd = safe(nv.getNvCccd());
            if (!kw.isEmpty() && !cccd.toLowerCase(Locale.ROOT).contains(kw)) {
                continue;
            }

            List<DiemCongXetTuyen> sources = diemCongByCccd.getOrDefault(normalizeCccdKey(cccd), List.of());
            if (sources.isEmpty()) {
                continue;
            }

            DiemCongTongHopRow row = new DiemCongTongHopRow();
            row.idDiemCong = sources.get(0).getIdDiemCong();
            row.cccd = cccd;
            row.nguyenVong = nv.getNvTt();
            row.maNganh = safe(nv.getNvMaNganh());
            row.maToHop = safe(nv.getTtThm());
            row.phuongThuc = safe(nv.getTtPhuongThuc());

            ToHopMon toHop = toHopMap.get(row.maToHop.toUpperCase(Locale.ROOT));
            boolean toHopCoTiengAnh = hasSubject(toHop, "N1");
            BigDecimal diemTiengAnh = BigDecimal.ZERO;
            BigDecimal diemGiai = BigDecimal.ZERO;

            for (DiemCongXetTuyen dc : sources) {
                if (hasCertificate(dc) && value(dc.getDiemCC()).compareTo(diemTiengAnh) > 0) {
                    row.chungChi = safe(dc.getChungChi());
                    row.mucDatDuoc = safe(dc.getMucDatDuoc());
                    row.diemQuyDoiChungChi = dc.getDiemQuyDoiChungChi();
                    row.coChungChi = Boolean.TRUE.equals(dc.getCoChungChi()) || !row.chungChi.isBlank();
                    diemTiengAnh = value(dc.getDiemCC());
                }

                if (hasAward(dc)) {
                    BigDecimal applied = getAppliedAwardPoint(dc, toHop);
                    if (applied.compareTo(diemGiai) > 0) {
                        row.capGiai = safe(dc.getCapGiai());
                        row.doiTuongGiai = safe(dc.getDoiTuongGiai());
                        row.maMonGiai = safe(dc.getMaMonGiai());
                        row.loaiGiai = safe(dc.getLoaiGiai());
                        row.diemCongMonGiai = value(dc.getDiemCongMonGiai());
                        row.diemCongKhongMon = value(dc.getDiemCongKhongMon());
                        diemGiai = applied;
                    }
                }
            }

            row.diemCongCc = toHopCoTiengAnh ? BigDecimal.ZERO : diemTiengAnh;
            row.diemUuTien = value(nv.getDiemUtqd());
            row.tongDiemCong = row.diemCongCc.add(diemGiai);
            if (row.tongDiemCong.compareTo(new BigDecimal("3.0")) > 0) {
                row.tongDiemCong = new BigDecimal("3.0");
            }
            rows.add(row);
        }

        rows.sort(Comparator
                .comparingInt((DiemCongTongHopRow row) -> row.idDiemCong)
                .thenComparing(row -> safe(row.cccd))
                .thenComparingInt(row -> row.nguyenVong));
        return rows;
    }

    private BigDecimal getAppliedAwardPoint(DiemCongXetTuyen dc, ToHopMon toHop) {
        if (hasSubject(toHop, dc.getMaMonGiai())) {
            return value(dc.getDiemCongMonGiai());
        }
        return value(dc.getDiemCongKhongMon());
    }

    private boolean hasCertificate(DiemCongXetTuyen dc) {
        return dc != null && (Boolean.TRUE.equals(dc.getCoChungChi()) || !safe(dc.getChungChi()).isBlank());
    }

    private boolean hasAward(DiemCongXetTuyen dc) {
        return dc != null
                && (!safe(dc.getCapGiai()).isBlank()
                || !safe(dc.getLoaiGiai()).isBlank()
                || !safe(dc.getMaMonGiai()).isBlank());
    }

    private boolean hasSubject(ToHopMon toHop, String monCode) {
        String mon = safe(monCode).toUpperCase(Locale.ROOT);
        if (toHop == null || mon.isBlank()) {
            return false;
        }
        return mon.equals(safe(toHop.getMon1()).toUpperCase(Locale.ROOT))
                || mon.equals(safe(toHop.getMon2()).toUpperCase(Locale.ROOT))
                || mon.equals(safe(toHop.getMon3()).toUpperCase(Locale.ROOT));
    }

    private BigDecimal value(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeCccdKey(String value) {
        String digits = safe(value).replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return safe(value).toUpperCase(Locale.ROOT);
        }
        return digits.replaceFirst("^0+(?!$)", "");
    }

    public static class DiemCongTongHopRow {
        public int idDiemCong;
        public String cccd;
        public int nguyenVong;
        public String maNganh;
        public String maToHop;
        public String phuongThuc;
        public String chungChi;
        public String mucDatDuoc;
        public BigDecimal diemQuyDoiChungChi;
        public BigDecimal diemCongCc;
        public boolean coChungChi;
        public String capGiai;
        public String doiTuongGiai;
        public String maMonGiai;
        public String loaiGiai;
        public BigDecimal diemCongMonGiai;
        public BigDecimal diemCongKhongMon;
        public BigDecimal diemUuTien;
        public BigDecimal tongDiemCong;
    }

    public void tinhToanDiemCongVaUuTien(DiemCongXetTuyen d, String loaiCC, String mucCC, String loaiGiai,
                                         String kv, String dt, BigDecimal diemThiGoc,
                                         BigDecimal diemCongMonNhap, BigDecimal diemCongKhongMonNhap) {
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

        // Điểm cộng giải - Tính tự động từ loại giải nếu không có điểm nhập tay
        BigDecimal diemCongMonGiai = BigDecimal.ZERO;
        BigDecimal diemCongKoMonGiai = BigDecimal.ZERO;
        if (diemCongMonNhap != null) {
            diemCongMonGiai = diemCongMonNhap;
            diemCongKoMonGiai = (diemCongKhongMonNhap != null) ? diemCongKhongMonNhap : BigDecimal.ZERO;
        } else if (loaiGiai != null) {
            // Tính tự động từ loại giải (logic cũ)
            if (loaiGiai.toLowerCase().contains("nhất")) {
                diemCongMonGiai = new BigDecimal("2.0");
                diemCongKoMonGiai = new BigDecimal("1.0");
            } else if (loaiGiai.toLowerCase().contains("nhì")) {
                diemCongMonGiai = new BigDecimal("1.5");
                diemCongKoMonGiai = new BigDecimal("0.75");
            } else if (loaiGiai.toLowerCase().contains("ba")) {
                diemCongMonGiai = new BigDecimal("1.0");
                diemCongKoMonGiai = new BigDecimal("0.5");
            }
        }

        d.setDiemCongMonGiai(diemCongMonGiai);
        d.setDiemCongKhongMon(diemCongKoMonGiai);

        // Tổng điểm cộng CC + điểm cộng giải (lấy điểm cao nhất giữa môn và không môn)
        BigDecimal diemCongGiai = diemCongMonGiai.max(diemCongKoMonGiai);
        BigDecimal tongDiemCC = diemCongCC.add(diemCongGiai);
        if(tongDiemCC.compareTo(new BigDecimal("3.0")) > 0) tongDiemCC = new BigDecimal("3.0");
        d.setDiemCC(tongDiemCC);

        // 3. Lấy Mức ưu tiên gốc (MĐUT)
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

        // 4. Tính ĐUT thực tế theo ngưỡng 22.5
        BigDecimal tongXet = diemThiGoc.add(tongDiemCC);
        if (tongXet.compareTo(new BigDecimal("22.5")) < 0) {
            d.setDiemUtxt(mdutGoc);
        } else {
            BigDecimal heSo = new BigDecimal("30").subtract(tongXet).divide(new BigDecimal("7.5"), 4, RoundingMode.HALF_UP);
            d.setDiemUtxt(heSo.multiply(mdutGoc).setScale(2, RoundingMode.HALF_UP));
        }
        d.setDiemTong(tongDiemCC.add(d.getDiemUtxt()));
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
