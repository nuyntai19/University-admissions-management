package vn.edu.sgu.phanmemtuyensinh.bus;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import vn.edu.sgu.phanmemtuyensinh.dal.NguyenVongXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class NguyenVongXetTuyenBUS {
    private NguyenVongXetTuyenDAO dao = new NguyenVongXetTuyenDAO();

    public List<NguyenVongXetTuyen> getAll() {
        return dao.getAll();
    }

    public List<NguyenVongXetTuyen> getByCccd(String cccd) {
        return dao.getByCccd(cccd);
    }

    public boolean add(NguyenVongXetTuyen nv) {
        if (nv.getNvCccd() == null || nv.getNvCccd().trim().isEmpty()) {
            System.out.println("CCCD không được để trống!");
            return false;
        }
        if (nv.getNvMaNganh() == null || nv.getNvMaNganh().trim().isEmpty()) {
            System.out.println("Mã ngành không được để trống!");
            return false;
        }
        return dao.add(nv);
    }

    public boolean update(NguyenVongXetTuyen nv) {
        return dao.update(nv);
    }

    public boolean delete(int idNv) {
        return dao.delete(idNv);
    }
    public int importNguyenVongFromExcel(String filePath) throws IOException {

        List<NguyenVongXetTuyen> result = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(4);
            Map<String, Integer> headerMap = new HashMap<>();

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String key = normalizeHeader(formatter.formatCellValue(headerRow.getCell(i)));
                headerMap.put(key, i);
            }

            int lastRow = sheet.getLastRowNum();
            int success = 0;

            for (int i = 5; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String cccd = getCell(row, formatter, headerMap, new String[]{"cccd"});
                if (cccd.isEmpty()) continue;

                NguyenVongXetTuyen nv = new NguyenVongXetTuyen();

                nv.setNvCccd(cccd);
                nv.setNvSoBaoDanh(getCell(row, formatter, headerMap, new String[]{"so bao danh", "sbodanh"}));
                nv.setNvTt(parseInt(getCell(row, formatter, headerMap, new String[]{"thu tu nv", "thutunv", "thtnv"})));
                nv.setNvMaNganh(getCell(row, formatter, headerMap, new String[]{"ma xet tuyen", "maxettuyen", "mxttuyn"}));
                nv.setNvTenMaNganh(getCell(row, formatter, headerMap, new String[]{"ten ma xet tuyen", "tenmaxettuyen", "tnmxttuyn"}));

                // bắt buộc
                if (nv.getNvMaNganh().isEmpty()) continue;

                nv.setNvKeys(generateNvKey(nv));

                // 👉 SAVE DB tại đây
                if (dao.add(nv)) {
                    success++;
                }
            }

            return success;
        }
    }
    
    private String getCell(Row row, DataFormatter f, Map<String, Integer> map, String[] keys) {
        for (String key : keys) {
            Integer idx = map.get(normalizeHeader(key));
            if (idx != null) {
                return f.formatCellValue(row.getCell(idx)).trim();
            }
        }
        return "";
    }

    private String normalizeHeader(String input) {
        if (input == null) return "";
        return input.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private BigDecimal parseBigDecimal(String s) {
        try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; }
    }
    private String generateNvKey(NguyenVongXetTuyen nv) {
        return nv.getNvCccd() + "_" + nv.getNvMaNganh() + "_" + nv.getNvTt(); 
    }
}
