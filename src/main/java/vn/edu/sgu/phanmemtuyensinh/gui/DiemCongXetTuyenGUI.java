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

        addCtrl(pnlInput, "CCCD:", txtCccd = new JTextField(15), 0, 0, g);
        addCtrl(pnlInput, "Khu vực (Auto):", txtKhuVuc = new JTextField(), 0, 1, g);
        addCtrl(pnlInput, "Đối tượng (Auto):", txtDoiTuong = new JTextField(), 0, 2, g);
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
        JButton btnAdd = new JButton("Lưu dữ liệu");
        btnAdd.addActionListener(e -> saveAction());
        pnlBtns.add(btnAdd);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlInput, BorderLayout.CENTER);
        pnlTop.add(pnlBtns, BorderLayout.SOUTH);

        // Table
        model = new DefaultTableModel(new String[]{"ID", "CCCD", "Điểm Cộng", "Điểm UT", "Tổng Kết", "Khóa"}, 0);
        table = new JTable(model);
        
        add(pnlTop, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void setupAutoComplete() {
        suggestionMenu = new JPopupMenu();
        listSuggestions = new JList<>();
        suggestionMenu.add(new JScrollPane(listSuggestions));
        
        txtCccd.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtCccd.getText().trim();
                if(text.length() >= 1) {
                    List<String> res = bus.suggestCccd(text);
                    if(!res.isEmpty()) {
                        DefaultListModel<String> m = new DefaultListModel<>();
                        for(String s : res) m.addElement(s);
                        listSuggestions.setModel(m);
                        suggestionMenu.show(txtCccd, 0, txtCccd.getHeight());
                        txtCccd.requestFocus();
                    }
                }
            }
        });

        listSuggestions.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                txtCccd.setText(listSuggestions.getSelectedValue());
                suggestionMenu.setVisible(false);
                // Giả lập tự hiện thông tin (Thực tế bạn gọi BUS tìm thí sinh)
                txtKhuVuc.setText("1"); txtDoiTuong.setText("01");
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
        }
    }

    private void loadData() {
        model.setRowCount(0);
        for(DiemCongXetTuyen d : bus.getAll()) {
            model.addRow(new Object[]{d.getIdDiemCong(), d.getTsCccd(), d.getDiemCC(), d.getDiemUtxt(), d.getDiemTong(), d.getDcKeys()});
        }
    }

    private void addCtrl(JPanel p, String l, JComponent c, int x, int y, GridBagConstraints g) {
        g.gridx = x; g.gridy = y; p.add(new JLabel(l), g);
        g.gridx = x+1; p.add(c, g);
    }
}