package vn.edu.sgu.tuyensinhweb.model;

import jakarta.persistence.*;

@Entity
@Table(name = "xt_thisinhxettuyen25")
public class ThiSinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idthisinh")
    private int idThiSinh;

    @Column(name = "cccd", unique = true, length = 20)
    private String cccd;

    @Column(name = "sobaodanh", length = 45)
    private String soBaoDanh;

    @Column(name = "ho", length = 100)
    private String ho;

    @Column(name = "ten", length = 100)
    private String ten;

    @Column(name = "ngay_sinh", length = 45)
    private String ngaySinh;

    @Column(name = "dien_thoai", length = 20)
    private String dienThoai;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "noi_sinh", length = 100)
    private String noiSinh;

    @Column(name = "doi_tuong", length = 45)
    private String doiTuong;

    @Column(name = "khu_vuc", length = 45)
    private String khuVuc;

    @Column(name = "dan_toc", length = 100)
    private String danToc;

    @Column(name = "ma_mon_nn", length = 20)
    private String maMonNn;

    // --- Getters & Setters ---
    public int getIdThiSinh() { return idThiSinh; }
    public void setIdThiSinh(int v) { this.idThiSinh = v; }

    public String getCccd() { return cccd; }
    public void setCccd(String v) { this.cccd = v; }

    public String getSoBaoDanh() { return soBaoDanh; }
    public void setSoBaoDanh(String v) { this.soBaoDanh = v; }

    public String getHo() { return ho; }
    public void setHo(String v) { this.ho = v; }

    public String getTen() { return ten; }
    public void setTen(String v) { this.ten = v; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String v) { this.ngaySinh = v; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String v) { this.dienThoai = v; }

    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String v) { this.gioiTinh = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }

    public String getNoiSinh() { return noiSinh; }
    public void setNoiSinh(String v) { this.noiSinh = v; }

    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String v) { this.doiTuong = v; }

    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String v) { this.khuVuc = v; }

    public String getDanToc() { return danToc; }
    public void setDanToc(String v) { this.danToc = v; }

    public String getMaMonNn() { return maMonNn; }
    public void setMaMonNn(String v) { this.maMonNn = v; }

    public String getHoTen() {
        return (ho != null ? ho : "") + " " + (ten != null ? ten : "");
    }
}
