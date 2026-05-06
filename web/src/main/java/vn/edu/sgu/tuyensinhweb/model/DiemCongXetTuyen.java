package vn.edu.sgu.tuyensinhweb.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_diemcongxetuyen")
public class DiemCongXetTuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong")
    private int idDiemCong;

    @Column(name = "ts_cccd", nullable = false)
    private String tsCccd;

    @Column(name = "diemCC")
    private BigDecimal diemCC;

    @Column(name = "diemUtxt")
    private BigDecimal diemUtxt;

    @Column(name = "diemTong")
    private BigDecimal diemTong;

    @Column(name = "diem_quy_doi_chung_chi")
    private BigDecimal diemQuyDoiChungChi;

    @Column(name = "diem_cong_khong_mon")
    private BigDecimal diemCongKhongMon;

    @Column(name = "dc_keys", nullable = false, unique = true)
    private String dcKeys;

    @Column(name = "chung_chi_ngoai_ngu")
    private String chungChi;

    @Column(name = "diem_chung_chi")
    private String mucDatDuoc;

    @Column(name = "loai_giai")
    private String loaiGiai;

    @Column(name = "cap_giai")
    private String capGiai;

    @Column(name = "diem_cong_mon_giai")
    private BigDecimal diemCongMonGiai;

    @Column(name = "co_chung_chi")
    private Boolean coChungChi;

    public DiemCongXetTuyen() {}

    // --- Getters & Setters ---
    public int getIdDiemCong() { return idDiemCong; }
    public void setIdDiemCong(int v) { this.idDiemCong = v; }
    public String getTsCccd() { return tsCccd; }
    public void setTsCccd(String v) { this.tsCccd = v; }
    public BigDecimal getDiemCC() { return diemCC; }
    public void setDiemCC(BigDecimal v) { this.diemCC = v; }
    public BigDecimal getDiemUtxt() { return diemUtxt; }
    public void setDiemUtxt(BigDecimal v) { this.diemUtxt = v; }
    public BigDecimal getDiemTong() { return diemTong; }
    public void setDiemTong(BigDecimal v) { this.diemTong = v; }
    public BigDecimal getDiemQuyDoiChungChi() { return diemQuyDoiChungChi; }
    public void setDiemQuyDoiChungChi(BigDecimal v) { this.diemQuyDoiChungChi = v; }
    public BigDecimal getDiemCongKhongMon() { return diemCongKhongMon; }
    public void setDiemCongKhongMon(BigDecimal v) { this.diemCongKhongMon = v; }
    public String getDcKeys() { return dcKeys; }
    public void setDcKeys(String v) { this.dcKeys = v; }
    public String getChungChi() { return chungChi; }
    public void setChungChi(String v) { this.chungChi = v; }
    public String getMucDatDuoc() { return mucDatDuoc; }
    public void setMucDatDuoc(String v) { this.mucDatDuoc = v; }
    public String getLoaiGiai() { return loaiGiai; }
    public void setLoaiGiai(String v) { this.loaiGiai = v; }
    public String getCapGiai() { return capGiai; }
    public void setCapGiai(String v) { this.capGiai = v; }
    public BigDecimal getDiemCongMonGiai() { return diemCongMonGiai; }
    public void setDiemCongMonGiai(BigDecimal v) { this.diemCongMonGiai = v; }
    public Boolean getCoChungChi() { return coChungChi; }
    public void setCoChungChi(Boolean v) { this.coChungChi = v; }
}
