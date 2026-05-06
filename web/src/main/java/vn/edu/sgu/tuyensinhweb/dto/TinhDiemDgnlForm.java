package vn.edu.sgu.tuyensinhweb.dto;

import java.math.BigDecimal;

/**
 * DTO cho form tính điểm ĐGNL.
 */
public class TinhDiemDgnlForm {
    private String maNganh;
    private BigDecimal diemThi;       // Điểm ĐGNL (thang 1200)
    private BigDecimal diemCong;      // Điểm cộng (≤3.0)
    private String khuVuc;            // KV1, KV2-NT, KV2
    private String doiTuong;          // 01-07

    public TinhDiemDgnlForm() {
        this.diemCong = BigDecimal.ZERO;
    }

    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String v) { this.maNganh = v; }

    public BigDecimal getDiemThi() { return diemThi; }
    public void setDiemThi(BigDecimal v) { this.diemThi = v; }

    public BigDecimal getDiemCong() { return diemCong; }
    public void setDiemCong(BigDecimal v) { this.diemCong = v; }

    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String v) { this.khuVuc = v; }

    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String v) { this.doiTuong = v; }
}
