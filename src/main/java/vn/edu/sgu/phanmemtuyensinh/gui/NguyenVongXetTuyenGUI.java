package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.NguyenVongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NguyenVongXetTuyenGUI extends JPanel {

    private NguyenVongXetTuyenBUS bus = new NguyenVongXetTuyenBUS();
    private JTextField txtCccd, txtMaNganh, txtThuTu;
    private JButton btnThem, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;

    public NguyenVongXetTuyenGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ NGUYỆN VỌNG & XÉT TUYỂN", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new GridLayout(1, 6, 10, 10));
        pnlInput.setBorder(BorderFactory.createTitledBorder("Thêm Nguyện Vọng"));

        pnlInput.add(new JLabel("CCCD:"));
        txtCccd = new JTextField();
        pnlInput.add(txtCccd);
        pnlInput.add(new JLabel("Mã Ngành:"));
        txtMaNganh = new JTextField();
        pnlInput.add(txtMaNganh);
        pnlInput.add(new JLabel("Thứ Tự:"));
        txtThuTu = new JTextField();
        pnlInput.add(txtThuTu);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnThem = new JButton("Thêm Nguyện Vọng");
        btnLamMoi = new JButton("Làm Mới");
        pnlButtons.add(btnThem);
        pnlButtons.add(btnLamMoi);

        add(pnlInput, BorderLayout.NORTH);
        add(pnlButtons, BorderLayout.CENTER);

        String[] columns = {"ID", "CCCD", "Mã Ngành", "Thứ Tự", "Điểm Xét Tuyển", "Kết Quả"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.SOUTH);

        btnThem.addActionListener(e -> themNguyenVong());
        btnLamMoi.addActionListener(e -> lamMoi());

        loadDuLieu();
    }

    private void loadDuLieu() {
        tableModel.setRowCount(0);
        List<NguyenVongXetTuyen> list = bus.getAll();
        for (NguyenVongXetTuyen nv : list) {
            tableModel.addRow(new Object[]{nv.getIdNv(), nv.getNnCccd(), nv.getNvMaNganh(), nv.getNvTt(), nv.getDiemXetTuyen(), nv.getNvKetQua()});
        }
    }

    private void themNguyenVong() {
        NguyenVongXetTuyen nv = new NguyenVongXetTuyen();
        nv.setNnCccd(txtCccd.getText());
        nv.setNvMaNganh(txtMaNganh.getText());
        try {
            nv.setNvTt(Integer.parseInt(txtThuTu.getText()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Thứ tự phải là số!");
            return;
        }
        if (bus.add(nv)) {
            loadDuLieu();
            lamMoi();
        }
    }

    private void lamMoi() {
        txtCccd.setText("");
        txtMaNganh.setText("");
        txtThuTu.setText("");
    }
}
