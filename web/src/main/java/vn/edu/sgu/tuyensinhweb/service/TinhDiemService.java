package vn.edu.sgu.tuyensinhweb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.sgu.tuyensinhweb.model.*;
import vn.edu.sgu.tuyensinhweb.repository.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service tính điểm xét tuyển cho thí sinh (công cụ tham khảo).
 * Tái sử dụng logic từ NguyenVongXetTuyenBUS + BangQuyDoiBUS desktop app.
 */
@Service
public class TinhDiemService {

    @Autowired private NganhRepository nganhRepo;
    @Autowired private NganhToHopRepository nganhToHopRepo;
    @Autowired private BangQuyDoiRepository bqdRepo;

    // ===== Bảng độ lệch điểm THPT (từ DolechTable.java) =====
    private static final Map<String, Map<String, BigDecimal>> DOLECH = new HashMap<>();
    static {
        Map<String, BigDecimal> rA00 = Map.of("A01",bd("-0.69"),"B00",bd("-1.21"),"C00",bd("2.32"),"C01",bd("0.94"),"D01",bd("-0.68"),"D07",bd("-1.62"));
        Map<String, BigDecimal> rA01 = Map.of("A00",bd("0.69"),"B00",bd("-0.52"),"C00",bd("3.01"),"C01",bd("1.63"),"D01",bd("0.01"),"D07",bd("-0.93"));
        Map<String, BigDecimal> rB00 = Map.of("A00",bd("1.21"),"A01",bd("0.52"),"C00",bd("3.53"),"C01",bd("2.15"),"D01",bd("0.53"),"D07",bd("-0.41"));
        Map<String, BigDecimal> rC00 = Map.of("A00",bd("-2.32"),"A01",bd("-3.01"),"B00",bd("-3.53"),"C01",bd("-1.38"),"D01",bd("-3.00"),"D07",bd("-3.94"));
        Map<String, BigDecimal> rC01 = Map.of("A00",bd("-0.94"),"A01",bd("-1.63"),"B00",bd("-2.15"),"C00",bd("1.38"),"D01",bd("-1.62"),"D07",bd("-2.56"));
        Map<String, BigDecimal> rD01 = Map.of("A00",bd("0.68"),"A01",bd("-0.01"),"B00",bd("-0.53"),"C00",bd("3.0"),"C01",bd("1.62"),"D07",bd("-0.94"));
        DOLECH.put("A00",rA00); DOLECH.put("A01",rA01); DOLECH.put("B00",rB00);
        DOLECH.put("C00",rC00); DOLECH.put("C01",rC01); DOLECH.put("D01",rD01);
    }
    private static BigDecimal bd(String s) { return new BigDecimal(s); }

    // ===== MÔN THI MAP =====
    private static final Map<String, String> MON_NAME = new LinkedHashMap<>();
    static {
        MON_NAME.put("TO","Toán"); MON_NAME.put("LI","Vật lý"); MON_NAME.put("HO","Hóa học");
        MON_NAME.put("SI","Sinh học"); MON_NAME.put("SU","Lịch sử"); MON_NAME.put("DI","Địa lý");
        MON_NAME.put("VA","Ngữ văn"); MON_NAME.put("N1","Tiếng Anh");
    }
    public static Map<String, String> getMonNameMap() { return MON_NAME; }

    // ===== Tính ĐUT (Điểm ưu tiên gốc - MĐUT) =====
    public BigDecimal tinhMDUT(String khuVuc, String doiTuong) {
        BigDecimal res = BigDecimal.ZERO;
        String kv = khuVuc == null ? "" : khuVuc.trim().toUpperCase();
        if (kv.contains("KV1") || kv.equals("1")) res = res.add(bd("0.75"));
        else if (kv.contains("2NT") || kv.contains("KV2-NT") || kv.contains("KV2NT")) res = res.add(bd("0.5"));
        else if (kv.contains("KV2") || kv.equals("2")) res = res.add(bd("0.25"));

        String dt = doiTuong == null ? "" : doiTuong.trim();
        if (List.of("01","1","02","2","03","3","04","4").contains(dt)) res = res.add(bd("2.0"));
        else if (List.of("05","5","06","6","06A","07","7","07A").contains(dt)) res = res.add(bd("1.0"));
        return res;
    }

    // ===== Nội suy tuyến tính =====
    public BigDecimal noiSuy(BigDecimal x, BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d) {
        if (a == null || b == null || c == null || d == null) return d;
        if (b.compareTo(a) == 0) return d;
        MathContext mc = MathContext.DECIMAL64;
        BigDecimal ratio = x.subtract(a, mc).divide(b.subtract(a, mc), 10, RoundingMode.HALF_UP);
        return c.add(ratio.multiply(d.subtract(c, mc), mc), mc).setScale(2, RoundingMode.HALF_UP);
    }

    // ===== Tìm khoảng bách phân vị và quy đổi =====
    public BigDecimal quyDoiVSAT(String mon, BigDecimal diemGoc) {
        if (diemGoc == null || diemGoc.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        String monQuery = mon;
        if ("N1".equals(mon)) monQuery = "N1_THI";
        List<BangQuyDoi> list = bqdRepo.findByPhuongThucAndMon("V-SAT", monQuery.toUpperCase());
        for (BangQuyDoi bqd : list) {
            if (diemGoc.compareTo(bqd.getDDiemA()) >= 0 && diemGoc.compareTo(bqd.getDDiemB()) <= 0) {
                return noiSuy(diemGoc, bqd.getDDiemA(), bqd.getDDiemB(), bqd.getDDiemC(), bqd.getDDiemD());
            }
        }
        return BigDecimal.ZERO;
    }

    /** Trả về BangQuyDoi interval tìm được (để hiển thị công thức trên UI) */
    public BangQuyDoi findInterval(String phuongThuc, String toHopOrMon, BigDecimal x) {
        if (x == null) return null;
        List<BangQuyDoi> list;
        if ("ĐGNL".equalsIgnoreCase(phuongThuc) || "DGNL".equalsIgnoreCase(phuongThuc)) {
            list = bqdRepo.findByPhuongThucAndToHop("ĐGNL", toHopOrMon);
            if (list.isEmpty()) list = bqdRepo.findByPhuongThucAndToHop("DGNL", toHopOrMon);
            if (list.isEmpty()) list = bqdRepo.findByPhuongThucAndToHop("ĐGNL", "CHUNG");
            if (list.isEmpty()) list = bqdRepo.findByPhuongThucAndToHop("DGNL", "CHUNG");
        } else {
            // V-SAT
            String queryKey = toHopOrMon;
            if ("N1".equals(toHopOrMon)) queryKey = "N1_THI";
            list = bqdRepo.findByPhuongThucAndMon(phuongThuc, queryKey.toUpperCase());
        }
        for (BangQuyDoi bqd : list) {
            if (x.compareTo(bqd.getDDiemA()) >= 0 && x.compareTo(bqd.getDDiemB()) <= 0) return bqd;
        }
        return null;
    }

    public BigDecimal quyDoiDGNL(String toHop, BigDecimal diemDGNL) {
        if (diemDGNL == null || diemDGNL.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        List<BangQuyDoi> list = bqdRepo.findByPhuongThucAndToHop("ĐGNL", toHop);
        if (list.isEmpty()) list = bqdRepo.findByPhuongThucAndToHop("DGNL", toHop);
        if (list.isEmpty()) list = bqdRepo.findByPhuongThucAndToHop("ĐGNL", "CHUNG");
        if (list.isEmpty()) list = bqdRepo.findByPhuongThucAndToHop("DGNL", "CHUNG");
        for (BangQuyDoi bqd : list) {
            if (diemDGNL.compareTo(bqd.getDDiemA()) >= 0 && diemDGNL.compareTo(bqd.getDDiemB()) <= 0) {
                return noiSuy(diemDGNL, bqd.getDDiemA(), bqd.getDDiemB(), bqd.getDDiemC(), bqd.getDDiemD());
            }
        }
        return BigDecimal.ZERO;
    }

    // ===== Tính ĐTHXT cho V-SAT/THPT =====
    public BigDecimal tinhDTHXT(BigDecimal d1, BigDecimal d2, BigDecimal d3, int w1, int w2, int w3) {
        BigDecimal W = bd(String.valueOf(w1 + w2 + w3));
        BigDecimal sum = d1.multiply(bd(String.valueOf(w1)))
                .add(d2.multiply(bd(String.valueOf(w2))))
                .add(d3.multiply(bd(String.valueOf(w3))));
        return sum.divide(W, 4, RoundingMode.HALF_UP).multiply(bd("3")).setScale(2, RoundingMode.HALF_UP);
    }

    // ===== Tính điểm ưu tiên ĐUT theo công thức =====
    public BigDecimal tinhDUT(BigDecimal dthgxt, BigDecimal diemCong, BigDecimal mDut) {
        BigDecimal sum = dthgxt; // Không cộng diemCong vào tổng điểm xét giảm ưu tiên
        if (sum.compareTo(bd("22.5")) < 0) return mDut;
        BigDecimal numerator = bd("30").subtract(sum);
        if (numerator.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return numerator.divide(bd("7.5"), 4, RoundingMode.HALF_UP).multiply(mDut).setScale(2, RoundingMode.HALF_UP);
    }

    // ===== Lấy độ lệch =====
    public BigDecimal getDoLech(String toHopGoc, String toHop) {
        if (toHopGoc == null || toHop == null) return BigDecimal.ZERO;
        String goc = toHopGoc.trim().toUpperCase();
        String th = toHop.trim().toUpperCase();
        if (goc.equals(th)) return BigDecimal.ZERO;
        Map<String, BigDecimal> row = DOLECH.get(goc);
        if (row == null) return BigDecimal.ZERO;
        return row.getOrDefault(th, BigDecimal.ZERO);
    }

    // ===== Lấy tên môn =====
    public String getTenMon(String monCode) {
        if (monCode == null) return "";
        return MON_NAME.getOrDefault(monCode.toUpperCase().replace("N1_THI","N1"), monCode);
    }
}
