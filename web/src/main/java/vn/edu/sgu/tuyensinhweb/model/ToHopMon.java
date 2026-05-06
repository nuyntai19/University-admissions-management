package vn.edu.sgu.tuyensinhweb.model;

import jakarta.persistence.*;

@Entity
@Table(name = "xt_tohop_monthi")
public class ToHopMon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtohop")
    private int idToHop;

    @Column(name = "matohop", unique = true, nullable = false, length = 45)
    private String maToHop;

    @Column(name = "mon1", nullable = false, length = 10)
    private String mon1;

    @Column(name = "mon2", nullable = false, length = 10)
    private String mon2;

    @Column(name = "mon3", nullable = false, length = 10)
    private String mon3;

    @Column(name = "tentohop", length = 150)
    private String tenToHop;

    // --- Getters & Setters ---
    public int getIdToHop() { return idToHop; }
    public void setIdToHop(int v) { this.idToHop = v; }

    public String getMaToHop() { return maToHop; }
    public void setMaToHop(String v) { this.maToHop = v; }

    public String getMon1() { return mon1; }
    public void setMon1(String v) { this.mon1 = v; }

    public String getMon2() { return mon2; }
    public void setMon2(String v) { this.mon2 = v; }

    public String getMon3() { return mon3; }
    public void setMon3(String v) { this.mon3 = v; }

    public String getTenToHop() { return tenToHop; }
    public void setTenToHop(String v) { this.tenToHop = v; }
}
