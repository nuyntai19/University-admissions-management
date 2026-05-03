package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.BangQuyDoiBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BangQuyDoiGUI extends JPanel {

    private BangQuyDoiBUS bus = new BangQuyDoiBUS();

    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnImport, btnImportText;
    private JComboBox<String> cboLocPhuongThuc;
    private JTextField txtLocKeyword;
    private JButton btnLoc;
    private JTable table;
    private DefaultTableModel tableModel;
    private int currentId = -1;

    private List<BangQuyDoi> allData = new ArrayList<>();
    private List<BangQuyDoi> filteredData = new ArrayList<>();

    public BangQuyDoiGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        loadDuLieu();
    }

    private void initComponents() {
        // --- TIÊU ĐỀ ---
        JLabel lblTitle = new JLabel("QUẢN LÝ BẢNG QUY ĐỔI ĐIỂM", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);

        // --- TOOLBAR: Nút bấm bên trái ---
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
        pnlButtons.setOpaque(false);

        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm Mới");
        btnImport = new JButton("Import V-SAT");
        btnImport.setBackground(new Color(34, 139, 34));
        btnImport.setForeground(Color.WHITE);
        btnImport.setOpaque(true);
        
        btnImportText = new JButton("Import TXT");
        btnImportText.setBackground(new Color(255, 140, 0));
        btnImportText.setForeground(Color.WHITE);
        btnImportText.setOpaque(true);

        pnlButtons.add(btnThem);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnSua);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnXoa);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnLamMoi);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnImport);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnImportText);

        // --- BỘ LỌC bên phải ---
        JPanel pnlLoc = new JPanel();
        pnlLoc.setLayout(new BoxLayout(pnlLoc, BoxLayout.X_AXIS));
        pnlLoc.setOpaque(false);

        JLabel lblPhuongThuc = new JLabel("Phương thức:");
        cboLocPhuongThuc = new JComboBox<>(new String[]{"Tất cả", "V-SAT", "DGNL", "THPT"});
        cboLocPhuongThuc.setPreferredSize(new Dimension(110, 30));
        cboLocPhuongThuc.setMaximumSize(new Dimension(120, 30));

        JLabel lblKeyword = new JLabel("  Tìm kiếm:");
        txtLocKeyword = new JTextField();
        txtLocKeyword.setPreferredSize(new Dimension(160, 30));
        txtLocKeyword.setMaximumSize(new Dimension(180, 30));
        txtLocKeyword.setToolTipText("Nhập Mã quy đổi, Tổ hợp hoặc Môn...");

        btnLoc = new JButton("Lọc");

        pnlLoc.add(lblPhuongThuc);
        pnlLoc.add(Box.createHorizontalStrut(6));
        pnlLoc.add(cboLocPhuongThuc);
        pnlLoc.add(lblKeyword);
        pnlLoc.add(Box.createHorizontalStrut(6));
        pnlLoc.add(txtLocKeyword);
        pnlLoc.add(Box.createHorizontalStrut(8));
        pnlLoc.add(btnLoc);

        // --- PANEL TOP ---
        JPanel pnlActions = new JPanel(new BorderLayout(8, 0));
        pnlActions.setOpaque(false);
        pnlActions.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        pnlActions.add(pnlButtons, BorderLayout.WEST);
        pnlActions.add(pnlLoc, BorderLayout.EAST);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        pnlTop.add(pnlActions, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        // --- BẢNG DỮ LIỆU ---
        String[] columns = {"ID", "Mã Quy Đổi", "Phương Thức", "Tổ Hợp", "Môn", "Phân vị", "a (gốc)", "b (gốc)", "c (QĐ)", "d (QĐ)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        configureColumnWidths();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- PANEL INFO phía dưới ---
        JLabel lblInfo = new JLabel("  💡 Tip: Import V-SAT sẽ tự động nạp toàn bộ bảng quy đổi 8 môn từ file txt vào database.");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(new Color(100, 100, 120));
        add(lblInfo, BorderLayout.SOUTH);

        // --- SỰ KIỆN ---
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                currentId = Integer.parseInt(tableModel.getValueAt(table.getSelectedRow(), 0).toString());
            }
        });

        btnThem.addActionListener(e -> themQuyDoi());
        btnSua.addActionListener(e -> suaQuyDoi());
        btnXoa.addActionListener(e -> xoaQuyDoi());
        btnLamMoi.addActionListener(e -> lamMoi());
        btnImport.addActionListener(e -> importVSAT());
        btnImportText.addActionListener(e -> importDGNLFromText());
        btnLoc.addActionListener(e -> applyFilter());
        cboLocPhuongThuc.addActionListener(e -> applyFilter());
        txtLocKeyword.addActionListener(e -> applyFilter());
    }

    private void loadDuLieu() {
        allData = bus.getAll();
        applyFilter();
    }

    private void applyFilter() {
        String phuongThuc = (String) cboLocPhuongThuc.getSelectedItem();
        String keyword = txtLocKeyword.getText().trim().toUpperCase(Locale.ROOT);

        filteredData = new ArrayList<>();
        for (BangQuyDoi bqd : allData) {
            // Lọc theo phương thức
            if (!"Tất cả".equals(phuongThuc)) {
                String pt = bqd.getDPhuongThuc() == null ? "" : bqd.getDPhuongThuc();
                if (!pt.equalsIgnoreCase(phuongThuc)) continue;
            }
            // Lọc theo keyword
            if (!keyword.isEmpty()) {
                String ma = bqd.getDMaQuyDoi() == null ? "" : bqd.getDMaQuyDoi().toUpperCase(Locale.ROOT);
                String th = bqd.getDToHop() == null ? "" : bqd.getDToHop().toUpperCase(Locale.ROOT);
                String mon = bqd.getDMon() == null ? "" : bqd.getDMon().toUpperCase(Locale.ROOT);
                if (!ma.contains(keyword) && !th.contains(keyword) && !mon.contains(keyword)) continue;
            }
            filteredData.add(bqd);
        }
        renderTable();
    }

    private void renderTable() {
        tableModel.setRowCount(0);
        for (BangQuyDoi bqd : filteredData) {
            tableModel.addRow(new Object[]{
                    bqd.getIdQd(), bqd.getDMaQuyDoi(), bqd.getDPhuongThuc(),
                    bqd.getDToHop(), bqd.getDMon(), bqd.getDPhanVi(),
                    bqd.getDDiemA(), bqd.getDDiemB(), bqd.getDDiemC(), bqd.getDDiemD()
            });
        }
        table.clearSelection();
        currentId = -1;
        // Cập nhật label count
    }

    private void lamMoi() {
        cboLocPhuongThuc.setSelectedIndex(0);
        txtLocKeyword.setText("");
        loadDuLieu();
    }

    // ===== THÊM =====
    private void themQuyDoi() {
        BangQuyDoi bqd = showFormDialog(null);
        if (bqd == null) return;
        if (bus.add(bqd)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadDuLieu();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== SỬA =====
    private void suaQuyDoi() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
            return;
        }
        int row = table.getSelectedRow();
        BangQuyDoi source = new BangQuyDoi();
        source.setIdQd(currentId);
        source.setDMaQuyDoi(str(tableModel.getValueAt(row, 1)));
        source.setDPhuongThuc(str(tableModel.getValueAt(row, 2)));
        source.setDToHop(str(tableModel.getValueAt(row, 3)));
        source.setDMon(str(tableModel.getValueAt(row, 4)));
        source.setDPhanVi(str(tableModel.getValueAt(row, 5)));
        source.setDDiemA(parseBD(str(tableModel.getValueAt(row, 6))));
        source.setDDiemB(parseBD(str(tableModel.getValueAt(row, 7))));
        source.setDDiemC(parseBD(str(tableModel.getValueAt(row, 8))));
        source.setDDiemD(parseBD(str(tableModel.getValueAt(row, 9))));

        BangQuyDoi bqd = showFormDialog(source);
        if (bqd == null) return;
        bqd.setIdQd(currentId);
        if (bus.update(bqd)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDuLieu();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== XÓA =====
    private void xoaQuyDoi() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        if (bus.delete(currentId)) {
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadDuLieu();
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== IMPORT V-SAT =====
    private void importVSAT() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Import sẽ nạp toàn bộ bảng quy đổi V-SAT 8 môn từ file data vào database.\n" +
                "Các bản ghi đã tồn tại sẽ được bỏ qua.\n\nBạn có muốn tiếp tục?",
                "Xác nhận Import V-SAT", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            String summary = bus.importVSATFromDataFile();
            JOptionPane.showMessageDialog(this, summary, "Kết quả Import V-SAT", JOptionPane.INFORMATION_MESSAGE);
            loadDuLieu();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi import: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void importDGNLFromText() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file văn bản bách phân vị (.txt)");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                String summary = bus.importDGNLFromTextFile(filePath);
                JOptionPane.showMessageDialog(this, summary, "Kết quả Import", JOptionPane.INFORMATION_MESSAGE);
                loadDuLieu();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi import: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===== DIALOG FORM =====
    private BangQuyDoi showFormDialog(BangQuyDoi source) {
        JComboBox<String> cboPhuongThuc = new JComboBox<>(new String[]{"V-SAT", "DGNL", "THPT"});
        JTextField txtToHop = createField();
        JTextField txtMon = createField();
        JTextField txtPhanVi = createField();
        JTextField txtA = createField();
        JTextField txtB = createField();
        JTextField txtC = createField();
        JTextField txtD = createField();
        JTextField txtMaQuyDoi = createField();
        txtMaQuyDoi.setEditable(false);
        txtMaQuyDoi.setBackground(new Color(240, 240, 240));

        // Auto-update mã quy đổi preview
        Runnable updateKey = () -> {
            String pt = (String) cboPhuongThuc.getSelectedItem();
            String th = txtToHop.getText().trim().toUpperCase(Locale.ROOT);
            String mon = txtMon.getText().trim().toUpperCase(Locale.ROOT);
            String pv = txtPhanVi.getText().trim().toUpperCase(Locale.ROOT);
            StringBuilder sb = new StringBuilder(pt == null ? "" : pt);
            if (!th.isEmpty()) sb.append("_").append(th);
            if (!mon.isEmpty()) sb.append("_").append(mon);
            if (!pv.isEmpty()) sb.append("_").append(pv);
            txtMaQuyDoi.setText(sb.toString());
        };
        cboPhuongThuc.addActionListener(e -> updateKey.run());
        txtToHop.getDocument().addDocumentListener(SimpleDocumentListener.onChange(updateKey));
        txtMon.getDocument().addDocumentListener(SimpleDocumentListener.onChange(updateKey));
        txtPhanVi.getDocument().addDocumentListener(SimpleDocumentListener.onChange(updateKey));

        if (source != null) {
            cboPhuongThuc.setSelectedItem(source.getDPhuongThuc());
            txtToHop.setText(nvl(source.getDToHop()));
            txtMon.setText(nvl(source.getDMon()));
            txtPhanVi.setText(nvl(source.getDPhanVi()));
            txtA.setText(source.getDDiemA() == null ? "" : source.getDDiemA().toPlainString());
            txtB.setText(source.getDDiemB() == null ? "" : source.getDDiemB().toPlainString());
            txtC.setText(source.getDDiemC() == null ? "" : source.getDDiemC().toPlainString());
            txtD.setText(source.getDDiemD() == null ? "" : source.getDDiemD().toPlainString());
        }
        updateKey.run();

        // Header
        JLabel lblHeading = new JLabel(source == null ? "THÊM BẢN GHI QUY ĐỔI" : "SỬA BẢN GHI QUY ĐỔI");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeading.setForeground(new Color(32, 55, 96));

        JLabel lblHint = new JLabel("V-SAT: nhập Môn (TO/LI/HO...) | DGNL: nhập Tổ hợp (A01/B00...)");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(new Color(100, 115, 140));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 3));
        pnlHeader.setOpaque(false);
        pnlHeader.add(lblHeading, BorderLayout.NORTH);
        pnlHeader.add(lblHint, BorderLayout.SOUTH);

        // Form grid
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 226, 241)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addRow(form, gbc, 0, "Phương thức: *", cboPhuongThuc);
        addRow(form, gbc, 1, "Tổ hợp (nếu có):", txtToHop);
        addRow(form, gbc, 2, "Môn (nếu có):", txtMon);
        addRow(form, gbc, 3, "Phân vị: *", txtPhanVi);

        // Separator line
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(200, 210, 230));
        form.add(sep, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        addRow(form, gbc, 5, "a - điểm gốc (cận dưới): *", txtA);
        addRow(form, gbc, 6, "b - điểm gốc (cận trên): *", txtB);
        addRow(form, gbc, 7, "c - điểm quy đổi (cận dưới): *", txtC);
        addRow(form, gbc, 8, "d - điểm quy đổi (cận trên): *", txtD);
        addRow(form, gbc, 9, "Mã quy đổi (tự sinh):", txtMaQuyDoi);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(pnlHeader, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        ModernTheme.styleDialogContent(content);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, content,
                    source == null ? "Thêm quy đổi" : "Sửa quy đổi",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return null;

            // Validate
            if (txtA.getText().isBlank() || txtB.getText().isBlank() || txtC.getText().isBlank() || txtD.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ 4 mốc điểm a, b, c, d!", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (txtPhanVi.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Phân vị không được để trống!", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            BangQuyDoi bqd = new BangQuyDoi();
            bqd.setDPhuongThuc((String) cboPhuongThuc.getSelectedItem());
            bqd.setDToHop(txtToHop.getText().trim());
            bqd.setDMon(txtMon.getText().trim());
            bqd.setDPhanVi(txtPhanVi.getText().trim());
            bqd.setDDiemA(parseBD(txtA.getText()));
            bqd.setDDiemB(parseBD(txtB.getText()));
            bqd.setDDiemC(parseBD(txtC.getText()));
            bqd.setDDiemD(parseBD(txtD.getText()));
            bqd.setDMaQuyDoi(txtMaQuyDoi.getText().trim());
            return bqd;
        }
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(labelText);
        lbl.setPreferredSize(new Dimension(210, 24));
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        if (comp instanceof JTextField) {
            ((JTextField) comp).setColumns(18);
        }
        form.add(comp, gbc);
    }

    private void configureColumnWidths() {
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(50);   // ID
        tcm.getColumn(1).setPreferredWidth(160);  // Mã quy đổi
        tcm.getColumn(2).setPreferredWidth(90);   // Phương thức
        tcm.getColumn(3).setPreferredWidth(70);   // Tổ hợp
        tcm.getColumn(4).setPreferredWidth(60);   // Môn
        tcm.getColumn(5).setPreferredWidth(70);   // Phân vị
        tcm.getColumn(6).setPreferredWidth(80);   // a
        tcm.getColumn(7).setPreferredWidth(80);   // b
        tcm.getColumn(8).setPreferredWidth(80);   // c
        tcm.getColumn(9).setPreferredWidth(80);   // d
    }

    private JTextField createField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(200, 30));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return tf;
    }

    private BigDecimal parseBD(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return new BigDecimal(s.trim()); } catch (Exception e) { return null; }
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
    private String nvl(String s) { return s == null ? "" : s; }
}
