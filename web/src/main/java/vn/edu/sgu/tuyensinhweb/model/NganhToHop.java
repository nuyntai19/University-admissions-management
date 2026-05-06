package vn.edu.sgu.tuyensinhweb.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_nganh_tohop")
public class NganhToHop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "manganh", nullable = false, length = 45)
    private String maNganh;

    @Column(name = "matohop", nullable = false, length = 45)
    private String maToHop;

    @Column(name = "ten_nganh_chuan", length = 255)
    private String tenNganhChuan;

    @Column(name = "ten_to_hop", length = 150)
    private String tenToHop;

    @Column(name = "la_tohop_goc", length = 20)
    private String laToHopGoc;

    @Column(name = "th_mon1", length = 10)
    private String thMon1;

    @Column(name = "hsmon1")
    private Integer hsMon1;

    @Column(name = "th_mon2", length = 10)
    private String thMon2;

    @Column(name = "hsmon2")
    private Integer hsMon2;

    @Column(name = "th_mon3", length = 10)
    private String thMon3;

    @Column(name = "hsmon3")
    private Integer hsMon3;

    @Column(name = "tb_keys", unique = true, length = 80)
    private String tbKeys;

    @Column(name = "dolech")
    private BigDecimal doLech;

    // --- Getters & Setters ---
    public int getId() { return id; }
    public void setId(int v) { this.id = v; }

    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String v) { this.maNganh = v; }

    public String getMaToHop() { return maToHop; }
    public void setMaToHop(String v) { this.maToHop = v; }

    public String getTenNganhChuan() { return tenNganhChuan; }
    public void setTenNganhChuan(String v) { this.tenNganhChuan = v; }

    public String getTenToHop() { return tenToHop; }
    public void setTenToHop(String v) { this.tenToHop = v; }

    public String getLaToHopGoc() { return laToHopGoc; }
    public void setLaToHopGoc(String v) { this.laToHopGoc = v; }

    public String getThMon1() { return thMon1; }
    public void setThMon1(String v) { this.thMon1 = v; }

    public Integer getHsMon1() { return hsMon1; }
    public void setHsMon1(Integer v) { this.hsMon1 = v; }

    public String getThMon2() { return thMon2; }
    public void setThMon2(String v) { this.thMon2 = v; }

    public Integer getHsMon2() { return hsMon2; }
    public void setHsMon2(Integer v) { this.hsMon2 = v; }

    public String getThMon3() { return thMon3; }
    public void setThMon3(String v) { this.thMon3 = v; }

    public Integer getHsMon3() { return hsMon3; }
    public void setHsMon3(Integer v) { this.hsMon3 = v; }

    public String getTbKeys() { return tbKeys; }
    public void setTbKeys(String v) { this.tbKeys = v; }

    public BigDecimal getDoLech() { return doLech; }
    public void setDoLech(BigDecimal v) { this.doLech = v; }
}
