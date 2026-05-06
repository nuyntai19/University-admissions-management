package vn.edu.sgu.tuyensinhweb.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_nganh")
public class Nganh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnganh")
    private int idNganh;

    @Column(name = "manganh", unique = true, nullable = false, length = 45)
    private String maNganh;

    @Column(name = "tennganh", nullable = false, length = 150)
    private String tenNganh;

    @Column(name = "n_tohopgoc", length = 10)
    private String toHopGoc;

    @Column(name = "n_chitieu")
    private int chiTieu;

    @Column(name = "n_diemsan")
    private BigDecimal diemSan;

    @Column(name = "n_diemtrungtuyen")
    private BigDecimal diemTrungTuyen;

    @Column(name = "n_dgnl", length = 1)
    private String dgnl;

    @Column(name = "n_thpt", length = 1)
    private String thpt;

    @Column(name = "n_vsat", length = 1)
    private String vsat;

    // --- Getters & Setters ---
    public int getIdNganh() { return idNganh; }
    public void setIdNganh(int v) { this.idNganh = v; }

    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String v) { this.maNganh = v; }

    public String getTenNganh() { return tenNganh; }
    public void setTenNganh(String v) { this.tenNganh = v; }

    public String getToHopGoc() { return toHopGoc; }
    public void setToHopGoc(String v) { this.toHopGoc = v; }

    public int getChiTieu() { return chiTieu; }
    public void setChiTieu(int v) { this.chiTieu = v; }

    public BigDecimal getDiemSan() { return diemSan; }
    public void setDiemSan(BigDecimal v) { this.diemSan = v; }

    public BigDecimal getDiemTrungTuyen() { return diemTrungTuyen; }
    public void setDiemTrungTuyen(BigDecimal v) { this.diemTrungTuyen = v; }

    public String getDgnl() { return dgnl; }
    public void setDgnl(String v) { this.dgnl = v; }

    public String getThpt() { return thpt; }
    public void setThpt(String v) { this.thpt = v; }

    public String getVsat() { return vsat; }
    public void setVsat(String v) { this.vsat = v; }

    /** Hiển thị trên dropdown: "7140114 - Quản lý giáo dục" */
    public String getDisplay() {
        return maNganh + " - " + tenNganh;
    }
}
