package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.NganhBUS;
import vn.edu.sgu.phanmemtuyensinh.bus.NganhToHopBUS;
import vn.edu.sgu.phanmemtuyensinh.bus.ToHopMonBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NganhToHop;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.Nganh;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ToHopMon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class NganhToHopGUI extends JPanel {

    private NganhToHopBUS bus = new NganhToHopBUS();
    private NganhBUS nganhBUS = new NganhBUS();
    private ToHopMonBUS toHopMonBUS = new ToHopMonBUS();

    private JComboBox<String> cboMaNganh, cboMaToHop;
    private JTextField txtMon1, txtHs1, txtMon2, txtHs2, txtMon3, txtHs3, txtDoLech;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;
    private int currentId = -1;

    public NganhToHopGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ NGÀNH - TỔ HỢP MÔN", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);

        JPanel pnlInput = new JPanel(new GridLayout(4, 6, 10, 10));
        pnlInput.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        pnlInput.add(new JLabel("Mã ngành:"));
        cboMaNganh = new JComboBox<>();
        pnlInput.add(cboMaNganh);
        pnlInput.add(new JLabel("Mã tổ hợp:"));
        cboMaToHop = new JComboBox<>();
        pnlInput.add(cboMaToHop);
        pnlInput.add(new JLabel("Độ lệch:"));
        txtDoLech = new JTextField();
        txtDoLech.setEditable(false);
        pnlInput.add(txtDoLech);

        pnlInput.add(new JLabel("Môn 1:"));
        txtMon1 = new JTextField();
        txtMon1.setEditable(false);
        pnlInput.add(txtMon1);
        pnlInput.add(new JLabel("HS1:"));
        txtHs1 = new JTextField("1");
        pnlInput.add(txtHs1);
        pnlInput.add(new JLabel("Môn 2:"));
        txtMon2 = new JTextField();
        txtMon2.setEditable(false);
        pnlInput.add(txtMon2);

        pnlInput.add(new JLabel("HS2:"));
        txtHs2 = new JTextField("1");
        pnlInput.add(txtHs2);
        pnlInput.add(new JLabel("Môn 3:"));
        txtMon3 = new JTextField();
        txtMon3.setEditable(false);
        pnlInput.add(txtMon3);
        pnlInput.add(new JLabel("HS3:"));
        txtHs3 = new JTextField("1");
        pnlInput.add(txtHs3);
        pnlInput.add(new JLabel(""));
        pnlInput.add(new JLabel(""));

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

        String[] columns = { "ID", "Mã Ngành", "Mã Tổ Hợp", "Môn 1", "HS1", "Môn 2", "HS2", "Môn 3", "HS3", "Độ lệch" };
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
                cboMaNganh.setSelectedItem(tableModel.getValueAt(row, 1).toString());
                cboMaToHop.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                txtMon1.setText(String.valueOf(tableModel.getValueAt(row, 3)));
                txtHs1.setText(String.valueOf(tableModel.getValueAt(row, 4)));
                txtMon2.setText(String.valueOf(tableModel.getValueAt(row, 5)));
                txtHs2.setText(String.valueOf(tableModel.getValueAt(row, 6)));
                txtMon3.setText(String.valueOf(tableModel.getValueAt(row, 7)));
                txtHs3.setText(String.valueOf(tableModel.getValueAt(row, 8)));
                txtDoLech.setText(String.valueOf(tableModel.getValueAt(row, 9)));
            }
        });

        btnThem.addActionListener(e -> themNganhToHop());
        btnSua.addActionListener(e -> suaNganhToHop());
        btnXoa.addActionListener(e -> xoaNganhToHop());
        btnLamMoi.addActionListener(e -> lamMoi());

        cboMaToHop.addActionListener(e -> refreshFromSelection());
        cboMaNganh.addActionListener(e -> refreshFromSelection());

        loadCombos();
        loadDuLieu();
        refreshFromSelection();
    }

    private void loadCombos() {
        cboMaNganh.removeAllItems();
        List<Nganh> nganhList = nganhBUS.getAll();
        for (Nganh n : nganhList) {
            cboMaNganh.addItem(n.getMaNganh());
        }

        cboMaToHop.removeAllItems();
        List<ToHopMon> thList = toHopMonBUS.getAll();
        for (ToHopMon th : thList) {
            cboMaToHop.addItem(th.getMaToHop());
        }
    }

    private void refreshFromSelection() {
        Object maToHopObj = cboMaToHop.getSelectedItem();
        String maToHop = maToHopObj == null ? "" : maToHopObj.toString();
        ToHopMon th = toHopMonBUS.getByMaToHop(maToHop);
        if (th != null) {
            txtMon1.setText(th.getMon1());
            txtMon2.setText(th.getMon2());
            txtMon3.setText(th.getMon3());
        } else {
            txtMon1.setText("");
            txtMon2.setText("");
            txtMon3.setText("");
        }

        Object maNganhObj = cboMaNganh.getSelectedItem();
        String maNganh = maNganhObj == null ? "" : maNganhObj.toString();
        BigDecimal doLech = bus.previewDoLech(maNganh, maToHop);
        txtDoLech.setText(doLech == null ? "0" : doLech.toPlainString());
    }

    private void loadDuLieu() {
        tableModel.setRowCount(0);
        List<NganhToHop> list = bus.getAll();
        for (NganhToHop nth : list) {
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
    }

    private void themNganhToHop() {
        NganhToHop nth = new NganhToHop();
        nth.setMaNganh(String.valueOf(cboMaNganh.getSelectedItem()));
        nth.setMaToHop(String.valueOf(cboMaToHop.getSelectedItem()));
        nth.setHsMon1(parseIntOrNull(txtHs1.getText()));
        nth.setHsMon2(parseIntOrNull(txtHs2.getText()));
        nth.setHsMon3(parseIntOrNull(txtHs3.getText()));

        if (bus.add(nth)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadDuLieu();
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaNganhToHop() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
            return;
        }

        NganhToHop nth = new NganhToHop();
        nth.setId(currentId);
        nth.setMaNganh(String.valueOf(cboMaNganh.getSelectedItem()));
        nth.setMaToHop(String.valueOf(cboMaToHop.getSelectedItem()));
        nth.setHsMon1(parseIntOrNull(txtHs1.getText()));
        nth.setHsMon2(parseIntOrNull(txtHs2.getText()));
        nth.setHsMon3(parseIntOrNull(txtHs3.getText()));

        if (bus.update(nth)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDuLieu();
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaNganhToHop() {
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

    private void lamMoi() {
        currentId = -1;
        txtHs1.setText("1");
        txtHs2.setText("1");
        txtHs3.setText("1");
        table.clearSelection();
        refreshFromSelection();
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
