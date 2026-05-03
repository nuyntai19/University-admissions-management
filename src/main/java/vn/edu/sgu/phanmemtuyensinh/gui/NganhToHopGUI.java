package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.NganhBUS;
import vn.edu.sgu.phanmemtuyensinh.bus.NganhToHopBUS;
import vn.edu.sgu.phanmemtuyensinh.bus.ToHopMonBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NganhToHop;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.Nganh;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ToHopMon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NganhToHopGUI extends JPanel {

    private NganhToHopBUS bus = new NganhToHopBUS();
    private NganhBUS nganhBUS = new NganhBUS();
    private ToHopMonBUS toHopMonBUS = new ToHopMonBUS();

    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnImport;
    private JTextField txtBoLoc;
    private JButton btnLoc;
    private JTable table;
    private DefaultTableModel tableModel;
    private int currentId = -1;

    // Pagination
    private List<NganhToHop> allData = new ArrayList<>();
    private List<NganhToHop> filteredData = new ArrayList<>();
    private int currentPage = 1;
    private int pageSize = 50;
    private int totalPages = 1;
    
    private JButton btnFirst, btnPrev, btnNext, btnLast;
    private JLabel lblPageInfo;

    public NganhToHopGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        loadDuLieu();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("QUẢN LÝ NGÀNH - TỔ HỢP MÔN", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);

        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm Mới");
        btnImport = new JButton("Import Excel");
        btnImport.setBackground(new Color(34, 139, 34));
        btnImport.setForeground(Color.WHITE);

        pnlButtons.add(btnThem);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnSua);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnXoa);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnLamMoi);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnImport);

        // Bộ lọc
        JPanel pnlLoc = new JPanel();
        pnlLoc.setLayout(new BoxLayout(pnlLoc, BoxLayout.X_AXIS));
        JLabel lblBoLoc = new JLabel("Bộ lọc:");
        txtBoLoc = new JTextField();
        txtBoLoc.setPreferredSize(new Dimension(200, 30));
        txtBoLoc.setMaximumSize(new Dimension(220, 30));
        txtBoLoc.setToolTipText("Nhập Mã ngành hoặc Mã tổ hợp...");
        btnLoc = new JButton("Lọc");

        pnlLoc.add(lblBoLoc);
        pnlLoc.add(Box.createHorizontalStrut(8));
        pnlLoc.add(txtBoLoc);
        pnlLoc.add(Box.createHorizontalStrut(8));
        pnlLoc.add(btnLoc);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        JPanel pnlActions = new JPanel(new BorderLayout());
        pnlActions.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        pnlActions.add(pnlButtons, BorderLayout.WEST);
        pnlActions.add(pnlLoc, BorderLayout.EAST);
        pnlTop.add(pnlActions, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        String[] columns = { "ID", "Mã Ngành", "Mã Tổ Hợp", "Môn 1", "HS1", "Môn 2", "HS2", "Môn 3", "HS3", "Độ lệch" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(50);
        tcm.getColumn(1).setPreferredWidth(100);
        tcm.getColumn(2).setPreferredWidth(100);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Pagination Panel ---
        JPanel pnlPagination = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnFirst = new JButton("|<");
        btnPrev = new JButton("<");
        lblPageInfo = new JLabel("Trang 1 / 1");
        btnNext = new JButton(">");
        btnLast = new JButton(">|");
        
        pnlPagination.add(btnFirst);
        pnlPagination.add(btnPrev);
        pnlPagination.add(Box.createHorizontalStrut(10));
        pnlPagination.add(lblPageInfo);
        pnlPagination.add(Box.createHorizontalStrut(10));
        pnlPagination.add(btnNext);
        pnlPagination.add(btnLast);
        
        add(pnlPagination, BorderLayout.SOUTH);

        // --- Bắt sự kiện ---
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                currentId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            }
        });

        btnThem.addActionListener(e -> themNganhToHop());
        btnSua.addActionListener(e -> suaNganhToHop());
        btnXoa.addActionListener(e -> xoaNganhToHop());
        btnLamMoi.addActionListener(e -> lamMoi());
        btnImport.addActionListener(e -> importExcel());
        
        btnLoc.addActionListener(e -> applyFilter());
        txtBoLoc.addActionListener(e -> applyFilter());

        btnFirst.addActionListener(e -> changePage(1));
        btnPrev.addActionListener(e -> changePage(currentPage - 1));
        btnNext.addActionListener(e -> changePage(currentPage + 1));
        btnLast.addActionListener(e -> changePage(totalPages));
    }

    private void loadDuLieu() {
        allData = bus.getAll();
        applyFilter();
    }
    
    private void applyFilter() {
        String keyword = txtBoLoc.getText().trim().toUpperCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredData = new ArrayList<>(allData);
        } else {
            filteredData = new ArrayList<>();
            for (NganhToHop nth : allData) {
                String mn = nth.getMaNganh() == null ? "" : nth.getMaNganh().toUpperCase(Locale.ROOT);
                String mt = nth.getMaToHop() == null ? "" : nth.getMaToHop().toUpperCase(Locale.ROOT);
                if (mn.contains(keyword) || mt.contains(keyword)) {
                    filteredData.add(nth);
                }
            }
        }
        currentPage = 1;
        updatePagination();
    }
    
    private void changePage(int page) {
        if (page >= 1 && page <= totalPages) {
            currentPage = page;
            updatePagination();
        }
    }
    
    private void updatePagination() {
        if (filteredData == null) {
            filteredData = new ArrayList<>();
        }
        int totalRows = filteredData.size();
        totalPages = (int) Math.ceil((double) totalRows / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        
        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages + " (Tổng: " + totalRows + ")");
        
        btnFirst.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        btnLast.setEnabled(currentPage < totalPages);
        
        renderTableData();
    }
    
    private void renderTableData() {
        tableModel.setRowCount(0);
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filteredData.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            NganhToHop nth = filteredData.get(i);
            tableModel.addRow(new Object[] {
                    nth.getId(),
                    nth.getMaNganh(),
                    nth.getMaToHop(),
                    nth.getThMon1(),
                    nth.getHsMon1(),
                    nth.getThMon2(),
                    nth.getHsMon2(),
                    nth.getThMon3(),
                    nth.getHsMon3(),
                    nth.getDoLech()
            });
        }
        table.clearSelection();
        currentId = -1;
    }

    private void themNganhToHop() {
        NganhToHop nth = showNganhToHopFormDialog(null);
        if (nth != null) {
            if (bus.add(nth)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                loadDuLieu();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void suaNganhToHop() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) return;

        NganhToHop source = new NganhToHop();
        source.setId(currentId);
        source.setMaNganh(String.valueOf(tableModel.getValueAt(row, 1)));
        source.setMaToHop(String.valueOf(tableModel.getValueAt(row, 2)));
        source.setHsMon1(parseIntOrNull(String.valueOf(tableModel.getValueAt(row, 4))));
        source.setHsMon2(parseIntOrNull(String.valueOf(tableModel.getValueAt(row, 6))));
        source.setHsMon3(parseIntOrNull(String.valueOf(tableModel.getValueAt(row, 8))));

        NganhToHop nth = showNganhToHopFormDialog(source);
        if (nth != null) {
            nth.setId(currentId);
            if (bus.update(nth)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadDuLieu();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xoaNganhToHop() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (bus.delete(currentId)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadDuLieu();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void lamMoi() {
        txtBoLoc.setText("");
        loadDuLieu();
    }

    private void importExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel", "xlsx"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                bus.importAndSaveToDatabase(filePath);
                JOptionPane.showMessageDialog(this, bus.getLastImportSummary(), "Kết quả import", JOptionPane.INFORMATION_MESSAGE);
                loadDuLieu();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi import: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private NganhToHop showNganhToHopFormDialog(NganhToHop source) {
        JComboBox<String> cboMaNganh = new JComboBox<>();
        List<Nganh> nganhList = nganhBUS.getAll();
        for (Nganh n : nganhList) {
            cboMaNganh.addItem(n.getMaNganh());
        }

        JComboBox<String> cboMaToHop = new JComboBox<>();
        List<ToHopMon> thList = toHopMonBUS.getAll();
        for (ToHopMon th : thList) {
            cboMaToHop.addItem(th.getMaToHop());
        }

        JTextField txtHs1 = createDialogTextField();
        JTextField txtHs2 = createDialogTextField();
        JTextField txtHs3 = createDialogTextField();
        JTextField txtDoLech = createDialogTextField();
        txtDoLech.setEditable(false);
        txtDoLech.setBackground(new Color(240, 240, 240));

        JLabel lblMon1 = new JLabel("Hệ số môn 1:");
        JLabel lblMon2 = new JLabel("Hệ số môn 2:");
        JLabel lblMon3 = new JLabel("Hệ số môn 3:");

        Runnable updateMonLabels = () -> {
            String maToHop = (String) cboMaToHop.getSelectedItem();
            ToHopMon th = toHopMonBUS.getByMaToHop(maToHop);
            if (th != null) {
                lblMon1.setText("Hệ số môn 1 (" + th.getMon1() + "):");
                lblMon2.setText("Hệ số môn 2 (" + th.getMon2() + "):");
                lblMon3.setText("Hệ số môn 3 (" + th.getMon3() + "):");
            } else {
                lblMon1.setText("Hệ số môn 1:");
                lblMon2.setText("Hệ số môn 2:");
                lblMon3.setText("Hệ số môn 3:");
            }
            
            String maNganh = (String) cboMaNganh.getSelectedItem();
            BigDecimal doLech = bus.previewDoLech(maNganh, maToHop);
            txtDoLech.setText(doLech == null ? "0" : doLech.toPlainString());
        };

        cboMaToHop.addActionListener(e -> updateMonLabels.run());
        cboMaNganh.addActionListener(e -> updateMonLabels.run());

        if (source != null) {
            cboMaNganh.setSelectedItem(source.getMaNganh());
            cboMaToHop.setSelectedItem(source.getMaToHop());
            txtHs1.setText(source.getHsMon1() != null ? source.getHsMon1().toString() : "1");
            txtHs2.setText(source.getHsMon2() != null ? source.getHsMon2().toString() : "1");
            txtHs3.setText(source.getHsMon3() != null ? source.getHsMon3().toString() : "1");
        } else {
            txtHs1.setText("1");
            txtHs2.setText("1");
            txtHs3.setText("1");
        }
        
        updateMonLabels.run();

        JLabel lblHeading = new JLabel(source == null ? "THÊM NGÀNH - TỔ HỢP" : "SỬA NGÀNH - TỔ HỢP");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblHeading.setForeground(new Color(32, 55, 96));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 4));
        pnlHeader.setOpaque(false);
        pnlHeader.add(lblHeading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 226, 241)),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(form, gbc, 0, new JLabel("Mã ngành:"), cboMaNganh);
        addFormRow(form, gbc, 1, new JLabel("Mã tổ hợp:"), cboMaToHop);
        addFormRow(form, gbc, 2, lblMon1, txtHs1);
        addFormRow(form, gbc, 3, lblMon2, txtHs2);
        addFormRow(form, gbc, 4, lblMon3, txtHs3);
        addFormRow(form, gbc, 5, new JLabel("Độ lệch:"), txtDoLech);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(pnlHeader, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        ModernTheme.styleDialogContent(content);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    content,
                    source == null ? "Thêm Ngành - Tổ hợp" : "Sửa Ngành - Tổ hợp",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            NganhToHop nth = new NganhToHop();
            String selectedMaNganh = (String) cboMaNganh.getSelectedItem();
            String selectedMaToHop = (String) cboMaToHop.getSelectedItem();
            
            nth.setMaNganh(selectedMaNganh);
            nth.setMaToHop(selectedMaToHop);
            nth.setHsMon1(parseIntOrNull(txtHs1.getText()));
            nth.setHsMon2(parseIntOrNull(txtHs2.getText()));
            nth.setHsMon3(parseIntOrNull(txtHs3.getText()));

            // Bổ sung: Gán Tên ngành và Tên tổ hợp để tránh lỗi hiển thị null
            Nganh n = nganhBUS.getByMaNganh(selectedMaNganh);
            if (n != null) nth.setTenNganhChuan(n.getTenNganh());
            
            ToHopMon th = toHopMonBUS.getByMaToHop(selectedMaToHop);
            if (th != null) nth.setTenToHop(th.getTenToHop());

            return nth;
        }
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, JLabel lbl, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        lbl.setPreferredSize(new Dimension(130, 20));
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        if (component instanceof JTextField) {
            ((JTextField) component).setColumns(15);
        }
        form.add(component, gbc);
    }

    private JTextField createDialogTextField() {
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(150, 30));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return textField;
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
