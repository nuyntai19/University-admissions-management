package vn.edu.sgu.tuyensinhweb.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_nguyenvongxettuyen")
public class NguyenVongXetTuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnv")
    private int idNv;

    @Column(name = "nn_cccd", nullable = false, length = 45)
    private String nvCccd;

    @Column(name = "nv_tt", nullable = false)
    private int nvTt;

    @Column(name = "nv_manganh", nullable = false, length = 45)
    private String nvMaNganh;

    @Column(name = "nv_tenmanganh", length = 255)
    private String nvTenMaNganh;

    @Column(name = "nv_tuyenthang", length = 100)
    private String nvTuyenThang;

    @Column(name = "diem_thxt")
    private BigDecimal diemThxt;

    @Column(name = "diem_utqd")
    private BigDecimal diemUtqd;

    @Column(name = "diem_cong")
    private BigDecimal diemCong;

    @Column(name = "diem_xettuyen")
    private BigDecimal diemXetTuyen;

    @Column(name = "nv_ketqua", length = 45)
    private String nvKetQua;

    @Column(name = "nv_keys", unique = true, length = 45)
    private String nvKeys;

    @Column(name = "tt_phuongthuc", length = 45)
    private String ttPhuongThuc;

    @Column(name = "tt_thm", length = 45)
    private String ttThm;

    // --- Getters & Setters ---
    public int getIdNv() { return idNv; }
    public void setIdNv(int v) { this.idNv = v; }

    public String getNvCccd() { return nvCccd; }
    public void setNvCccd(String v) { this.nvCccd = v; }

    public int getNvTt() { return nvTt; }
    public void setNvTt(int v) { this.nvTt = v; }

    public String getNvMaNganh() { return nvMaNganh; }
    public void setNvMaNganh(String v) { this.nvMaNganh = v; }

    public String getNvTenMaNganh() { return nvTenMaNganh; }
    public void setNvTenMaNganh(String v) { this.nvTenMaNganh = v; }

    public String getNvTuyenThang() { return nvTuyenThang; }
    public void setNvTuyenThang(String v) { this.nvTuyenThang = v; }

    public BigDecimal getDiemThxt() { return diemThxt; }
    public void setDiemThxt(BigDecimal v) { this.diemThxt = v; }

    public BigDecimal getDiemUtqd() { return diemUtqd; }
    public void setDiemUtqd(BigDecimal v) { this.diemUtqd = v; }

    public BigDecimal getDiemCong() { return diemCong; }
    public void setDiemCong(BigDecimal v) { this.diemCong = v; }

    public BigDecimal getDiemXetTuyen() { return diemXetTuyen; }
    public void setDiemXetTuyen(BigDecimal v) { this.diemXetTuyen = v; }

    public String getNvKetQua() { return nvKetQua; }
    public void setNvKetQua(String v) { this.nvKetQua = v; }

    public String getNvKeys() { return nvKeys; }
    public void setNvKeys(String v) { this.nvKeys = v; }

    public String getTtPhuongThuc() { return ttPhuongThuc; }
    public void setTtPhuongThuc(String v) { this.ttPhuongThuc = v; }

    public String getTtThm() { return ttThm; }
    public void setTtThm(String v) { this.ttThm = v; }
}
