package vn.edu.sgu.tuyensinhweb.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_diemthixettuyen", uniqueConstraints = @UniqueConstraint(columnNames = {"cccd", "d_phuongthuc"}))
public class DiemThiXetTuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi")
    private int idDiemThi;

    @Column(name = "cccd", nullable = false, length = 20)
    private String cccd;

    @Column(name = "sobaodanh", length = 45)
    private String soBaoDanh;

    @Column(name = "d_phuongthuc", length = 10)
    private String phuongThuc;

    @Column(name = "TO") private BigDecimal to;
    @Column(name = "LI") private BigDecimal li;
    @Column(name = "HO") private BigDecimal ho;
    @Column(name = "SI") private BigDecimal si;
    @Column(name = "SU") private BigDecimal su;
    @Column(name = "DI") private BigDecimal di;
    @Column(name = "VA") private BigDecimal va;
    @Column(name = "GDCD") private BigDecimal gdcd;
    @Column(name = "N1_THI") private BigDecimal n1Thi;
    @Column(name = "N1_CC") private BigDecimal n1Cc;
    @Column(name = "CNCN") private BigDecimal cncn;
    @Column(name = "CNNN") private BigDecimal cnnn;
    @Column(name = "TI") private BigDecimal ti;
    @Column(name = "KTPL") private BigDecimal ktpl;
    @Column(name = "NL1") private BigDecimal nl1;
    @Column(name = "diem_xet_tot_nghiep") private BigDecimal diemXetTotNghiep;

    public DiemThiXetTuyen() {}

    // --- Getters & Setters ---
    public int getIdDiemThi() { return idDiemThi; }
    public void setIdDiemThi(int v) { this.idDiemThi = v; }
    public String getCccd() { return cccd; }
    public void setCccd(String v) { this.cccd = v; }
    public String getSoBaoDanh() { return soBaoDanh; }
    public void setSoBaoDanh(String v) { this.soBaoDanh = v; }
    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String v) { this.phuongThuc = v; }
    public BigDecimal getTo() { return to; }
    public void setTo(BigDecimal v) { this.to = v; }
    public BigDecimal getLi() { return li; }
    public void setLi(BigDecimal v) { this.li = v; }
    public BigDecimal getHo() { return ho; }
    public void setHo(BigDecimal v) { this.ho = v; }
    public BigDecimal getSi() { return si; }
    public void setSi(BigDecimal v) { this.si = v; }
    public BigDecimal getSu() { return su; }
    public void setSu(BigDecimal v) { this.su = v; }
    public BigDecimal getDi() { return di; }
    public void setDi(BigDecimal v) { this.di = v; }
    public BigDecimal getVa() { return va; }
    public void setVa(BigDecimal v) { this.va = v; }
    public BigDecimal getGdcd() { return gdcd; }
    public void setGdcd(BigDecimal v) { this.gdcd = v; }
    public BigDecimal getN1Thi() { return n1Thi; }
    public void setN1Thi(BigDecimal v) { this.n1Thi = v; }
    public BigDecimal getN1Cc() { return n1Cc; }
    public void setN1Cc(BigDecimal v) { this.n1Cc = v; }
    public BigDecimal getCncn() { return cncn; }
    public void setCncn(BigDecimal v) { this.cncn = v; }
    public BigDecimal getCnnn() { return cnnn; }
    public void setCnnn(BigDecimal v) { this.cnnn = v; }
    public BigDecimal getTi() { return ti; }
    public void setTi(BigDecimal v) { this.ti = v; }
    public BigDecimal getKtpl() { return ktpl; }
    public void setKtpl(BigDecimal v) { this.ktpl = v; }
    public BigDecimal getNl1() { return nl1; }
    public void setNl1(BigDecimal v) { this.nl1 = v; }
    public BigDecimal getDiemXetTotNghiep() { return diemXetTotNghiep; }
    public void setDiemXetTotNghiep(BigDecimal v) { this.diemXetTotNghiep = v; }
}
