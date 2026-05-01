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

        // Create input form with GridLayout (2 columns: label + input per row)
        JPanel pnlFormContent = new JPanel(new GridLayout(5, 4, 15, 10));
        pnlFormContent.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Row 1: Phương thức | Tổ hợp
        pnlFormContent.add(new JLabel("Phương thức:"));
        cboPhuongThuc = new JComboBox<>(new String[] { "DGNL", "V-SAT", "THPT" });
        pnlFormContent.add(cboPhuongThuc);
        pnlFormContent.add(new JLabel("Tổ hợp (nếu có):"));
        txtToHop = new JTextField();
        pnlFormContent.add(txtToHop);

        // Row 2: Môn | Phân vị
        pnlFormContent.add(new JLabel("Môn (nếu có):"));
        txtMon = new JTextField();
        pnlFormContent.add(txtMon);
        pnlFormContent.add(new JLabel("Phân vị:"));
        txtPhanVi = new JTextField();
        pnlFormContent.add(txtPhanVi);

        // Row 3: a | b
        pnlFormContent.add(new JLabel("a (điểm gốc):"));
        txtA = new JTextField();
        pnlFormContent.add(txtA);
        pnlFormContent.add(new JLabel("b (điểm gốc):"));
        txtB = new JTextField();
        pnlFormContent.add(txtB);

        // Row 4: c | d
        pnlFormContent.add(new JLabel("c (điểm quy đổi):"));
        txtC = new JTextField();
        pnlFormContent.add(txtC);
        pnlFormContent.add(new JLabel("d (điểm quy đổi):"));
        txtD = new JTextField();
        pnlFormContent.add(txtD);

        // Row 5: Mã quy đổi
        pnlFormContent.add(new JLabel("Mã quy đổi (tự sinh):"));
        txtMaQuyDoi = new JTextField();
        txtMaQuyDoi.setEditable(false);
        pnlFormContent.add(txtMaQuyDoi);
        pnlFormContent.add(new JLabel("")); // Empty label
        pnlFormContent.add(new JLabel("")); // Empty component

        // Wrap form in scroll pane
        JScrollPane scrollInputs = new JScrollPane(pnlFormContent,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollInputs.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Thông tin quy đổi"),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

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
        pnlTop.add(scrollInputs, BorderLayout.CENTER);
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
