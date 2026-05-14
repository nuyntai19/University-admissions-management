package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JTabbedPane;
import javax.swing.table.DefaultTableModel;

import vn.edu.sgu.phanmemtuyensinh.bus.NguyenVongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.bus.ThiSinhBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;

public class NguyenVongXetTuyenGUI extends JPanel {

    private NguyenVongXetTuyenBUS bus = new NguyenVongXetTuyenBUS();
    private NguyenVongXetTuyenDialog dialog;

    private JTextField txtSearch;
    private JComboBox<String> cboLocPhuongThuc;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnXetTuyen, btnImport, btnBaoCao;

    private JTable table;
    private DefaultTableModel tableModel;

    private int currentPage = 1;
    private int pageSize = 100;
    private int totalRecords = 0;
    private JLabel lblPageInfo;
    private List<NguyenVongXetTuyen> currentDataList;
    private java.util.Map<String, ThiSinh> thiSinhMap;

    public NguyenVongXetTuyenGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        initEvents();
        loadDuLieu();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(10, 6));
        JLabel lblTitle = new JLabel("QUẢN LÝ NGUYỆN VỌNG & XÉT TUYỂN", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(createToolbar(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createToolbar() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new BorderLayout(0, 0));
        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        
        // Loại bỏ các icon unicode để tránh bị lặp với icon của Theme
        btnThem    = btn("Thêm",    new Color(40, 167, 69));
        btnSua     = btn("Sửa",     new Color(255, 152, 0));
        btnXoa     = btn("Xóa",     new Color(220, 53, 69));
        btnImport  = btn("Import",  new Color(0, 123, 255));
        btnLamMoi  = btn("Làm mới", new Color(23, 162, 184));
        btnBaoCao  = btn("Báo cáo", new Color(111, 66, 193));
        
        pnlLeft.add(btnThem); 
        pnlLeft.add(btnSua); 
        pnlLeft.add(btnXoa);
        pnlLeft.add(btnImport); 
        pnlLeft.add(btnLamMoi);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        
        cboLocPhuongThuc = new JComboBox<>(new String[]{"Tất cả", "THPT", "V-SAT", "ĐGNL", "Tuyển thẳng"});
        cboLocPhuongThuc.setPreferredSize(new Dimension(110, 30));
        
        txtSearch = new JTextField(15);
        JButton btnSearch = btn("Tìm", new Color(0, 123, 255));
        
        pnlRight.add(new JLabel("Phương thức:"));
        pnlRight.add(cboLocPhuongThuc);
        pnlRight.add(new JLabel(" Tìm (CCCD/Ngành):"));
        pnlRight.add(txtSearch);
        pnlRight.add(btnSearch);
        
        btnSearch.addActionListener(e -> search());
        txtSearch.addActionListener(e -> search());
        cboLocPhuongThuc.addActionListener(e -> search());

        row1.add(pnlLeft,  BorderLayout.WEST);
        row1.add(pnlRight, BorderLayout.EAST);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        btnXetTuyen = new JButton("Xét tuyển");
        btnXetTuyen.setBackground(new Color(108, 117, 125));
        btnXetTuyen.setForeground(Color.WHITE);
        btnXetTuyen.setFocusPainted(false);
        // Tăng chiều rộng lên 140 cho nút Xét tuyển
        btnXetTuyen.setPreferredSize(new Dimension(140, 30));
        btnXetTuyen.setToolTipText("Chọn dòng -> chỉ tính dòng đó | Không chọn -> tính tất cả");

        JLabel hint = new JLabel("Chọn dòng: tính 1 nguyện vọng  |  Không chọn: tính tất cả");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(120, 120, 120));

        row2.add(btnXetTuyen);
        row2.add(btnBaoCao);
        row2.add(hint);

        wrapper.add(row1);
        wrapper.add(row2);
        return wrapper;
    }

    private JButton btn(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        // Giảm chiều rộng xuống 115 để tránh đè lên ô tìm kiếm
        b.setPreferredSize(new Dimension(115, 32));
        return b;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {
                "ID", "CCCD", "Họ", "Tên", "Nguyện Vọng", "Phương Thức",
                "Mã Ngành", "Tên Ngành", "Tổ Hợp Môn",
                "Điểm THXT", "Điểm UTQD", "Điểm Cộng",
                "Điểm Xét Tuyển", "Kết Quả"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel) {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                if (getParent() instanceof javax.swing.JViewport) {
                    return getPreferredSize().width < getParent().getWidth();
                }
                return super.getScrollableTracksViewportWidth();
            }
        };
        table.setRowHeight(28);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        int[] widths = {50, 110, 150, 100, 90, 110, 90, 240, 90, 90, 90, 90, 110, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(widths[i]);
        }
        
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlPage = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnFirst = new JButton("<<");
        JButton btnPrev  = new JButton("< Trước");
        lblPageInfo      = new JLabel("Trang 1 / 1 (0 dòng)");
        JButton btnNext  = new JButton("Sau >");
        JButton btnLast  = new JButton(">>");

        btnFirst.addActionListener(e -> { currentPage = 1; updateTable(); });
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; updateTable(); } });
        btnNext.addActionListener(e -> {
            int max = (int) Math.ceil((double) totalRecords / pageSize);
            if (currentPage < max) { currentPage++; updateTable(); }
        });
        btnLast.addActionListener(e -> {
            int max = (int) Math.ceil((double) totalRecords / pageSize);
            currentPage = max > 0 ? max : 1; updateTable();
        });

        pnlPage.add(btnFirst); pnlPage.add(btnPrev);
        pnlPage.add(lblPageInfo);
        pnlPage.add(btnNext);  pnlPage.add(btnLast);
        panel.add(pnlPage, BorderLayout.SOUTH);
        return panel;
    }

    private void loadDuLieu() {
        ThiSinhBUS tsBus = new ThiSinhBUS();
        List<ThiSinh> allTs = tsBus.getAll();
        thiSinhMap = new java.util.HashMap<>();
        if (allTs != null) {
            for (ThiSinh ts : allTs) {
                thiSinhMap.put(ts.getCccd(), ts);
            }
        }
        
        search();
    }

    private void search() {
        String kw = txtSearch.getText().trim().toLowerCase();
        String phuongThuc = (String) (cboLocPhuongThuc != null ? cboLocPhuongThuc.getSelectedItem() : "Tất cả");
        
        List<NguyenVongXetTuyen> all = bus.getAll();
        currentDataList = new java.util.ArrayList<>();
        for (NguyenVongXetTuyen nv : all) {
            boolean matchPt = "Tất cả".equals(phuongThuc) || (nv.getTtPhuongThuc() != null && nv.getTtPhuongThuc().equalsIgnoreCase(phuongThuc));
            if (!matchPt) continue;
            
            boolean m1 = nv.getNvCccd()       != null && nv.getNvCccd().toLowerCase().contains(kw);
            boolean m2 = nv.getNvMaNganh()    != null && nv.getNvMaNganh().toLowerCase().contains(kw);
            boolean m3 = nv.getNvTenMaNganh() != null && nv.getNvTenMaNganh().toLowerCase().contains(kw);
            if (m1 || m2 || m3) currentDataList.add(nv);
        }
        totalRecords = currentDataList.size();
        currentPage = 1;
        updateTable();
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        if (totalRecords == 0) {
            lblPageInfo.setText("Trang 1 / 1 (0 dòng)");
            return;
        }
        int max = (int) Math.ceil((double) totalRecords / pageSize);
        if (currentPage > max) currentPage = max;
        if (currentPage < 1)   currentPage = 1;
        int start = (currentPage - 1) * pageSize;
        int end   = Math.min(start + pageSize, totalRecords);
        for (int i = start; i < end; i++) {
            NguyenVongXetTuyen nv = currentDataList.get(i);
            String ho = "";
            String ten = "";
            if (thiSinhMap != null && thiSinhMap.containsKey(nv.getNvCccd())) {
                ThiSinh ts = thiSinhMap.get(nv.getNvCccd());
                ho = ts.getHo() == null ? "" : ts.getHo();
                ten = ts.getTen() == null ? "" : ts.getTen();
            }
            tableModel.addRow(new Object[]{
                    nv.getIdNv(), nv.getNvCccd(), ho, ten, nv.getNvTt(),
                    nv.getTtPhuongThuc(), nv.getNvMaNganh(), nv.getNvTenMaNganh(),
                    nv.getTtThm(),
                    nv.getDiemThxt(), nv.getDiemUtqd(), nv.getDiemCong(),
                    nv.getDiemXetTuyen(), nv.getNvKetQua()
            });
        }
        lblPageInfo.setText(String.format("Trang %d / %d (%d dòng)", currentPage, max, totalRecords));
    }

    private void initEvents() {
        btnThem.addActionListener(e -> themNguyenVong());
        btnSua.addActionListener(e -> suaNguyenVong());
        btnXoa.addActionListener(e -> xoa());
        btnLamMoi.addActionListener(e -> loadDuLieu());
        btnXetTuyen.addActionListener(e -> xetTuyen());
        btnImport.addActionListener(e -> importExcel());
        btnBaoCao.addActionListener(e -> moBaoCaoTrungTuyen());
    }

    private NguyenVongXetTuyenDialog getDialog() {
        if (dialog == null)
            dialog = new NguyenVongXetTuyenDialog((Frame) SwingUtilities.getWindowAncestor(this));
        return dialog;
    }

    private void themNguyenVong() {
        NguyenVongXetTuyenDialog dlg = getDialog();
        dlg.resetForm();
        dlg.setVisible(true);
        if (!dlg.isConfirm()) return;
        NguyenVongXetTuyen nv = new NguyenVongXetTuyen();
        nv.setNvCccd(dlg.getCccd());
        nv.setNvMaNganh(dlg.getMaNganh());
        nv.setNvTenMaNganh(dlg.getTenNganh());
        nv.setNvTt(dlg.getThuTu());
        nv.setTtPhuongThuc(dlg.getPhuongThuc());
        nv.setTtThm(dlg.getToHop());
        if (bus.add(nv)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadDuLieu();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaNguyenVong() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần sửa!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idNv = (int) tableModel.getValueAt(row, 0);
        NguyenVongXetTuyen nv = currentDataList.stream()
                .filter(x -> x.getIdNv() == idNv).findFirst().orElse(null);
        if (nv == null) return;
        NguyenVongXetTuyenDialog dlg = getDialog();
        dlg.resetForm();
        dlg.setData(nv);
        dlg.setVisible(true);
        if (!dlg.isConfirm()) return;
        nv.setNvMaNganh(dlg.getMaNganh());
        nv.setNvTenMaNganh(dlg.getTenNganh());
        nv.setNvTt(dlg.getThuTu());
        nv.setTtPhuongThuc(dlg.getPhuongThuc());
        nv.setTtThm(dlg.getToHop());
        if (bus.update(nv)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDuLieu();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoa() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idNv = (int) tableModel.getValueAt(row, 0);
        String cccd = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa nguyện vọng của CCCD: " + cccd + "?\nHành động này không thể hoàn tác!",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        if (bus.delete(idNv)) {
            JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
            loadDuLieu();
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xetTuyen() {
        int row = table.getSelectedRow();
        if (row != -1) {
            int idNv = (int) tableModel.getValueAt(row, 0);
            String cccd = (String) tableModel.getValueAt(row, 1);
            int updated = bus.runXetTuyenForNv(idNv);
            loadDuLieu();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (idNv == (int) tableModel.getValueAt(i, 0)) {
                    table.setRowSelectionInterval(i, i); break;
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Đã tính lại điểm cho CCCD: " + cccd + "\n(Cập nhật " + updated + " dòng)");
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Không có dòng nào được chọn.\nBạn có muốn chạy Xét Tuyển toàn bộ?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            int updated = bus.runXetTuyenAll();
            loadDuLieu();
            JOptionPane.showMessageDialog(this, "Đã cập nhật điểm cho " + updated + " nguyện vọng.");
        }
    }

    private void importExcel() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = fc.getSelectedFile().getAbsolutePath();
        try {
            int count = bus.importNguyenVongFromExcel(path);
            if (count == 0 && !bus.getLastError().isBlank()) {
                JOptionPane.showMessageDialog(this, "Import thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Import thành công " + count + " dòng!");
            loadDuLieu();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi import: " + ex.getMessage());
        }
    }

    private void moBaoCaoTrungTuyen() {
        List<Object[]> chiTiet = bus.getTrungTuyenChiTiet();
        List<Object[]> tongHop = bus.getThongKeTrungTuyenTheoNganhPhuongThuc();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Báo cáo trúng tuyển", true);
        dialog.setSize(1180, 720);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("BÁO CÁO TRÚNG TUYỂN THEO NGÀNH / PHƯƠNG THỨC", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel pnlSummary = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlSummary.add(createReportChip("Tổng bản ghi trúng tuyển: " + chiTiet.size(), new Color(46, 125, 50)));
        pnlSummary.add(createReportChip("Tổng nhóm ngành/phương thức: " + tongHop.size(), new Color(30, 136, 229)));

        JTabbedPane tabs = new JTabbedPane();
        
        // --- START NEW LOGIC FOR FILTER & EXPORT ---
        JPanel pnlChiTiet = new JPanel(new BorderLayout());
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        java.util.Set<String> nganhSet = new java.util.TreeSet<>();
        for (Object[] row : chiTiet) {
            String maNganh = row[0] != null ? row[0].toString() : "";
            String tenNganh = row[1] != null ? row[1].toString() : "";
            nganhSet.add(maNganh + " - " + tenNganh);
        }
        
        javax.swing.JComboBox<String> cboNganh = new javax.swing.JComboBox<>();
        cboNganh.addItem("Tất cả");
        for (String nganh : nganhSet) {
            cboNganh.addItem(nganh);
        }
        
        JButton btnExportExcel = new JButton("Xuất Excel");
        btnExportExcel.setBackground(new Color(33, 115, 70));
        btnExportExcel.setForeground(Color.WHITE);
        btnExportExcel.setFocusPainted(false);
        
        pnlFilter.add(new JLabel("Lọc theo Ngành:"));
        pnlFilter.add(cboNganh);
        pnlFilter.add(btnExportExcel);
        
        JTable tblChiTiet = createChiTietTable(chiTiet);
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>((DefaultTableModel) tblChiTiet.getModel());
        tblChiTiet.setRowSorter(sorter);
        
        cboNganh.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                String selected = e.getItem().toString();
                if ("Tất cả".equals(selected)) {
                    sorter.setRowFilter(null);
                } else {
                    String maNganh = selected.split(" - ")[0];
                    sorter.setRowFilter(javax.swing.RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(maNganh) + "$", 0));
                }
            }
        });
        
        btnExportExcel.addActionListener(e -> exportTableToExcel(tblChiTiet, "ChiTietTrungTuyen"));
        
        pnlChiTiet.add(pnlFilter, BorderLayout.NORTH);
        pnlChiTiet.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);
        
        tabs.addTab("Chi tiết trúng tuyển", pnlChiTiet);
        // --- END NEW LOGIC ---

        tabs.addTab("Tổng hợp theo ngành/phương thức", new JScrollPane(createTongHopTable(tongHop)));

        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        header.add(lblTitle, BorderLayout.NORTH);
        header.add(pnlSummary, BorderLayout.SOUTH);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(tabs, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JTable createChiTietTable(List<Object[]> data) {
        DefaultTableModel model = new DefaultTableModel(new String[]{
                "Mã ngành", "Tên ngành", "CCCD", "Nguyện vọng", "Phương thức", "Tổ hợp",
                "Điểm THXT", "Điểm cộng", "Điểm UT", "Điểm XT"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Object[] row : data) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        int[] widths = {100, 220, 130, 90, 110, 110, 90, 90, 90, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        return table;
    }

    private JTable createTongHopTable(List<Object[]> data) {
        DefaultTableModel model = new DefaultTableModel(new String[]{
                "Mã ngành", "Tên ngành", "Phương thức", "Số trúng tuyển"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Object[] row : data) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        int[] widths = {100, 260, 120, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        return table;
    }

    private JLabel createReportChip(String text, Color color) {
        JLabel chip = new JLabel(text);
        chip.setOpaque(true);
        chip.setBackground(color);
        chip.setForeground(Color.WHITE);
        chip.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        chip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return chip;
    }

    private void exportTableToExcel(JTable table, String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
        fileChooser.setSelectedFile(new java.io.File(defaultFileName + ".xlsx"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel files", "xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".xlsx")) {
                fileToSave = new java.io.File(fileToSave.getParentFile(), fileToSave.getName() + ".xlsx");
            }

            try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                 java.io.FileOutputStream out = new java.io.FileOutputStream(fileToSave)) {
                
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Data");
                
                org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
                for (int i = 0; i < table.getColumnCount(); i++) {
                    headerRow.createCell(i).setCellValue(table.getColumnName(i));
                }
                
                for (int i = 0; i < table.getRowCount(); i++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        Object val = table.getValueAt(i, j);
                        if (val != null) {
                            if (val instanceof Number) {
                                row.createCell(j).setCellValue(((Number) val).doubleValue());
                            } else {
                                row.createCell(j).setCellValue(val.toString());
                            }
                        }
                    }
                }
                
                workbook.write(out);
                JOptionPane.showMessageDialog(this, "Xuất Excel thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}