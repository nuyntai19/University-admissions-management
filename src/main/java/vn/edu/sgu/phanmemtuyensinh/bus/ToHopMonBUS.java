package vn.edu.sgu.phanmemtuyensinh.bus;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.edu.sgu.phanmemtuyensinh.dal.ToHopMonDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ToHopMon;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ToHopMonBUS {
    private ToHopMonDAO dao;

    public ToHopMonBUS() {
        dao = new ToHopMonDAO();
    }

    // Lấy danh sách để hiển thị lên bảng (Table) ở giao diện
    public List<ToHopMon> getAll() {
        return dao.getAll();
    }

    public ToHopMon getByMaToHop(String maToHop) {
        if (maToHop == null || maToHop.trim().isEmpty()) {
            return null;
        }
        return dao.getByMaToHop(maToHop.trim());
    }

    // Logic xử lý trước khi thêm mới
    public boolean add(ToHopMon toHop) {
        // Tầng BUS sẽ kiểm tra dữ liệu hợp lệ trước khi gọi DAO
        if (toHop.getMaToHop() == null || toHop.getMaToHop().trim().isEmpty()) {
            System.out.println("Lỗi: Mã tổ hợp không được để trống!");
            return false;
        }
        return dao.add(toHop);
    }

    // Logic xử lý trước khi cập nhật
    public boolean update(ToHopMon toHop) {
        return dao.update(toHop);
    }

    // Logic xử lý trước khi xóa
    public boolean delete(int idToHop) {
        return dao.delete(idToHop);
    }

    public List<ToHopMon> importDanhSachToHop(String filePath) throws IOException {
        List<ToHopMon> result = new ArrayList<>();
        Set<String> existedMaToHop = new HashSet<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
                Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String maToHop = formatter.formatCellValue(row.getCell(5)).trim();
                String maToHopChiTiet = formatter.formatCellValue(row.getCell(3)).trim();

                if (maToHop.isEmpty() || existedMaToHop.contains(maToHop)) {
                    continue;
                }

                String[] mons = parseMonTuMaToHop(maToHopChiTiet);
                if (mons.length < 3) {
                    continue;
                }

                ToHopMon toHopMon = new ToHopMon();
                toHopMon.setMaToHop(maToHop);
                toHopMon.setMon1(mons[0]);
                toHopMon.setMon2(mons[1]);
                toHopMon.setMon3(mons[2]);
                toHopMon.setTenToHop("");

                result.add(toHopMon);
                existedMaToHop.add(maToHop);
            }
        }

        return result;
    }

    private String[] parseMonTuMaToHop(String maToHopChiTiet) {
        int start = maToHopChiTiet.indexOf('(');
        int end = maToHopChiTiet.indexOf(')');
        if (start < 0 || end <= start) {
            return new String[0];
        }

        String inside = maToHopChiTiet.substring(start + 1, end);
        String[] phanTu = inside.split(",");
        if (phanTu.length < 3) {
            return new String[0];
        }

        String[] mons = new String[3];
        for (int i = 0; i < 3; i++) {
            String item = phanTu[i].trim();
            String[] monVaHeSo = item.split("-");
            if (monVaHeSo.length == 0 || monVaHeSo[0].trim().isEmpty()) {
                return new String[0];
            }
            mons[i] = monVaHeSo[0].trim();
        }
        return mons;
    }

    public int importAndSaveToDatabase(String filePath) throws IOException {
        List<ToHopMon> toHopList = importDanhSachToHop(filePath);
        int countSuccess = 0;
        int countSkipped = 0;

        for (ToHopMon toHop : toHopList) {
            if (dao.add(toHop)) {
                countSuccess++;
            } else {
                countSkipped++;
            }
        }

        System.out.println(
                "Import kết thúc: " + countSuccess + " thành công, " + countSkipped + " bị bỏ qua (trùng hoặc lỗi)");
        return countSuccess;
    }
}