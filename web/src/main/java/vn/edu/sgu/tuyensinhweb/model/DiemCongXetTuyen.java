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

    @Column(name = "manganh")
    private String maNganh;

    @Column(name = "matohop")
    private String maToHop;

    @Column(name = "phuongthuc")
    private String phuongThuc;

    @Column(name = "diemCC")
    private BigDecimal diemCC;

    @Column(name = "diemUtxt")
    private BigDecimal diemUtxt;

    @Column(name = "diemTong")
    private BigDecimal diemTong;

    @Column(name = "ghichu")
    private String ghiChu;

    @Column(name = "dc_keys", nullable = false, unique = true)
    private String dcKeys;

    @Column(name = "cap_giai")
    private String capGiai;

    @Column(name = "doi_tuong_giai")
    private String doiTuongGiai;

    @Column(name = "ma_mon_giai")
    private String maMonGiai;

    @Column(name = "loai_giai")
    private String loaiGiai;

    @Column(name = "diem_cong_mon_giai")
    private BigDecimal diemCongMonGiai;

    @Column(name = "diem_cong_khong_mon")
    private BigDecimal diemCongKhongMon;

    @Column(name = "co_chung_chi")
    private Boolean coChungChi;

    @Column(name = "chung_chi_ngoai_ngu")
    private String chungChi;

    @Column(name = "diem_chung_chi")
    private String mucDatDuoc;

    @Column(name = "diem_quy_doi_chung_chi")
    private BigDecimal diemQuyDoiChungChi;

    public DiemCongXetTuyen() {}

    // --- Getters & Setters ---
    public int getIdDiemCong() { return idDiemCong; }
    public void setIdDiemCong(int v) { this.idDiemCong = v; }
    public String getTsCccd() { return tsCccd; }
    public void setTsCccd(String v) { this.tsCccd = v; }
    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String v) { this.maNganh = v; }
    public String getMaToHop() { return maToHop; }
    public void setMaToHop(String v) { this.maToHop = v; }
    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String v) { this.phuongThuc = v; }
    public BigDecimal getDiemCC() { return diemCC; }
    public void setDiemCC(BigDecimal v) { this.diemCC = v; }
    public BigDecimal getDiemUtxt() { return diemUtxt; }
    public void setDiemUtxt(BigDecimal v) { this.diemUtxt = v; }
    public BigDecimal getDiemTong() { return diemTong; }
    public void setDiemTong(BigDecimal v) { this.diemTong = v; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String v) { this.ghiChu = v; }
    public String getDcKeys() { return dcKeys; }
    public void setDcKeys(String v) { this.dcKeys = v; }
    public String getCapGiai() { return capGiai; }
    public void setCapGiai(String v) { this.capGiai = v; }
    public String getDoiTuongGiai() { return doiTuongGiai; }
    public void setDoiTuongGiai(String v) { this.doiTuongGiai = v; }
    public String getMaMonGiai() { return maMonGiai; }
    public void setMaMonGiai(String v) { this.maMonGiai = v; }
    public String getLoaiGiai() { return loaiGiai; }
    public void setLoaiGiai(String v) { this.loaiGiai = v; }
    public BigDecimal getDiemCongMonGiai() { return diemCongMonGiai; }
    public void setDiemCongMonGiai(BigDecimal v) { this.diemCongMonGiai = v; }
    public BigDecimal getDiemCongKhongMon() { return diemCongKhongMon; }
    public void setDiemCongKhongMon(BigDecimal v) { this.diemCongKhongMon = v; }
    public Boolean getCoChungChi() { return coChungChi; }
    public void setCoChungChi(Boolean v) { this.coChungChi = v; }
    public String getChungChi() { return chungChi; }
    public void setChungChi(String v) { this.chungChi = v; }
    public String getMucDatDuoc() { return mucDatDuoc; }
    public void setMucDatDuoc(String v) { this.mucDatDuoc = v; }
    public BigDecimal getDiemQuyDoiChungChi() { return diemQuyDoiChungChi; }
    public void setDiemQuyDoiChungChi(BigDecimal v) { this.diemQuyDoiChungChi = v; }
}
