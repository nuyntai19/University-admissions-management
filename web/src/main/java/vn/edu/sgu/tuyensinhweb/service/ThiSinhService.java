package vn.edu.sgu.tuyensinhweb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.sgu.tuyensinhweb.model.ThiSinh;
import vn.edu.sgu.tuyensinhweb.repository.ThiSinhRepository;

import java.util.Optional;

/**
 * Service xác thực thí sinh: kiểm tra CCCD + password (ngày sinh ddMMyyyy).
 */
@Service
public class ThiSinhService {

    @Autowired
    private ThiSinhRepository thiSinhRepo;

    /**
     * Xác thực thí sinh bằng CCCD và mật khẩu (8 chữ số ngày sinh).
     * @return ThiSinh nếu đăng nhập thành công, null nếu thất bại.
     */
    public ThiSinh authenticate(String cccd, String password) {
        if (cccd == null || cccd.trim().isEmpty()) return null;
        if (password == null || password.trim().isEmpty()) return null;

        Optional<ThiSinh> opt = thiSinhRepo.findByCccdAndPassword(cccd.trim(), password.trim());
        return opt.orElse(null);
    }

    /**
     * Tìm thí sinh theo CCCD.
     */
    public ThiSinh findByCccd(String cccd) {
        return thiSinhRepo.findByCccd(cccd.trim()).orElse(null);
    }
}
