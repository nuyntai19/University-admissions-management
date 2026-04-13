package vn.edu.sgu.phanmemtuyensinh.bus;

import vn.edu.sgu.phanmemtuyensinh.dal.DiemCongXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DiemCongXetTuyenBUS {
    private DiemCongXetTuyenDAO dao = new DiemCongXetTuyenDAO();

    public List<DiemCongXetTuyen> getAll() { return dao.getAll(); }
    public List<String> suggestCccd(String kw) { return dao.searchCccd(kw); }

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

    public boolean save(DiemCongXetTuyen d) { return dao.saveOrUpdate(d); }
    public boolean delete(int id) { return dao.delete(id); }
}