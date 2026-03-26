package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.DiemCongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DiemCongXetTuyenGUI extends JPanel {

    private DiemCongXetTuyenBUS bus = new DiemCongXetTuyenBUS();
    private JTextField txtCccd, txtDiemCong;
    private JButton btnThem, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;

    public DiemCongXetTuyenGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ ĐIỂM CỘNG XÉT TUYỂN", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new GridLayout(1, 4, 10, 10));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Nhập Điểm Cộng"));

        pnlInput.add(new JLabel("CCCD:"));
        txtCccd = new JTextField();
        pnlInput.add(txtCccd);
        pnlInput.add(new JLabel("Điểm Cộng:"));
        txtDiemCong = new JTextField();
        pnlInput.add(txtDiemCong);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnThem = new JButton("Thêm");
        btnLamMoi = new JButton("Làm Mới");
        pnlButtons.add(btnThem);
        pnlButtons.add(btnLamMoi);

        add(pnlInput, BorderLayout.NORTH);
        add(pnlButtons, BorderLayout.CENTER);

        String[] columns = {"ID", "CCCD", "Mã Ngành", "Mã Tổ Hợp", "Điểm CC", "Điểm Utxt", "Điểm Tổng"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.SOUTH);

        btnThem.addActionListener(e -> themDiemCong());
        btnLamMoi.addActionListener(e -> lamMoi());

        loadDuLieu();
    }

    private void loadDuLieu() {
        tableModel.setRowCount(0);
        List<DiemCongXetTuyen> list = bus.getAll();
        for (DiemCongXetTuyen d : list) {
            tableModel.addRow(new Object[]{d.getIdDiemCong(), d.getTsCccd(), d.getMaNganh(), d.getMaToHop(), d.getDiemCC(), d.getDiemUtxt(), d.getDiemTong()});
        }
    }

    private void themDiemCong() {
        DiemCongXetTuyen d = new DiemCongXetTuyen();
        d.setTsCccd(txtCccd.getText());
        try {
            d.setDiemCC(new java.math.BigDecimal(txtDiemCong.getText()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Điểm phải là số!");
            return;
        }
        if (bus.add(d)) {
            loadDuLieu();
            lamMoi();
        }
    }

    private void lamMoi() {
        txtCccd.setText("");
        txtDiemCong.setText("");
    }
}
