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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import vn.edu.sgu.phanmemtuyensinh.bus.NguyenVongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;

public class NguyenVongXetTuyenGUI extends JPanel {

    private NguyenVongXetTuyenBUS bus = new NguyenVongXetTuyenBUS();
    private NguyenVongXetTuyenDialog dialog;

    private JTextField txtSearch;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnXetTuyen, btnImport;

    private JTable table;
    private DefaultTableModel tableModel;

    private int currentPage = 1;
    private int pageSize = 100;
    private int totalRecords = 0;
    private JLabel lblPageInfo;
    private List<NguyenVongXetTuyen> currentDataList;

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
        
        pnlLeft.add(btnThem); 
        pnlLeft.add(btnSua); 
        pnlLeft.add(btnXoa);
        pnlLeft.add(btnImport); 
        pnlLeft.add(btnLamMoi);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        txtSearch = new JTextField(15);
        JButton btnSearch = btn("Tìm", new Color(0, 123, 255));
        pnlRight.add(new JLabel("Tìm (CCCD / Ngành):"));
        pnlRight.add(txtSearch);
        pnlRight.add(btnSearch);
        btnSearch.addActionListener(e -> search());
        txtSearch.addActionListener(e -> search());

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
                "ID", "CCCD", "Nguyện Vọng", "Phương Thức",
                "Mã Ngành", "Tên Ngành", "Tổ Hợp Môn",
                "Điểm THXT", "Điểm UTQD", "Điểm Cộng",
                "Điểm Xét Tuyển", "Kết Quả"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {50, 130, 100, 120, 110, 260, 100, 100, 100, 100, 120, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

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
        currentDataList = bus.getAll();
        totalRecords = currentDataList.size();
        currentPage = 1;
        updateTable();
    }

    private void search() {
        String kw = txtSearch.getText().trim().toLowerCase();
        List<NguyenVongXetTuyen> all = bus.getAll();
        currentDataList = new java.util.ArrayList<>();
        for (NguyenVongXetTuyen nv : all) {
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
            tableModel.addRow(new Object[]{
                    nv.getIdNv(), nv.getNvCccd(), nv.getNvTt(),
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
}