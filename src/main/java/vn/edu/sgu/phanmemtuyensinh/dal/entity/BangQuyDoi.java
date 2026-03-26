package vn.edu.sgu.phanmemtuyensinh.dal.entity;

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

    @Column(name = "d_maquydoi", unique = true, length = 45)
    private String dMaQuyDoi;

    @Column(name = "d_phanvi", length = 45)
    private String dPhanVi;

    public BangQuyDoi() {
    }

    public int getIdQd() { return idQd; }
    public void setIdQd(int idQd) { this.idQd = idQd; }

    public String getDPhuongThuc() { return dPhuongThuc; }
    public void setDPhuongThuc(String dPhuongThuc) { this.dPhuongThuc = dPhuongThuc; }

    public String getDToHop() { return dToHop; }
    public void setDToHop(String dToHop) { this.dToHop = dToHop; }

    public String getDMon() { return dMon; }
    public void setDMon(String dMon) { this.dMon = dMon; }

    public BigDecimal getDDiemA() { return dDiemA; }
    public void setDDiemA(BigDecimal dDiemA) { this.dDiemA = dDiemA; }

    public BigDecimal getDDiemB() { return dDiemB; }
    public void setDDiemB(BigDecimal dDiemB) { this.dDiemB = dDiemB; }

    public BigDecimal getDDiemC() { return dDiemC; }
    public void setDDiemC(BigDecimal dDiemC) { this.dDiemC = dDiemC; }

    public BigDecimal getDDiemD() { return dDiemD; }
    public void setDDiemD(BigDecimal dDiemD) { this.dDiemD = dDiemD; }

    public String getDMaQuyDoi() { return dMaQuyDoi; }
    public void setDMaQuyDoi(String dMaQuyDoi) { this.dMaQuyDoi = dMaQuyDoi; }

    public String getDPhanVi() { return dPhanVi; }
    public void setDPhanVi(String dPhanVi) { this.dPhanVi = dPhanVi; }
}
