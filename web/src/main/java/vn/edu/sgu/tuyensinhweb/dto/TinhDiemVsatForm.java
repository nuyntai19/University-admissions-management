package vn.edu.sgu.tuyensinhweb.dto;

import java.math.BigDecimal;

/**
 * DTO cho form tính điểm V-SAT / THPT.
 */
public class TinhDiemVsatForm {
    private String maNganh;
    private BigDecimal diemTO = BigDecimal.ZERO;   // Toán
    private BigDecimal diemLI = BigDecimal.ZERO;   // Vật lý
    private BigDecimal diemHO = BigDecimal.ZERO;   // Hóa học
    private BigDecimal diemSI = BigDecimal.ZERO;   // Sinh học
    private BigDecimal diemSU = BigDecimal.ZERO;   // Lịch sử
    private BigDecimal diemDI = BigDecimal.ZERO;   // Địa lý
    private BigDecimal diemVA = BigDecimal.ZERO;   // Ngữ văn
    private BigDecimal diemN1 = BigDecimal.ZERO;   // Tiếng Anh
    private BigDecimal diemCong = BigDecimal.ZERO; // Điểm cộng (≤3.0)
    private String khuVuc;            // KV1, KV2-NT, KV2
    private String doiTuong;          // 01-07

    public TinhDiemVsatForm() {}

    // --- Getters & Setters ---
    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String v) { this.maNganh = v; }

    public BigDecimal getDiemTO() { return diemTO; }
    public void setDiemTO(BigDecimal v) { this.diemTO = v; }

    public BigDecimal getDiemLI() { return diemLI; }
    public void setDiemLI(BigDecimal v) { this.diemLI = v; }

    public BigDecimal getDiemHO() { return diemHO; }
    public void setDiemHO(BigDecimal v) { this.diemHO = v; }

    public BigDecimal getDiemSI() { return diemSI; }
    public void setDiemSI(BigDecimal v) { this.diemSI = v; }

    public BigDecimal getDiemSU() { return diemSU; }
    public void setDiemSU(BigDecimal v) { this.diemSU = v; }

    public BigDecimal getDiemDI() { return diemDI; }
    public void setDiemDI(BigDecimal v) { this.diemDI = v; }

    public BigDecimal getDiemVA() { return diemVA; }
    public void setDiemVA(BigDecimal v) { this.diemVA = v; }

    public BigDecimal getDiemN1() { return diemN1; }
    public void setDiemN1(BigDecimal v) { this.diemN1 = v; }

    public BigDecimal getDiemCong() { return diemCong; }
    public void setDiemCong(BigDecimal v) { this.diemCong = v; }

    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String v) { this.khuVuc = v; }

    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String v) { this.doiTuong = v; }
}
