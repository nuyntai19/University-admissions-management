package vn.edu.sgu.phanmemtuyensinh.bus;

import vn.edu.sgu.phanmemtuyensinh.dal.BangQuyDoiDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public class BangQuyDoiBUS {
    private final BangQuyDoiDAO dao = new BangQuyDoiDAO();
    private String lastError = "";

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
        if (!validateAndNormalize(bqd, true)) {
            return false;
        }
        return dao.add(bqd);
    }

    public boolean update(BangQuyDoi bqd) {
        if (!validateAndNormalize(bqd, false)) {
            return false;
        }
        return dao.update(bqd);
    }

    public boolean delete(int idQd) {
        return dao.delete(idQd);
    }

    /**
     * Quy đổi điểm theo phương pháp bách phân vị + nội suy tuyến tính.
     * - Với V-SAT: truyền mon (TO/LI/...) và toHop có thể null.
     * - Với ĐGNL: truyền toHop (A01/...) và mon có thể null.
     */
    public BigDecimal quyDoiNoiSuy(String phuongThuc, String toHop, String mon, BigDecimal x) {
        lastError = "";
        if (x == null) {
            lastError = "Thiếu điểm đầu vào";
            return null;
        }
        String pt = norm(phuongThuc);
        if (pt.isEmpty()) {
            lastError = "Thiếu phương thức";
            return null;
        }

        String th = emptyToNull(norm(toHop));
        String m = emptyToNull(norm(mon));

        BangQuyDoi interval = dao.findIntervalExclusiveLower(pt, th, m, x);
        if (interval == null) {
            interval = dao.findIntervalInclusive(pt, th, m, x);
        }
        if (interval == null) {
            lastError = "Không tìm thấy khoảng quy đổi phù hợp";
            return null;
        }

        BigDecimal a = interval.getDDiemA();
        BigDecimal b = interval.getDDiemB();
        BigDecimal c = interval.getDDiemC();
        BigDecimal d = interval.getDDiemD();
        if (a == null || b == null || c == null || d == null) {
            lastError = "Thiếu dữ liệu mốc quy đổi (a,b,c,d)";
            return null;
        }
        if (b.compareTo(a) == 0) {
            return d;
        }

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
}
