package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.BangQuyDoiBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.List;

public class BangQuyDoiGUI extends JPanel {

    private BangQuyDoiBUS bus = new BangQuyDoiBUS();
    private JComboBox<String> cboPhuongThuc;
    private JTextField txtMaQuyDoi, txtToHop, txtMon, txtPhanVi, txtA, txtB, txtC, txtD;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;
    private int currentId = -1;

    public BangQuyDoiGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ BẢNG QUY ĐỔI", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);

        JPanel pnlInput = new JPanel(new GridLayout(4, 6, 10, 10));
        pnlInput.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        pnlInput.add(new JLabel("Phương thức:"));
        cboPhuongThuc = new JComboBox<>(new String[] { "DGNL", "V-SAT", "THPT" });
        pnlInput.add(cboPhuongThuc);
        pnlInput.add(new JLabel("Tổ hợp (nếu có):"));
        txtToHop = new JTextField();
        pnlInput.add(txtToHop);
        pnlInput.add(new JLabel("Môn (nếu có):"));
        txtMon = new JTextField();
        pnlInput.add(txtMon);

        pnlInput.add(new JLabel("Phân vị:"));
        txtPhanVi = new JTextField();
        pnlInput.add(txtPhanVi);
        pnlInput.add(new JLabel("a (điểm gốc):"));
        txtA = new JTextField();
        pnlInput.add(txtA);
        pnlInput.add(new JLabel("b (điểm gốc):"));
        txtB = new JTextField();
        pnlInput.add(txtB);

        pnlInput.add(new JLabel("c (điểm quy đổi):"));
        txtC = new JTextField();
        pnlInput.add(txtC);
        pnlInput.add(new JLabel("d (điểm quy đổi):"));
        txtD = new JTextField();
        pnlInput.add(txtD);
        pnlInput.add(new JLabel("Mã quy đổi (tự sinh):"));
        txtMaQuyDoi = new JTextField();
        txtMaQuyDoi.setEditable(false);
        pnlInput.add(txtMaQuyDoi);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm Mới");
        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        pnlTop.add(pnlInput, BorderLayout.CENTER);
        pnlTop.add(pnlButtons, BorderLayout.SOUTH);
        add(pnlTop, BorderLayout.NORTH);

        String[] columns = { "ID", "Mã Quy Đổi", "Phương Thức", "Tổ Hợp", "Môn", "Phân vị", "a", "b", "c", "d" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                currentId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                txtMaQuyDoi.setText(String.valueOf(tableModel.getValueAt(row, 1)));
                cboPhuongThuc.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 2)));
                txtToHop.setText(valueOrEmpty(tableModel.getValueAt(row, 3)));
                txtMon.setText(valueOrEmpty(tableModel.getValueAt(row, 4)));
                txtPhanVi.setText(valueOrEmpty(tableModel.getValueAt(row, 5)));
                txtA.setText(valueOrEmpty(tableModel.getValueAt(row, 6)));
                txtB.setText(valueOrEmpty(tableModel.getValueAt(row, 7)));
                txtC.setText(valueOrEmpty(tableModel.getValueAt(row, 8)));
                txtD.setText(valueOrEmpty(tableModel.getValueAt(row, 9)));
            }
        });

        btnThem.addActionListener(e -> themQuyDoi());
        btnSua.addActionListener(e -> suaQuyDoi());
        btnXoa.addActionListener(e -> xoaQuyDoi());
        btnLamMoi.addActionListener(e -> lamMoi());

        // Update derived key preview
        cboPhuongThuc.addActionListener(e -> updateDerivedKey());
        txtToHop.getDocument().addDocumentListener(SimpleDocumentListener.onChange(this::updateDerivedKey));
        txtMon.getDocument().addDocumentListener(SimpleDocumentListener.onChange(this::updateDerivedKey));
        txtPhanVi.getDocument().addDocumentListener(SimpleDocumentListener.onChange(this::updateDerivedKey));

        loadDuLieu();
        updateDerivedKey();
    }

    private void loadDuLieu() {
        tableModel.setRowCount(0);
        List<BangQuyDoi> list = bus.getAll();
        for (BangQuyDoi bqd : list) {
            tableModel.addRow(new Object[] {
                    bqd.getIdQd(),
                    bqd.getDMaQuyDoi(),
                    bqd.getDPhuongThuc(),
                    bqd.getDToHop(),
                    bqd.getDMon(),
                    bqd.getDPhanVi(),
                    bqd.getDDiemA(),
                    bqd.getDDiemB(),
                    bqd.getDDiemC(),
                    bqd.getDDiemD()
            });
        }
    }

    private void themQuyDoi() {
        BangQuyDoi bqd = new BangQuyDoi();
        fillFromForm(bqd);
        if (bus.add(bqd)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadDuLieu();
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaQuyDoi() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
            return;
        }

        BangQuyDoi bqd = new BangQuyDoi();
        bqd.setIdQd(currentId);
        fillFromForm(bqd);

        if (bus.update(bqd)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDuLieu();
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaQuyDoi() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (bus.delete(currentId)) {
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadDuLieu();
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFromForm(BangQuyDoi bqd) {
        bqd.setDPhuongThuc(String.valueOf(cboPhuongThuc.getSelectedItem()));
        bqd.setDToHop(txtToHop.getText());
        bqd.setDMon(txtMon.getText());
        bqd.setDPhanVi(txtPhanVi.getText());
        bqd.setDDiemA(parseBigDecimal(txtA.getText()));
        bqd.setDDiemB(parseBigDecimal(txtB.getText()));
        bqd.setDDiemC(parseBigDecimal(txtC.getText()));
        bqd.setDDiemD(parseBigDecimal(txtD.getText()));
        // d_maquydoi will be derived/validated in BUS; keep preview for user
        bqd.setDMaQuyDoi(txtMaQuyDoi.getText());
    }

    private void lamMoi() {
        currentId = -1;
        txtMaQuyDoi.setText("");
        txtToHop.setText("");
        txtMon.setText("");
        txtPhanVi.setText("");
        txtA.setText("");
        txtB.setText("");
        txtC.setText("");
        txtD.setText("");
        table.clearSelection();
        updateDerivedKey();
    }

    private void updateDerivedKey() {
        String pt = norm(String.valueOf(cboPhuongThuc.getSelectedItem()));
        String th = norm(txtToHop.getText());
        String mon = norm(txtMon.getText());
        String pv = norm(txtPhanVi.getText());
        if (pt.isEmpty() || pv.isEmpty()) {
            txtMaQuyDoi.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(pt);
        if (!th.isEmpty()) {
            sb.append('_').append(th);
        }
        if (!mon.isEmpty()) {
            sb.append('_').append(mon);
        }
        sb.append('_').append(pv);
        txtMaQuyDoi.setText(sb.toString());
    }

    private BigDecimal parseBigDecimal(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String valueOrEmpty(Object o) {
        return o == null ? "" : o.toString();
    }

    private String norm(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }
}
