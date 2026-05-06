package vn.edu.sgu.tuyensinhweb.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_bangquydoi")
public class BangQuyDoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idqd")
    private int idQd;

    @Column(name = "d_phuongthuc", length = 45)
    private String dPhuongThuc;

    @Column(name = "d_tohop", length = 45)
    private String dToHop;

    @Column(name = "d_mon", length = 45)
    private String dMon;

    @Column(name = "d_diema")
    private BigDecimal dDiemA;

    @Column(name = "d_diemb")
    private BigDecimal dDiemB;

    @Column(name = "d_diemc")
    private BigDecimal dDiemC;

    @Column(name = "d_diemd")
    private BigDecimal dDiemD;

    @Column(name = "d_maquydoi", unique = true, length = 80)
    private String dMaQuyDoi;

    @Column(name = "d_phanvi", length = 45)
    private String dPhanVi;

    // --- Getters & Setters ---
    public int getIdQd() { return idQd; }
    public void setIdQd(int v) { this.idQd = v; }

    public String getDPhuongThuc() { return dPhuongThuc; }
    public void setDPhuongThuc(String v) { this.dPhuongThuc = v; }

    public String getDToHop() { return dToHop; }
    public void setDToHop(String v) { this.dToHop = v; }

    public String getDMon() { return dMon; }
    public void setDMon(String v) { this.dMon = v; }

    public BigDecimal getDDiemA() { return dDiemA; }
    public void setDDiemA(BigDecimal v) { this.dDiemA = v; }

    public BigDecimal getDDiemB() { return dDiemB; }
    public void setDDiemB(BigDecimal v) { this.dDiemB = v; }

    public BigDecimal getDDiemC() { return dDiemC; }
    public void setDDiemC(BigDecimal v) { this.dDiemC = v; }

    public BigDecimal getDDiemD() { return dDiemD; }
    public void setDDiemD(BigDecimal v) { this.dDiemD = v; }

    public String getDMaQuyDoi() { return dMaQuyDoi; }
    public void setDMaQuyDoi(String v) { this.dMaQuyDoi = v; }

    public String getDPhanVi() { return dPhanVi; }
    public void setDPhanVi(String v) { this.dPhanVi = v; }
}
