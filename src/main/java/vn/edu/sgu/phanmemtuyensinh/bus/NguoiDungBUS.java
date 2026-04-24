package vn.edu.sgu.phanmemtuyensinh.bus;

import java.util.List;

import vn.edu.sgu.phanmemtuyensinh.dal.NguoiDungDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguoiDung;

public class NguoiDungBUS {
    private final NguoiDungDAO dao = new NguoiDungDAO();
    private String lastError = "";

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public List<NguoiDung> getAll() {
        return dao.getAll();
    }

    public NguoiDung getByTaiKhoan(String taiKhoan) {
        return dao.getByTaiKhoan(taiKhoan);
    }

    public boolean add(NguoiDung nd) {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return false;
        }
        if (nd.getTaiKhoan() == null || nd.getTaiKhoan().trim().isEmpty()) {
            lastError = "Tài khoản không được để trống!";
            return false;
        }
        if (nd.getMatKhau() == null || nd.getMatKhau().trim().isEmpty()) {
            lastError = "Mật khẩu không được để trống!";
            return false;
        }
        if (!isValidEmail(nd.getEmail())) {
            lastError = "Email không hợp lệ!";
            return false;
        }
        if (!isValidPhone(nd.getDienThoai())) {
            lastError = "Số điện thoại phải bắt đầu bằng số 0 và tối đa 10 chữ số!";
            return false;
        }
        if (dao.getByTaiKhoan(nd.getTaiKhoan().trim()) != null) {
            lastError = "Tài khoản đã tồn tại, vui lòng chọn tài khoản khác!";
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
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return false;
        }
        if (nd.getTaiKhoan() == null || nd.getTaiKhoan().trim().isEmpty()) {
            lastError = "Tài khoản không được để trống!";
            return false;
        }
        if (!isValidEmail(nd.getEmail())) {
            lastError = "Email không hợp lệ!";
            return false;
        }
        if (!isValidPhone(nd.getDienThoai())) {
            lastError = "Số điện thoại phải bắt đầu bằng số 0 và tối đa 10 chữ số!";
            return false;
        }
        NguoiDung existing = dao.getByTaiKhoan(nd.getTaiKhoan().trim());
        if (existing != null && existing.getIdNguoiDung() != nd.getIdNguoiDung()) {
            lastError = "Tài khoản đã tồn tại, vui lòng chọn tài khoản khác!";
            return false;
        }
        if (nd.getPhanQuyen() == null || nd.getPhanQuyen().trim().isEmpty()) {
            nd.setPhanQuyen("user");
        }
        if (nd.getTrangThaiHoatDong() != 0 && nd.getTrangThaiHoatDong() != 1) {
            nd.setTrangThaiHoatDong(1);
        }
        return dao.update(nd);
    }

    public boolean delete(int idNguoiDung) {
        lastError = "";
        if (!AuthorizationContext.ensureWritePermission()) {
            lastError = AuthorizationContext.WRITE_PERMISSION_DENIED;
            return false;
        }
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

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String normalized = email.trim();
        return normalized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String normalized = phone.trim();
        return normalized.matches("^0\\d{0,9}$");
    }
}
