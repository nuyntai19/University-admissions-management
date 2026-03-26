package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import vn.edu.sgu.phanmemtuyensinh.bus.ToHopMonBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ToHopMon;

public class ToHopMonGUI extends JPanel {

    private ToHopMonBUS bus = new ToHopMonBUS();

    // Các thành phần giao diện
    private JTextField txtMa, txtMon1, txtMon2, txtMon3, txtTen;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnImport;
    private JTable table;
    private DefaultTableModel tableModel;
    
    // Biến lưu ID đang được chọn
    private int currentId = -1;

    public ToHopMonGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        loadDataToTable();
    }

    private void initComponents() {
        // --- PHẦN TIÊU ĐỀ ---
        JLabel lblTitle = new JLabel("QUẢN LÝ TỔ HỢP MÔN", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);

        // --- PHẦN NHẬP LIỆU (Bên trên) ---
        JPanel pnlInput = new JPanel(new GridLayout(3, 4, 10, 10));
        pnlInput.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        pnlInput.add(new JLabel("Mã tổ hợp:"));
        txtMa = new JTextField();
        pnlInput.add(txtMa);

        pnlInput.add(new JLabel("Tên tổ hợp:"));
        txtTen = new JTextField();
        pnlInput.add(txtTen);

        pnlInput.add(new JLabel("Môn 1:"));
        txtMon1 = new JTextField();
        pnlInput.add(txtMon1);

        pnlInput.add(new JLabel("Môn 2:"));
        txtMon2 = new JTextField();
        pnlInput.add(txtMon2);

        pnlInput.add(new JLabel("Môn 3:"));
        txtMon3 = new JTextField();
        pnlInput.add(txtMon3);
        
        // --- NÚT BẤM ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm Mới");
        btnImport = new JButton("Import Excel");
        btnImport.setBackground(new Color(34, 139, 34));
        btnImport.setForeground(Color.WHITE);
        
        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);
        pnlButtons.add(btnImport);
        pnlInput.add(pnlButtons); // Nét phá cách: nhét nút vào ô trống cuối của GridLayout

        // Combine title + input into one north panel
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        pnlTop.add(pnlInput, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        // --- PHẦN BẢNG DỮ LIỆU (Bên dưới) ---
        String[] columns = {"ID", "Mã Tổ Hợp", "Môn 1", "Môn 2", "Môn 3", "Tên Tổ Hợp"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Không cho sửa trực tiếp trên bảng
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- BẮT SỰ KIỆN (EVENTS) ---
        
        // Sự kiện click vào dòng trong bảng
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                currentId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                txtMa.setText(tableModel.getValueAt(row, 1).toString());
                txtMon1.setText(tableModel.getValueAt(row, 2).toString());
                txtMon2.setText(tableModel.getValueAt(row, 3).toString());
                txtMon3.setText(tableModel.getValueAt(row, 4).toString());
                txtTen.setText(tableModel.getValueAt(row, 5) != null ? tableModel.getValueAt(row, 5).toString() : "");
            }
        });

        // Nút Làm mới
        btnLamMoi.addActionListener(e -> clearForm());

        // Nút Thêm
        btnThem.addActionListener(e -> {
            ToHopMon th = new ToHopMon();
            th.setMaToHop(txtMa.getText());
            th.setMon1(txtMon1.getText());
            th.setMon2(txtMon2.getText());
            th.setMon3(txtMon3.getText());
            th.setTenToHop(txtTen.getText());

            if (bus.add(th)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại (Kiểm tra lại mã tổ hợp có trùng không)!");
            }
        });

        // Nút Sửa
        btnSua.addActionListener(e -> {
            if (currentId == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
                return;
            }
            ToHopMon th = new ToHopMon();
            th.setIdToHop(currentId);
            th.setMaToHop(txtMa.getText());
            th.setMon1(txtMon1.getText());
            th.setMon2(txtMon2.getText());
            th.setMon3(txtMon3.getText());
            th.setTenToHop(txtTen.getText());

            if (bus.update(th)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        });

        // Nút Xóa
        btnXoa.addActionListener(e -> {
            if (currentId == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (bus.delete(currentId)) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadDataToTable();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại!");
                }
            }
        });

        // Nút Import Excel
        btnImport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    int imported = bus.importAndSaveToDatabase(filePath);
                    JOptionPane.showMessageDialog(this, "Import thành công! Đã thêm " + imported + " tổ hợp môn.");
                    loadDataToTable();
                    clearForm();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi import: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
    }

    // Hàm load dữ liệu từ Database lên Bảng
    private void loadDataToTable() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        List<ToHopMon> list = bus.getAll();
        for (ToHopMon th : list) {
            tableModel.addRow(new Object[]{
                    th.getIdToHop(), th.getMaToHop(), 
                    th.getMon1(), th.getMon2(), th.getMon3(), th.getTenToHop()
            });
        }
    }

    // Hàm xóa trắng các ô nhập liệu
    private void clearForm() {
        currentId = -1;
        txtMa.setText("");
        txtMon1.setText("");
        txtMon2.setText("");
        txtMon3.setText("");
        txtTen.setText("");
        table.clearSelection();
    }
}