package vn.edu.sgu.phanmemtuyensinh.bus;

import java.util.List;

import vn.edu.sgu.phanmemtuyensinh.dal.NguoiDungDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguoiDung;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NguoiDungBUS {
    private final NguoiDungDAO dao = new NguoiDungDAO();
    // helper for student account auto-creation
    public boolean createStudentAccountFromThiSinh(ThiSinh ts) {
        lastError = "";
        if (ts == null || ts.getCccd() == null || ts.getCccd().trim().isEmpty()) {
            lastError = "Thiếu CCCD của thí sinh";
            return false;
        }

        String taiKhoan = ts.getCccd().trim();
        if (dao.getByTaiKhoan(taiKhoan) != null) {
            lastError = "Tài khoản đã tồn tại";
            return false; // already exists
        }

        NguoiDung nd = new NguoiDung();
        nd.setTaiKhoan(taiKhoan);
        String derived = derivePasswordFromThiSinh(ts);
        nd.setMatKhau(derived == null ? (ts.getPassword() == null ? "" : ts.getPassword()) : derived);
        String hoTen = (ts.getHo() == null ? "" : ts.getHo()).trim();
        if (hoTen.isEmpty()) {
            hoTen = (ts.getHo() == null ? "" : ts.getHo());
        }
        if ((nd.getHoTen() == null || nd.getHoTen().isEmpty()) && (ts.getHo() != null || ts.getTen() != null)) {
            nd.setHoTen(((ts.getHo() == null ? "" : ts.getHo()) + " " + (ts.getTen() == null ? "" : ts.getTen())).trim());
        }
        nd.setEmail(ts.getEmail());
        nd.setDienThoai(ts.getDienThoai());
        nd.setPhanQuyen("student");
        nd.setTrangThaiHoatDong(1);
        nd.setNgayTao(java.time.LocalDateTime.now());

        boolean ok = dao.addDirect(nd);
        if (!ok && lastError.isBlank()) {
            lastError = "Không thể tạo tài khoản cho thí sinh " + taiKhoan;
        }
        return ok;
    }

    private String derivePasswordFromThiSinh(ThiSinh ts) {
        if (ts == null) return null;
        String ngaySinh = ts.getNgaySinh();
        if (ngaySinh == null || ngaySinh.trim().isEmpty()) {
            return null;
        }
        String s = ngaySinh.trim();
        String[] patterns = {"dd/MM/yyyy", "yyyy-MM-dd", "dd/MM/yy"};
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p);
                sdf.setLenient(false);
                ParsePosition pos = new ParsePosition(0);
                Date d = sdf.parse(s, pos);
                if (d != null && pos.getIndex() == s.length()) {
                    SimpleDateFormat out = new SimpleDateFormat("ddMMyyyy");
                    return out.format(d);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
    private String lastError = "";

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public List<NguoiDung> getAll() {
        return dao.getAll();
    }

    public List<NguoiDung> getPage(int page, int pageSize) {
        return dao.getPage(page, pageSize);
    }

    public long countAll() {
        return dao.countAll();
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
        if (nd.getEmail() != null && !nd.getEmail().trim().isEmpty() && !isValidEmail(nd.getEmail())) {
            lastError = "Email không hợp lệ!";
            return false;
        }
        if (nd.getDienThoai() != null && !nd.getDienThoai().trim().isEmpty() && !isValidPhone(nd.getDienThoai())) {
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
        if (nd.getEmail() != null && !nd.getEmail().trim().isEmpty() && !isValidEmail(nd.getEmail())) {
            lastError = "Email không hợp lệ!";
            return false;
        }
        if (nd.getDienThoai() != null && !nd.getDienThoai().trim().isEmpty() && !isValidPhone(nd.getDienThoai())) {
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
