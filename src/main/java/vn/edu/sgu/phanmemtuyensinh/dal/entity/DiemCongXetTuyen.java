package vn.edu.sgu.phanmemtuyensinh.dal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_diemcongxetuyen")
public class DiemCongXetTuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong")
    private int idDiemCong;

    @Column(name = "ts_cccd", nullable = false, length = 45)
    private String tsCccd;

    @Column(name = "manganh", length = 20)
    private String maNganh;

    @Column(name = "matohop", length = 10)
    private String maToHop;

    @Column(name = "phuongthuc", length = 45)
    private String phuongThuc;

    @Column(name = "diemCC")
    private BigDecimal diemCC;

    @Column(name = "diemUtxt")
    private BigDecimal diemUtxt;

    @Column(name = "diemTong")
    private BigDecimal diemTong;

    @Column(name = "ghichu")
    private String ghiChu;

    @Column(name = "dc_keys", unique = true, nullable = false, length = 45)
    private String dcKeys;

    public DiemCongXetTuyen() {
    }

    public int getIdDiemCong() { return idDiemCong; }
    public void setIdDiemCong(int idDiemCong) { this.idDiemCong = idDiemCong; }

    public String getTsCccd() { return tsCccd; }
    public void setTsCccd(String tsCccd) { this.tsCccd = tsCccd; }

    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String maNganh) { this.maNganh = maNganh; }

    public String getMaToHop() { return maToHop; }
    public void setMaToHop(String maToHop) { this.maToHop = maToHop; }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

    public BigDecimal getDiemCC() { return diemCC; }
    public void setDiemCC(BigDecimal diemCC) { this.diemCC = diemCC; }

    public BigDecimal getDiemUtxt() { return diemUtxt; }
    public void setDiemUtxt(BigDecimal diemUtxt) { this.diemUtxt = diemUtxt; }

    public BigDecimal getDiemTong() { return diemTong; }
    public void setDiemTong(BigDecimal diemTong) { this.diemTong = diemTong; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getDcKeys() { return dcKeys; }
    public void setDcKeys(String dcKeys) { this.dcKeys = dcKeys; }
}
