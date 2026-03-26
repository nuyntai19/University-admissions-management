package vn.edu.sgu.phanmemtuyensinh.dal.entity;

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

    @Column(name = "tennganh", nullable = false, length = 100)
    private String tenNganh;

    @Column(name = "n_tohopgoc", length = 3)
    private String toHopGoc;

    @Column(name = "n_chitieu", nullable = false)
    private int chiTieu;

    @Column(name = "n_diemsan")
    private BigDecimal diemSan;

    @Column(name = "n_diemtrungtuyen")
    private BigDecimal diemTrungTuyen;

    @Column(name = "n_tuyenthang", length = 1)
    private String tuyenThang;

    @Column(name = "n_dgnl", length = 1)
    private String dgnl;

    @Column(name = "n_thpt", length = 1)
    private String thpt;

    @Column(name = "n_vsat", length = 1)
    private String vsat;

    @Column(name = "sl_xtt")
    private int slXtt;

    @Column(name = "sl_dgnl")
    private int slDgnl;

    @Column(name = "sl_vsat")
    private int slVsat;

    @Column(name = "sl_thpt", length = 45)
    private String slThpt;

    public Nganh() {
    }

    public int getIdNganh() { return idNganh; }
    public void setIdNganh(int idNganh) { this.idNganh = idNganh; }

    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String maNganh) { this.maNganh = maNganh; }

    public String getTenNganh() { return tenNganh; }
    public void setTenNganh(String tenNganh) { this.tenNganh = tenNganh; }

    public String getToHopGoc() { return toHopGoc; }
    public void setToHopGoc(String toHopGoc) { this.toHopGoc = toHopGoc; }

    public int getChiTieu() { return chiTieu; }
    public void setChiTieu(int chiTieu) { this.chiTieu = chiTieu; }

    public BigDecimal getDiemSan() { return diemSan; }
    public void setDiemSan(BigDecimal diemSan) { this.diemSan = diemSan; }

    public BigDecimal getDiemTrungTuyen() { return diemTrungTuyen; }
    public void setDiemTrungTuyen(BigDecimal diemTrungTuyen) { this.diemTrungTuyen = diemTrungTuyen; }

    public String getTuyenThang() { return tuyenThang; }
    public void setTuyenThang(String tuyenThang) { this.tuyenThang = tuyenThang; }

    public String getDgnl() { return dgnl; }
    public void setDgnl(String dgnl) { this.dgnl = dgnl; }

    public String getThpt() { return thpt; }
    public void setThpt(String thpt) { this.thpt = thpt; }

    public String getVsat() { return vsat; }
    public void setVsat(String vsat) { this.vsat = vsat; }

    public int getSlXtt() { return slXtt; }
    public void setSlXtt(int slXtt) { this.slXtt = slXtt; }

    public int getSlDgnl() { return slDgnl; }
    public void setSlDgnl(int slDgnl) { this.slDgnl = slDgnl; }

    public int getSlVsat() { return slVsat; }
    public void setSlVsat(int slVsat) { this.slVsat = slVsat; }

    public String getSlThpt() { return slThpt; }
    public void setSlThpt(String slThpt) { this.slThpt = slThpt; }
}
