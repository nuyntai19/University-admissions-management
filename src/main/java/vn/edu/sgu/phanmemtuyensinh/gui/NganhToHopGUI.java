package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.NganhToHopBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NganhToHop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NganhToHopGUI extends JPanel {

    private NganhToHopBUS bus = new NganhToHopBUS();
    private JTextField txtMaNganh, txtMaToHop, txtMon1, txtMon2, txtMon3;
    private JButton btnThem, btnXoa, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;
    private int currentId = -1;

    public NganhToHopGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ NGÀNH - TỔ HỢP MÔN", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new GridLayout(2, 5, 10, 10));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Thông Tin"));

        pnlInput.add(new JLabel("Mã Ngành:"));
        txtMaNganh = new JTextField();
        pnlInput.add(txtMaNganh);
        pnlInput.add(new JLabel("Mã Tổ Hợp:"));
        txtMaToHop = new JTextField();
        pnlInput.add(txtMaToHop);
        pnlInput.add(new JLabel(""));

        pnlInput.add(new JLabel("Môn 1:"));
        txtMon1 = new JTextField();
        pnlInput.add(txtMon1);
        pnlInput.add(new JLabel("Môn 2:"));
        txtMon2 = new JTextField();
        pnlInput.add(txtMon2);
        pnlInput.add(new JLabel("Môn 3:"));
        txtMon3 = new JTextField();
        pnlInput.add(txtMon3);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnThem = new JButton("Thêm");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm Mới");
        pnlButtons.add(btnThem);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);

        add(pnlInput, BorderLayout.NORTH);
        add(pnlButtons, BorderLayout.CENTER);

        String[] columns = {"ID", "Mã Ngành", "Mã Tổ Hợp", "Môn 1", "Môn 2", "Môn 3"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.SOUTH);

        btnThem.addActionListener(e -> themNganhToHop());
        btnXoa.addActionListener(e -> xoaNganhToHop());
        btnLamMoi.addActionListener(e -> lamMoi());

        loadDuLieu();
    }

    private void loadDuLieu() {
        tableModel.setRowCount(0);
        List<NganhToHop> list = bus.getAll();
        for (NganhToHop nth : list) {
            tableModel.addRow(new Object[]{nth.getId(), nth.getMaNganh(), nth.getMaToHop(), nth.getThMon1(), nth.getThMon2(), nth.getThMon3()});
        }
    }

    private void themNganhToHop() {
        NganhToHop nth = new NganhToHop();
        nth.setMaNganh(txtMaNganh.getText());
        nth.setMaToHop(txtMaToHop.getText());
        nth.setThMon1(txtMon1.getText());
        nth.setThMon2(txtMon2.getText());
        nth.setThMon3(txtMon3.getText());
        if (bus.add(nth)) {
            loadDuLieu();
            lamMoi();
        }
    }

    private void xoaNganhToHop() {
        if (currentId != -1 && bus.delete(currentId)) {
            loadDuLieu();
            lamMoi();
        }
    }

    private void lamMoi() {
        currentId = -1;
        txtMaNganh.setText("");
        txtMaToHop.setText("");
        txtMon1.setText("");
        txtMon2.setText("");
        txtMon3.setText("");
    }
}
