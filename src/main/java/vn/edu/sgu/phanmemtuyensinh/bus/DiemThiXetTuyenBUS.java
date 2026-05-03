package vn.edu.sgu.phanmemtuyensinh.bus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
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
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ooxml.util.SAXHelper;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import vn.edu.sgu.phanmemtuyensinh.dal.DiemThiXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;

public class DiemThiXetTuyenBUS {
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern DATASET_CODE_PATTERN = Pattern.compile("^TS_\\d{1,10}$", Pattern.CASE_INSENSITIVE);

    private final DiemThiXetTuyenDAO dao = new DiemThiXetTuyenDAO();
    private final ThiSinhDAO thiSinhDAO = new ThiSinhDAO();
    private String lastError = "";
    private String lastImportSummary = "";

    @FunctionalInterface
    public interface ImportProgressListener {
        void onProgress(int percent, String message);
    }

    public List<DiemThiXetTuyen> getAll() {
        return dao.getAll();
    }

    public DiemThiXetTuyen getByCccd(String cccd) {
        return dao.getByCccd(cccd);
    }

    public DiemThiXetTuyen getById(int idDiemThi) {
        return dao.getById(idDiemThi);
    }

    public List<DiemThiXetTuyen> getPage(int page, int pageSize) {
        return dao.getPage(page, pageSize);
    }

    public List<DiemThiXetTuyen> getPageWithSort(int page, int pageSize, String sortOrder) {
        return dao.getPageWithSort(page, pageSize, sortOrder);
    }

    public long countAll() {
        return dao.countAll();
    }

    public List<DiemThiXetTuyen> getByPhuongThuc(String phuongThuc) {
        return dao.getByPhuongThuc(phuongThuc);
    }

    public List<DiemThiXetTuyen> searchByKeyword(String keyword, int page, int pageSize) {
        return dao.searchByKeyword(keyword, page, pageSize);
    }

    public long countByKeyword(String keyword) {
        return dao.countByKeyword(keyword);
    }

    /**
     * Thống kê điểm theo từng môn thi.
     * Trả về Map: key = tên môn, value = double[] {soCoDiem, diemTB, diemMax, diemMin, soTrong}
     */
    public java.util.LinkedHashMap<String, double[]> getThongKeDiemTheoMon() {
        List<DiemThiXetTuyen> all = dao.getAll();
        String[] tenMon = {"TO","VA","LI","HO","SI","SU","DI","GDCD","N1_THI","N1_CC","CNCN","CNNN","TI","KTPL","NL1",
                           "NK1","NK2","NK3","NK4","NK5","NK6","NK7","NK8","NK9","NK10","Điểm xét TN"};

        java.util.LinkedHashMap<String, double[]> result = new java.util.LinkedHashMap<>();
        for (String mon : tenMon) {
            result.put(mon, new double[]{0, 0, Double.MIN_VALUE, Double.MAX_VALUE, 0});
        }

        for (DiemThiXetTuyen d : all) {
            java.math.BigDecimal[] vals = {
                d.getTo(), d.getVa(), d.getLi(), d.getHo(), d.getSi(), d.getSu(), d.getDi(), d.getGdcd(),
                d.getN1Thi(), d.getN1Cc(), d.getCncn(), d.getCnnn(), d.getTi(), d.getKtpl(), d.getNl1(),
                d.getNk1(), d.getNk2(), d.getNk3(), d.getNk4(), d.getNk5(), d.getNk6(), d.getNk7(),
                d.getNk8(), d.getNk9(), d.getNk10(), d.getDiemXetTotNghiep()
            };
            for (int i = 0; i < tenMon.length; i++) {
                double[] stat = result.get(tenMon[i]);
                if (vals[i] != null) {
                    double v = vals[i].doubleValue();
                    stat[0]++;                                           // soCoDiem
                    stat[1] += v;                                        // tổng (tính TB sau)
                    if (v > stat[2]) stat[2] = v;                       // max
                    if (v < stat[3]) stat[3] = v;                       // min
                } else {
                    stat[4]++;                                           // soTrong
                }
            }
        }

        // Tính điểm trung bình
        for (double[] stat : result.values()) {
            if (stat[0] > 0) {
                stat[1] = stat[1] / stat[0];
            } else {
                stat[2] = 0; // Không có điểm → max/min reset về 0
                stat[3] = 0;
            }
        }
        return result;
    }

    public boolean add(DiemThiXetTuyen diem) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        if (!validateDiemThi(diem, true)) {
            return false;
        }
        return dao.add(diem);
    }

    public boolean update(DiemThiXetTuyen diem) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        if (!validateDiemThi(diem, false)) {
            return false;
        }
        return dao.update(diem);
    }

    public boolean delete(int idDiemThi) {
        if (!AuthorizationContext.ensureWritePermission(msg -> lastError = msg)) {
            return false;
        }
        return dao.delete(idDiemThi);
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

        reportProgress(progressListener, 1, "Đang đọc file import...");

        List<ImportRecord> records;
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".xlsx")) {
            records = importFromExcel(filePath);
        } else {
            records = importFromTextLike(filePath);
        }

        reportProgress(progressListener, 45,
                "Đã đọc " + records.size() + " dòng hợp lệ. Đang lưu vào DB...");

        // Pre-load toàn bộ dữ liệu 1 lần duy nhất - tránh N+1 query
        java.util.Set<String> existingCccd = dao.getAllCccd();
        java.util.Map<String, DiemThiXetTuyen> existingDiemMap = dao.getAllDiemMap();
        // Pre-load map CCCD->ThiSinh và SBD->ThiSinh để không query từng dòng
        java.util.Map<String, Integer> cccdToIdMap = thiSinhDAO.getAllCccdToIdMap();
        java.util.Map<String, Integer> sbdToIdMap  = thiSinhDAO.getAllSbdToIdMap();

        int success = 0;
        int failed = 0;
        int skipped = 0;
        List<String> sampleErrors = new ArrayList<>();

        int total = records.size();
        for (int i = 0; i < total; i++) {
            ImportRecord record = records.get(i);

            // Bước 1: CCCD phải tồn tại bên bảng thí sinh
            String cccdImport = safe(record.diem.getCccd());
            String sbdImport  = safe(record.diem.getSoBaoDanh());

            Integer idByCccd = cccdToIdMap.get(cccdImport);
            if (idByCccd == null) {
                failed++;
                if (sampleErrors.size() < 8) {
                    sampleErrors.add("Dòng " + record.lineNo + ": CCCD " + cccdImport
                            + " không tồn tại trong bảng thí sinh!");
                }
                continue;
            }

            // Bước 2: Nếu SBD cũng có → kiểm tra không được lệch dòng
            if (!sbdImport.isEmpty()) {
                Integer idBySbd = sbdToIdMap.get(sbdImport.toUpperCase());
                if (idBySbd != null && !idByCccd.equals(idBySbd)) {
                    failed++;
                    if (sampleErrors.size() < 8) {
                        sampleErrors.add("Dòng " + record.lineNo + ": CCCD " + cccdImport
                                + " và SBD " + sbdImport
                                + " thuộc 2 thí sinh khác nhau trong CSDL! Kiểm tra lại file.");
                    }
                    continue;
                }
            }

            if (!validateDiemThi(record.diem, false)) {
                failed++;
                if (sampleErrors.size() < 8) {
                    sampleErrors.add("Dòng " + record.lineNo + ": " + getLastError());
                }
                if (total > 0) {
                    int percent = Math.max(45, 45 + (int) (((i + 1) * 55.0) / total));
                    reportProgress(progressListener, percent,
                            "Đang lưu vào DB " + (i + 1) + "/" + total + " bản ghi...");
                }
                continue;
            }

            String cccd = record.diem.getCccd();
            boolean isNew = !existingCccd.contains(cccd);

            boolean ok = false;
            if (isNew) {
                ok = dao.add(record.diem);
            } else {
                DiemThiXetTuyen existing = existingDiemMap.get(cccd);
                if (existing != null && isDiemChanged(existing, record.diem)) {
                    record.diem.setIdDiemThi(existing.getIdDiemThi());
                    ok = dao.update(record.diem);
                } else {
                    skipped++;
                    continue;
                }
            }

            if (ok) {
                success++;
            } else {
                failed++;
                if (sampleErrors.size() < 8) {
                    sampleErrors.add("Dòng " + record.lineNo + ": Lỗi lưu dữ liệu vào cơ sở dữ liệu");
                }
            }

            if (total > 0) {
                int percent = Math.max(45, 45 + (int) (((i + 1) * 55.0) / total));
                reportProgress(progressListener, percent,
                        "Đang lưu vào DB " + (i + 1) + "/" + total + " bản ghi...");
            }
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Tổng đọc: ").append(records.size())
                .append(" | Mới: ").append(success)
                .append(" | Bỏ qua (không thay đổi): ").append(skipped)
                .append(" | Thất bại: ").append(failed);
        if (!sampleErrors.isEmpty()) {
            summary.append("\nMột số lỗi:\n- ").append(String.join("\n- ", sampleErrors));
        }
        lastImportSummary = summary.toString();
        reportProgress(progressListener, 100, "Import điểm thi hoàn tất.");
        return success;
    }

    public String getLastImportSummary() {
        return lastImportSummary == null ? "" : lastImportSummary;
    }

    private void reportProgress(ImportProgressListener listener, int percent, String message) {
        if (listener != null) {
            listener.onProgress(percent, message);
        }
    }

    private boolean isDiemChanged(DiemThiXetTuyen existing, DiemThiXetTuyen newDiem) {
        // So sánh các trường điểm để kiểm tra xem có thay đổi không
        return !compareDecimal(existing.getTo(), newDiem.getTo())
                || !compareDecimal(existing.getVa(), newDiem.getVa())
                || !compareDecimal(existing.getLi(), newDiem.getLi())
                || !compareDecimal(existing.getHo(), newDiem.getHo())
                || !compareDecimal(existing.getSi(), newDiem.getSi())
                || !compareDecimal(existing.getSu(), newDiem.getSu())
                || !compareDecimal(existing.getDi(), newDiem.getDi())
                || !compareDecimal(existing.getN1Thi(), newDiem.getN1Thi())
                || !compareDecimal(existing.getN1Cc(), newDiem.getN1Cc())
                || !compareDecimal(existing.getCncn(), newDiem.getCncn())
                || !compareDecimal(existing.getCnnn(), newDiem.getCnnn())
                || !compareDecimal(existing.getTi(), newDiem.getTi())
                || !compareDecimal(existing.getGdcd(), newDiem.getGdcd())
                || !compareDecimal(existing.getKtpl(), newDiem.getKtpl())
                || !compareDecimal(existing.getNl1(), newDiem.getNl1())
                || !compareDecimal(existing.getNk1(), newDiem.getNk1())
                || !compareDecimal(existing.getNk2(), newDiem.getNk2())
                || !compareDecimal(existing.getNk3(), newDiem.getNk3())
                || !compareDecimal(existing.getNk4(), newDiem.getNk4())
                || !compareDecimal(existing.getNk5(), newDiem.getNk5())
                || !compareDecimal(existing.getNk6(), newDiem.getNk6())
                || !compareDecimal(existing.getNk7(), newDiem.getNk7())
                || !compareDecimal(existing.getNk8(), newDiem.getNk8())
                || !compareDecimal(existing.getNk9(), newDiem.getNk9())
                || !compareDecimal(existing.getNk10(), newDiem.getNk10())
                || !compareDecimal(existing.getDiemXetTotNghiep(), newDiem.getDiemXetTotNghiep());
    }

    private boolean compareDecimal(BigDecimal d1, BigDecimal d2) {
        if (d1 == null && d2 == null) {
            return true;
        }
        if (d1 == null || d2 == null) {
            return false;
        }
        return d1.compareTo(d2) == 0;
    }

    private List<ImportRecord> importFromExcel(String filePath) throws IOException {
        List<ImportRecord> result = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        IOUtils.setByteArrayMaxOverride(300_000_000);

        try (OPCPackage pkg = OPCPackage.open(filePath, PackageAccess.READ)) {
            XSSFReader xssfReader = new XSSFReader(pkg);
            StylesTable styles = xssfReader.getStylesTable();
            ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(pkg);
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

            if (!sheets.hasNext()) return result;

            try (java.io.InputStream sheetIs = sheets.next()) {
                // State shared between callbacks
                final Map<String, Integer> headerMap = new java.util.LinkedHashMap<>();
                final Map<Integer, String> currentRowData = new java.util.HashMap<>();
                final int[] rowNumHolder = {0};
                final boolean[] headerParsed = {false};

                XSSFSheetXMLHandler.SheetContentsHandler handler = new XSSFSheetXMLHandler.SheetContentsHandler() {
                    @Override public void startRow(int rowNum) {
                        rowNumHolder[0] = rowNum;
                        currentRowData.clear();
                    }

                    @Override public void endRow(int rowNum) {
                        if (rowNum == 0 || (!headerParsed[0] && rowNum <= 10)) {
                            // Build header map
                            Map<String, Integer> tempMap = new java.util.LinkedHashMap<>();
                            for (Map.Entry<Integer, String> e : currentRowData.entrySet()) {
                                String key = normalizeHeader(e.getValue());
                                if (!key.isEmpty()) tempMap.put(key, e.getKey());
                            }
                            if (tempMap.containsKey("cccd") || tempMap.containsKey("sobaodanh") || tempMap.containsKey("to")) {
                                headerMap.clear();
                                headerMap.putAll(tempMap);
                                headerParsed[0] = true;
                            }
                            return;
                        }
                        if (!headerParsed[0]) return;

                        // Build a fake row-like data structure and parse it
                        String cccd = getCellStr(headerMap, currentRowData, "cccd", "cancuoc");
                        if (cccd == null || cccd.isEmpty()) {
                            // fallback col 1
                            cccd = safe(currentRowData.get(1));
                        }
                        if (cccd == null || cccd.isBlank()) return;

                        DiemThiXetTuyen d = new DiemThiXetTuyen();
                        d.setCccd(cccd);
                        d.setSoBaoDanh(safe(getCellStr(headerMap, currentRowData, "sobaodanh", "sbd", "mathisinh")));
                        // Chương trình học (năm) không phải phương thức thi - luôn mặc định THPT
                        String pt = safe(getCellStr(headerMap, currentRowData, "dphuongthuc", "phuongthuc", "ptxt"));
                        d.setPhuongThuc(pt.isEmpty() ? "THPT" : pt.toUpperCase());

                        d.setTo(parseDecimal(getCellStr(headerMap, currentRowData, "to", "toan")));
                        d.setVa(parseDecimal(getCellStr(headerMap, currentRowData, "va", "van", "nguvan")));
                        d.setLi(parseDecimal(getCellStr(headerMap, currentRowData, "li", "ly", "vatly")));
                        d.setHo(parseDecimal(getCellStr(headerMap, currentRowData, "ho", "hoa")));
                        d.setSi(parseDecimal(getCellStr(headerMap, currentRowData, "si", "sinh", "sinhhoc")));
                        d.setSu(parseDecimal(getCellStr(headerMap, currentRowData, "su", "lichsu")));
                        d.setDi(parseDecimal(getCellStr(headerMap, currentRowData, "di", "dia", "diali")));
                        d.setN1Thi(parseDecimal(getCellStr(headerMap, currentRowData, "n1thi", "n1", "nn", "ngoaingu", "mamonne")));
                        d.setN1Cc(parseDecimal(getCellStr(headerMap, currentRowData, "n1cc", "ccnn")));
                        d.setCncn(parseDecimal(getCellStr(headerMap, currentRowData, "cncn")));
                        d.setCnnn(parseDecimal(getCellStr(headerMap, currentRowData, "cnnn")));
                        d.setTi(parseDecimal(getCellStr(headerMap, currentRowData, "ti", "tin", "tinhoc")));
                        d.setGdcd(parseDecimal(getCellStr(headerMap, currentRowData, "gdcd", "giaoduccongdan")));
                        d.setKtpl(parseDecimal(getCellStr(headerMap, currentRowData, "ktpl", "kinhtephapluat")));
                        d.setNl1(parseDecimal(getCellStr(headerMap, currentRowData, "nl1", "dgnl")));
                        d.setNk1(parseDecimal(getCellStr(headerMap, currentRowData, "nk1")));
                        d.setNk2(parseDecimal(getCellStr(headerMap, currentRowData, "nk2")));
                        d.setNk3(parseDecimal(getCellStr(headerMap, currentRowData, "nk3")));
                        d.setNk4(parseDecimal(getCellStr(headerMap, currentRowData, "nk4")));
                        d.setNk5(parseDecimal(getCellStr(headerMap, currentRowData, "nk5")));
                        d.setNk6(parseDecimal(getCellStr(headerMap, currentRowData, "nk6")));
                        d.setNk7(parseDecimal(getCellStr(headerMap, currentRowData, "nk7")));
                        d.setNk8(parseDecimal(getCellStr(headerMap, currentRowData, "nk8")));
                        d.setNk9(parseDecimal(getCellStr(headerMap, currentRowData, "nk9")));
                        d.setNk10(parseDecimal(getCellStr(headerMap, currentRowData, "nk10")));
                        d.setDiemXetTotNghiep(parseDecimal(getCellStr(headerMap, currentRowData, "diemxettotnghiep", "diemxettn")));

                        result.add(new ImportRecord(rowNum + 1, d));
                    }

                    @Override public void cell(String cellRef, String formattedValue, XSSFComment comment) {
                        if (cellRef == null) return;
                        int col = new CellReference(cellRef).getCol();
                        currentRowData.put(col, formattedValue == null ? "" : formattedValue.trim());
                    }

                    @Override public void headerFooter(String text, boolean isHeader, String tagName) {}
                };

                XMLReader parser = SAXHelper.newXMLReader();
                ContentHandler content = new XSSFSheetXMLHandler(styles, null, sst, handler, formatter, false);
                parser.setContentHandler(content);
                parser.parse(new InputSource(sheetIs));
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Lỗi đọc file Excel: " + e.getMessage(), e);
        }

        return result;
    }

    /** Tìm giá trị ô theo tên cột (hỗ trợ nhiều alias) */
    private String getCellStr(Map<String, Integer> headerMap, Map<Integer, String> rowData, String... aliases) {
        for (String alias : aliases) {
            Integer col = headerMap.get(alias);
            if (col != null) {
                String val = rowData.get(col);
                return val == null ? "" : val.trim();
            }
        }
        return "";
    }

    private List<ImportRecord> importFromTextLike(String filePath) throws IOException {
        List<ImportRecord> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trim = line.trim();
                if (trim.isEmpty() || trim.startsWith("---") || trim.toLowerCase().startsWith("stt")) {
                    continue;
                }

                String[] parts = trim.contains("\t") ? trim.split("\\t+") : trim.split("\\s{2,}");
                if (parts.length < 2) {
                    continue;
                }

                DiemThiXetTuyen diem = parseTextParts(parts);
                if (isBlank(safe(diem.getCccd()))) {
                    continue;
                }
                result.add(new ImportRecord(lineNo, diem));
            }
        }

        return result;
    }

    private DiemThiXetTuyen parseExcelRow(Row row, Map<String, Integer> headerMap, DataFormatter formatter) {
        DiemThiXetTuyen d = new DiemThiXetTuyen();

        d.setCccd(readHeaderCell(row, headerMap, formatter, "cccd", "cancuoc", "cmnd", "maso"));
        d.setSoBaoDanh(readHeaderCell(row, headerMap, formatter, "sobaodanh", "sbd", "mathisinh"));
        String pt = readHeaderCell(row, headerMap, formatter, "dphuongthuc", "phuongthuc", "ptxt", "chuongtrinhhoc");
        // Nếu file không có cột phương thức (như Ds thi sinh.xlsx) thì mặc định THPT
        d.setPhuongThuc(isBlank(pt) ? "THPT" : pt);

        if (isBlank(safe(d.getCccd()))) {
            d.setCccd(readCell(row, 1, formatter));
        }
        if (isBlank(safe(d.getSoBaoDanh()))) {
            d.setSoBaoDanh(readCell(row, 2, formatter));
        }

        d.setTo(parseDecimal(readHeaderCell(row, headerMap, formatter, "to", "toan")));
        d.setVa(parseDecimal(readHeaderCell(row, headerMap, formatter, "va", "van", "nguvan")));
        d.setLi(parseDecimal(readHeaderCell(row, headerMap, formatter, "li", "ly", "vatly")));
        d.setHo(parseDecimal(readHeaderCell(row, headerMap, formatter, "ho", "hoa")));
        d.setSi(parseDecimal(readHeaderCell(row, headerMap, formatter, "si", "sinh", "sinhhoc")));
        d.setSu(parseDecimal(readHeaderCell(row, headerMap, formatter, "su", "lichsu")));
        d.setDi(parseDecimal(readHeaderCell(row, headerMap, formatter, "di", "dia", "diali")));
        d.setN1Thi(parseDecimal(readHeaderCell(row, headerMap, formatter, "n1thi", "n1", "nn", "ngoaingu", "mamonne")));
        d.setN1Cc(parseDecimal(readHeaderCell(row, headerMap, formatter, "n1cc", "ccnn", "chungchingoaingu")));
        d.setCncn(parseDecimal(readHeaderCell(row, headerMap, formatter, "cncn")));
        d.setCnnn(parseDecimal(readHeaderCell(row, headerMap, formatter, "cnnn")));
        d.setTi(parseDecimal(readHeaderCell(row, headerMap, formatter, "ti", "tin", "tinhoc")));
        d.setGdcd(parseDecimal(readHeaderCell(row, headerMap, formatter, "gdcd", "giaoduccongdan")));
        d.setKtpl(parseDecimal(readHeaderCell(row, headerMap, formatter, "ktpl", "kinhtephapluat")));
        d.setNl1(parseDecimal(readHeaderCell(row, headerMap, formatter, "nl1", "dgnl")));
        d.setNk1(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk1")));
        d.setNk2(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk2")));
        d.setNk3(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk3")));
        d.setNk4(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk4")));
        d.setNk5(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk5")));
        d.setNk6(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk6")));
        d.setNk7(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk7")));
        d.setNk8(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk8")));
        d.setNk9(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk9")));
        d.setNk10(parseDecimal(readHeaderCell(row, headerMap, formatter, "nk10")));
        d.setDiemXetTotNghiep(parseDecimal(readHeaderCell(row, headerMap, formatter, "diemxettotnghiep", "diemxettn")));

        // Fallback theo định dạng file mẫu khi không có header phù hợp.
        if (allSubjectScoresMissing(d)) {
            d.setTo(parseDecimal(readCell(row, 7, formatter)));
            d.setVa(parseDecimal(readCell(row, 8, formatter)));
            d.setLi(parseDecimal(readCell(row, 9, formatter)));
            d.setHo(parseDecimal(readCell(row, 10, formatter)));
            d.setSi(parseDecimal(readCell(row, 11, formatter)));
            d.setSu(parseDecimal(readCell(row, 12, formatter)));
            d.setDi(parseDecimal(readCell(row, 13, formatter)));
            d.setGdcd(parseDecimal(readCell(row, 14, formatter)));
            d.setKtpl(parseDecimal(readCell(row, 17, formatter)));
            d.setTi(parseDecimal(readCell(row, 18, formatter)));
            d.setCncn(parseDecimal(readCell(row, 19, formatter)));
            d.setCnnn(parseDecimal(readCell(row, 20, formatter)));
            d.setN1Thi(parseDecimal(readCell(row, 15, formatter)));
            d.setNk1(parseDecimal(readCell(row, 22, formatter)));
            d.setNk2(parseDecimal(readCell(row, 23, formatter)));
            d.setNk3(parseDecimal(readCell(row, 24, formatter)));
            d.setNk4(parseDecimal(readCell(row, 25, formatter)));
            d.setNk5(parseDecimal(readCell(row, 26, formatter)));
            d.setNk6(parseDecimal(readCell(row, 27, formatter)));
            d.setNk7(parseDecimal(readCell(row, 28, formatter)));
            d.setNk8(parseDecimal(readCell(row, 29, formatter)));
            d.setNk9(parseDecimal(readCell(row, 30, formatter)));
            d.setNk10(parseDecimal(readCell(row, 31, formatter)));
            d.setDiemXetTotNghiep(parseDecimal(readCell(row, 32, formatter)));
        }

        return d;
    }

    private DiemThiXetTuyen parseTextParts(String[] parts) {
        DiemThiXetTuyen d = new DiemThiXetTuyen();

        d.setCccd(getPart(parts, 1));
        d.setSoBaoDanh(getPart(parts, 1));

        d.setTo(parseDecimal(getPart(parts, 7)));
        d.setVa(parseDecimal(getPart(parts, 8)));
        d.setLi(parseDecimal(getPart(parts, 9)));
        d.setHo(parseDecimal(getPart(parts, 10)));
        d.setSi(parseDecimal(getPart(parts, 11)));
        d.setSu(parseDecimal(getPart(parts, 12)));
        d.setDi(parseDecimal(getPart(parts, 13)));
        d.setGdcd(parseDecimal(getPart(parts, 14)));
        d.setKtpl(parseDecimal(getPart(parts, 17)));
        d.setTi(parseDecimal(getPart(parts, 18)));
        d.setCncn(parseDecimal(getPart(parts, 19)));
        d.setCnnn(parseDecimal(getPart(parts, 20)));
        d.setN1Thi(parseDecimal(getPart(parts, 15)));
        d.setNk1(parseDecimal(getPart(parts, 22)));
        d.setNk2(parseDecimal(getPart(parts, 23)));
        d.setNk3(parseDecimal(getPart(parts, 24)));
        d.setNk4(parseDecimal(getPart(parts, 25)));
        d.setNk5(parseDecimal(getPart(parts, 26)));
        d.setNk6(parseDecimal(getPart(parts, 27)));
        d.setNk7(parseDecimal(getPart(parts, 28)));
        d.setNk8(parseDecimal(getPart(parts, 29)));
        d.setNk9(parseDecimal(getPart(parts, 30)));
        d.setNk10(parseDecimal(getPart(parts, 31)));
        d.setDiemXetTotNghiep(parseDecimal(getPart(parts, 32)));

        return d;
    }

    private boolean validateDiemThi(DiemThiXetTuyen diem, boolean checkDuplicate) {
        if (diem == null) {
            lastError = "Dữ liệu điểm thi không hợp lệ!";
            return false;
        }

        String cccd = safe(diem.getCccd());
        String soBaoDanh = safe(diem.getSoBaoDanh());
        String phuongThuc = safe(diem.getPhuongThuc());

        if (cccd.isEmpty()) {
            lastError = "CCCD không được để trống!";
            return false;
        }
        if (!isAcceptedCandidateCode(cccd)) {
            lastError = "CCCD không hợp lệ (cần 12 số hoặc mã TS_xxxx)!";
            return false;
        }

        // Mặc định phương thức nếu bị rỗng
        if (phuongThuc.isEmpty()) {
            diem.setPhuongThuc("THPT");
            phuongThuc = "THPT";
        }

        diem.setCccd(trimMax(cccd, 20));
        diem.setSoBaoDanh(trimMax(soBaoDanh, 45));
        diem.setPhuongThuc(trimMax(phuongThuc.toUpperCase(), 10));
        lastError = "";
        return true;
    }




    private boolean isNullOrZero(BigDecimal value) {
        return value == null || value.compareTo(java.math.BigDecimal.ZERO) == 0;
    }

    private boolean checkScore(BigDecimal score, double min, double max, String label) {
        if (score == null) {
            return true;
        }
        double value = score.doubleValue();
        if (value < min || value > max) {
            lastError = "Điểm " + label + " phải trong khoảng " + min + " - " + max;
            return false;
        }
        return true;
    }

    private boolean allSubjectScoresMissing(DiemThiXetTuyen d) {
        return d.getTo() == null
                && d.getVa() == null
                && d.getLi() == null
                && d.getHo() == null
                && d.getSi() == null
                && d.getSu() == null
                && d.getDi() == null;
    }

    private boolean hasAnyScore(DiemThiXetTuyen d) {
        return d.getTo() != null || d.getLi() != null || d.getHo() != null || d.getSi() != null
                || d.getSu() != null || d.getDi() != null || d.getVa() != null || d.getN1Thi() != null
                || d.getN1Cc() != null || d.getCncn() != null || d.getCnnn() != null || d.getTi() != null
                || d.getGdcd() != null || d.getKtpl() != null || d.getNl1() != null
                || d.getNk1() != null || d.getNk2() != null || d.getNk3() != null || d.getNk4() != null
                || d.getNk5() != null || d.getNk6() != null || d.getNk7() != null || d.getNk8() != null
                || d.getNk9() != null || d.getNk10() != null || d.getDiemXetTotNghiep() != null;
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) {
            return map;
        }

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String text = normalizeHeader(formatter.formatCellValue(headerRow.getCell(i)));
            if (!text.isBlank()) {
                map.put(text, i);
            }
        }
        return map;
    }

    private String readHeaderCell(Row row, Map<String, Integer> headerMap, DataFormatter formatter, String... aliases) {
        for (String alias : aliases) {
            Integer index = headerMap.get(normalizeHeader(alias));
            if (index != null) {
                return readCell(row, index, formatter);
            }
        }
        return "";
    }

    private String normalizeHeader(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.trim().toLowerCase()
                .replace("đ", "d")
                .replace("á", "a").replace("à", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a")
                .replace("ă", "a").replace("ắ", "a").replace("ằ", "a").replace("ẳ", "a").replace("ẵ", "a").replace("ặ", "a")
                .replace("â", "a").replace("ấ", "a").replace("ầ", "a").replace("ẩ", "a").replace("ẫ", "a").replace("ậ", "a")
                .replace("é", "e").replace("è", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e")
                .replace("ê", "e").replace("ế", "e").replace("ề", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
                .replace("í", "i").replace("ì", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i")
                .replace("ó", "o").replace("ò", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o")
                .replace("ô", "o").replace("ố", "o").replace("ồ", "o").replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
                .replace("ơ", "o").replace("ớ", "o").replace("ờ", "o").replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
                .replace("ú", "u").replace("ù", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u")
                .replace("ư", "u").replace("ứ", "u").replace("ừ", "u").replace("ử", "u").replace("ữ", "u").replace("ự", "u")
                .replace("ý", "y").replace("ỳ", "y").replace("ỷ", "y").replace("ỹ", "y").replace("ỵ", "y");

        return normalized.replaceAll("[^a-z0-9]", "");
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        if (row == null || index < 0) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(index)).trim();
    }

    private BigDecimal parseDecimal(String value) {
        String text = safe(value);
        if (text.isEmpty()) {
            return null;
        }

        if (text.contains(",") && !text.contains(".")) {
            text = text.replace(',', '.');
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String getPart(String[] parts, int idx) {
        if (parts == null || idx < 0 || idx >= parts.length) {
            return "";
        }
        return parts[idx] == null ? "" : parts[idx].trim();
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
        return CCCD_PATTERN.matcher(cccdOrCode).matches()
                || DATASET_CODE_PATTERN.matcher(cccdOrCode).matches();
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class ImportRecord {
        private final int lineNo;
        private final DiemThiXetTuyen diem;

        private ImportRecord(int lineNo, DiemThiXetTuyen diem) {
            this.lineNo = lineNo;
            this.diem = diem;
        }
    }
}
