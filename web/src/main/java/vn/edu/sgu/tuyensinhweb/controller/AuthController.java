package vn.edu.sgu.tuyensinhweb.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.sgu.tuyensinhweb.model.*;
import vn.edu.sgu.tuyensinhweb.service.*;

import java.util.List;

@Controller
public class AuthController {

    @Autowired private ThiSinhService thiSinhService;
    @Autowired private TraCuuService traCuuService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Xử lý đăng nhập: username = CCCD, password = 8 chữ số ngày sinh
     */
    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes ra) {
        ThiSinh ts = thiSinhService.authenticate(username, password);
        if (ts == null) {
            ra.addFlashAttribute("error",
                "Không tìm thấy thí sinh. Vui lòng kiểm tra lại CCCD và mật khẩu (8 chữ số ngày sinh ddMMyyyy).");
            return "redirect:/login";
        }
        session.setAttribute("thiSinh", ts);
        return "redirect:/tracuu";
    }



    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
