package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.bus.DiemCongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.List;

public class DiemCongXetTuyenGUI extends JPanel {
    private DiemCongXetTuyenBUS bus = new DiemCongXetTuyenBUS();
    private JTextField txtCccd, txtKhuVuc, txtDoiTuong, txtMucCC, txtDcKeys, txtDiemUT, txtDiemTong;
    private JComboBox<String> cbCC, cbGiai;
    private JTable table;
    private DefaultTableModel model;
    private JPopupMenu suggestionMenu;
    private JList<String> listSuggestions;
    private int selectedId = -1;

    public DiemCongXetTuyenGUI() {
        setLayout(new BorderLayout(10, 10));
        initComponents();
        setupAutoComplete();
        loadData();
    }

    private void initComponents() {
        // Panel Input (GridBagLayout cho thẳng hàng)
        JPanel pnlInput = new JPanel(new GridBagLayout());
        pnlInput.setBorder(BorderFactory.createTitledBorder("Nhập liệu & Quy đổi tự động"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5); g.fill = GridBagConstraints.HORIZONTAL;

        addCtrl(pnlInput, "CCCD/SBD:", txtCccd = new JTextField(15), 0, 0, g);
        addCtrl(pnlInput, "Khu vực:", txtKhuVuc = new JTextField(), 0, 1, g);
        addCtrl(pnlInput, "Đối tượng:", txtDoiTuong = new JTextField(), 0, 2, g);
        txtKhuVuc.setEditable(false); txtDoiTuong.setEditable(false);

        addCtrl(pnlInput, "Chứng chỉ:", cbCC = new JComboBox<>(new String[]{"None", "IELTS"}), 2, 0, g);
        addCtrl(pnlInput, "Mức điểm:", txtMucCC = new JTextField(), 2, 1, g);
        addCtrl(pnlInput, "Giải thưởng:", cbGiai = new JComboBox<>(new String[]{"None", "Nhất QG"}), 2, 2, g);

        addCtrl(pnlInput, "Điểm UT thực tế:", txtDiemUT = new JTextField(), 4, 0, g);
        addCtrl(pnlInput, "Tổng cộng:", txtDiemTong = new JTextField(), 4, 1, g);
        addCtrl(pnlInput, "Mã khóa (Keys):", txtDcKeys = new JTextField(), 4, 2, g);
        txtDiemUT.setEditable(false); txtDiemTong.setEditable(false);

        // Buttons
        JPanel pnlBtns = new JPanel();
        JButton btnAdd = new JButton("Thêm");
        JButton btnDelete = new JButton("Xóa");
        JButton btnUpdate = new JButton("Sửa");
        JButton btnReset = new JButton("Làm mới");
        
        btnAdd.addActionListener(e -> saveAction());
        btnDelete.addActionListener(e -> deleteAction());
        btnUpdate.addActionListener(e -> updateAction());
        btnReset.addActionListener(e -> resetForm());

        pnlBtns.add(btnAdd);
        pnlBtns.add(btnUpdate);
        pnlBtns.add(btnDelete);
        pnlBtns.add(btnReset);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlInput, BorderLayout.CENTER);
        pnlTop.add(pnlBtns, BorderLayout.SOUTH);

        // Table
        model = new DefaultTableModel(new String[]{"ID", "CCCD", "Điểm Cộng", "Điểm UT", "Tổng Kết", "Khóa"}, 0);
        table = new JTable(model);
        
        add(pnlTop, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fillFormFromSelectedRow();
            }
        });
    }

    private void setupAutoComplete() {
        suggestionMenu = new JPopupMenu();
        listSuggestions = new JList<>();
        suggestionMenu.add(new JScrollPane(listSuggestions));

        txtCccd.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtCccd.getText().trim();
                if(text.length() >= 1) {
                    // Lấy danh sách thí sinh thay vì chỉ list String
                    List<vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh> res = bus.timKiemThiSinh(text);
                    if(!res.isEmpty()) {
                        DefaultListModel<String> m = new DefaultListModel<>();
                        for(vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh ts : res) {
                            // Hiển thị gợi ý: "SBD | CCCD | Họ Tên" để người dùng dễ phân biệt
                            m.addElement(ts.getSoBaoDanh() + " - " + ts.getCccd() + " - " + ts.getHo() + " " + ts.getTen());
                        }
                        listSuggestions.setModel(m);
                        suggestionMenu.show(txtCccd, 0, txtCccd.getHeight());
                        txtCccd.requestFocus();
                    }
                }
            }
        });

        listSuggestions.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String selectedValue = listSuggestions.getSelectedValue();
                if (selectedValue != null) {
                    // Tách chuỗi lấy CCCD (phần nằm giữa dấu gạch ngang đầu tiên và thứ hai)
                    String[] parts = selectedValue.split(" - ");
                    String sbd = parts[0].trim();
                    String cccd = parts[1].trim();

                    // 1. Điền CCCD vào ô nhập (để lưu DB theo mốc CCCD)
                    txtCccd.setText(cccd);
                    suggestionMenu.setVisible(false);

                    // 2. Gọi BUS lấy thông tin chi tiết để điền vào các ô Read-only
                    Object[] thongTin = bus.layThongTinThiSinh(cccd);
                    if (thongTin != null) {
                        txtKhuVuc.setText(thongTin[0] != null ? thongTin[0].toString() : "");
                        txtDoiTuong.setText(thongTin[1] != null ? thongTin[1].toString() : "");
                    }

                    // 3. Có thể dùng SBD làm tiền tố cho mã khóa (Keys) nếu muốn
                    txtDcKeys.setText("KEY_" + sbd); 
                }
            }
        });
    }

    private void saveAction() {
        DiemCongXetTuyen d = new DiemCongXetTuyen();
        d.setTsCccd(txtCccd.getText());
        d.setDcKeys(txtDcKeys.getText());
        
        // Gọi BUS tính toán
        bus.tinhToanDiemCongVaUuTien(d, cbCC.getSelectedItem().toString(), txtMucCC.getText(), 
                                      cbGiai.getSelectedItem().toString(), txtKhuVuc.getText(), 
                                      txtDoiTuong.getText(), new BigDecimal("24.0"));
        
        if(bus.save(d)) {
            JOptionPane.showMessageDialog(this, "Thành công!");
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row != -1) {
            selectedId = (int) model.getValueAt(row, 0);

            txtCccd.setText(model.getValueAt(row, 1).toString());
            txtDcKeys.setText(model.getValueAt(row, 5).toString());

            Object[] thongTin = bus.layThongTinThiSinh(txtCccd.getText());
            if (thongTin != null) {
                txtKhuVuc.setText(thongTin[0] != null ? thongTin[0].toString() : "");
                txtDoiTuong.setText(thongTin[1] != null ? thongTin[1].toString() : "");
            }

        }
    }

    private void updateAction() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
            return;
        }

        DiemCongXetTuyen d = new DiemCongXetTuyen();
        d.setIdDiemCong(selectedId);
        d.setTsCccd(txtCccd.getText());
        d.setDcKeys(txtDcKeys.getText());
        
        // Tính toán lại điểm trước khi cập nhật
        bus.tinhToanDiemCongVaUuTien(d, cbCC.getSelectedItem().toString(), txtMucCC.getText(), 
                                      cbGiai.getSelectedItem().toString(), txtKhuVuc.getText(), 
                                      txtDoiTuong.getText(), new BigDecimal("24.0"));

        if (bus.update(d)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadData();
            resetForm();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAction() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bản ghi này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (bus.delete(selectedId)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadData();
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetForm() {
        selectedId = -1;
        txtCccd.setText("");
        txtKhuVuc.setText("");
        txtDoiTuong.setText("");
        txtMucCC.setText("");
        txtDcKeys.setText("");
        txtDiemUT.setText("");
        txtDiemTong.setText("");
        cbCC.setSelectedIndex(0);
        cbGiai.setSelectedIndex(0);
        table.clearSelection();
    }

    private void loadData() {
        model.setRowCount(0);
        List<DiemCongXetTuyen> list = bus.getAll();
        if (list != null) {
            for (DiemCongXetTuyen d : list) {
                model.addRow(new Object[]{
                    d.getIdDiemCong(), 
                    d.getTsCccd(), 
                    d.getDiemCC(), 
                    d.getDiemUtxt(), 
                    d.getDiemTong(), 
                    d.getDcKeys()
                });
            }
        }
    }

    private void addCtrl(JPanel p, String l, JComponent c, int x, int y, GridBagConstraints g) {
        g.gridx = x; g.gridy = y; p.add(new JLabel(l), g);
        g.gridx = x+1; p.add(c, g);
    }
}