package vn.edu.sgu.phanmemtuyensinh.dal.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "xt_diemcongxetuyen")
public class DiemCongXetTuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong")
    private int idDiemCong;

    @Column(name = "ts_cccd", nullable = false)
    private String tsCccd;

    @Column(name = "diemCC") // Tổng điểm cộng sau khi chặn trần 3.0
    private BigDecimal diemCC;

    @Column(name = "diemUtxt") // Điểm UT thực tế sau khi tính giảm dần
    private BigDecimal diemUtxt;

    @Column(name = "diemTong") // Tổng (ĐC + UT)
    private BigDecimal diemTong;

    @Column(name = "diem_quy_doi_chung_chi") // Điểm từ chứng chỉ ngoại ngữ
    private BigDecimal diemQuyDoiChungChi;

    @Column(name = "diem_cong_khong_mon") // Điểm từ giải thưởng/khác
    private BigDecimal diemCongKhongMon;

    @Column(name = "dc_keys", nullable = false, unique = true)
    private String dcKeys;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String ghiChu;
    
    @Column(name = "chung_chi_ngoai_ngu")
    private String chungChi;
    
    @Column(name = "diem_chung_chi")
    private String mucDatDuoc;
    
    @Column(name = "loai_giai")
    private String loaiGiai;

    @Column(name = "cap_giai")
    private String capGiai;

    @Column(name = "doi_tuong_giai")
    private String doiTuongGiai;

    @Column(name = "ma_mon_giai")
    private String maMonGiai;

    @Column(name = "diem_cong_mon_giai")
    private BigDecimal diemCongMonGiai;

    @Column(name = "co_chung_chi")
    private Boolean coChungChi;

    @Column(name = "manganh")
    private String maNganh;

    @Column(name = "matohop")
    private String maToHop;

    @Column(name = "phuongthuc")
    private String phuongThuc;

    // --- Bổ sung các trường phụ phục vụ tính toán ---
    @Transient // Không lưu xuống DB, chỉ dùng để truyền dữ liệu từ GUI
    private BigDecimal diemThiGoc; 
    @Transient
    private BigDecimal mucUuTienGoc;
    @Transient 
    private BigDecimal mdutGoc; 
    @Transient 
    private BigDecimal diemThgxt;

    public DiemCongXetTuyen() {}

    // Getter và Setter cho tất cả các trường...
    public int getIdDiemCong() { return idDiemCong; }
    public void setIdDiemCong(int idDiemCong) { this.idDiemCong = idDiemCong; }
    public String getTsCccd() { return tsCccd; }
    public void setTsCccd(String tsCccd) { this.tsCccd = tsCccd; }
    public BigDecimal getDiemCC() { return diemCC; }
    public void setDiemCC(BigDecimal diemCC) { this.diemCC = diemCC; }
    public BigDecimal getDiemUtxt() { return diemUtxt; }
    public void setDiemUtxt(BigDecimal diemUtxt) { this.diemUtxt = diemUtxt; }
    public BigDecimal getDiemTong() { return diemTong; }
    public void setDiemTong(BigDecimal diemTong) { this.diemTong = diemTong; }
    public BigDecimal getDiemQuyDoiChungChi() { return diemQuyDoiChungChi; }
    public void setDiemQuyDoiChungChi(BigDecimal diemQuyDoiChungChi) { this.diemQuyDoiChungChi = diemQuyDoiChungChi; }
    public BigDecimal getDiemCongKhongMon() { return diemCongKhongMon; }
    public void setDiemCongKhongMon(BigDecimal diemCongKhongMon) { this.diemCongKhongMon = diemCongKhongMon; }
    public String getDcKeys() { return dcKeys; }
    public void setDcKeys(String dcKeys) { this.dcKeys = dcKeys; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public BigDecimal getDiemThiGoc() { return diemThiGoc; }
    public void setDiemThiGoc(BigDecimal diemThiGoc) { this.diemThiGoc = diemThiGoc; }
    public BigDecimal getMucUuTienGoc() { return mucUuTienGoc; }
    public void setMucUuTienGoc(BigDecimal mucUuTienGoc) { this.mucUuTienGoc = mucUuTienGoc; }
    public String getChungChi() { return chungChi; }
    public void setChungChi(String chungChi) { this.chungChi = chungChi; }
    public String getMucDatDuoc() { return mucDatDuoc; }
    public void setMucDatDuoc(String mucDatDuoc) { this.mucDatDuoc = mucDatDuoc; }
    public String getLoaiGiai() { return loaiGiai; }
    public void setLoaiGiai(String loaiGiai) { this.loaiGiai = loaiGiai; }
    public BigDecimal getMdutGoc() { return mdutGoc; }
    public void setMdutGoc(BigDecimal mdutGoc) { this.mdutGoc = mdutGoc; }
    public BigDecimal getDiemThgxt() { return diemThgxt; }
    public void setDiemThgxt(BigDecimal diemThgxt) { this.diemThgxt = diemThgxt; }
    public String getCapGiai() { return capGiai; }
    public void setCapGiai(String capGiai) { this.capGiai = capGiai; }
    public String getDoiTuongGiai() { return doiTuongGiai; }
    public void setDoiTuongGiai(String doiTuongGiai) { this.doiTuongGiai = doiTuongGiai; }
    public String getMaMonGiai() { return maMonGiai; }
    public void setMaMonGiai(String maMonGiai) { this.maMonGiai = maMonGiai; }
    public BigDecimal getDiemCongMonGiai() { return diemCongMonGiai; }
    public void setDiemCongMonGiai(BigDecimal diemCongMonGiai) { this.diemCongMonGiai = diemCongMonGiai; }
    public Boolean getCoChungChi() { return coChungChi; }
    public void setCoChungChi(Boolean coChungChi) { this.coChungChi = coChungChi; }
    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String maNganh) { this.maNganh = maNganh; }
    public String getMaToHop() { return maToHop; }
    public void setMaToHop(String maToHop) { this.maToHop = maToHop; }
    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }
}