package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.NguyenVongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NguyenVongXetTuyenGUI extends JPanel {

    private NguyenVongXetTuyenBUS bus = new NguyenVongXetTuyenBUS();
    private NguyenVongXetTuyenDialog dialog;

    private JTextField txtSearch;

    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnXetTuyen, btnImport;

    private JTable table;
    private DefaultTableModel tableModel;

    public NguyenVongXetTuyenGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        initEvents();
        loadDuLieu();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ NGUYỆN VỌNG & XÉT TUYỂN", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(createToolbar(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createToolbar() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        btnThem = createButton("Thêm", new Color(40, 167, 69));
        btnSua = createButton("Sửa", new Color(255, 193, 7));
        btnXoa = createButton("Xóa", new Color(220, 53, 69));
        btnImport = createButton("Import", new Color(0, 123, 255));
        btnLamMoi = createButton("Làm mới", new Color(23, 162, 184));
        btnXetTuyen = createButton("Xét tuyển / Cập nhật điểm", new Color(108, 117, 125));

        pnlLeft.add(btnThem);
        pnlLeft.add(btnSua);
        pnlLeft.add(btnXoa);
        pnlLeft.add(btnImport);
        pnlLeft.add(btnLamMoi);
        pnlLeft.add(btnXetTuyen);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        txtSearch = new JTextField(18);
        JButton btnSearch = new JButton("Tìm");

        pnlRight.add(new JLabel("Tìm CCCD / Số báo danh:"));
        pnlRight.add(txtSearch);
        pnlRight.add(btnSearch);

        btnSearch.addActionListener(e -> search());
        txtSearch.addActionListener(e -> search());

        panel.add(pnlLeft, BorderLayout.WEST);
        panel.add(pnlRight, BorderLayout.EAST);

        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private JScrollPane createTablePanel() {

        String[] columns = {
                "ID", "CCCD", "Số Báo Danh", "Nguyện Vọng", "Phương Thức",
                "Mã Ngành", "Tên Ngành",
                "Điểm THXT", "Điểm UTQD", "Điểm Cộng",
                "Điểm Xét Tuyển", "Kết Quả"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {50,130,130,130,180,120,280,130,130,130,130,130};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        return scrollPane;
    }

    private void loadDuLieu() {
        tableModel.setRowCount(0);

        List<NguyenVongXetTuyen> list = bus.getAll();

        for (NguyenVongXetTuyen nv : list) {
            tableModel.addRow(new Object[]{
                    nv.getIdNv(),
                    nv.getNvCccd(),
                    nv.getNvSoBaoDanh(),
                    nv.getNvTt(),
                    nv.getTtPhuongThuc(),
                    nv.getNvMaNganh(),
                    nv.getNvTenMaNganh(),
                    nv.getDiemThxt(),
                    nv.getDiemUtqd(),
                    nv.getDiemCong(),
                    nv.getDiemXetTuyen(),
                    nv.getNvKetQua()
            });
        }
    }

    private void search() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        for (NguyenVongXetTuyen nv : bus.getAll()) {
            if (nv.getNvCccd().toLowerCase().contains(keyword)
//                    || nv.getTenNganh().toLowerCase().contains(keyword)
                    ) {

                tableModel.addRow(new Object[]{
                        nv.getIdNv(),
                        nv.getNvCccd(),
                        nv.getNvSoBaoDanh(),
                        nv.getNvTt(),
                        nv.getTtPhuongThuc(),
                        nv.getNvMaNganh(),
                        nv.getNvTenMaNganh(),
                        nv.getDiemThxt(),
                        nv.getDiemUtqd(),
                        nv.getDiemCong(),
                        nv.getDiemXetTuyen(),
                        nv.getNvKetQua()
                });
            }
        }
    }

    private void initEvents() {

        btnThem.addActionListener(e -> themNguyenVong());

        btnXoa.addActionListener(e -> xoa());
        btnLamMoi.addActionListener(e -> loadDuLieu());
        btnXetTuyen.addActionListener(e -> xetTuyen());
        btnImport.addActionListener(e -> importExcel());
    }

    private void themNguyenVong() {

        if (dialog == null) {
            dialog = new NguyenVongXetTuyenDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this)
            );
        }

        dialog.setVisible(true);

        if (!dialog.isConfirm()) return;

        NguyenVongXetTuyen nv = new NguyenVongXetTuyen();

        nv.setNvCccd(dialog.getCccd().split(" - ")[0]);
        nv.setNvMaNganh(dialog.getMaNganh().split(" - ")[0]);
        nv.setNvTt(dialog.getThuTu());

        if (bus.add(nv)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadDuLieu();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoa() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableModel.getValueAt(row, 0);

        if (bus.delete(id)) {
            JOptionPane.showMessageDialog(this, "Đã xóa!");
            loadDuLieu();
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xetTuyen() {
        loadDuLieu();
        JOptionPane.showMessageDialog(this, "Đã chạy xét tuyển / cập nhật điểm!");
    }

    private void importExcel() {
        JFileChooser fileChooser = new JFileChooser();

        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String filePath = fileChooser.getSelectedFile().getAbsolutePath();

        try {
            int count = bus.importNguyenVongFromExcel(filePath);

            if (count == 0 && !bus.getLastError().isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Import thất bại: " + bus.getLastError(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Import thành công " + count + " dòng!");

            loadDuLieu(); // reload table

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi import: " + ex.getMessage());
        }
    }
}