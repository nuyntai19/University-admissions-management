package vn.edu.sgu.tuyensinhweb.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.sgu.tuyensinhweb.model.*;
import vn.edu.sgu.tuyensinhweb.service.TraCuuService;

import java.util.List;

@Controller
public class TraCuuController {

    @Autowired
    private TraCuuService traCuuService;

    /**
     * Trang tra cứu kết quả xét tuyển (cần đăng nhập)
     */
    @GetMapping("/tracuu")
    public String traCuu(HttpSession session, Model model) {
        ThiSinh ts = (ThiSinh) session.getAttribute("thiSinh");
        if (ts == null) return "redirect:/login";

        List<NguyenVongXetTuyen> nvList = traCuuService.getNguyenVongByCccd(ts.getCccd());
        List<DiemThiXetTuyen> diemList = traCuuService.getDiemThiByCccd(ts.getCccd());
        List<DiemCongXetTuyen> diemCongList = traCuuService.getDiemCongByCccd(ts.getCccd());
        java.util.Map<String, Nganh> nganhMap = traCuuService.getAllNganhMap();

        model.addAttribute("thiSinh", ts);
        model.addAttribute("nvList", nvList);
        model.addAttribute("diemList", diemList);
        model.addAttribute("diemCongList", diemCongList);
        model.addAttribute("nganhMap", nganhMap);
        return "tracuu";
    }
}
