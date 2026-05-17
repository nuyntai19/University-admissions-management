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
 * Service t\u00ednh \u0111i\u1ec3m x\u00e9t tuy\u1ec3n cho th\u00ed sinh (c\u00f4ng c\u1ee5 tham kh\u1ea3o).
 * T\u00e1i s\u1eed d\u1ee5ng logic t\u1eeb NguyenVongXetTuyenBUS + BangQuyDoiBUS desktop app.
 */
@Service
public class TinhDiemService {

    @Autowired private NganhRepository nganhRepo;
    @Autowired private NganhToHopRepository nganhToHopRepo;
    @Autowired private BangQuyDoiRepository bqdRepo;

    // ===== B\u1ea3ng \u0111\u1ed9 l\u1ec7ch \u0111i\u1ec3m THPT (t\u1eeb DolechTable.java) =====
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

    // ===== M\u00d4N THI MAP =====
    private static final Map<String, String> MON_NAME = new LinkedHashMap<>();
    static {
        MON_NAME.put("TO","To\u00e1n"); MON_NAME.put("LI","V\u1eadt l\u00fd"); MON_NAME.put("HO","H\u00f3a h\u1ecdc");
        MON_NAME.put("SI","Sinh h\u1ecdc"); MON_NAME.put("SU","L\u1ecbch s\u1eed"); MON_NAME.put("DI","\u0110\u1ecba l\u00fd");
        MON_NAME.put("VA","Ng\u1eef v\u0103n"); MON_NAME.put("N1","Ti\u1ebfng Anh");
    }
    public static Map<String, String> getMonNameMap() { return MON_NAME; }

    // ===== T\u00ednh \u0110UT (\u0110i\u1ec3m \u01b0u ti\u00ean g\u1ed1c - M\u0110UT) =====
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

    // ===== N\u1ed9i suy tuy\u1ebfn t\u00ednh =====
    public BigDecimal noiSuy(BigDecimal x, BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d) {
        if (a == null || b == null || c == null || d == null) return d;
        if (b.compareTo(a) == 0) return d;
        MathContext mc = MathContext.DECIMAL64;
        BigDecimal ratio = x.subtract(a, mc).divide(b.subtract(a, mc), 10, RoundingMode.HALF_UP);
        return c.add(ratio.multiply(d.subtract(c, mc), mc), mc).setScale(2, RoundingMode.HALF_UP);
    }

    // ===== T\u00ecm kho\u1ea3ng b\u00e1ch ph\u00e2n v\u1ecb v\u00e0 quy \u0111\u1ed5i =====
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
        // \u0110i\u1ec3m n\u1eb1m ngo\u00e0i t\u1ea5t c\u1ea3 kho\u1ea3ng b\u00e1ch ph\u00e2n v\u1ecb \u2192 d\u00f9ng kho\u1ea3ng cu\u1ed1i (b l\u1edbn nh\u1ea5t)
        BangQuyDoi lastInterval = null;
        for (BangQuyDoi bqd : list) {
            if (lastInterval == null || bqd.getDDiemB().compareTo(lastInterval.getDDiemB()) > 0) {
                lastInterval = bqd;
            }
        }
        if (lastInterval != null && diemGoc.compareTo(lastInterval.getDDiemB()) > 0) {
            return lastInterval.getDDiemD();
        }
        return BigDecimal.ZERO;
    }

    /**
     * T\u00ecm b\u1ea3n ghi \u0110GNL - th\u1eed exact match tr\u01b0\u1edbc, fallback d\u00f9ng LIKE '%GNL' 
     * \u0111\u1ec3 x\u1eed l\u00fd v\u1ea5n \u0111\u1ec1 encoding k\u00fd t\u1ef1 \u0110 gi\u1eefa desktop v\u00e0 web app.
     */
    private List<BangQuyDoi> findDgnlRecords(String toHop) {
        List<BangQuyDoi> list = bqdRepo.findByPhuongThucAndToHop("\u0110GNL", toHop);
        if (list.isEmpty()) list = bqdRepo.findByPhuongThucAndToHop("DGNL", toHop);
        // Fallback: LIKE '%GNL' \u0111\u1ec3 x\u1eed l\u00fd v\u1ea5n \u0111\u1ec1 encoding k\u00fd t\u1ef1 \u0110
        if (list.isEmpty()) list = bqdRepo.findByPhuongThucLikeAndToHop("%GNL", toHop);
        // Th\u1eed v\u1edbi t\u1ed5 h\u1ee3p CHUNG
        if (list.isEmpty()) list = bqdRepo.findByPhuongThucAndToHop("\u0110GNL", "CHUNG");
        if (list.isEmpty()) list = bqdRepo.findByPhuongThucAndToHop("DGNL", "CHUNG");
        if (list.isEmpty()) list = bqdRepo.findByPhuongThucLikeAndToHop("%GNL", "CHUNG");
        return list;
    }

    /** Tr\u1ea3 v\u1ec1 BangQuyDoi interval t\u00ecm \u0111\u01b0\u1ee3c (\u0111\u1ec3 hi\u1ec3n th\u1ecb c\u00f4ng th\u1ee9c tr\u00ean UI) */
    public BangQuyDoi findInterval(String phuongThuc, String toHopOrMon, BigDecimal x) {
        if (x == null) return null;
        List<BangQuyDoi> list;
        if ("\u0110GNL".equalsIgnoreCase(phuongThuc) || "DGNL".equalsIgnoreCase(phuongThuc)) {
            list = findDgnlRecords(toHopOrMon);
        } else {
            // V-SAT
            String queryKey = toHopOrMon;
            if ("N1".equals(toHopOrMon)) queryKey = "N1_THI";
            list = bqdRepo.findByPhuongThucAndMon(phuongThuc, queryKey.toUpperCase());
        }
        for (BangQuyDoi bqd : list) {
            if (x.compareTo(bqd.getDDiemA()) >= 0 && x.compareTo(bqd.getDDiemB()) <= 0) return bqd;
        }
        // \u0110i\u1ec3m n\u1eb1m ngo\u00e0i t\u1ea5t c\u1ea3 kho\u1ea3ng b\u00e1ch ph\u00e2n v\u1ecb \u2192 d\u00f9ng kho\u1ea3ng cu\u1ed1i (b l\u1edbn nh\u1ea5t)
        BangQuyDoi lastInterval = null;
        for (BangQuyDoi bqd : list) {
            if (lastInterval == null || bqd.getDDiemB().compareTo(lastInterval.getDDiemB()) > 0) {
                lastInterval = bqd;
            }
        }
        if (lastInterval != null && x.compareTo(lastInterval.getDDiemB()) > 0) {
            return lastInterval;
        }
        return null;
    }

    public BigDecimal quyDoiDGNL(String toHop, BigDecimal diemDGNL) {
        if (diemDGNL == null || diemDGNL.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        List<BangQuyDoi> list = findDgnlRecords(toHop);
        for (BangQuyDoi bqd : list) {
            if (diemDGNL.compareTo(bqd.getDDiemA()) >= 0 && diemDGNL.compareTo(bqd.getDDiemB()) <= 0) {
                return noiSuy(diemDGNL, bqd.getDDiemA(), bqd.getDDiemB(), bqd.getDDiemC(), bqd.getDDiemD());
            }
        }
        // \u0110i\u1ec3m n\u1eb1m ngo\u00e0i t\u1ea5t c\u1ea3 kho\u1ea3ng b\u00e1ch ph\u00e2n v\u1ecb \u2192 d\u00f9ng kho\u1ea3ng cu\u1ed1i (b l\u1edbn nh\u1ea5t)
        BangQuyDoi lastInterval = null;
        for (BangQuyDoi bqd : list) {
            if (lastInterval == null || bqd.getDDiemB().compareTo(lastInterval.getDDiemB()) > 0) {
                lastInterval = bqd;
            }
        }
        if (lastInterval != null && diemDGNL.compareTo(lastInterval.getDDiemB()) > 0) {
            return lastInterval.getDDiemD();
        }
        return BigDecimal.ZERO;
    }

    // ===== T\u00ednh \u0110THXT cho V-SAT/THPT =====
    public BigDecimal tinhDTHXT(BigDecimal d1, BigDecimal d2, BigDecimal d3, int w1, int w2, int w3) {
        BigDecimal W = bd(String.valueOf(w1 + w2 + w3));
        BigDecimal sum = d1.multiply(bd(String.valueOf(w1)))
                .add(d2.multiply(bd(String.valueOf(w2))))
                .add(d3.multiply(bd(String.valueOf(w3))));
        return sum.divide(W, 4, RoundingMode.HALF_UP).multiply(bd("3")).setScale(2, RoundingMode.HALF_UP);
    }

    // ===== T\u00ednh \u0111i\u1ec3m \u01b0u ti\u00ean \u0110UT theo c\u00f4ng th\u1ee9c =====
    public BigDecimal tinhDUT(BigDecimal dthgxt, BigDecimal diemCong, BigDecimal mDut) {
        BigDecimal sum = dthgxt; // Kh\u00f4ng c\u1ed9ng diemCong v\u00e0o t\u1ed5ng \u0111i\u1ec3m x\u00e9t gi\u1ea3m \u01b0u ti\u00ean
        if (sum.compareTo(bd("22.5")) < 0) return mDut;
        BigDecimal numerator = bd("30").subtract(sum);
        if (numerator.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return numerator.divide(bd("7.5"), 4, RoundingMode.HALF_UP).multiply(mDut).setScale(2, RoundingMode.HALF_UP);
    }

    // ===== L\u1ea5y \u0111\u1ed9 l\u1ec7ch =====
    public BigDecimal getDoLech(String toHopGoc, String toHop) {
        if (toHopGoc == null || toHop == null) return BigDecimal.ZERO;
        String goc = toHopGoc.trim().toUpperCase();
        String th = toHop.trim().toUpperCase();
        if (goc.equals(th)) return BigDecimal.ZERO;
        Map<String, BigDecimal> row = DOLECH.get(goc);
        if (row == null) return BigDecimal.ZERO;
        return row.getOrDefault(th, BigDecimal.ZERO);
    }

    // ===== L\u1ea5y t\u00ean m\u00f4n =====
    public String getTenMon(String monCode) {
        if (monCode == null) return "";
        return MON_NAME.getOrDefault(monCode.toUpperCase().replace("N1_THI","N1"), monCode);
    }
}
