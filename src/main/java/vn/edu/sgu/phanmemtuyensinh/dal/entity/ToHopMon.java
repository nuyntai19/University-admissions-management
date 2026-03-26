package vn.edu.sgu.phanmemtuyensinh.dal.entity;

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

    @Column(name = "tentohop", length = 100)
    private String tenToHop;

    // Hàm khởi tạo không tham số (Bắt buộc cho Hibernate)
    public ToHopMon() {
    }

    // --- GETTER VÀ SETTER ---
    // (Trong NetBeans, bạn có thể nhấn Alt + Insert -> Getter and Setter -> Chọn tất cả -> Generate để tự sinh ra phần này cực nhanh)

    public int getIdToHop() { return idToHop; }
    public void setIdToHop(int idToHop) { this.idToHop = idToHop; }

    public String getMaToHop() { return maToHop; }
    public void setMaToHop(String maToHop) { this.maToHop = maToHop; }

    public String getMon1() { return mon1; }
    public void setMon1(String mon1) { this.mon1 = mon1; }

    public String getMon2() { return mon2; }
    public void setMon2(String mon2) { this.mon2 = mon2; }

    public String getMon3() { return mon3; }
    public void setMon3(String mon3) { this.mon3 = mon3; }

    public String getTenToHop() { return tenToHop; }
    public void setTenToHop(String tenToHop) { this.tenToHop = tenToHop; }
}