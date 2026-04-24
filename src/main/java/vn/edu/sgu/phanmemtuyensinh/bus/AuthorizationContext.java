package vn.edu.sgu.phanmemtuyensinh.bus;

import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;

import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguoiDung;

public final class AuthorizationContext {
    public static final String WRITE_PERMISSION_DENIED = "Bạn không có quyền thực hiện thao tác ghi dữ liệu";

    // Ứng dụng desktop 1 phiên đăng nhập, cần context dùng chung cho cả SwingWorker/background thread.
    private static final AtomicReference<NguoiDung> CURRENT_USER = new AtomicReference<>();

    private AuthorizationContext() {
    }

    public static void setCurrentUser(NguoiDung user) {
        if (user == null) {
            CURRENT_USER.set(null);
            return;
        }
        CURRENT_USER.set(user);
    }

    public static void clear() {
        CURRENT_USER.set(null);
    }

    public static NguoiDung getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static boolean isAdmin() {
        NguoiDung user = CURRENT_USER.get();
        return user != null && "admin".equalsIgnoreCase(user.getPhanQuyen());
    }

    public static boolean ensureWritePermission() {
        return ensureWritePermission(null);
    }

    public static boolean ensureWritePermission(Consumer<String> errorSink) {
        if (isAdmin()) {
            return true;
        }

        if (errorSink != null) {
            errorSink.accept(WRITE_PERMISSION_DENIED);
        }
        return false;
    }
}
