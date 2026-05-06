package vn.edu.sgu.tuyensinhweb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.sgu.tuyensinhweb.model.*;
import vn.edu.sgu.tuyensinhweb.repository.*;

import java.util.List;

/**
 * Service tra cứu kết quả xét tuyển của thí sinh.
 */
@Service
public class TraCuuService {

    @Autowired private NguyenVongRepository nvRepo;
    @Autowired private DiemThiRepository diemThiRepo;
    @Autowired private DiemCongRepository diemCongRepo;
    @Autowired private NganhRepository nganhRepo;

    /**
     * Lấy danh sách toàn bộ ngành để tra cứu điểm chuẩn/chỉ tiêu.
     */
    public java.util.Map<String, Nganh> getAllNganhMap() {
        return nganhRepo.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Nganh::getMaNganh, n -> n, (existing, replacement) -> existing));
    }

    /**
     * Lấy danh sách nguyện vọng của thí sinh theo CCCD, sắp xếp theo thứ tự NV.
     */
    public List<NguyenVongXetTuyen> getNguyenVongByCccd(String cccd) {
        if (cccd == null || cccd.trim().isEmpty()) return List.of();
        return nvRepo.findByNvCccdOrderByNvTtAsc(cccd.trim());
    }

    /**
     * Lấy danh sách điểm thi của thí sinh (tất cả phương thức).
     */
    public List<DiemThiXetTuyen> getDiemThiByCccd(String cccd) {
        if (cccd == null || cccd.trim().isEmpty()) return List.of();
        return diemThiRepo.findByCccd(cccd.trim());
    }

    /**
     * Lấy điểm cộng (chứng chỉ, giải...) của thí sinh.
     */
    public List<DiemCongXetTuyen> getDiemCongByCccd(String cccd) {
        if (cccd == null || cccd.trim().isEmpty()) return List.of();
        return diemCongRepo.findByTsCccd(cccd.trim());
    }
}
