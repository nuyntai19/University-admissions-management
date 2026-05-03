package vn.edu.sgu.phanmemtuyensinh.dal.entity;

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

    public NguyenVongXetTuyen() {
    }

    // getters/setters

    public int getIdNv() { return idNv; }
    public void setIdNv(int idNv) { this.idNv = idNv; }

    public String getNvCccd() { return nvCccd; }
    public void setNvCccd(String nvCccd) { this.nvCccd = nvCccd; }
    
    public int getNvTt() { return nvTt; }
    public void setNvTt(int nvTt) { this.nvTt = nvTt; }

    public String getNvMaNganh() { return nvMaNganh; }
    public void setNvMaNganh(String nvMaNganh) { this.nvMaNganh = nvMaNganh; }

    public String getNvTenMaNganh() { return nvTenMaNganh; }
    public void setNvTenMaNganh(String nvTenMaNganh) { this.nvTenMaNganh = nvTenMaNganh; }

    public String getNvTuyenThang() { return nvTuyenThang; }
    public void setNvTuyenThang(String nvTuyenThang) { this.nvTuyenThang = nvTuyenThang; }

    public BigDecimal getDiemThxt() { return diemThxt; }
    public void setDiemThxt(BigDecimal diemThxt) { this.diemThxt = diemThxt; }

    public BigDecimal getDiemUtqd() { return diemUtqd; }
    public void setDiemUtqd(BigDecimal diemUtqd) { this.diemUtqd = diemUtqd; }

    public BigDecimal getDiemCong() { return diemCong; }
    public void setDiemCong(BigDecimal diemCong) { this.diemCong = diemCong; }

    public BigDecimal getDiemXetTuyen() { return diemXetTuyen; }
    public void setDiemXetTuyen(BigDecimal diemXetTuyen) { this.diemXetTuyen = diemXetTuyen; }

    public String getNvKetQua() { return nvKetQua; }
    public void setNvKetQua(String nvKetQua) { this.nvKetQua = nvKetQua; }

    public String getNvKeys() { return nvKeys; }
    public void setNvKeys(String nvKeys) { this.nvKeys = nvKeys; }

    public String getTtPhuongThuc() { return ttPhuongThuc; }
    public void setTtPhuongThuc(String ttPhuongThuc) { this.ttPhuongThuc = ttPhuongThuc; }

    public String getTtThm() { return ttThm; }
    public void setTtThm(String ttThm) { this.ttThm = ttThm; }
}