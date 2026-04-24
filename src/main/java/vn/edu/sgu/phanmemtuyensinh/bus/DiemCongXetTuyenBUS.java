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

    public void tinhToanDiemCongVaUuTien(DiemCongXetTuyen d, String loaiCC, String mucCC, String loaiGiai, String kv, String dt, BigDecimal diemThiGoc) {
        // 1. Quy đổi điểm cộng (Max 3.0)
        BigDecimal dCC = BigDecimal.ZERO;
        if(loaiCC.equals("IELTS") && Double.parseDouble(mucCC) >= 7.0) dCC = dCC.add(new BigDecimal("2.0"));
        if(loaiGiai.contains("Nhất")) dCC = dCC.add(new BigDecimal("2.0"));
        if(dCC.compareTo(new BigDecimal("3.0")) > 0) dCC = new BigDecimal("3.0");
        d.setDiemCC(dCC);

        // 2. Lấy Mức ưu tiên gốc (MĐUT)
        BigDecimal mdutGoc = BigDecimal.ZERO;
        if(kv.equals("1")) mdutGoc = mdutGoc.add(new BigDecimal("0.75"));
        if(dt.equals("01")) mdutGoc = mdutGoc.add(new BigDecimal("2.0"));

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
    // Gọi hàm getByCccd đã có sẵn trong DiemThiXetTuyenDAO của bạn
    vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen dt = diemThiDao.getByCccd(cccd);
    
    if (dt != null) {
        // Bạn có thể chọn tổng điểm 3 môn hoặc một môn cụ thể tùy quy định
        // Ở đây tôi ví dụ cộng 3 môn cơ bản TO + VA + N1_THI (hoặc dùng hàm tinhDiemGoc bạn đã viết)
        BigDecimal toan = dt.getTo() != null ? dt.getTo() : BigDecimal.ZERO;
        BigDecimal van = dt.getVa() != null ? dt.getVa() : BigDecimal.ZERO;
        BigDecimal anh = dt.getN1Thi() != null ? dt.getN1Thi() : BigDecimal.ZERO;
        
        return toan.add(van).add(anh);
    }
    return BigDecimal.ZERO;
}
}