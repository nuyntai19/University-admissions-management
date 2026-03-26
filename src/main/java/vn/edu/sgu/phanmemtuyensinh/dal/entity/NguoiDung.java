package vn.edu.sgu.phanmemtuyensinh.dal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_nguoidung")
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnguoidung")
    private int idNguoiDung;

    @Column(name = "taikhoan", unique = true, nullable = false, length = 45)
    private String taiKhoan;

    @Column(name = "matkhau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "hoten", length = 100)
    private String hoTen;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "dienthoai", length = 20)
    private String dienThoai;

    @Column(name = "phanquyen", nullable = false, length = 45)
    private String phanQuyen; // "admin" hoặc "user"

    @Column(name = "trangthaihoatdong")
    private int trangThaiHoatDong; // 1: active, 0: inactive

    @Column(name = "ngaytao")
    private LocalDateTime ngayTao;

    @Column(name = "ngaysua")
    private LocalDateTime ngaySua;

    public NguoiDung() {
    }

    public int getIdNguoiDung() { return idNguoiDung; }
    public void setIdNguoiDung(int idNguoiDung) { this.idNguoiDung = idNguoiDung; }

    public String getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(String taiKhoan) { this.taiKhoan = taiKhoan; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getPhanQuyen() { return phanQuyen; }
    public void setPhanQuyen(String phanQuyen) { this.phanQuyen = phanQuyen; }

    public int getTrangThaiHoatDong() { return trangThaiHoatDong; }
    public void setTrangThaiHoatDong(int trangThaiHoatDong) { this.trangThaiHoatDong = trangThaiHoatDong; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public LocalDateTime getNgaySua() { return ngaySua; }
    public void setNgaySua(LocalDateTime ngaySua) { this.ngaySua = ngaySua; }
}
