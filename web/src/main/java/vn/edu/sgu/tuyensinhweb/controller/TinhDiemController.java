package vn.edu.sgu.tuyensinhweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.tuyensinhweb.dto.ToHopResult;
import vn.edu.sgu.tuyensinhweb.model.*;
import vn.edu.sgu.tuyensinhweb.repository.*;
import vn.edu.sgu.tuyensinhweb.service.TinhDiemService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Controller cho công cụ tính điểm xét tuyển (KHÔNG cần đăng nhập)
 */
@Controller
@RequestMapping("/tinhdiem")
public class TinhDiemController {

    @Autowired private NganhRepository nganhRepo;
    @Autowired private NganhToHopRepository nganhToHopRepo;
    @Autowired private TinhDiemService tinhDiemSvc;

    private static BigDecimal bd(String s) { return new BigDecimal(s); }

    // ===== TRANG CHỌN PHƯƠNG THỨC =====
    @GetMapping
    public String index() {
        return "tinhdiem";
    }

    // ===== PHƯƠNG THỨC ĐGNL =====
    @GetMapping("/dgnl")
    public String formDGNL(Model model) {
        model.addAttribute("nganhList", nganhRepo.findAllByOrderByMaNganhAsc());
        return "tinhdiem-dgnl";
    }

    @PostMapping("/dgnl")
    public String tinhDGNL(@RequestParam String maNganh,
                           @RequestParam BigDecimal diemThi,
                           @RequestParam(defaultValue = "0") BigDecimal diemCong,
                           @RequestParam(defaultValue = "") String khuVuc,
                           @RequestParam(defaultValue = "") String doiTuong,
                           Model model) {
        Nganh nganh = nganhRepo.findByMaNganh(maNganh).orElse(null);
        if (nganh == null) { model.addAttribute("error", "Không tìm thấy ngành!"); return formDGNL(model); }

        // Cap điểm cộng <= 3.0
        if (diemCong.compareTo(bd("3.0")) > 0) diemCong = bd("3.0");

        String toHopGoc = nganh.getToHopGoc() != null ? nganh.getToHopGoc() : "D01";

        // Quy đổi ĐGNL → thang 30
        BangQuyDoi interval = tinhDiemSvc.findInterval("ĐGNL", toHopGoc, diemThi);
        BigDecimal diemQuyDoi = tinhDiemSvc.quyDoiDGNL(toHopGoc, diemThi);

        // Công thức hiển thị
        String congThuc = "";
        if (interval != null) {
            congThuc = String.format("%s + ( %s - %s ) / ( %s - %s ) * ( %s - %s )",
                interval.getDDiemC().toPlainString(), diemThi.toPlainString(),
                interval.getDDiemA().toPlainString(), interval.getDDiemB().toPlainString(),
                interval.getDDiemA().toPlainString(), interval.getDDiemD().toPlainString(),
                interval.getDDiemC().toPlainString());
        }

        // ĐUT
        BigDecimal mDut = tinhDiemSvc.tinhMDUT(khuVuc, doiTuong);
        BigDecimal dut = tinhDiemSvc.tinhDUT(diemQuyDoi, diemCong, mDut);

        // ĐXT
        BigDecimal dxt = diemQuyDoi.add(diemCong).add(dut);
        if (dxt.compareTo(bd("30")) > 0) dxt = bd("30");

        // Kết quả
        String ketQua = "Chưa có ngưỡng";
        if (nganh.getDiemSan() != null) {
            ketQua = dxt.compareTo(nganh.getDiemSan()) >= 0 ? "Đạt" : "Không đạt";
        }

        model.addAttribute("nganhList", nganhRepo.findAllByOrderByMaNganhAsc());
        model.addAttribute("nganh", nganh);
        model.addAttribute("diemThi", diemThi);
        model.addAttribute("diemCong", diemCong);
        model.addAttribute("khuVuc", khuVuc);
        model.addAttribute("doiTuong", doiTuong);
        model.addAttribute("toHopGoc", toHopGoc);
        model.addAttribute("congThuc", congThuc);
        model.addAttribute("diemQuyDoi", diemQuyDoi);
        model.addAttribute("mDut", mDut);
        model.addAttribute("dut", dut);
        model.addAttribute("dxt", dxt);
        model.addAttribute("nguong", nganh.getDiemSan());
        model.addAttribute("ketQua", ketQua);
        model.addAttribute("hasResult", true);

        return "tinhdiem-dgnl";
    }

    // ===== PHƯƠNG THỨC V-SAT =====
    @GetMapping("/vsat")
    public String formVSAT(Model model) {
        model.addAttribute("nganhList", nganhRepo.findAllByOrderByMaNganhAsc());
        model.addAttribute("monMap", TinhDiemService.getMonNameMap());
        return "tinhdiem-vsat";
    }

    @PostMapping("/vsat")
    public String tinhVSAT(@RequestParam String maNganh,
                           @RequestParam(defaultValue = "0") BigDecimal diemTO,
                           @RequestParam(defaultValue = "0") BigDecimal diemLI,
                           @RequestParam(defaultValue = "0") BigDecimal diemHO,
                           @RequestParam(defaultValue = "0") BigDecimal diemSI,
                           @RequestParam(defaultValue = "0") BigDecimal diemSU,
                           @RequestParam(defaultValue = "0") BigDecimal diemDI,
                           @RequestParam(defaultValue = "0") BigDecimal diemVA,
                           @RequestParam(defaultValue = "0") BigDecimal diemN1,
                           @RequestParam(defaultValue = "0") BigDecimal diemCong,
                           @RequestParam(defaultValue = "") String khuVuc,
                           @RequestParam(defaultValue = "") String doiTuong,
                           Model model) {
        Nganh nganh = nganhRepo.findByMaNganh(maNganh).orElse(null);
        if (nganh == null) { model.addAttribute("error", "Không tìm thấy ngành!"); return formVSAT(model); }

        if (diemCong.compareTo(bd("3.0")) > 0) diemCong = bd("3.0");

        // Build map điểm gốc
        Map<String, BigDecimal> diemMap = new LinkedHashMap<>();
        diemMap.put("TO", diemTO); diemMap.put("LI", diemLI); diemMap.put("HO", diemHO);
        diemMap.put("SI", diemSI); diemMap.put("SU", diemSU); diemMap.put("DI", diemDI);
        diemMap.put("VA", diemVA); diemMap.put("N1", diemN1);

        BigDecimal mDut = tinhDiemSvc.tinhMDUT(khuVuc, doiTuong);
        List<NganhToHop> toHops = nganhToHopRepo.findByMaNganh(maNganh);
        String toHopGoc = nganh.getToHopGoc() != null ? nganh.getToHopGoc() : "";

        List<ToHopResult> results = new ArrayList<>();
        BigDecimal maxDxt = bd("-1");
        int bestIdx = -1;

        for (int i = 0; i < toHops.size(); i++) {
            NganhToHop nth = toHops.get(i);
            ToHopResult tr = new ToHopResult();
            tr.setMaToHop(nth.getMaToHop());
            tr.setTenToHop(nth.getTenToHop() != null ? nth.getTenToHop() : nth.getMaToHop());
            tr.setToHopGoc(toHopGoc);

            String m1 = nth.getThMon1(), m2 = nth.getThMon2(), m3 = nth.getThMon3();
            int w1 = nth.getHsMon1() != null ? nth.getHsMon1() : 1;
            int w2 = nth.getHsMon2() != null ? nth.getHsMon2() : 1;
            int w3 = nth.getHsMon3() != null ? nth.getHsMon3() : 1;

            // Quy đổi từng môn V-SAT → thang 10
            BigDecimal d1 = processMonVSAT(m1, diemMap.getOrDefault(mapMonCode(m1), BigDecimal.ZERO), tr, w1);
            BigDecimal d2 = processMonVSAT(m2, diemMap.getOrDefault(mapMonCode(m2), BigDecimal.ZERO), tr, w2);
            BigDecimal d3 = processMonVSAT(m3, diemMap.getOrDefault(mapMonCode(m3), BigDecimal.ZERO), tr, w3);

            // ĐTHXT = [(d1*w1 + d2*w2 + d3*w3) / W] * 3
            BigDecimal dthxt = tinhDiemSvc.tinhDTHXT(d1, d2, d3, w1, w2, w3);
            tr.setDthxt(dthxt);

            // Xét nguong = tổng 3 môn + ưu tiên
            BigDecimal tongMon = d1.multiply(bd(String.valueOf(w1)))
                    .add(d2.multiply(bd(String.valueOf(w2))))
                    .add(d3.multiply(bd(String.valueOf(w3))));
            tr.setDiemXetNguong(tongMon);

            // Độ lệch
            BigDecimal doLech = nth.getDoLech() != null ? nth.getDoLech() : tinhDiemSvc.getDoLech(toHopGoc, nth.getMaToHop());
            tr.setDoLech(doLech);

            // ĐTHGXT = ĐTHXT - độ lệch
            BigDecimal dthgxt = dthxt.subtract(doLech);
            tr.setDthgxt(dthgxt);

            tr.setDiemCong(diemCong);

            // ĐUT
            BigDecimal dut = tinhDiemSvc.tinhDUT(dthgxt, diemCong, mDut);
            tr.setDut(dut);

            // ĐXT
            BigDecimal dxt = dthgxt.add(diemCong).add(dut).setScale(2, RoundingMode.HALF_UP);
            if (dxt.compareTo(bd("30")) > 0) dxt = bd("30");
            tr.setDiemXetTuyen(dxt);

            // Ngưỡng
            tr.setNguongDauVao(nganh.getDiemSan());
            if (nganh.getDiemSan() != null) {
                tr.setKetQua(dxt.compareTo(nganh.getDiemSan()) >= 0 ? "Đạt" : "Không đạt");
            } else {
                tr.setKetQua("Chưa có ngưỡng");
            }

            results.add(tr);
            if (dxt.compareTo(maxDxt) > 0) { maxDxt = dxt; bestIdx = i; }
        }

        if (bestIdx >= 0) results.get(bestIdx).setBest(true);

        model.addAttribute("nganhList", nganhRepo.findAllByOrderByMaNganhAsc());
        model.addAttribute("monMap", TinhDiemService.getMonNameMap());
        model.addAttribute("nganh", nganh);
        model.addAttribute("diemMap", diemMap);
        model.addAttribute("diemCong", diemCong);
        model.addAttribute("khuVuc", khuVuc);
        model.addAttribute("doiTuong", doiTuong);
        model.addAttribute("mDut", mDut);
        model.addAttribute("results", results);
        model.addAttribute("toHopGoc", toHopGoc);
        model.addAttribute("hasResult", true);

        return "tinhdiem-vsat";
    }

    /** Quy đổi 1 môn V-SAT, thêm detail vào ToHopResult */
    private BigDecimal processMonVSAT(String monCode, BigDecimal diemGoc, ToHopResult tr, int heSo) {
        ToHopResult.MonDetail md = new ToHopResult.MonDetail();
        String mc = mapMonCode(monCode);
        md.setMonCode(mc);
        md.setTenMon(tinhDiemSvc.getTenMon(mc));
        md.setDiemGoc(diemGoc);
        md.setHeSo(heSo);

        if (diemGoc.compareTo(BigDecimal.ZERO) == 0) {
            md.setDiemQuyDoi(BigDecimal.ZERO);
            md.setLoi("Điểm nhập vào không nằm trong phân vị nào = 0");
        } else {
            BangQuyDoi interval = tinhDiemSvc.findInterval("V-SAT", mc, diemGoc);
            if (interval != null) {
                BigDecimal qd = tinhDiemSvc.noiSuy(diemGoc, interval.getDDiemA(), interval.getDDiemB(), interval.getDDiemC(), interval.getDDiemD());
                md.setDiemQuyDoi(qd);
                md.setCongThuc(String.format("%s + ( %s - %s ) / ( %s - %s ) * ( %s - %s ) = %s",
                    interval.getDDiemC().toPlainString(), diemGoc.toPlainString(),
                    interval.getDDiemA().toPlainString(), interval.getDDiemB().toPlainString(),
                    interval.getDDiemA().toPlainString(), interval.getDDiemD().toPlainString(),
                    interval.getDDiemC().toPlainString(), qd.toPlainString()));
            } else {
                md.setDiemQuyDoi(BigDecimal.ZERO);
                md.setLoi("Điểm nhập vào không nằm trong phân vị nào = 0");
            }
        }
        tr.getMonDetails().add(md);
        return md.getDiemQuyDoi();
    }

    // ===== PHƯƠNG THỨC THPT =====
    @GetMapping("/thpt")
    public String formTHPT(Model model) {
        model.addAttribute("nganhList", nganhRepo.findAllByOrderByMaNganhAsc());
        model.addAttribute("monMap", TinhDiemService.getMonNameMap());
        return "tinhdiem-thpt";
    }

    @PostMapping("/thpt")
    public String tinhTHPT(@RequestParam String maNganh,
                           @RequestParam(defaultValue = "0") BigDecimal diemTO,
                           @RequestParam(defaultValue = "0") BigDecimal diemLI,
                           @RequestParam(defaultValue = "0") BigDecimal diemHO,
                           @RequestParam(defaultValue = "0") BigDecimal diemSI,
                           @RequestParam(defaultValue = "0") BigDecimal diemSU,
                           @RequestParam(defaultValue = "0") BigDecimal diemDI,
                           @RequestParam(defaultValue = "0") BigDecimal diemVA,
                           @RequestParam(defaultValue = "0") BigDecimal diemN1,
                           @RequestParam(defaultValue = "0") BigDecimal diemCong,
                           @RequestParam(defaultValue = "") String khuVuc,
                           @RequestParam(defaultValue = "") String doiTuong,
                           Model model) {
        Nganh nganh = nganhRepo.findByMaNganh(maNganh).orElse(null);
        if (nganh == null) { model.addAttribute("error", "Không tìm thấy ngành!"); return formTHPT(model); }

        if (diemCong.compareTo(bd("3.0")) > 0) diemCong = bd("3.0");

        // Build map điểm gốc
        Map<String, BigDecimal> diemMap = new LinkedHashMap<>();
        diemMap.put("TO", diemTO); diemMap.put("LI", diemLI); diemMap.put("HO", diemHO);
        diemMap.put("SI", diemSI); diemMap.put("SU", diemSU); diemMap.put("DI", diemDI);
        diemMap.put("VA", diemVA); diemMap.put("N1", diemN1);

        BigDecimal mDut = tinhDiemSvc.tinhMDUT(khuVuc, doiTuong);
        List<NganhToHop> toHops = nganhToHopRepo.findByMaNganh(maNganh);
        String toHopGoc = nganh.getToHopGoc() != null ? nganh.getToHopGoc() : "";

        List<ToHopResult> results = new ArrayList<>();
        BigDecimal maxDxt = bd("-1");
        int bestIdx = -1;

        for (int i = 0; i < toHops.size(); i++) {
            NganhToHop nth = toHops.get(i);
            ToHopResult tr = new ToHopResult();
            tr.setMaToHop(nth.getMaToHop());
            tr.setTenToHop(nth.getTenToHop() != null ? nth.getTenToHop() : nth.getMaToHop());
            tr.setToHopGoc(toHopGoc);

            String m1 = nth.getThMon1(), m2 = nth.getThMon2(), m3 = nth.getThMon3();
            int w1 = nth.getHsMon1() != null ? nth.getHsMon1() : 1;
            int w2 = nth.getHsMon2() != null ? nth.getHsMon2() : 1;
            int w3 = nth.getHsMon3() != null ? nth.getHsMon3() : 1;

            // Lấy điểm trực tiếp
            BigDecimal d1 = processMonTHPT(m1, diemMap.getOrDefault(mapMonCode(m1), BigDecimal.ZERO), tr, w1);
            BigDecimal d2 = processMonTHPT(m2, diemMap.getOrDefault(mapMonCode(m2), BigDecimal.ZERO), tr, w2);
            BigDecimal d3 = processMonTHPT(m3, diemMap.getOrDefault(mapMonCode(m3), BigDecimal.ZERO), tr, w3);

            // ĐTHXT
            BigDecimal dthxt = tinhDiemSvc.tinhDTHXT(d1, d2, d3, w1, w2, w3);
            tr.setDthxt(dthxt);

            // Xét nguong
            BigDecimal tongMon = d1.multiply(bd(String.valueOf(w1)))
                    .add(d2.multiply(bd(String.valueOf(w2))))
                    .add(d3.multiply(bd(String.valueOf(w3))));
            tr.setDiemXetNguong(tongMon);

            // Độ lệch
            BigDecimal doLech = nth.getDoLech() != null ? nth.getDoLech() : tinhDiemSvc.getDoLech(toHopGoc, nth.getMaToHop());
            tr.setDoLech(doLech);

            // ĐTHGXT
            BigDecimal dthgxt = dthxt.subtract(doLech);
            tr.setDthgxt(dthgxt);

            tr.setDiemCong(diemCong);

            // ĐUT
            BigDecimal dut = tinhDiemSvc.tinhDUT(dthgxt, diemCong, mDut);
            tr.setDut(dut);

            // ĐXT
            BigDecimal dxt = dthgxt.add(diemCong).add(dut).setScale(2, RoundingMode.HALF_UP);
            if (dxt.compareTo(bd("30")) > 0) dxt = bd("30");
            tr.setDiemXetTuyen(dxt);

            // Ngưỡng
            tr.setNguongDauVao(nganh.getDiemSan());
            if (nganh.getDiemSan() != null) {
                tr.setKetQua(dxt.compareTo(nganh.getDiemSan()) >= 0 ? "Đạt" : "Không đạt");
            } else {
                tr.setKetQua("Chưa có ngưỡng");
            }

            results.add(tr);
            if (dxt.compareTo(maxDxt) > 0) { maxDxt = dxt; bestIdx = i; }
        }

        if (bestIdx >= 0) results.get(bestIdx).setBest(true);

        model.addAttribute("nganhList", nganhRepo.findAllByOrderByMaNganhAsc());
        model.addAttribute("monMap", TinhDiemService.getMonNameMap());
        model.addAttribute("nganh", nganh);
        model.addAttribute("diemMap", diemMap);
        model.addAttribute("diemCong", diemCong);
        model.addAttribute("khuVuc", khuVuc);
        model.addAttribute("doiTuong", doiTuong);
        model.addAttribute("mDut", mDut);
        model.addAttribute("results", results);
        model.addAttribute("toHopGoc", toHopGoc);
        model.addAttribute("hasResult", true);

        return "tinhdiem-thpt";
    }

    private BigDecimal processMonTHPT(String monCode, BigDecimal diemGoc, ToHopResult tr, int heSo) {
        ToHopResult.MonDetail md = new ToHopResult.MonDetail();
        String mc = mapMonCode(monCode);
        md.setMonCode(mc);
        md.setTenMon(tinhDiemSvc.getTenMon(mc));
        md.setDiemGoc(diemGoc);
        md.setHeSo(heSo);
        md.setDiemQuyDoi(diemGoc);
        md.setCongThuc(diemGoc.toPlainString());
        tr.getMonDetails().add(md);
        return md.getDiemQuyDoi();
    }

    /** Map mã môn từ tổ hợp sang mã trong bảng quy đổi */
    private String mapMonCode(String code) {
        if (code == null) return "";
        String c = code.toUpperCase().trim();
        if (c.equals("N1_THI") || c.equals("N1")) return "N1";
        return c;
    }
}
