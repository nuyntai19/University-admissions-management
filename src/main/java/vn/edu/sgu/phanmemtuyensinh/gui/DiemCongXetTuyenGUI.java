package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.DiemCongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.bus.NguyenVongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.List;

public class DiemCongXetTuyenGUI extends JPanel {
    private DiemCongXetTuyenBUS bus = new DiemCongXetTuyenBUS();
    private NguyenVongXetTuyenBUS nvBus = new NguyenVongXetTuyenBUS();
    private JTextField txtSearch;
    private JButton btnSearch;
    private JTable table;
    private DefaultTableModel model;
    private int selectedId = -1;

    // Pagination variables
    private int currentPage = 1;
    private int pageSize = 50;
    private long totalRecords = 0;
    private JLabel lblPageInfo;

    public DiemCongXetTuyenGUI() {
        setLayout(new BorderLayout(10, 10));
        initComponents();
        loadData();
    }

    private void initComponents() {
        // Buttons
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnAdd = createButton("Thêm", new Color(40, 167, 69));
        JButton btnUpdate = createButton("Sửa", new Color(255, 193, 7));
        btnUpdate.setForeground(Color.BLACK);
        JButton btnDelete = createButton("Xóa", new Color(220, 53, 69));
        JButton btnReset = createButton("Làm mới", new Color(108, 117, 125));

        JButton btnImportUuTien = createButton("Import Ưu Tiên", new Color(0, 120, 200));
        JButton btnImportTiengAnh = createButton("Import Tiếng Anh", new Color(255, 165, 0));
        
        btnAdd.addActionListener(e -> saveAction());
        btnDelete.addActionListener(e -> deleteAction());
        btnUpdate.addActionListener(e -> updateAction());
        btnReset.addActionListener(e -> resetForm());
        btnImportUuTien.addActionListener(e -> importUuTien());
        btnImportTiengAnh.addActionListener(e -> importTiengAnh());

        pnlBtns.add(btnAdd);
        pnlBtns.add(btnUpdate);
        pnlBtns.add(btnDelete);
        pnlBtns.add(btnReset);
        pnlBtns.add(btnImportUuTien);
        pnlBtns.add(btnImportTiengAnh);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearch.add(new JLabel("Tìm kiếm (CCCD/Chứng chỉ): "));
        txtSearch = new JTextField(20);
        btnSearch = createButton("Tìm", new Color(23, 162, 184));
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBorder(BorderFactory.createTitledBorder("Bảng Điều Khiển"));
        pnlTop.add(pnlSearch, BorderLayout.NORTH);
        pnlTop.add(pnlBtns, BorderLayout.CENTER);

        // Table - hiển thị ĐẦY ĐỦ tất cả các cột quan trọng
        model = new DefaultTableModel(new String[]{
            "ID",                  // iddiemcong
            "CCCD",                // ts_cccd
            // --- Nhóm Chứng chỉ ngoại ngữ (từ file Ds quy doi tieng Anh) ---
            "Chứng chỉ",          // chung_chi_ngoai_ngu
            "Điểm/Bậc CC",        // diem_chung_chi
            "Điểm quy đổi",       // diem_quy_doi_chung_chi
            "Điểm cộng CC",       // diemCC
            "Có C/C",             // co_chung_chi
            // --- Nhóm Giải thưởng (từ file Uu tien xet tuyen) ---
            "Cấp giải",           // cap_giai
            "Đối tượng",          // doi_tuong_giai
            "Mã môn giải",        // ma_mon_giai
            "Loại giải",          // loai_giai
            "Điểm cộng môn",      // diem_cong_mon_giai
            "Điểm cộng ko môn",   // diem_cong_khong_mon
            // --- Tổng hợp ---
            "Điểm UT",            // diemUtxt
            "Tổng kết"            // diemTong
        }, 0);
        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Đặt chiều rộng cột hợp lý
        int[] colWidths = {
            40,   // ID
            120,  // CCCD
            130,  // Chứng chỉ
            100,  // Điểm/Bậc CC
            90,   // Điểm quy đổi
            100,  // Điểm cộng CC
            55,   // Có C/C
            85,   // Cấp giải
            110,  // Đối tượng
            80,   // Mã môn giải
            130,  // Loại giải
            110,  // Điểm cộng môn
            120,  // Điểm cộng ko môn
            70,   // Điểm UT
            70    // Tổng kết
        };
        for (int i = 0; i < colWidths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        add(pnlTop, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // Pagination Panel
        JPanel pnlPagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnFirst = new JButton("<<");
        JButton btnPrev = new JButton("< Trước");
        lblPageInfo = new JLabel("Trang 1 / 1 (0 dòng)");
        JButton btnNext = new JButton("Sau >");
        JButton btnLast = new JButton(">>");

        btnFirst.addActionListener(e -> { currentPage = 1; loadData(); });
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { 
            int maxPage = (int) Math.ceil((double) totalRecords / pageSize);
            if (currentPage < maxPage) { currentPage++; loadData(); } 
        });
        btnLast.addActionListener(e -> { 
            int maxPage = (int) Math.ceil((double) totalRecords / pageSize);
            currentPage = maxPage > 0 ? maxPage : 1; 
            loadData(); 
        });

        pnlPagination.add(btnFirst);
        pnlPagination.add(btnPrev);
        pnlPagination.add(lblPageInfo);
        pnlPagination.add(btnNext);
        pnlPagination.add(btnLast);
        
        add(pnlPagination, BorderLayout.SOUTH);
        
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fillFormFromSelectedRow();
            }
        });
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    private void saveAction() {
        DiemCongXetTuyenDialog dialog = new DiemCongXetTuyenDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirm()) {
            DiemCongXetTuyen d = dialog.getData();
            bus.tinhToanDiemCongVaUuTien(d, dialog.getChungChiStr(), dialog.getMucDatDuocStr(), 
                                          dialog.getGiaiThuongStr(), dialog.getKhuVucStr(), 
                                          dialog.getDoiTuongStr(), new BigDecimal("24.0"));
            if (bus.save(d)) {
                loadData();
                // Tự động tính lại nguyện vọng nếu CCCD này đã có
                int nvUpdated = nvBus.runXetTuyenForCccd(d.getTsCccd());
                if (nvUpdated > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Thêm thành công!\nĐã tự động tính lại điểm cho " + nvUpdated + " nguyện vọng của CCCD: " + d.getTsCccd());
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm thành công!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row != -1) {
            selectedId = (int) model.getValueAt(row, 0);
        }
    }

    private void updateAction() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
            return;
        }

        DiemCongXetTuyen initData = bus.getById(selectedId);
        if (initData == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu trong Database!");
            return;
        }

        DiemCongXetTuyenDialog dialog = new DiemCongXetTuyenDialog((Frame) SwingUtilities.getWindowAncestor(this), initData);
        dialog.setVisible(true);

        if (dialog.isConfirm()) {
            DiemCongXetTuyen d = dialog.getData();
            bus.tinhToanDiemCongVaUuTien(d, dialog.getChungChiStr(), dialog.getMucDatDuocStr(), 
                                          dialog.getGiaiThuongStr(), dialog.getKhuVucStr(), 
                                          dialog.getDoiTuongStr(), new BigDecimal("24.0"));
            if (bus.update(d)) {
                loadData();
                // Tự động tính lại nguyện vọng nếu CCCD này đã có
                int nvUpdated = nvBus.runXetTuyenForCccd(d.getTsCccd());
                if (nvUpdated > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Sửa thành công!\nĐã tự động tính lại điểm cho " + nvUpdated + " nguyện vọng của CCCD: " + d.getTsCccd());
                } else {
                    JOptionPane.showMessageDialog(this, "Sửa thành công!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sửa thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteAction() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bản ghi này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (bus.delete(selectedId)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetForm() {
        selectedId = -1;
        if (txtSearch != null) txtSearch.setText("");
        table.clearSelection();
        loadData();
    }

    private void loadData() {
        if (model == null || lblPageInfo == null) return;
        model.setRowCount(0);
        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";
        totalRecords = bus.countAll(keyword);
        int maxPage = (int) Math.ceil((double) totalRecords / pageSize);
        if (maxPage == 0) maxPage = 1;
        if (currentPage > maxPage) currentPage = maxPage;
        
        lblPageInfo.setText("Trang " + currentPage + " / " + maxPage + " (" + totalRecords + " dòng)");

        List<DiemCongXetTuyen> list = bus.getPage(keyword, (currentPage - 1) * pageSize, pageSize);
        if (list != null) {
            for (DiemCongXetTuyen d : list) {
                model.addRow(new Object[]{
                    d.getIdDiemCong(),
                    d.getTsCccd(),
                    // Nhóm Chứng chỉ
                    safe(d.getChungChi()),
                    safe(d.getMucDatDuoc()),
                    d.getDiemQuyDoiChungChi(),
                    d.getDiemCC(),
                    d.getCoChungChi() != null && d.getCoChungChi() ? "Có" : "",
                    // Nhóm Giải thưởng
                    safe(d.getCapGiai()),
                    safe(d.getDoiTuongGiai()),
                    safe(d.getMaMonGiai()),
                    safe(d.getLoaiGiai()),
                    d.getDiemCongMonGiai(),
                    d.getDiemCongKhongMon(),
                    // Tổng hợp
                    d.getDiemUtxt(),
                    d.getDiemTong()
                });
            }
        }
    }

    private String safe(String s) { return s != null ? s : ""; }

    private void addCtrl(JPanel p, String l, JComponent c, int x, int y, GridBagConstraints g) {
        g.gridx = x; g.gridy = y; p.add(new JLabel(l), g);
        g.gridx = x+1; p.add(c, g);
    }

    // ===== IMPORT ƯU TIÊN (file Uu tien xet tuyen.xlsx) =====
    private void importUuTien() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            try {
                int[] res = bus.importFromExcel(path);
                if (res[0] == 0 && res[1] == 0 && !bus.getLastError().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Lỗi import: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            String.format("Import Điểm Ưu tiên hoàn tất!\n✅ Thành công: %d\n❌ Thất bại: %d\n(Các CCCD lỗi hoặc trùng đã bị bỏ qua)", res[0], res[1]));
                    loadData();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===== IMPORT TIẾNG ANH (file Ds quy doi tieng Anh.xlsx) =====
    private void importTiengAnh() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            try {
                String result = new vn.edu.sgu.phanmemtuyensinh.bus.BangQuyDoiBUS().importTiengAnhFromExcel(path);
                if (result.startsWith("Lỗi") || result.startsWith("Không tìm thấy")) {
                    JOptionPane.showMessageDialog(this, result, "Lỗi Import", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, result, "Kết quả Import", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}