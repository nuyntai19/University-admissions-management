package vn.edu.sgu.phanmemtuyensinh.bus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import vn.edu.sgu.phanmemtuyensinh.dal.DiemThiXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.NguoiDungDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguoiDung;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;

public class ThiSinhBUS {

    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern SO_BAO_DANH_PATTERN = Pattern.compile("^TS_\\d{1,20}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern DATE_TOKEN_PATTERN = Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{4}$|^\\d{4}-\\d{1,2}-\\d{1,2}$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    private static final Map<String, String> PROVINCE_CODE_MAP = createProvinceCodeMap();
    private static boolean poiMaxOverrideConfigured = false;

    private final ThiSinhDAO dao = new ThiSinhDAO();
    private final vn.edu.sgu.phanmemtuyensinh.bus.NguoiDungBUS nguoiDungBUS = new vn.edu.sgu.phanmemtuyensinh.bus.NguoiDungBUS();
    private final NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();
    private String lastError = "";
    private String lastImportSummary = "";
    private final List<String> lastImportRowErrors = new ArrayList<>();
    private final DiemThiXetTuyenDAO diemDAO = new DiemThiXetTuyenDAO();
    private int lastImportSourceRows = 0;

    @FunctionalInterface
    public interface ImportProgressListener {
        void onProgress(int percent, String message);
    }

    public List<ThiSinh> getAll() {
        return dao.getAll();
    }

    public ThiSinh getByCccd(String cccd) {
        return dao.getByCccd(cccd);
    }

    public ThiSinh getById(int idThiSinh) {
        return dao.getById(idThiSinh);
    }

    public List<ThiSinh> getPage(int page, int pageSize) {
        return dao.getPage(page, pageSize);
    }

    public long countAll() {
        return dao.countAll();
    }

    public List<ThiSinh> searchByHoTen(String hoTen) {
        return dao.searchByHoTen(hoTen);
    }

    public List<ThiSinh> searchByKeyword(String keyword, int page, int pageSize) {
        return dao.searchByKeyword(keyword, page, pageSize);
    }

    public long countByKeyword(String keyword) {
        return dao.countByKeyword(keyword);
    }

    public boolean add(ThiSinh ts) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        if (!validateThiSinh(ts, true)) {
            return false;
        }
        boolean ok = dao.add(ts);
        if (ok) {
            createStudentAccountIfMissing(ts);
        }
        return ok;
    }

    public boolean update(ThiSinh ts) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        if (!validateThiSinh(ts, false)) {
            return false;
        }
        boolean ok = dao.update(ts);
        if (ok) {
            createStudentAccountIfMissing(ts);
        }
        return ok;
    }

    public boolean delete(int idThiSinh) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        return dao.delete(idThiSinh);
    }

    public String getLastError() {
        return lastError == null ? "" : lastError;
    }

    public int importAndSaveToDatabase(String filePath) throws IOException {
        return importAndSaveToDatabase(filePath, null);
    }

    public int importAndSaveToDatabase(String filePath, ImportProgressListener progressListener) throws IOException {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            lastImportSummary = lastError;
            reportProgress(progressListener, 100, lastError);
            return 0;
        }

        lastImportRowErrors.clear();
        lastImportSourceRows = 0;
        reportProgress(progressListener, 1, "Đang đọc file import...");
        List<ThiSinh> list;
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".xlsx")) {
            list = importFromExcel(filePath);
        } else {
            list = importFromTextLike(filePath);
        }

        int sourceRows = Math.max(lastImportSourceRows, list.size());
        reportProgress(progressListener, 45,
            "Đã đọc " + sourceRows + " dòng nguồn, hợp lệ " + list.size() + " dòng. Đang lưu vào DB...");

        if (list.isEmpty()) {
            reportProgress(progressListener, 100, "Không có dữ liệu hợp lệ để import.");
        }

        int imported = 0;
        int updated = 0;
        int failed = 0;
        List<String> sampleErrors = new ArrayList<>();
        int total = list.size();
        for (int i = 0; i < total; i++) {
            ThiSinh ts = list.get(i);
            ThiSinh existing = dao.getByCccd(ts.getCccd());
            boolean ok;

            if (existing != null) {
                mergeImportedThiSinh(existing, ts);
                ok = update(existing);
                if (ok) {
                    updated++;
                }
            } else {
                ok = add(ts);
            }

            if (ok) {
                imported++;
            } else {
                failed++;
                if (sampleErrors.size() < 8) {
                    sampleErrors.add("CCCD " + ts.getCccd() + ": " + getLastError());
                }
            }

            if (total > 0) {
                int percent = Math.max(45, 45 + (int) (((i + 1) * 55.0) / total));
                reportProgress(progressListener, percent,
                    "Đang lưu vào DB " + (i + 1) + "/" + total
                        + " hồ sơ hợp lệ (từ " + sourceRows + " dòng nguồn)...");
            }
        }

            lastImportSummary = "Tổng dòng nguồn: " + sourceRows
                + " | Dòng hợp lệ: " + list.size()
                + " | Thành công: " + imported
                + " | Cập nhật: " + updated
                + " | Thất bại: " + failed;
        if (!lastImportRowErrors.isEmpty()) {
            int shown = Math.min(8, lastImportRowErrors.size());
            lastImportSummary += "\nDòng lỗi khi đọc/validate: " + lastImportRowErrors.size()
                + " (hiển thị " + shown + ")"
                + "\n- " + String.join("\n- ", lastImportRowErrors.subList(0, shown));
        }
        if (!sampleErrors.isEmpty()) {
            lastImportSummary += "\nMột số lỗi:\n- " + String.join("\n- ", sampleErrors);
        }
        reportProgress(progressListener, 100, "Import hoàn tất.");
        return imported;
    }

    public String getLastImportSummary() {
        return lastImportSummary == null ? "" : lastImportSummary;
    }

    /**
     * Quét toàn bộ danh sách thí sinh và tạo tài khoản cho những thí sinh chưa có.
     * Trả về số tài khoản mới được tạo.
     */
    public int createAccountsForAll(ImportProgressListener progressListener) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            reportProgress(progressListener, 100, lastError);
            return 0;
        }
        List<ThiSinh> list = dao.getAll();
        int total = list.size();
        int created = 0;
        for (int i = 0; i < total; i++) {
            ThiSinh ts = list.get(i);
            try {
                boolean ok = createStudentAccountIfMissing(ts);
                if (ok) created++;
            } catch (Exception ignored) {
            }
            int percent = total == 0 ? 100 : Math.min(100, 1 + (int) (((i + 1) * 99.0) / total));
            reportProgress(progressListener, percent, "Đã quét " + (i + 1) + "/" + total);
        }
        reportProgress(progressListener, 100, "Hoàn tất tạo tài khoản. Tạo mới: " + created);
        return created;
    }

    private boolean createStudentAccountIfMissing(ThiSinh ts) {
        if (ts == null || safe(ts.getCccd()).isEmpty()) {
            return false;
        }

        String taiKhoan = ts.getCccd().trim();
        if (nguoiDungDAO.getByTaiKhoan(taiKhoan) != null) {
            return false;
        }

        NguoiDung nd = new NguoiDung();
        nd.setTaiKhoan(taiKhoan);
        nd.setMatKhau(deriveStudentPassword(ts));
        nd.setHoTen(buildStudentFullName(ts));
        nd.setEmail(safe(ts.getEmail()));
        nd.setDienThoai(safe(ts.getDienThoai()));
        nd.setPhanQuyen("student");
        nd.setTrangThaiHoatDong(1);
        nd.setNgayTao(java.time.LocalDateTime.now());
        return nguoiDungDAO.addDirect(nd);
    }

    private String deriveStudentPassword(ThiSinh ts) {
        String ngaySinh = safe(ts == null ? null : ts.getNgaySinh());
        if (ngaySinh.isEmpty()) {
            return safe(ts == null ? null : ts.getPassword());
        }

        String[] patterns = {"dd/MM/yyyy", "dd/MM/yy", "yyyy-MM-dd"};
        for (String pattern : patterns) {
            try {
                java.text.SimpleDateFormat input = new java.text.SimpleDateFormat(pattern);
                input.setLenient(false);
                java.text.ParsePosition pos = new java.text.ParsePosition(0);
                java.util.Date date = input.parse(ngaySinh, pos);
                if (date != null && pos.getIndex() == ngaySinh.length()) {
                    return new java.text.SimpleDateFormat("ddMMyyyy").format(date);
                }
            } catch (Exception ignored) {
            }
        }
        return safe(ts == null ? null : ts.getPassword());
    }

    private String buildStudentFullName(ThiSinh ts) {
        String ho = safe(ts == null ? null : ts.getHo());
        String ten = safe(ts == null ? null : ts.getTen());
        if (ho.isEmpty() && ten.isEmpty()) {
            return safe(ts == null ? null : ts.getCccd());
        }
        return (ho + " " + ten).trim();
    }

    public int chuanHoaCccdTrongXlsx(String inputPath, String outputPath) throws IOException {
        ensurePoiLargeFileSupport();
        if (inputPath == null || !inputPath.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new IOException("Chỉ hỗ trợ file .xlsx");
        }

        String out = outputPath;
        if (out == null || out.isBlank()) {
            throw new IOException("Đường dẫn file xuất không hợp lệ");
        }
        if (!out.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            out += ".xlsx";
        }

        final DataFormatter formatter = new DataFormatter();
        final int[] processed = {0};
        final int[] cccdChanged = {0};

        try (OPCPackage pkg = OPCPackage.open(inputPath);
             SXSSFWorkbook outputWb = new SXSSFWorkbook(200)) {
            outputWb.setCompressTempFiles(true);

            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(pkg);
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();

            if (!sheets.hasNext()) {
                throw new IOException("Không đọc được sheet dữ liệu");
            }

            try (InputStream sheetInput = sheets.next()) {
                String sheetName = sheets.getSheetName();
                Sheet outputSheet = outputWb.createSheet(sheetName == null ? "Sheet1" : sheetName);

                List<String> headers = new ArrayList<>();
                Map<String, Integer> prefixCounters = new HashMap<>();
                Map<Integer, String> currentRow = new HashMap<>();

                int[] headerWidth = new int[] {0};
                int[] cccdIdx = new int[] {-1};
                int[] gioiTinhIdx = new int[] {-1};
                int[] ngaySinhIdx = new int[] {-1};
                int[] noiSinhIdx = new int[] {-1};

                XSSFSheetXMLHandler.SheetContentsHandler sheetHandler = new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private int rowNum;

                    @Override
                    public void startRow(int rowNum) {
                        this.rowNum = rowNum;
                        currentRow.clear();
                    }

                    @Override
                    public void endRow(int rowNum) {
                        Row outRow = outputSheet.createRow(this.rowNum);

                        if (this.rowNum == 0) {
                            int maxCol = currentRow.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
                            for (int c = 0; c <= maxCol; c++) {
                                String h = safe(currentRow.get(c));
                                headers.add(h);
                                outRow.createCell(c).setCellValue(h);
                            }
                            headerWidth[0] = headers.size();

                            cccdIdx[0] = findHeaderIndexInList(headers, new String[] {"cccd", "cancuoc", "socccd"});
                            gioiTinhIdx[0] = findHeaderIndexInList(headers, new String[] {"gioitinh", "gioi"});
                            ngaySinhIdx[0] = findHeaderIndexInList(headers, new String[] {"ngaysinh"});
                            noiSinhIdx[0] = findHeaderIndexInList(headers, new String[] {"noisinh"});
                            return;
                        }

                        processed[0]++;
                        for (int c = 0; c < headerWidth[0]; c++) {
                            String value = safe(currentRow.get(c));
                            outRow.createCell(c).setCellValue(value);
                        }

                        if (cccdIdx[0] < 0) {
                            return;
                        }

                        String gioiTinh = gioiTinhIdx[0] >= 0 ? safe(currentRow.get(gioiTinhIdx[0])) : "";
                        String ngaySinh = ngaySinhIdx[0] >= 0 ? safe(currentRow.get(ngaySinhIdx[0])) : "";
                        String noiSinh = noiSinhIdx[0] >= 0 ? safe(currentRow.get(noiSinhIdx[0])) : "";

                        int year = extractYear(ngaySinh);
                        String provinceCode = resolveProvinceCode(noiSinh);
                        String centuryGender = resolveCenturyGenderCode(gioiTinh, year);
                        String yy = String.format("%02d", Math.abs(year) % 100);

                        String prefix = provinceCode + centuryGender + yy;
                        int seq = prefixCounters.getOrDefault(prefix, 0) + 1;
                        prefixCounters.put(prefix, seq);
                        String newCccd = prefix + String.format("%06d", seq);

                        outRow.getCell(cccdIdx[0]).setCellValue(newCccd);
                        cccdChanged[0]++;
                    }

                    @Override
                    public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
                        if (cellReference == null) {
                            return;
                        }
                        int col = new CellReference(cellReference).getCol();
                        currentRow.put(col, safe(formattedValue));
                    }

                    @Override
                    public void headerFooter(String text, boolean isHeader, String tagName) {
                    }
                };

                XMLReader parser = SAXHelper.newXMLReader();
                ContentHandler handler = new XSSFSheetXMLHandler(styles, null, sst, sheetHandler, formatter, false);
                parser.setContentHandler(handler);
                parser.parse(new InputSource(sheetInput));

                if (cccdIdx[0] < 0) {
                    throw new IOException("Không tìm thấy cột CCCD");
                }

                for (int c = 0; c < headerWidth[0]; c++) {
                    outputSheet.setColumnWidth(c, Math.min(22 * 256, 255 * 256));
                }
            }

            try (FileOutputStream fos = new FileOutputStream(out)) {
                outputWb.write(fos);
            }

            outputWb.dispose();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Không thể chuẩn hóa CCCD từ file XLSX lớn: " + e.getMessage(), e);
        }

        lastImportSummary = "Đã chuẩn hóa CCCD 12 số cho " + cccdChanged[0] + " dòng (tổng dòng dữ liệu: " + processed[0] + ").";
        return cccdChanged[0];
    }

    // Đã gỡ bỏ logic import/sync điểm thi theo yêu cầu

    private List<ThiSinh> importFromExcel(String filePath) throws IOException {
        List<ThiSinh> result = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            Map<String, Integer> headerMap = new HashMap<>();
            Map<String, Integer> exactHeaderMap = new HashMap<>();
            
            // Tìm dòng header trong 10 dòng đầu tiên
            int headerRowIndex = 0;
            Row headerRow = null;
            for (int r = 0; r <= Math.min(10, lastRow); r++) {
                Row tempRow = sheet.getRow(r);
                if (tempRow != null) {
                    Map<String, Integer> tempMap = buildHeaderMapFromRow(tempRow, formatter);
                    if (tempMap.containsKey("cccd") || tempMap.containsKey("so bao danh") || tempMap.containsKey("sobaodanh") || tempMap.containsKey("ngay sinh") || tempMap.containsKey("ngaysinh")) {
                        headerRowIndex = r;
                        headerRow = tempRow;
                        headerMap = tempMap;
                        exactHeaderMap = buildExactHeaderMapFromRow(tempRow, formatter);
                        break;
                    }
                }
            }

            lastImportSourceRows = Math.max(0, lastRow - headerRowIndex);

            for (int i = headerRowIndex + 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                ThiSinh ts = parseThiSinhFromExcelRow(row, formatter, headerMap, exactHeaderMap);
                if (ts == null) {
                    appendImportError("Dòng " + (i + 1) + ": không nhận diện được CCCD/SBD");
                    continue;
                }

                applyAutoFixForImport(ts);

                if (validateThiSinh(ts, false)) {
                    result.add(ts);
                } else {
                    appendImportError("Dòng " + (i + 1) + " - " + safe(ts.getCccd()) + ": " + getLastError());
                }
            }
        }

        return result;
    }

    private List<ThiSinh> importFromTextLike(String filePath) throws IOException {
        List<ThiSinh> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Map<String, Integer> headerMap = new HashMap<>();
            boolean headerDetected = false;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trim = line.trim();
                if (trim.isEmpty() || trim.startsWith("---") || trim.toLowerCase().startsWith("stt")) {
                    continue;
                }

                String[] parts = trim.contains("\t") ? trim.split("\\t+") : trim.split("\\s{2,}");
                if (parts.length < 2) {
                    continue;
                }

                if (!headerDetected && looksLikeHeader(parts)) {
                    headerMap = buildHeaderMapFromParts(parts);
                    headerDetected = true;
                    continue;
                }

                lastImportSourceRows++;

                ThiSinh ts = parseThiSinhFromParts(parts, headerMap);
                if (ts == null) {
                    appendImportError("Dòng " + lineNumber + ": không nhận diện được CCCD/SBD");
                    continue;
                }

                applyAutoFixForImport(ts);

                if (validateThiSinh(ts, false)) {
                    result.add(ts);
                } else {
                    appendImportError("Dòng " + lineNumber + " - " + safe(ts.getCccd()) + ": " + getLastError());
                }
            }
        }
        return result;
    }

    private ThiSinh parseThiSinhFromExcelRow(Row row, DataFormatter formatter,
            Map<String, Integer> headerMap, Map<String, Integer> exactHeaderMap) {
        String cccd = valueFromExcelRow(row, formatter, headerMap, new String[] {"cccd", "can cuoc", "so cccd", "cancuoc", "socccd"}, 1);
        if (cccd.isEmpty()) {
            return null;
        }

        String hoTen = valueFromExcelRow(row, formatter, headerMap, new String[] {"ho ten", "ho va ten", "hoten", "hovaten"}, 2);
        String ho = valueFromExcelRowExact(row, formatter, exactHeaderMap, new String[] {"Họ", "ho"}, -1);
        String ten = valueFromExcelRowExact(row, formatter, exactHeaderMap, new String[] {"Tên", "ten"}, -1);
        String soBaoDanh = valueFromExcelRow(row, formatter, headerMap, new String[] {"so bao danh", "sobaodanh", "sbd"}, -1);
        String ngaySinh = dateFromExcelRow(row, formatter, headerMap, new String[] {"ngay sinh", "ngaysinh"}, 3);
        String gioiTinh = valueFromExcelRow(row, formatter, headerMap, new String[] {"gioi tinh", "gioitinh"}, 4);
        String doiTuong = valueFromExcelRow(row, formatter, headerMap, new String[] {"doi tuong", "doituong", "dtut"}, 5);
        String khuVuc = valueFromExcelRow(row, formatter, headerMap, new String[] {"khu vuc", "khuvuc", "kvut"}, 6);
        String dienThoai = valueFromExcelRow(row, formatter, headerMap, new String[] {"dien thoai", "dienthoai", "sdt"}, -1);
        String email = valueFromExcelRow(row, formatter, headerMap, new String[] {"email"}, -1);
        String updatedAt = valueFromExcelRow(row, formatter, headerMap, new String[] {"updated at", "updatedat"}, -1);
        String maMonNn = valueFromExcelRow(row, formatter, headerMap, new String[] {"ma mon nn", "mamonnn", "ngoai ngu", "ngoaingu"}, 16);
        String chuongTrinhHoc = valueFromExcelRow(row, formatter, headerMap, new String[] {"chuong trinh hoc", "chuongtrinhhoc"}, 21);
        String danToc = valueFromExcelRow(row, formatter, headerMap, new String[] {"dan toc", "dantoc"}, row.getLastCellNum() - 3);
        String maDanToc = valueFromExcelRow(row, formatter, headerMap, new String[] {"ma dan toc", "madantoc"}, row.getLastCellNum() - 2);
        String noiSinh = valueFromExcelRow(row, formatter, headerMap, new String[] {"noi sinh", "noisinh"}, row.getLastCellNum() - 1);

        ThiSinh ts = new ThiSinh();
        ts.setCccd(cccd);
        ts.setSoBaoDanh(soBaoDanh);
        if (!safe(ho).isEmpty() || !safe(ten).isEmpty()) {
            ts.setHo(ho);
            ts.setTen(ten);
        } else {
            fillName(ts, hoTen);
        }
        ts.setNgaySinh(ngaySinh);
        ts.setGioiTinh(gioiTinh);
        ts.setDoiTuong(doiTuong);
        ts.setKhuVuc(khuVuc);
        ts.setDienThoai(dienThoai);
        ts.setEmail(email);
        ts.setMaMonNn(maMonNn);
        ts.setChuongTrinhHoc(chuongTrinhHoc);
        ts.setDanToc(danToc);
        ts.setMaDanToc(maDanToc);
        ts.setNoiSinh(noiSinh);
        ts.setUpdatedAt(updatedAt);
        return ts;
    }

    private ThiSinh parseThiSinhFromParts(String[] parts, Map<String, Integer> headerMap) {
        String cccd = valueFromParts(parts, headerMap, new String[] {"cccd", "cancuoc", "socccd"}, 1);
        String soBaoDanh = valueFromParts(parts, headerMap, new String[] {"sobaodanh", "sbd"}, -1);
        String hoTen = valueFromParts(parts, headerMap, new String[] {"hoten", "hovaten"}, 2);
        String ho = valueFromParts(parts, headerMap, new String[] {"ho"}, -1);
        String ten = valueFromParts(parts, headerMap, new String[] {"ten"}, -1);
        String ngaySinh = valueFromParts(parts, headerMap, new String[] {"ngaysinh"}, 3);
        String gioiTinh = valueFromParts(parts, headerMap, new String[] {"gioitinh"}, 4);
        String doiTuong = valueFromParts(parts, headerMap, new String[] {"doituong", "dtut"}, 5);
        String khuVuc = valueFromParts(parts, headerMap, new String[] {"khuvuc", "kvut"}, 6);
        String dienThoai = valueFromParts(parts, headerMap, new String[] {"dienthoai", "sdt"}, -1);
        String email = valueFromParts(parts, headerMap, new String[] {"email"}, -1);
        String updatedAt = valueFromParts(parts, headerMap, new String[] {"updatedat"}, -1);
        String maMonNn = valueFromParts(parts, headerMap, new String[] {"mamonnn", "ngoaingu"}, 16);
        String chuongTrinhHoc = valueFromParts(parts, headerMap, new String[] {"chuongtrinhhoc"}, 21);
        String danToc = valueFromParts(parts, headerMap, new String[] {"dantoc"}, parts.length - 3);
        String maDanToc = valueFromParts(parts, headerMap, new String[] {"madantoc"}, parts.length - 2);
        String noiSinh = valueFromParts(parts, headerMap, new String[] {"noisinh"}, parts.length - 1);

        if (cccd.isEmpty() || !isAcceptedCandidateCode(cccd)) {
            int cccdIdx = findCandidateCodeIndex(parts);
            if (cccdIdx >= 0) {
                cccd = safe(parts[cccdIdx]);
                if (soBaoDanh.isEmpty() && cccdIdx + 1 < parts.length) {
                    String sbdCandidate = safe(parts[cccdIdx + 1]);
                    if (isValidSoBaoDanhFormat(sbdCandidate)) {
                        soBaoDanh = sbdCandidate;
                    }
                }
                if (hoTen.isEmpty() && cccdIdx + 2 < parts.length) {
                    hoTen = safe(parts[cccdIdx + 2]);
                }
            }
        }

        if (ngaySinh.isEmpty()) {
            int dobIdx = findDateIndex(parts);
            if (dobIdx >= 0) {
                ngaySinh = safe(parts[dobIdx]);
            }
        }

        if (gioiTinh.isEmpty()) {
            int genderIdx = findGenderIndex(parts);
            if (genderIdx >= 0) {
                gioiTinh = safe(parts[genderIdx]);
            }
        }

        if (maDanToc.isEmpty() || !NUMERIC_PATTERN.matcher(maDanToc).matches()) {
            int idx = findEthnicCodeIndexFromTail(parts);
            if (idx >= 0) {
                maDanToc = safe(parts[idx]);
                if (idx - 1 >= 0 && danToc.isEmpty()) {
                    danToc = safe(parts[idx - 1]);
                }
                if (idx + 1 < parts.length && noiSinh.isEmpty()) {
                    noiSinh = safe(parts[idx + 1]);
                }
            }
        }

        if (cccd.isEmpty()) {
            return null;
        }

        ThiSinh ts = new ThiSinh();
        ts.setCccd(cccd);
        ts.setSoBaoDanh(soBaoDanh);
        if (!safe(ho).isEmpty() || !safe(ten).isEmpty()) {
            ts.setHo(ho);
            ts.setTen(ten);
        } else {
            fillName(ts, hoTen);
        }
        ts.setNgaySinh(ngaySinh);
        ts.setGioiTinh(gioiTinh);
        ts.setDoiTuong(doiTuong);
        ts.setKhuVuc(khuVuc);
        ts.setDienThoai(dienThoai);
        ts.setEmail(email);
        ts.setMaMonNn(maMonNn);
        ts.setChuongTrinhHoc(chuongTrinhHoc);
        ts.setDanToc(danToc);
        ts.setMaDanToc(maDanToc);
        ts.setNoiSinh(noiSinh);
        ts.setUpdatedAt(updatedAt);
        return ts;
    }

    private int findCandidateCodeIndex(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            String token = safe(parts[i]);
            if (CCCD_PATTERN.matcher(token).matches()) {
                return i;
            }
        }
        return -1;
    }

    private int findDateIndex(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            String token = safe(parts[i]);
            if (DATE_TOKEN_PATTERN.matcher(token).matches()) {
                return i;
            }
        }
        return -1;
    }

    private int findGenderIndex(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            String token = normalizeHeader(parts[i]);
            if (token.equals("nam") || token.equals("nu")) {
                return i;
            }
        }
        return -1;
    }

    private int findEthnicCodeIndexFromTail(String[] parts) {
        for (int i = parts.length - 1; i >= 0; i--) {
            String token = safe(parts[i]);
            if (!NUMERIC_PATTERN.matcher(token).matches()) {
                continue;
            }
            try {
                int v = Integer.parseInt(token);
                if (v > 0 && v < 1000) {
                    return i;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    private Map<String, Integer> buildHeaderMapFromRow(Row row, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String key = normalizeHeader(formatter.formatCellValue(row.getCell(i)));
            if (!key.isEmpty()) {
                map.put(key, i);
            }
        }
        return map;
    }

    private Map<String, Integer> buildExactHeaderMapFromRow(Row row, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String key = safe(formatter.formatCellValue(row.getCell(i)));
            if (!key.isEmpty()) {
                map.put(key, i);
            }
        }
        return map;
    }

    private Map<String, Integer> buildHeaderMapFromParts(String[] parts) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < parts.length; i++) {
            String key = normalizeHeader(parts[i]);
            if (!key.isEmpty()) {
                map.put(key, i);
            }
        }
        return map;
    }

    private boolean looksLikeHeader(String[] parts) {
        for (String part : parts) {
            String key = normalizeHeader(part);
            if (key.equals("cccd") || key.equals("ho ten") || key.equals("ho va ten") || key.equals("ngay sinh")) {
                return true;
            }
        }
        return false;
    }

    private String valueFromExcelRow(Row row, DataFormatter formatter, Map<String, Integer> headerMap, String[] aliases, int fallbackIndex) {
        int idx = findHeaderIndex(headerMap, aliases);
        if (idx >= 0) {
            return safe(formatter.formatCellValue(row.getCell(idx)));
        }
        if (fallbackIndex >= 0 && fallbackIndex < row.getLastCellNum()) {
            return safe(formatter.formatCellValue(row.getCell(fallbackIndex)));
        }
        return "";
    }

    private String valueFromExcelRowExact(Row row, DataFormatter formatter, Map<String, Integer> exactHeaderMap,
            String[] aliases, int fallbackIndex) {
        int idx = findExactHeaderIndex(exactHeaderMap, aliases);
        if (idx >= 0) {
            return safe(formatter.formatCellValue(row.getCell(idx)));
        }
        if (fallbackIndex >= 0 && fallbackIndex < row.getLastCellNum()) {
            return safe(formatter.formatCellValue(row.getCell(fallbackIndex)));
        }
        return "";
    }

    private String dateFromExcelRow(Row row, DataFormatter formatter, Map<String, Integer> headerMap, String[] aliases, int fallbackIndex) {
        int idx = findHeaderIndex(headerMap, aliases);
        if (idx < 0) {
            idx = fallbackIndex;
        }
        if (idx >= 0 && idx < row.getLastCellNum()) {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(idx);
            if (cell != null) {
                if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                        && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    java.util.Date date = cell.getDateCellValue();
                    return new java.text.SimpleDateFormat("dd/MM/yyyy").format(date);
                }
                String val = safe(formatter.formatCellValue(cell));
                // Nếu là số serial nguyên của Excel (ví dụ 39288 cho 25/07/2007)
                if (val.matches("^\\d{4,5}(\\.\\d+)?$")) {
                    try {
                        double excelDate = Double.parseDouble(val);
                        java.util.Date d = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(excelDate);
                        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(d);
                    } catch (Exception ignored) { }
                }
                // Nếu DataFormatter trả về dạng M/d/yy hoặc tương tự (VD: 2/26/07) từ Excel US locale
                if (val.matches("^\\d{1,2}/\\d{1,2}/\\d{2}$")) {
                    try {
                        java.util.Date d = new java.text.SimpleDateFormat("M/d/yy").parse(val);
                        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(d);
                    } catch (Exception ignored) { }
                }
                // Nếu là M/d/yyyy (VD: 7/25/2007)
                if (val.matches("^\\d{1,2}/\\d{1,2}/\\d{4}$")) {
                    try {
                        java.util.Date d = new java.text.SimpleDateFormat("M/d/yyyy").parse(val);
                        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(d);
                    } catch (Exception ignored) { }
                }
                return val;
            }
        }
        return "";
    }

    private String valueFromParts(String[] parts, Map<String, Integer> headerMap, String[] aliases, int fallbackIndex) {
        int idx = findHeaderIndex(headerMap, aliases);
        if (idx >= 0 && idx < parts.length) {
            return safe(parts[idx]);
        }
        if (fallbackIndex >= 0 && fallbackIndex < parts.length) {
            return safe(parts[fallbackIndex]);
        }
        return "";
    }

    private int findHeaderIndex(Map<String, Integer> headerMap, String[] aliases) {
        if (headerMap == null || headerMap.isEmpty()) {
            return -1;
        }
        for (String alias : aliases) {
            Integer idx = headerMap.get(alias); // alias is expected to be pre-normalized
            if (idx != null) {
                return idx;
            }
        }
        return -1;
    }

    private int findExactHeaderIndex(Map<String, Integer> exactHeaderMap, String[] aliases) {
        if (exactHeaderMap == null || exactHeaderMap.isEmpty()) {
            return -1;
        }
        for (String alias : aliases) {
            for (Map.Entry<String, Integer> entry : exactHeaderMap.entrySet()) {
                if (entry.getKey().trim().equalsIgnoreCase(alias.trim())) {
                    return entry.getValue();
                }
            }
        }
        return -1;
    }

    private int findHeaderIndexInList(List<String> headers, String[] aliases) {
        if (headers == null || headers.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < headers.size(); i++) {
            String key = normalizeHeader(headers.get(i));
            for (String alias : aliases) {
                if (key.equals(alias)) { // alias is expected to be pre-normalized
                    return i;
                }
            }
        }
        return -1;
    }

    private int extractYear(String ngaySinh) {
        String v = safe(ngaySinh);
        if (v.matches("^\\d{1,2}/\\d{1,2}/\\d{4}$")) {
            try {
                return Integer.parseInt(v.substring(v.length() - 4));
            } catch (NumberFormatException ignored) {
            }
        }
        if (v.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")) {
            try {
                return Integer.parseInt(v.substring(0, 4));
            } catch (NumberFormatException ignored) {
            }
        }
        return 2007;
    }

    private String resolveCenturyGenderCode(String gioiTinh, int year) {
        String gt = normalizeHeader(gioiTinh);
        boolean female = gt.contains("nu");

        int centuryIndex = Math.max(0, (year / 100) - 19);
        int maleCode = Math.min(9, centuryIndex * 2);
        int femaleCode = Math.min(9, maleCode + 1);
        return String.valueOf(female ? femaleCode : maleCode);
    }

    private String resolveProvinceCode(String noiSinh) {
        String normalized = normalizeProvinceName(noiSinh);
        String direct = PROVINCE_CODE_MAP.get(normalized);
        if (direct != null) {
            return direct;
        }

        for (Map.Entry<String, String> entry : PROVINCE_CODE_MAP.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "000";
    }

    private String normalizeProvinceName(String text) {
        String t = normalizeHeader(text).toUpperCase(Locale.ROOT);
        t = t.replace("TP ", "").replace("THANH PHO ", "");
        t = t.replace("TINH ", "").trim();
        return t;
    }

    private static Map<String, String> createProvinceCodeMap() {
        Map<String, String> map = new HashMap<>();
        map.put("HA NOI", "001");
        map.put("HA GIANG", "002");
        map.put("CAO BANG", "004");
        map.put("BAC KAN", "006");
        map.put("TUYEN QUANG", "008");
        map.put("LAO CAI", "010");
        map.put("DIEN BIEN", "011");
        map.put("LAI CHAU", "012");
        map.put("YEN BAI", "015");
        map.put("HOA BINH", "017");
        map.put("THAI NGUYEN", "019");
        map.put("LANG SON", "020");
        map.put("QUANG NINH", "022");
        map.put("BAC GIANG", "024");
        map.put("PHU THO", "025");
        map.put("VINH PHUC", "026");
        map.put("BAC NINH", "027");
        map.put("HAI DUONG", "030");
        map.put("HAI PHONG", "031");
        map.put("HUNG YEN", "033");
        map.put("THAI BINH", "034");
        map.put("HA NAM", "035");
        map.put("NAM DINH", "036");
        map.put("NINH BINH", "037");
        map.put("THANH HOA", "038");
        map.put("NGHE AN", "040");
        map.put("HA TINH", "042");
        map.put("QUANG BINH", "044");
        map.put("QUANG TRI", "045");
        map.put("THUA THIEN HUE", "046");
        map.put("DA NANG", "048");
        map.put("QUANG NAM", "049");
        map.put("QUANG NGAI", "051");
        map.put("BINH DINH", "052");
        map.put("PHU YEN", "054");
        map.put("KHANH HOA", "056");
        map.put("NINH THUAN", "058");
        map.put("BINH THUAN", "060");
        map.put("KON TUM", "062");
        map.put("GIA LAI", "064");
        map.put("DAK LAK", "066");
        map.put("DAK NONG", "067");
        map.put("LAM DONG", "068");
        map.put("BINH PHUOC", "070");
        map.put("TAY NINH", "072");
        map.put("BINH DUONG", "074");
        map.put("DONG NAI", "075");
        map.put("BA RIA VUNG TAU", "077");
        map.put("HO CHI MINH", "079");
        map.put("LONG AN", "080");
        map.put("TIEN GIANG", "082");
        map.put("BEN TRE", "083");
        map.put("TRA VINH", "084");
        map.put("VINH LONG", "086");
        map.put("DONG THAP", "087");
        map.put("AN GIANG", "089");
        map.put("KIEN GIANG", "091");
        map.put("CAN THO", "092");
        map.put("HAU GIANG", "093");
        map.put("SOC TRANG", "094");
        map.put("BAC LIEU", "095");
        map.put("CA MAU", "096");
        return map;
    }

    private synchronized void ensurePoiLargeFileSupport() {
        if (poiMaxOverrideConfigured) {
            return;
        }
        IOUtils.setByteArrayMaxOverride(300_000_000);
        poiMaxOverrideConfigured = true;
    }

    private String normalizeHeader(String input) {
        String text = safe(input).toLowerCase(Locale.ROOT);
        text = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd');
        text = text.replace('_', ' ').replace('-', ' ');
        text = text.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        return text;
    }

    private void mergeImportedThiSinh(ThiSinh target, ThiSinh source) {
        target.setSoBaoDanh(pick(target.getSoBaoDanh(), source.getSoBaoDanh()));
        target.setHo(pick(target.getHo(), source.getHo()));
        target.setTen(pick(target.getTen(), source.getTen()));
        target.setNgaySinh(pick(target.getNgaySinh(), source.getNgaySinh()));
        target.setDienThoai(pick(target.getDienThoai(), source.getDienThoai()));
        target.setGioiTinh(pick(target.getGioiTinh(), source.getGioiTinh()));
        target.setEmail(pick(target.getEmail(), source.getEmail()));
        target.setNoiSinh(pick(target.getNoiSinh(), source.getNoiSinh()));
        target.setDoiTuong(pick(target.getDoiTuong(), source.getDoiTuong()));
        target.setKhuVuc(pick(target.getKhuVuc(), source.getKhuVuc()));
        target.setDanToc(pick(target.getDanToc(), source.getDanToc()));
        target.setMaDanToc(pick(target.getMaDanToc(), source.getMaDanToc()));
        target.setChuongTrinhHoc(pick(target.getChuongTrinhHoc(), source.getChuongTrinhHoc()));
        target.setMaMonNn(pick(target.getMaMonNn(), source.getMaMonNn()));
        target.setPassword(deriveStudentPassword(target));
        target.setUpdatedAt(LocalDate.now().toString());
    }

    private String pick(String oldValue, String newValue) {
        String normalizedNew = safe(newValue);
        if (!normalizedNew.isEmpty()) {
            return normalizedNew;
        }
        return safe(oldValue);
    }

    private void applyAutoFixForImport(ThiSinh ts) {
        if (ts == null) {
            return;
        }

        ts.setCccd(normalizeCandidateCode(ts.getCccd()));
        if (safe(ts.getSoBaoDanh()).isEmpty()) {
            ts.setSoBaoDanh("TS_" + ts.getCccd());
        }

        String ho = safe(ts.getHo());
        String ten = safe(ts.getTen());
        if (ho.isEmpty() && ten.isEmpty()) {
            fillName(ts, ts.getCccd());
        }

        String gt = normalizeHeader(ts.getGioiTinh());
        if (gt.equals("nu")) {
            ts.setGioiTinh("Nữ");
        } else if (gt.equals("nam")) {
            ts.setGioiTinh("Nam");
        }

        if (safe(ts.getUpdatedAt()).isEmpty()) {
            ts.setUpdatedAt(LocalDate.now().toString());
        }

        // Khi import thí sinh: luôn sinh password từ ngày sinh (ddMMyyyy), bỏ qua cột password của file nguồn.
        ts.setPassword(deriveStudentPassword(ts));
    }

    private String normalizeCandidateCode(String value) {
        return safe(value);
    }

    private void appendImportError(String message) {
        if (safe(message).isEmpty()) {
            return;
        }
        if (lastImportRowErrors.size() < 500) {
            lastImportRowErrors.add(message);
        }
    }

    private void fillName(ThiSinh ts, String fullName) {
        if (fullName == null || fullName.isBlank()) {
            ts.setHo("");
            ts.setTen("");
            return;
        }

        String[] tokens = fullName.trim().split("\\s+");
        if (tokens.length == 1) {
            ts.setHo(tokens[0]);
            ts.setTen(tokens[0]);
            return;
        }

        ts.setTen(tokens[tokens.length - 1]);
        ts.setHo(fullName.substring(0, fullName.length() - ts.getTen().length()).trim());
    }

    private boolean validateThiSinh(ThiSinh ts, boolean checkDuplicate) {
        if (ts == null) {
            lastError = "Dữ liệu thí sinh không hợp lệ!";
            return false;
        }

        String cccd = safe(ts.getCccd());
        String soBaoDanh = safe(ts.getSoBaoDanh());
        String ho = safe(ts.getHo());
        String ten = safe(ts.getTen());
        String email = safe(ts.getEmail());
        String dienThoai = safe(ts.getDienThoai());
        String ngaySinh = safe(ts.getNgaySinh());
        String gioiTinh = safe(ts.getGioiTinh());
        String noiSinh = safe(ts.getNoiSinh());
        String doiTuong = safe(ts.getDoiTuong());
        String khuVuc = safe(ts.getKhuVuc());
        String password = safe(ts.getPassword());
        String updatedAt = safe(ts.getUpdatedAt());

        if (cccd.isEmpty()) {
            lastError = "CCCD không được để trống!";
            return false;
        }
        if (!isAcceptedCandidateCode(cccd)) {
            lastError = "CCCD chỉ được phép gồm đúng 12 chữ số!";
            return false;
        }
        if (soBaoDanh.isEmpty()) {
            lastError = "Số báo danh không được để trống!";
            return false;
        }
        if (!isValidSoBaoDanhFormat(soBaoDanh)) {
            lastError = "Số báo danh phải theo mẫu TS_<số>!";
            return false;
        }
        if (ho.isEmpty() || ten.isEmpty()) {
            lastError = "Họ và tên không được để trống!";
            return false;
        }
        if (isNumericOnly(ho) || isNumericOnly(ten)) {
            lastError = "Họ và tên không được chỉ gồm chữ số!";
            return false;
        }
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            lastError = "Email không đúng định dạng!";
            return false;
        }
        if (!dienThoai.isEmpty() && !PHONE_PATTERN.matcher(dienThoai).matches()) {
            lastError = "Số điện thoại không hợp lệ!";
            return false;
        }
        if (ngaySinh.isEmpty()) {
            lastError = "Ngày sinh không được để trống!";
            return false;
        }
        if (!isValidDate(ngaySinh)) {
            lastError = "Ngày sinh [" + ngaySinh + "] không hợp lệ (cần dd/MM/yyyy)!";
            return false;
        }
        String normalizedKhuVuc = normalizeKhuVuc(khuVuc);
        if (normalizedKhuVuc == null) {
            lastError = "Khu vực chỉ chấp nhận: KV1, KV2-NT, KV2, KV3 (hoặc để trống)!";
            return false;
        }
        String normalizedDoiTuong = normalizeDoiTuong(doiTuong);
        if (checkDuplicate && dao.getByCccd(cccd) != null) {
            lastError = "CCCD đã tồn tại trong hệ thống!";
            return false;
        }

        ts.setCccd(trimMax(cccd, 20));
        ts.setSoBaoDanh(trimMax(soBaoDanh.toUpperCase(Locale.ROOT), 45));
        ts.setHo(trimMax(ho, 100));
        ts.setTen(trimMax(ten, 100));
        ts.setNgaySinh(trimMax(ngaySinh, 45));
        ts.setDienThoai(trimMax(dienThoai, 20));
        ts.setPassword(trimMax(password, 100));
        ts.setGioiTinh(trimMax(gioiTinh, 10));
        ts.setEmail(trimMax(email, 100));
        ts.setNoiSinh(trimMax(noiSinh, 45));
        ts.setDoiTuong(trimMax(normalizedDoiTuong, 45));
        ts.setKhuVuc(trimMax(normalizedKhuVuc, 45));
        ts.setUpdatedAt(trimMax(updatedAt, 45));
        lastError = "";
        return true;
    }

    private boolean isValidSoBaoDanhFormat(String soBaoDanh) {
        return SO_BAO_DANH_PATTERN.matcher(safe(soBaoDanh)).matches();
    }

    private boolean isNumericOnly(String text) {
        String compact = safe(text).replaceAll("\\s+", "");
        return !compact.isEmpty() && NUMERIC_PATTERN.matcher(compact).matches();
    }

    private String normalizeKhuVuc(String value) {
        String v = safe(value).toUpperCase(Locale.ROOT).replace("_", "").replace(" ", "");
        if (v.isEmpty()) {
            return "";
        }
        if ("KV1".equals(v) || "1".equals(v)) {
            return "KV1";
        }
        if ("KV2NT".equals(v) || "KV2-NT".equals(v) || "2NT".equals(v)) {
            return "KV2-NT";
        }
        if ("KV2".equals(v) || "2".equals(v)) {
            return "KV2";
        }
        if ("KV3".equals(v) || "3".equals(v)) {
            return "KV3";
        }
        return null;
    }

    private String normalizeDoiTuong(String value) {
        // Không validate, chấp nhận mọi giá trị đối tượng ưu tiên
        return safe(value);
    }

    private boolean isValidDate(String text) {
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("dd/MM/yy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate.parse(text, formatter);
                return true;
            } catch (DateTimeParseException ignored) {
            }
        }
        return false;
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.equalsIgnoreCase("nan") || normalized.equalsIgnoreCase("null")) {
            return "";
        }
        return normalized;
    }

    private boolean isAcceptedCandidateCode(String cccdOrCode) {
        return CCCD_PATTERN.matcher(cccdOrCode).matches();
    }

    private String trimMax(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private void reportProgress(ImportProgressListener listener, int percent, String message) {
        if (listener == null) {
            return;
        }
        int bounded = Math.max(0, Math.min(100, percent));
        listener.onProgress(bounded, message);
    }
}
