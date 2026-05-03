package vn.edu.sgu.phanmemtuyensinh.dal.entity;

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

    @Column(name = "N1")
    private Integer n1;

    @Column(name = "TO")
    private Integer to;

    @Column(name = "LI")
    private Integer li;

    @Column(name = "HO")
    private Integer ho;

    @Column(name = "SI")
    private Integer si;

    @Column(name = "VA")
    private Integer va;

    @Column(name = "SU")
    private Integer su;

    @Column(name = "DI")
    private Integer di;

    @Column(name = "TI")
    private Integer ti;

    @Column(name = "GDCD")
    private Integer gdcd;

    @Column(name = "KHAC")
    private Integer khac;

    @Column(name = "KTPL")
    private Integer ktpl;

    @Column(name = "CNCN")
    private Integer cncn;

    @Column(name = "CNNN")
    private Integer cnnn;

    @Column(name = "dolech")
    private BigDecimal doLech;

    public NganhToHop() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public void setMaNganh(String maNganh) {
        this.maNganh = maNganh;
    }

    public String getMaToHop() {
        return maToHop;
    }

    public void setMaToHop(String maToHop) {
        this.maToHop = maToHop;
    }

    public String getTenNganhChuan() {
        return tenNganhChuan;
    }

    public void setTenNganhChuan(String tenNganhChuan) {
        this.tenNganhChuan = tenNganhChuan;
    }

    public String getTenToHop() {
        return tenToHop;
    }

    public void setTenToHop(String tenToHop) {
        this.tenToHop = tenToHop;
    }

    public String getLaToHopGoc() {
        return laToHopGoc;
    }

    public void setLaToHopGoc(String laToHopGoc) {
        this.laToHopGoc = laToHopGoc;
    }

    public String getThMon1() {
        return thMon1;
    }

    public void setThMon1(String thMon1) {
        this.thMon1 = thMon1;
    }

    public Integer getHsMon1() {
        return hsMon1;
    }

    public void setHsMon1(Integer hsMon1) {
        this.hsMon1 = hsMon1;
    }

    public String getThMon2() {
        return thMon2;
    }

    public void setThMon2(String thMon2) {
        this.thMon2 = thMon2;
    }

    public Integer getHsMon2() {
        return hsMon2;
    }

    public void setHsMon2(Integer hsMon2) {
        this.hsMon2 = hsMon2;
    }

    public String getThMon3() {
        return thMon3;
    }

    public void setThMon3(String thMon3) {
        this.thMon3 = thMon3;
    }

    public Integer getHsMon3() {
        return hsMon3;
    }

    public void setHsMon3(Integer hsMon3) {
        this.hsMon3 = hsMon3;
    }

    public String getTbKeys() {
        return tbKeys;
    }

    public void setTbKeys(String tbKeys) {
        this.tbKeys = tbKeys;
    }

    public Integer getN1() {
        return n1;
    }

    public void setN1(Integer n1) {
        this.n1 = n1;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public Integer getLi() {
        return li;
    }

    public void setLi(Integer li) {
        this.li = li;
    }

    public Integer getHo() {
        return ho;
    }

    public void setHo(Integer ho) {
        this.ho = ho;
    }

    public Integer getSi() {
        return si;
    }

    public void setSi(Integer si) {
        this.si = si;
    }

    public Integer getVa() {
        return va;
    }

    public void setVa(Integer va) {
        this.va = va;
    }

    public Integer getSu() {
        return su;
    }

    public void setSu(Integer su) {
        this.su = su;
    }

    public Integer getDi() {
        return di;
    }

    public void setDi(Integer di) {
        this.di = di;
    }

    public Integer getTi() {
        return ti;
    }

    public void setTi(Integer ti) {
        this.ti = ti;
    }

    public Integer getGdcd() {
        return gdcd;
    }

    public void setGdcd(Integer gdcd) {
        this.gdcd = gdcd;
    }

    public Integer getKhac() {
        return khac;
    }

    public void setKhac(Integer khac) {
        this.khac = khac;
    }

    public Integer getKtpl() {
        return ktpl;
    }

    public void setKtpl(Integer ktpl) {
        this.ktpl = ktpl;
    }

    public Integer getCncn() {
        return cncn;
    }

    public void setCncn(Integer cncn) {
        this.cncn = cncn;
    }

    public Integer getCnnn() {
        return cnnn;
    }

    public void setCnnn(Integer cnnn) {
        this.cnnn = cnnn;
    }

    public BigDecimal getDoLech() {
        return doLech;
    }

    public void setDoLech(BigDecimal doLech) {
        this.doLech = doLech;
    }
}
