package vn.edu.sgu.tuyensinhweb.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO tổng hợp kết quả tính điểm (chứa nhiều ToHopResult).
 */
public class TinhDiemResult {
    private String maNganh;
    private String tenNganh;
    private String toHopGoc;
    private BigDecimal nguongDauVao;
    private BigDecimal mDut;              // MĐUT (mức điểm ưu tiên gốc)
    private BigDecimal diemCong;
    private String khuVuc;
    private String doiTuong;
    private List<ToHopResult> toHopResults = new ArrayList<>();
    private String phuongThuc;            // ĐGNL / V-SAT / THPT

    // Riêng ĐGNL (chỉ 1 kết quả, không chia theo tổ hợp)
    private BigDecimal diemThi;           // Điểm thi gốc
    private BigDecimal diemQuyDoi;        // Điểm sau quy đổi (thang 30)
    private BigDecimal dut;               // ĐUT
    private BigDecimal diemXetTuyen;      // ĐXT cuối
    private String congThuc;
    private String ketQua;                // Đạt / Không đạt

    // --- Getters & Setters ---
    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String v) { this.maNganh = v; }

    public String getTenNganh() { return tenNganh; }
    public void setTenNganh(String v) { this.tenNganh = v; }

    public String getToHopGoc() { return toHopGoc; }
    public void setToHopGoc(String v) { this.toHopGoc = v; }

    public BigDecimal getNguongDauVao() { return nguongDauVao; }
    public void setNguongDauVao(BigDecimal v) { this.nguongDauVao = v; }

    public BigDecimal getMDut() { return mDut; }
    public void setMDut(BigDecimal v) { this.mDut = v; }

    public BigDecimal getDiemCong() { return diemCong; }
    public void setDiemCong(BigDecimal v) { this.diemCong = v; }

    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String v) { this.khuVuc = v; }

    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String v) { this.doiTuong = v; }

    public List<ToHopResult> getToHopResults() { return toHopResults; }
    public void setToHopResults(List<ToHopResult> v) { this.toHopResults = v; }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String v) { this.phuongThuc = v; }

    public BigDecimal getDiemThi() { return diemThi; }
    public void setDiemThi(BigDecimal v) { this.diemThi = v; }

    public BigDecimal getDiemQuyDoi() { return diemQuyDoi; }
    public void setDiemQuyDoi(BigDecimal v) { this.diemQuyDoi = v; }

    public BigDecimal getDut() { return dut; }
    public void setDut(BigDecimal v) { this.dut = v; }

    public BigDecimal getDiemXetTuyen() { return diemXetTuyen; }
    public void setDiemXetTuyen(BigDecimal v) { this.diemXetTuyen = v; }

    public String getCongThuc() { return congThuc; }
    public void setCongThuc(String v) { this.congThuc = v; }

    public String getKetQua() { return ketQua; }
    public void setKetQua(String v) { this.ketQua = v; }
}
