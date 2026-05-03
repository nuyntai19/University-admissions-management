package vn.edu.sgu.phanmemtuyensinh.bus;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import vn.edu.sgu.phanmemtuyensinh.dal.DiemCongXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.DiemThiXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.NganhDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.NganhToHopDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.NguyenVongXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.ToHopMonDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.Nganh;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NganhToHop;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ToHopMon;

public class NguyenVongXetTuyenBUS {
    private NguyenVongXetTuyenDAO dao = new NguyenVongXetTuyenDAO();
    private String lastError = "";
    private vn.edu.sgu.phanmemtuyensinh.dal.DiemThiXetTuyenDAO diemDao = new vn.edu.sgu.phanmemtuyensinh.dal.DiemThiXetTuyenDAO();
    private BangQuyDoiBUS bqdBus = new BangQuyDoiBUS();
    private DiemCongXetTuyenDAO diemCongDao = new DiemCongXetTuyenDAO();
    private NganhToHopDAO nganhToHopDAO = new NganhToHopDAO();
    private NganhDAO nganhDAO = new NganhDAO();

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public List<NguyenVongXetTuyen> getAll() {
        return dao.getAll();
    }

    public List<NguyenVongXetTuyen> getByCccd(String cccd) {
        return dao.getByCccd(cccd);
    }

    public int getNextThuTu(String cccd) {
        List<NguyenVongXetTuyen> list = dao.getByCccd(cccd);
        if (list == null || list.isEmpty()) return 1;
        return list.stream().mapToInt(NguyenVongXetTuyen::getNvTt).max().orElse(0) + 1;
    }

    public boolean add(NguyenVongXetTuyen nv) {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return false;
        }
        if (nv.getNvCccd() == null || nv.getNvCccd().trim().isEmpty()) {
            lastError = "CCCD không được để trống!";
            return false;
        }
        if (nv.getNvMaNganh() == null || nv.getNvMaNganh().trim().isEmpty()) {
            lastError = "Mã ngành không được để trống!";
            return false;
        }
        return dao.add(nv);
    }

    public boolean update(NguyenVongXetTuyen nv) {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return false;
        }
        return dao.update(nv);
    }

    public boolean delete(int idNv) {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return false;
        }
        return dao.delete(idNv);
    }
    public int importNguyenVongFromExcel(String filePath) throws IOException {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return 0;
        }

        // Pre-load SBD → CCCD map để tra cứu nhanh
        vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO thiSinhDAO =
                new vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO();
        Map<String, String> sbdToCccd = thiSinhDAO.getAllSbdToCccdMap();

        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            List<NguyenVongXetTuyen> listNv = new ArrayList<>();
            int totalSheetsFound = 0;

            // Duyệt qua tất cả các sheet để tìm sheet nào chứa cột CCCD
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                int headerRowIdx = -1;
                Map<String, Integer> headerMap = new HashMap<>();

                for (int i = 0; i <= Math.min(10, sheet.getLastRowNum()); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    Map<String, Integer> tempMap = new HashMap<>();
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        String raw = formatter.formatCellValue(row.getCell(c));
                        String key = normalizeHeader(raw);
                        if (!key.isBlank()) {
                            tempMap.put(key, c);
                        }
                    }
                    
                    // Nếu sheet này có cột cần thiết
                    if (tempMap.containsKey("cccd") || tempMap.containsKey("sobaodanh") || tempMap.containsKey("sbodanh") || tempMap.containsKey("cmnd")) {
                        headerRowIdx = i;
                        headerMap = tempMap;
                        break;
                    }
                }
                
                if (headerRowIdx < 0) continue; // Bỏ qua sheet không có dữ liệu cần thiết
                totalSheetsFound++;

                for (int i = headerRowIdx + 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String sbd = getCell(row, formatter, headerMap, new String[]{"sobaodanh", "sbd", "sbodanh"});
                    String cccd = getCell(row, formatter, headerMap, new String[]{"cccd", "cmnd"});

                    // Nếu CCCD trống, thử lấy từ SBD column
                    if (cccd.isEmpty() && sbd.isEmpty()) continue;
                    if (cccd.isEmpty() && !sbd.isEmpty()) {
                        String realCccd = sbdToCccd.get(sbd.toUpperCase());
                        if (realCccd != null) cccd = realCccd;
                    }
                    if (cccd.isEmpty()) continue;

                    NguyenVongXetTuyen nv = new NguyenVongXetTuyen();
                    nv.setNvCccd(cccd);
                    nv.setNvTt(parseInt(getCell(row, formatter, headerMap,
                            new String[]{"thutunv", "thtu", "sothutu", "nv", "tt"})));
                    nv.setNvMaNganh(getCell(row, formatter, headerMap,
                            new String[]{"maxettuyen", "manganh", "mact", "maxt"}));
                    nv.setNvTenMaNganh(getCell(row, formatter, headerMap,
                            new String[]{"tenmaxettuyen", "tennganh", "tenmanganh"}));
                    nv.setNvTuyenThang(getCell(row, formatter, headerMap,
                            new String[]{"nguyenvongtuyenthangdieu8", "nguyenvongtuyenthang", "tuyenthang"}));
                    nv.setTtPhuongThuc(getCell(row, formatter, headerMap,
                            new String[]{"phuongthuc", "ptxt", "pt"}));

                    if (nv.getNvMaNganh().isEmpty()) continue;
                    nv.setNvKeys(generateNvKey(nv));

                    listNv.add(nv);
                }
            }

            if (totalSheetsFound == 0) {
                lastError = "Không tìm thấy dòng header trong file (cần cột CCCD)!";
                return 0;
            }
            
            int success = 0;
            if (!listNv.isEmpty()) {
                if (dao.addList(listNv)) {
                    success = listNv.size();
                } else {
                    lastError = "Lỗi khi lưu dữ liệu vào database!";
                }
            }
            return success;
        }
    }

    /**
     * Chạy xét tuyển cho tất cả nguyện vọng: tính điểm, lưu lại vào bảng nguyện vọng
     * Trình tự (gọi tắt): lấy điểm gốc -> (nếu có) quy đổi -> tổng = diemThxt + diemCong + diemUtqd
     * Task 4: Tính ĐC (điểm cộng) từ giải + chứng chỉ; ĐUT (điểm ưu tiên) theo công thức
     * Trả về số bản ghi đã cập nhật
     */
    /**
     * Chạy xét tuyển cho tất cả nguyện vọng: tính điểm, lưu lại vào bảng nguyện vọng
     * Tuân thủ công thức trong 'cac cong thuc tinh.txt'
     */
    public int runXetTuyenAll() {
        List<NguyenVongXetTuyen> list = dao.getAll();
        if (list == null || list.isEmpty()) return 0;
        return runXetTuyenForList(list);
    }

    /**
     * Tính lại điểm xét tuyển cho toàn bộ nguyện vọng của 1 CCCD cụ thể.
     */
    public int runXetTuyenForCccd(String cccd) {
        List<NguyenVongXetTuyen> filtered = dao.getByCccd(cccd);
        if (filtered == null || filtered.isEmpty()) return -1;
        return runXetTuyenForList(filtered);
    }

    /**
     * Tính lại điểm xét tuyển cho 1 nguyện vọng cụ thể (theo ID).
     */
    public int runXetTuyenForNv(int idNv) {
        NguyenVongXetTuyen nv = dao.getById(idNv);
        if (nv == null) return 0;
        List<NguyenVongXetTuyen> list = new ArrayList<>();
        list.add(nv);
        return runXetTuyenForList(list);
    }

    /**
     * Tính lại điểm xét tuyển cho toàn bộ thí sinh đăng ký vào 1 ngành cụ thể.
     */
    public int runXetTuyenForMaNganh(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) return 0;
        List<NguyenVongXetTuyen> list = dao.getByMaNganh(maNganh.trim());
        if (list == null || list.isEmpty()) return 0;
        return runXetTuyenForList(list);
    }

    /**
     * HÀM LÕI: Tính và lưu điểm xét tuyển cho danh sách nguyện vọng.
     * Thực hiện đúng các bước trong file 'cac cong thuc tinh.txt'
     */
    private int runXetTuyenForList(List<NguyenVongXetTuyen> list) {
        Map<String, BigDecimal> nguongMap = loadNguongDauVao();
        
        // Pre-load dữ liệu mốc quy đổi, điểm thi, và ngành-tổ hợp
        DiemThiXetTuyenDAO diemThiDAO = new DiemThiXetTuyenDAO();
        // Load toàn bộ điểm thi, lưu map theo "CCCD_PhuongThuc" để tránh ghi đè
        List<DiemThiXetTuyen> allDiem = diemThiDAO.getAll();
        Map<String, DiemThiXetTuyen> diemMap = new HashMap<>();
        for (DiemThiXetTuyen d : allDiem) {
            String pt = normalizePhuongThuc(d.getPhuongThuc());
            diemMap.put(d.getCccd() + "_" + pt, d);
        }

        ToHopMonDAO toHopMonDAO = new ToHopMonDAO();
        Map<String, ToHopMon> thmMap = new HashMap<>();
        for (ToHopMon t : toHopMonDAO.getAll()) thmMap.put(t.getMaToHop(), t);

        NganhToHopDAO nthDAO = new NganhToHopDAO();
        List<NganhToHop> nthList = nthDAO.getAll();
        Map<String, List<NganhToHop>> nganhToHopMap = new HashMap<>();
        for (NganhToHop n : nthList) {
            nganhToHopMap.computeIfAbsent(n.getMaNganh(), k -> new ArrayList<>()).add(n);
        }

        DiemCongXetTuyenDAO dcDAO = new DiemCongXetTuyenDAO();
        Map<String, DiemCongXetTuyen> dCongMap = new HashMap<>();
        for (DiemCongXetTuyen dc : dcDAO.getAll()) dCongMap.put(dc.getTsCccd(), dc);

        ThiSinhDAO thiSinhDAO = new ThiSinhDAO();
        Map<String, ThiSinh> thiSinhMap = new HashMap<>();
        for (ThiSinh ts : thiSinhDAO.getAll()) thiSinhMap.put(ts.getCccd(), ts);

        // Pre-load bảng quy đổi và nhóm theo Key để tra cứu O(1)
        List<vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi> allBqd = bqdBus.getAll();
        Map<String, List<vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi>> bqdMap = new HashMap<>();
        for (vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi b : allBqd) {
            String pt = b.getDPhuongThuc().trim().toUpperCase(Locale.ROOT);
            String th = b.getDToHop() == null ? "" : b.getDToHop().trim().toUpperCase(Locale.ROOT);
            String mon = b.getDMon() == null ? "" : b.getDMon().trim().toUpperCase(Locale.ROOT);
            String key = pt + (th.isEmpty() ? "" : "_" + th) + (mon.isEmpty() ? "" : "_" + mon);
            bqdMap.computeIfAbsent(key, k -> new ArrayList<>()).add(b);
        }

        List<NguyenVongXetTuyen> updateBatch = new ArrayList<>(list.size());
        
        System.out.println("Bắt đầu tính toán cho " + list.size() + " nguyện vọng...");

        for (NguyenVongXetTuyen nv : list) {
            try {
                String cccd = nv.getNvCccd();
                String phuongThuc = normalizePhuongThuc(nv.getTtPhuongThuc());
                if (phuongThuc.isEmpty()) phuongThuc = "THPT";
                String maNganh = normalizeMaNganh(nv.getNvMaNganh());
                String toHopChosen = nv.getTtThm();
                
                // 1. Tìm điểm thi tương ứng với phương thức của nguyện vọng
                DiemThiXetTuyen diemRecord = diemMap.get(cccd + "_" + phuongThuc);
                if (diemRecord == null) {
                    // Nếu không có điểm theo phương thức cụ thể, thử lấy điểm chung (cho THPT)
                    diemRecord = diemMap.get(cccd + "_THPT");
                    if (diemRecord == null) diemRecord = diemMap.get(cccd + "_");
                }

                // 2. Lấy thông tin điểm cộng (Chứng chỉ, giải...)
                DiemCongXetTuyen dcRecord = dCongMap.get(cccd);
                int mucCC = (dcRecord != null) ? getMucChungChi(dcRecord.getChungChi(), dcRecord.getMucDatDuoc()) : 0;
                
                // Xác định mức điểm ưu tiên gốc (MĐUT) từ thông tin thí sinh
                ThiSinh ts = thiSinhMap.get(cccd);
                BigDecimal mDut = calculateBasePriority(ts);

                BigDecimal thxtScale30 = BigDecimal.ZERO;
                String bestToHop = toHopChosen;

                // 3. TÍNH ĐIỂM TỔ HỢP XÉT TUYỂN (ĐTHXT)
                if (diemRecord != null) {
                    List<NganhToHop> possibleNth = nganhToHopMap.get(maNganh);
                    if (possibleNth != null) {
                        BigDecimal maxDthxt = BigDecimal.valueOf(-1);
                        
                        for (NganhToHop nth : possibleNth) {
                            // Nếu người dùng đã chọn tổ hợp cụ thể, chỉ tính tổ hợp đó
                            if (toHopChosen != null && !toHopChosen.isEmpty() && !toHopChosen.equalsIgnoreCase(nth.getMaToHop())) {
                                continue;
                            }

                            BigDecimal scoreScale30 = calculateDthxt(phuongThuc, diemRecord, nth, mucCC, bqdMap);
                            if (scoreScale30.compareTo(maxDthxt) > 0) {
                                maxDthxt = scoreScale30;
                                bestToHop = nth.getMaToHop();
                            }
                        }
                        if (maxDthxt.compareTo(BigDecimal.ZERO) >= 0) {
                            thxtScale30 = maxDthxt;
                        }
                    }
                }

                // Cập nhật lại tổ hợp nếu chưa có
                if ((toHopChosen == null || toHopChosen.isEmpty()) && bestToHop != null) {
                    nv.setTtThm(bestToHop);
                }
                String finalToHop = (bestToHop != null) ? bestToHop : toHopChosen;

                // 4. TÍNH ĐIỂM TỔ HỢP GỐC XÉT TUYỂN (ĐTHGXT) - Mục 3.2
                BigDecimal thgxt = thxtScale30;
                NganhToHop mapping = findNganhToHop(maNganh, finalToHop);
                if (mapping != null && mapping.getDoLech() != null) {
                    thgxt = thxtScale30.subtract(mapping.getDoLech());
                }

                // 5. TÍNH ĐIỂM CỘNG (ĐC) - Mục 3.3
                BigDecimal diemCong = BigDecimal.ZERO;
                if (dcRecord != null) {
                    if (dcRecord.getDiemCongMonGiai() != null) diemCong = diemCong.add(dcRecord.getDiemCongMonGiai());
                    
                    boolean coTiengAnh = isToHopHasEnglish(finalToHop, thmMap);
                    if (!coTiengAnh && mucCC > 0) {
                        diemCong = diemCong.add(new BigDecimal(mucCC == 1 ? "1.0" : mucCC == 2 ? "1.5" : "2.0"));
                    }
                }
                if (diemCong.compareTo(new BigDecimal("3.0")) > 0) diemCong = new BigDecimal("3.0");

                // 6. TÍNH ĐIỂM ƯU TIÊN (ĐUT) - Mục 3.4
                BigDecimal dut = BigDecimal.ZERO;
                BigDecimal sumGxtCong = thgxt.add(diemCong);
                if (sumGxtCong.compareTo(new BigDecimal("22.5")) < 0) {
                    dut = mDut;
                } else {
                    BigDecimal numerator = new BigDecimal("30").subtract(sumGxtCong);
                    if (numerator.compareTo(BigDecimal.ZERO) > 0) {
                        dut = numerator.divide(new BigDecimal("7.5"), 4, RoundingMode.HALF_UP).multiply(mDut);
                    }
                }

                // 7. TỔNG ĐIỂM XÉT TUYỂN (ĐXT) = ĐTHGXT + ĐC + ĐUT
                BigDecimal dxt = thgxt.add(diemCong).add(dut);
                if (dxt.compareTo(new BigDecimal("30")) > 0) dxt = new BigDecimal("30");

                // 8. KẾT QUẢ
                BigDecimal nguong = nguongMap.get(normalizeMaNganh(maNganh));
                nv.setNvKetQua(nguong != null ? (dxt.compareTo(nguong) >= 0 ? "Đạt" : "Trượt") : "Chưa có ngưỡng");

                nv.setDiemThxt(thxtScale30);
                nv.setDiemCong(diemCong);
                nv.setDiemUtqd(dut);
                nv.setDiemXetTuyen(dxt);

                updateBatch.add(nv);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return dao.updateList(updateBatch) ? updateBatch.size() : 0;
    }

    /**
     * Hàm tính Điểm Tổ Hợp Xét Tuyển (ĐTHXT) đã quy đổi về thang 30 cho từng phương thức.
     */
    private BigDecimal calculateDthxt(String pt, DiemThiXetTuyen d, NganhToHop nth, int mucCC, Map<String, List<vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi>> bqdCache) {
        if (pt.contains("V-SAT") || pt.contains("VSAT")) {
            BigDecimal d1 = quyDoiMonVsat(nth.getThMon1(), getDiemMon(d, nth.getThMon1()), bqdCache);
            BigDecimal d2 = quyDoiMonVsat(nth.getThMon2(), getDiemMon(d, nth.getThMon2()), bqdCache);
            BigDecimal d3 = quyDoiMonVsat(nth.getThMon3(), getDiemMon(d, nth.getThMon3()), bqdCache);
            
            int w1 = (nth.getHsMon1() != null) ? nth.getHsMon1() : 1;
            int w2 = (nth.getHsMon2() != null) ? nth.getHsMon2() : 1;
            int w3 = (nth.getHsMon3() != null) ? nth.getHsMon3() : 1;
            BigDecimal W = new BigDecimal(w1 + w2 + w3);
            
            BigDecimal sumWeighted = d1.multiply(new BigDecimal(w1)).add(d2.multiply(new BigDecimal(w2))).add(d3.multiply(new BigDecimal(w3)));
            return sumWeighted.divide(W, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("3"));
        } 
        else if (pt.contains("DGNL")) {
            BigDecimal rawDgnl = d.getNl1();
            if (rawDgnl == null || rawDgnl.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
            BigDecimal quyDoi = bqdBus.quyDoiNoiSuyCached(pt, nth.getMaToHop(), null, rawDgnl, bqdCache);
            return (quyDoi != null) ? quyDoi : BigDecimal.ZERO;
        } 
        else {
            BigDecimal d1 = getDiemMon(d, nth.getThMon1());
            BigDecimal d2 = getDiemMon(d, nth.getThMon2());
            BigDecimal d3 = getDiemMon(d, nth.getThMon3());
            if (mucCC > 0) {
                BigDecimal qd = new BigDecimal(mucCC == 1 ? "8.0" : mucCC == 2 ? "9.0" : "10.0");
                if ("N1".equalsIgnoreCase(nth.getThMon1()) && qd.compareTo(d1) > 0) d1 = qd;
                if ("N1".equalsIgnoreCase(nth.getThMon2()) && qd.compareTo(d2) > 0) d2 = qd;
                if ("N1".equalsIgnoreCase(nth.getThMon3()) && qd.compareTo(d3) > 0) d3 = qd;
            }
            int w1 = (nth.getHsMon1() != null) ? nth.getHsMon1() : 1;
            int w2 = (nth.getHsMon2() != null) ? nth.getHsMon2() : 1;
            int w3 = (nth.getHsMon3() != null) ? nth.getHsMon3() : 1;
            BigDecimal W = new BigDecimal(w1 + w2 + w3);
            BigDecimal sumWeighted = d1.multiply(new BigDecimal(w1)).add(d2.multiply(new BigDecimal(w2))).add(d3.multiply(new BigDecimal(w3)));
            return sumWeighted.divide(W, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("3"));
        }
    }

    private BigDecimal quyDoiMonVsat(String monCode, BigDecimal rawScore, Map<String, List<vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi>> bqdCache) {
        if (rawScore == null || rawScore.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        BigDecimal y = bqdBus.quyDoiNoiSuyCached("V-SAT", null, monCode, rawScore, bqdCache);
        return (y != null) ? y : BigDecimal.ZERO;
    }

    private BigDecimal getDiemMon(DiemThiXetTuyen d, String monCode) {
        if (d == null || monCode == null) return BigDecimal.ZERO;
        String m = monCode.toUpperCase();
        if (m.equals("TO")) return d.getTo() != null ? d.getTo() : BigDecimal.ZERO;
        if (m.equals("LI")) return d.getLi() != null ? d.getLi() : BigDecimal.ZERO;
        if (m.equals("HO")) return d.getHo() != null ? d.getHo() : BigDecimal.ZERO;
        if (m.equals("SI")) return d.getSi() != null ? d.getSi() : BigDecimal.ZERO;
        if (m.equals("SU")) return d.getSu() != null ? d.getSu() : BigDecimal.ZERO;
        if (m.equals("DI")) return d.getDi() != null ? d.getDi() : BigDecimal.ZERO;
        if (m.equals("VA")) return d.getVa() != null ? d.getVa() : BigDecimal.ZERO;
        if (m.equals("GDCD")) return d.getGdcd() != null ? d.getGdcd() : BigDecimal.ZERO;
        if (m.equals("N1")) return d.getN1Thi() != null ? d.getN1Thi() : BigDecimal.ZERO;
        if (m.equals("TI")) return d.getTi() != null ? d.getTi() : BigDecimal.ZERO;
        if (m.equals("KTPL")) return d.getKtpl() != null ? d.getKtpl() : BigDecimal.ZERO;
        if (m.equals("NL1")) return d.getNl1() != null ? d.getNl1() : BigDecimal.ZERO;
        return BigDecimal.ZERO;
    }

    private boolean isToHopHasEnglish(String maToHop, Map<String, ToHopMon> thmMap) {
        if (maToHop == null) return false;
        ToHopMon thm = thmMap.get(maToHop);
        if (thm == null) return false;
        return "N1".equalsIgnoreCase(thm.getMon1()) || "N1".equalsIgnoreCase(thm.getMon2()) || "N1".equalsIgnoreCase(thm.getMon3());
    }

    private BigDecimal tinhDiemToHop(Map<String, BigDecimal> diemMap, ToHopMon thm) {
        BigDecimal tong = BigDecimal.ZERO;
        BigDecimal d1 = diemMap.get(thm.getMon1());
        BigDecimal d2 = diemMap.get(thm.getMon2());
        BigDecimal d3 = diemMap.get(thm.getMon3());
        if (d1 != null) tong = tong.add(d1);
        if (d2 != null) tong = tong.add(d2);
        if (d3 != null) tong = tong.add(d3);
        return tong;
    }

    private Map<String, BigDecimal> loadNguongDauVao() {
        Map<String, BigDecimal> result = new HashMap<>();
        List<Nganh> list = nganhDAO.getAll();
        for (Nganh n : list) {
            if (n.getMaNganh() != null && n.getDiemSan() != null) {
                result.put(normalizeMaNganh(n.getMaNganh()), n.getDiemSan());
            }
        }
        return result;
    }

    private String normalizeMaNganh(String maNganh) {
        return maNganh == null ? "" : maNganh.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal quyDoiDiemThxt(String phuongThuc, String maNganh, String toHop, BigDecimal diemGoc) {
        if (diemGoc == null) {
            return BigDecimal.ZERO;
        }

        String pt = normalizePhuongThuc(phuongThuc);
        if (pt.isEmpty()) {
            return diemGoc;
        }

        if (pt.contains("THPT")) {
            NganhToHop mapping = findNganhToHop(maNganh, toHop);
            if (mapping != null && mapping.getDoLech() != null) {
                return diemGoc.subtract(mapping.getDoLech());
            }
            return diemGoc;
        }

        if (pt.contains("DGNL") || pt.contains("V-SAT") || pt.contains("VSAT")) {
            BigDecimal quyDoi = bqdBus.quyDoiNoiSuy(pt, toHop, null, diemGoc);
            return quyDoi != null ? quyDoi : diemGoc;
        }

        return diemGoc;
    }

    private NganhToHop findNganhToHop(String maNganh, String toHop) {
        if (maNganh == null || maNganh.isBlank()) {
            return null;
        }

        List<NganhToHop> list = nganhToHopDAO.getByMaNganh(maNganh.trim());
        if (list == null || list.isEmpty()) {
            return null;
        }

        String normalizedToHop = toHop == null ? "" : toHop.trim().toUpperCase(Locale.ROOT);
        for (NganhToHop item : list) {
            if (item == null) {
                continue;
            }
            if (normalizedToHop.isEmpty() || normalizedToHop.equalsIgnoreCase(item.getMaToHop())) {
                return item;
            }
        }
        return list.get(0);
    }

    private String normalizePhuongThuc(String phuongThuc) {
        return phuongThuc == null ? "" : phuongThuc.trim().toUpperCase(Locale.ROOT);
    }
    
    private String getCell(Row row, DataFormatter f, Map<String, Integer> map, String[] keys) {
        for (String key : keys) {
            Integer idx = map.get(normalizeHeader(key));
            if (idx != null) {
                return f.formatCellValue(row.getCell(idx)).trim();
            }
        }
        return "";
    }

    private String normalizeHeader(String input) {
        if (input == null) return "";
        try {
            String temp = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("").toLowerCase().replaceAll("đ", "d").replaceAll("[^a-z0-9]", "");
        } catch (Exception e) {
            return input.toLowerCase().replaceAll("[^a-z0-9]", "");
        }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private int getMucChungChi(String loaiCC, String diemStr) {
        if (loaiCC == null || loaiCC.isEmpty() || loaiCC.equalsIgnoreCase("None")) return 0;
        if (diemStr == null || diemStr.isEmpty()) return 0;

        String cc = loaiCC.trim().toLowerCase(Locale.ROOT);
        double diem = 0;
        try {
            diem = Double.parseDouble(diemStr.trim().replace(',', '.'));
        } catch (Exception e) {
            // For B1, B2, C, Bậc 3, etc.
        }
        String diemUpper = diemStr.trim().toUpperCase(Locale.ROOT);

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
            // Giả định thí sinh nhập tổng điểm hoặc nhập 1 số lớn
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

    private BigDecimal parseBigDecimal(String s) {
        try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; }
    }
    private String generateNvKey(NguyenVongXetTuyen nv) {
        return nv.getNvCccd() + "_" + nv.getNvMaNganh() + "_" + nv.getNvTt(); 
    }
    
    /**
     * Tính mức điểm ưu tiên gốc (MĐUT) dựa trên Khu vực và Đối tượng.
     */
    private BigDecimal calculateBasePriority(ThiSinh ts) {
        if (ts == null) return BigDecimal.ZERO;
        BigDecimal res = BigDecimal.ZERO;
        
        // 1. Điểm ưu tiên Khu vực (KV1: 0.75, KV2-NT: 0.5, KV2: 0.25, KV3: 0)
        String kv = (ts.getKhuVuc() != null) ? ts.getKhuVuc().trim().toUpperCase() : "";
        if (kv.equals("KV1") || kv.equals("1")) {
            res = res.add(new BigDecimal("0.75"));
        } else if (kv.equals("KV2-NT") || kv.equals("KV2NT") || kv.equals("2NT")) {
            res = res.add(new BigDecimal("0.5"));
        } else if (kv.equals("KV2") || kv.equals("2")) {
            res = res.add(new BigDecimal("0.25"));
        }
        
        // 2. Điểm ưu tiên Đối tượng (Nhóm UT1: 2.0, Nhóm UT2: 1.0)
        String dt = (ts.getDoiTuong() != null) ? ts.getDoiTuong().trim() : "";
        // Nhóm ưu tiên 1: 01, 02, 03, 04
        if (dt.equals("01") || dt.equals("1") || dt.equals("02") || dt.equals("2") 
            || dt.equals("03") || dt.equals("3") || dt.equals("04") || dt.equals("4")) {
            res = res.add(new BigDecimal("2.0"));
        } 
        // Nhóm ưu tiên 2: 05, 06, 07
        else if (dt.equals("05") || dt.equals("5") || dt.equals("06") || dt.equals("6") 
                 || dt.equals("06A") || dt.equals("07") || dt.equals("7") || dt.equals("07A")) {
            res = res.add(new BigDecimal("1.0"));
        }
        
        return res;
    }

}
