package vn.edu.sgu.tuyensinhweb.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Kết quả tính điểm chi tiết cho 1 tổ hợp
 */
public class ToHopResult {
    private String maToHop;
    private String tenToHop;
    private List<MonDetail> monDetails = new ArrayList<>();
    private BigDecimal diemXetNguong;  // Tổng 3 môn + ưu tiên (trước x3)
    private BigDecimal dthxt;          // ĐTHXT (thang 30)
    private BigDecimal doLech;
    private String toHopGoc;
    private BigDecimal dthgxt;         // ĐTHGXT = ĐTHXT - doLech
    private BigDecimal diemCong;
    private BigDecimal dut;            // ĐUT
    private BigDecimal diemXetTuyen;   // ĐXT = ĐTHGXT + ĐC + ĐUT
    private BigDecimal nguongDauVao;
    private String ketQua;             // "Đạt" / "Không đạt"
    private boolean isBest;            // Tổ hợp cho điểm cao nhất?

    public static class MonDetail {
        private String monCode;
        private String tenMon;
        private BigDecimal diemGoc;
        private BigDecimal diemQuyDoi;
        private int heSo;
        private String congThuc;       // Chuỗi mô tả công thức
        private String loi;            // Lỗi nếu có (VD: không tìm thấy phân vị)

        // --- Getters & Setters ---
        public String getMonCode() { return monCode; }
        public void setMonCode(String v) { this.monCode = v; }
        public String getTenMon() { return tenMon; }
        public void setTenMon(String v) { this.tenMon = v; }
        public BigDecimal getDiemGoc() { return diemGoc; }
        public void setDiemGoc(BigDecimal v) { this.diemGoc = v; }
        public BigDecimal getDiemQuyDoi() { return diemQuyDoi; }
        public void setDiemQuyDoi(BigDecimal v) { this.diemQuyDoi = v; }
        public int getHeSo() { return heSo; }
        public void setHeSo(int v) { this.heSo = v; }
        public String getCongThuc() { return congThuc; }
        public void setCongThuc(String v) { this.congThuc = v; }
        public String getLoi() { return loi; }
        public void setLoi(String v) { this.loi = v; }
    }

    // --- Getters & Setters ---
    public String getMaToHop() { return maToHop; }
    public void setMaToHop(String v) { this.maToHop = v; }
    public String getTenToHop() { return tenToHop; }
    public void setTenToHop(String v) { this.tenToHop = v; }
    public List<MonDetail> getMonDetails() { return monDetails; }
    public void setMonDetails(List<MonDetail> v) { this.monDetails = v; }
    public BigDecimal getDiemXetNguong() { return diemXetNguong; }
    public void setDiemXetNguong(BigDecimal v) { this.diemXetNguong = v; }
    public BigDecimal getDthxt() { return dthxt; }
    public void setDthxt(BigDecimal v) { this.dthxt = v; }
    public BigDecimal getDoLech() { return doLech; }
    public void setDoLech(BigDecimal v) { this.doLech = v; }
    public String getToHopGoc() { return toHopGoc; }
    public void setToHopGoc(String v) { this.toHopGoc = v; }
    public BigDecimal getDthgxt() { return dthgxt; }
    public void setDthgxt(BigDecimal v) { this.dthgxt = v; }
    public BigDecimal getDiemCong() { return diemCong; }
    public void setDiemCong(BigDecimal v) { this.diemCong = v; }
    public BigDecimal getDut() { return dut; }
    public void setDut(BigDecimal v) { this.dut = v; }
    public BigDecimal getDiemXetTuyen() { return diemXetTuyen; }
    public void setDiemXetTuyen(BigDecimal v) { this.diemXetTuyen = v; }
    public BigDecimal getNguongDauVao() { return nguongDauVao; }
    public void setNguongDauVao(BigDecimal v) { this.nguongDauVao = v; }
    public String getKetQua() { return ketQua; }
    public void setKetQua(String v) { this.ketQua = v; }
    public boolean isBest() { return isBest; }
    public void setBest(boolean v) { this.isBest = v; }
}
