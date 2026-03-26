package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.BangQuyDoiBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.BangQuyDoi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BangQuyDoiGUI extends JPanel {

    private BangQuyDoiBUS bus = new BangQuyDoiBUS();
    private JTextField txtMaQuyDoi, txtToHop, txtMon, txtDiemA;
    private JButton btnThem, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;

    public BangQuyDoiGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ BẢNG QUY ĐỔI", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new GridLayout(1, 8, 10, 10));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Thông Tin Quy Đổi"));

        pnlInput.add(new JLabel("Mã Quy Đổi:"));
        txtMaQuyDoi = new JTextField();
        pnlInput.add(txtMaQuyDoi);
        pnlInput.add(new JLabel("Tổ Hợp:"));
        txtToHop = new JTextField();
        pnlInput.add(txtToHop);
        pnlInput.add(new JLabel("Môn:"));
        txtMon = new JTextField();
        pnlInput.add(txtMon);
        pnlInput.add(new JLabel("Điểm A:"));
        txtDiemA = new JTextField();
        pnlInput.add(txtDiemA);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnThem = new JButton("Thêm");
        btnLamMoi = new JButton("Làm Mới");
        pnlButtons.add(btnThem);
        pnlButtons.add(btnLamMoi);

        add(pnlInput, BorderLayout.NORTH);
        add(pnlButtons, BorderLayout.CENTER);

        String[] columns = {"ID", "Mã Quy Đổi", "Phương Thức", "Tổ Hợp", "Môn", "Điểm A"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.SOUTH);

        btnThem.addActionListener(e -> themQuyDoi());
        btnLamMoi.addActionListener(e -> lamMoi());

        loadDuLieu();
    }

    private void loadDuLieu() {
        tableModel.setRowCount(0);
        List<BangQuyDoi> list = bus.getAll();
        for (BangQuyDoi bqd : list) {
            tableModel.addRow(new Object[]{bqd.getIdQd(), bqd.getDMaQuyDoi(), bqd.getDPhuongThuc(), bqd.getDToHop(), bqd.getDMon(), bqd.getDDiemA()});
        }
    }

    private void themQuyDoi() {
        BangQuyDoi bqd = new BangQuyDoi();
        bqd.setDMaQuyDoi(txtMaQuyDoi.getText());
        bqd.setDToHop(txtToHop.getText());
        bqd.setDMon(txtMon.getText());
        try {
            bqd.setDDiemA(new java.math.BigDecimal(txtDiemA.getText()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Điểm phải là số!");
            return;
        }
        if (bus.add(bqd)) {
            loadDuLieu();
            lamMoi();
        }
    }

    private void lamMoi() {
        txtMaQuyDoi.setText("");
        txtToHop.setText("");
        txtMon.setText("");
        txtDiemA.setText("");
    }
}
