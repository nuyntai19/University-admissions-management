package vn.edu.sgu.phanmemtuyensinh.dal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import vn.edu.sgu.phanmemtuyensinh.bus.BangQuyDoiBUS;

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

    @Column(name = "TO")
    private BigDecimal to;

    @Column(name = "LI")
    private BigDecimal li;

    @Column(name = "HO")
    private BigDecimal ho;

    @Column(name = "SI")
    private BigDecimal si;

    @Column(name = "SU")
    private BigDecimal su;

    @Column(name = "DI")
    private BigDecimal di;

    @Column(name = "VA")
    private BigDecimal va;

    @Column(name = "GDCD")
    private BigDecimal gdcd;

    @Column(name = "N1_THI")
    private BigDecimal n1Thi;

    @Column(name = "N1_CC")
    private BigDecimal n1Cc;

    @Column(name = "CNCN")
    private BigDecimal cncn;

    @Column(name = "CNNN")
    private BigDecimal cnnn;

    @Column(name = "TI")
    private BigDecimal ti;

    @Column(name = "KTPL")
    private BigDecimal ktpl;

    @Column(name = "NL1")
    private BigDecimal nl1;

    @Column(name = "NK1")
    private BigDecimal nk1;

    @Column(name = "NK2")
    private BigDecimal nk2;

    @Column(name = "NK3")
    private BigDecimal nk3;

    @Column(name = "NK4")
    private BigDecimal nk4;

    @Column(name = "NK5")
    private BigDecimal nk5;

    @Column(name = "NK6")
    private BigDecimal nk6;

    @Column(name = "NK7")
    private BigDecimal nk7;

    @Column(name = "NK8")
    private BigDecimal nk8;

    @Column(name = "NK9")
    private BigDecimal nk9;

    @Column(name = "NK10")
    private BigDecimal nk10;

    @Column(name = "diem_xet_tot_nghiep")
    private BigDecimal diemXetTotNghiep;

    @Column(name = "vsat_to")
    private BigDecimal vsatTo;

    @Column(name = "vsat_va")
    private BigDecimal vsatVa;

    @Column(name = "vsat_anh")
    private BigDecimal vsatAnh;

    @Column(name = "vsat_li")
    private BigDecimal vsatLi;

    @Column(name = "vsat_ho")
    private BigDecimal vsatHo;

    @Column(name = "vsat_si")
    private BigDecimal vsatSi;

    @Column(name = "vsat_su")
    private BigDecimal vsatSu;

    @Column(name = "vsat_di")
    private BigDecimal vsatDi;


    public DiemThiXetTuyen() {
    }

    public int getIdDiemThi() { return idDiemThi; }
    public void setIdDiemThi(int idDiemThi) { this.idDiemThi = idDiemThi; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getSoBaoDanh() { return soBaoDanh; }
    public void setSoBaoDanh(String soBaoDanh) { this.soBaoDanh = soBaoDanh; }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

    public BigDecimal getTo() { return to; }
    public void setTo(BigDecimal to) { this.to = to; }

    public BigDecimal getLi() { return li; }
    public void setLi(BigDecimal li) { this.li = li; }

    public BigDecimal getHo() { return ho; }
    public void setHo(BigDecimal ho) { this.ho = ho; }

    public BigDecimal getSi() { return si; }
    public void setSi(BigDecimal si) { this.si = si; }

    public BigDecimal getSu() { return su; }
    public void setSu(BigDecimal su) { this.su = su; }

    public BigDecimal getDi() { return di; }
    public void setDi(BigDecimal di) { this.di = di; }

    public BigDecimal getVa() { return va; }
    public void setVa(BigDecimal va) { this.va = va; }

    public BigDecimal getGdcd() { return gdcd; }
    public void setGdcd(BigDecimal gdcd) { this.gdcd = gdcd; }

    public BigDecimal getN1Thi() { return n1Thi; }
    public void setN1Thi(BigDecimal n1Thi) { this.n1Thi = n1Thi; }

    public BigDecimal getN1Cc() { return n1Cc; }
    public void setN1Cc(BigDecimal n1Cc) { this.n1Cc = n1Cc; }

    public BigDecimal getCncn() { return cncn; }
    public void setCncn(BigDecimal cncn) { this.cncn = cncn; }

    public BigDecimal getCnnn() { return cnnn; }
    public void setCnnn(BigDecimal cnnn) { this.cnnn = cnnn; }

    public BigDecimal getTi() { return ti; }
    public void setTi(BigDecimal ti) { this.ti = ti; }

    public BigDecimal getKtpl() { return ktpl; }
    public void setKtpl(BigDecimal ktpl) { this.ktpl = ktpl; }

    public BigDecimal getNl1() { return nl1; }
    public void setNl1(BigDecimal nl1) { this.nl1 = nl1; }

    public BigDecimal getNl1Thang30() {
        if (nl1 == null) return null;
        try {
            BangQuyDoiBUS bqdBus = new BangQuyDoiBUS();
            // Use CHUNG fallback; a more specific toHop can be used when available
            BigDecimal converted = bqdBus.quyDoiNoiSuy("ĐGNL", "CHUNG", null, nl1);
            if (converted != null) return converted.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        } catch (Exception ignored) {}

        // Fallback legacy heuristic
        BigDecimal maxScale30 = new BigDecimal("30");
        if (nl1.compareTo(maxScale30) <= 0) return nl1;
        return nl1.divide(new BigDecimal("40"), 2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    public BigDecimal getNk1() { return nk1; }
    public void setNk1(BigDecimal nk1) { this.nk1 = nk1; }

    public BigDecimal getNk3() { return nk3; }
    public void setNk3(BigDecimal nk3) { this.nk3 = nk3; }

    public BigDecimal getNk4() { return nk4; }
    public void setNk4(BigDecimal nk4) { this.nk4 = nk4; }

    public BigDecimal getNk5() { return nk5; }
    public void setNk5(BigDecimal nk5) { this.nk5 = nk5; }

    public BigDecimal getNk6() { return nk6; }
    public void setNk6(BigDecimal nk6) { this.nk6 = nk6; }

    public BigDecimal getNk7() { return nk7; }
    public void setNk7(BigDecimal nk7) { this.nk7 = nk7; }

    public BigDecimal getNk8() { return nk8; }
    public void setNk8(BigDecimal nk8) { this.nk8 = nk8; }

    public BigDecimal getNk9() { return nk9; }
    public void setNk9(BigDecimal nk9) { this.nk9 = nk9; }

    public BigDecimal getNk10() { return nk10; }
    public void setNk10(BigDecimal nk10) { this.nk10 = nk10; }

    public BigDecimal getDiemXetTotNghiep() { return diemXetTotNghiep; }
    public void setDiemXetTotNghiep(BigDecimal diemXetTotNghiep) { this.diemXetTotNghiep = diemXetTotNghiep; }

    public BigDecimal getNk2() { return nk2; }
    public void setNk2(BigDecimal nk2) { this.nk2 = nk2; }

    public BigDecimal getVsatTo() { return vsatTo; }
    public void setVsatTo(BigDecimal vsatTo) { this.vsatTo = vsatTo; }

    public BigDecimal getVsatVa() { return vsatVa; }
    public void setVsatVa(BigDecimal vsatVa) { this.vsatVa = vsatVa; }

    public BigDecimal getVsatAnh() { return vsatAnh; }
    public void setVsatAnh(BigDecimal vsatAnh) { this.vsatAnh = vsatAnh; }

    public BigDecimal getVsatLi() { return vsatLi; }
    public void setVsatLi(BigDecimal vsatLi) { this.vsatLi = vsatLi; }

    public BigDecimal getVsatHo() { return vsatHo; }
    public void setVsatHo(BigDecimal vsatHo) { this.vsatHo = vsatHo; }

    public BigDecimal getVsatSi() { return vsatSi; }
    public void setVsatSi(BigDecimal vsatSi) { this.vsatSi = vsatSi; }

    public BigDecimal getVsatSu() { return vsatSu; }
    public void setVsatSu(BigDecimal vsatSu) { this.vsatSu = vsatSu; }

    public BigDecimal getVsatDi() { return vsatDi; }
    public void setVsatDi(BigDecimal vsatDi) { this.vsatDi = vsatDi; }
}
