package vn.edu.sgu.phanmemtuyensinh.utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Quản lý bảng độ lệch điểm giữa các tổ hợp so với tổ hợp gốc.
 * Dữ liệu này được dùng để gán mặc định vào database (bảng xt_nganh_tohop) 
 * khi import hoặc thêm mới mapping.
 */
public final class DolechTable {
    private static final Map<String, Map<String, BigDecimal>> TABLE = new HashMap<>();

    static {
        // Khởi tạo bảng độ lệch chuẩn theo tài liệu 'cac cong thuc tinh.txt'
        // Cột m quy về hàng n: Điểm quy đổi = Điểm thực - (Giá trị trong ô)
        
        // Hàng A00
        Map<String, BigDecimal> rA00 = new HashMap<>();
        rA00.put("A01", new BigDecimal("-0.69"));
        rA00.put("B00", new BigDecimal("-1.21"));
        rA00.put("C00", new BigDecimal("2.32"));
        rA00.put("C01", new BigDecimal("0.94"));
        rA00.put("D01", new BigDecimal("-0.68"));
        rA00.put("D07", new BigDecimal("-1.62"));
        TABLE.put("A00", rA00);

        // Hàng A01
        Map<String, BigDecimal> rA01 = new HashMap<>();
        rA01.put("A00", new BigDecimal("0.69"));
        rA01.put("B00", new BigDecimal("-0.52"));
        rA01.put("C00", new BigDecimal("3.01"));
        rA01.put("C01", new BigDecimal("1.63"));
        rA01.put("D01", new BigDecimal("0.01"));
        rA01.put("D07", new BigDecimal("-0.93"));
        TABLE.put("A01", rA01);

        // Hàng B00
        Map<String, BigDecimal> rB00 = new HashMap<>();
        rB00.put("A00", new BigDecimal("1.21"));
        rB00.put("A01", new BigDecimal("0.52"));
        rB00.put("C00", new BigDecimal("3.53"));
        rB00.put("C01", new BigDecimal("2.15"));
        rB00.put("D01", new BigDecimal("0.53"));
        rB00.put("D07", new BigDecimal("-0.41"));
        TABLE.put("B00", rB00);

        // Hàng C00
        Map<String, BigDecimal> rC00 = new HashMap<>();
        rC00.put("A00", new BigDecimal("-2.32"));
        rC00.put("A01", new BigDecimal("-3.01"));
        rC00.put("B00", new BigDecimal("-3.53"));
        rC00.put("C01", new BigDecimal("-1.38"));
        rC00.put("D01", new BigDecimal("-3.00"));
        rC00.put("D07", new BigDecimal("-3.94"));
        TABLE.put("C00", rC00);

        // Hàng C01
        Map<String, BigDecimal> rC01 = new HashMap<>();
        rC01.put("A00", new BigDecimal("-0.94"));
        rC01.put("A01", new BigDecimal("-1.63"));
        rC01.put("B00", new BigDecimal("-2.15"));
        rC01.put("C00", new BigDecimal("1.38"));
        rC01.put("D01", new BigDecimal("-1.62"));
        rC01.put("D07", new BigDecimal("-2.56"));
        TABLE.put("C01", rC01);

        // Hàng D01
        Map<String, BigDecimal> rD01 = new HashMap<>();
        rD01.put("A00", new BigDecimal("0.68"));
        rD01.put("A01", new BigDecimal("-0.01"));
        rD01.put("B00", new BigDecimal("-0.53"));
        rD01.put("C00", new BigDecimal("3.0"));
        rD01.put("C01", new BigDecimal("1.62"));
        rD01.put("D07", new BigDecimal("-0.94"));
        TABLE.put("D01", rD01);
    }

    private DolechTable() {
    }

    /**
     * Lấy giá trị độ lệch giữa tổ hợp đang xét và tổ hợp gốc.
     * @param toHopGoc Mã tổ hợp gốc của ngành (VD: A00)
     * @param toHop Mã tổ hợp của thí sinh (VD: A01)
     * @return BigDecimal độ lệch, mặc định là 0 nếu không tìm thấy.
     */
    public static BigDecimal getDoLech(String toHopGoc, String toHop) {
        String goc = norm(toHopGoc);
        String th = norm(toHop);
        if (goc.isEmpty() || th.isEmpty() || goc.equals(th)) {
            return BigDecimal.ZERO;
        }

        Map<String, BigDecimal> row = TABLE.get(goc);
        if (row == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal v = row.get(th);
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }
}
