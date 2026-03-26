package vn.edu.sgu.phanmemtuyensinh.bus;

import java.util.List;

import vn.edu.sgu.phanmemtuyensinh.dal.NguoiDungDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguoiDung;

public class NguoiDungBUS {
    private final NguoiDungDAO dao = new NguoiDungDAO();

    public List<NguoiDung> getAll() {
        return dao.getAll();
    }

    public NguoiDung getByTaiKhoan(String taiKhoan) {
        return dao.getByTaiKhoan(taiKhoan);
    }

    public boolean add(NguoiDung nd) {
        if (nd.getTaiKhoan() == null || nd.getTaiKhoan().trim().isEmpty()) {
            System.out.println("Tài khoản không được để trống!");
            return false;
        }
        if (nd.getMatKhau() == null || nd.getMatKhau().trim().isEmpty()) {
            System.out.println("Mật khẩu không được để trống!");
            return false;
        }
        if (nd.getPhanQuyen() == null || nd.getPhanQuyen().trim().isEmpty()) {
            nd.setPhanQuyen("user");
        }
        if (nd.getTrangThaiHoatDong() != 0 && nd.getTrangThaiHoatDong() != 1) {
            nd.setTrangThaiHoatDong(1);
        }
        return dao.add(nd);
    }

    public boolean update(NguoiDung nd) {
        if (nd.getPhanQuyen() == null || nd.getPhanQuyen().trim().isEmpty()) {
            nd.setPhanQuyen("user");
        }
        if (nd.getTrangThaiHoatDong() != 0 && nd.getTrangThaiHoatDong() != 1) {
            nd.setTrangThaiHoatDong(1);
        }
        return dao.update(nd);
    }

    public boolean delete(int idNguoiDung) {
        return dao.delete(idNguoiDung);
    }

    public boolean authenticate(String taiKhoan, String matKhau) {
        return authenticateAndGetUser(taiKhoan, matKhau) != null;
    }

    public NguoiDung authenticateAndGetUser(String taiKhoan, String matKhau) {
        NguoiDung nd = dao.getByTaiKhoan(taiKhoan);
        if (nd == null) {
            return null;
        }
        if (nd.getTrangThaiHoatDong() != 1) {
            return null;
        }
        if (!nd.getMatKhau().equals(matKhau)) {
            return null;
        }
        return nd;
    }
}
