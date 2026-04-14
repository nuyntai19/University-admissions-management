package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.utils.AutoSuggestComboBox;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class NguyenVongXetTuyenDialog extends JDialog {

    private AutoSuggestComboBox cbCccd;
    private AutoSuggestComboBox cbNganh;
    private JComboBox<String> cbPhuongThuc;
    private JTextField txtThuTu;

    private boolean isConfirm = false;

    public NguyenVongXetTuyenDialog(Frame parent) {
        super(parent, "Thêm nguyện vọng", true);
        setSize(550, 360);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        add(createForm(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);
    }

    private JPanel createForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 15, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        // CCCD
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(label("CCCD"), gbc);

        cbCccd = new AutoSuggestComboBox(this::mockSearchCccd);
        styleInput(cbCccd);

        gbc.gridx = 1;
        panel.add(cbCccd, gbc);
        y++;

        // Phương thức
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(label("Phương thức"), gbc);

        cbPhuongThuc = new JComboBox<>(new String[]{
                "THPT", "Học bạ", "ĐGNL"
        });
        styleInput(cbPhuongThuc);

        gbc.gridx = 1;
        panel.add(cbPhuongThuc, gbc);
        y++;

        // Ngành
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(label("Ngành"), gbc);

        cbNganh = new AutoSuggestComboBox(this::mockSearchNganh);
        styleInput(cbNganh);

        gbc.gridx = 1;
        panel.add(cbNganh, gbc);
        y++;

        // Nguyện vọng
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(label("Nguyện vọng"), gbc);

        txtThuTu = new JTextField();
        styleInput(txtThuTu);

        gbc.gridx = 1;
        panel.add(txtThuTu, gbc);

        return panel;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    // 🎨 STYLE INPUT (QUAN TRỌNG NHẤT)
    private void styleInput(JComponent comp) {
        comp.setPreferredSize(new Dimension(260, 36));
        comp.setBackground(Color.WHITE);

        Border line = new LineBorder(new Color(200, 200, 200), 1, true);
        Border padding = new EmptyBorder(5, 10, 5, 10);

        comp.setBorder(new CompoundBorder(line, padding));
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(Color.WHITE);

        JButton btnCancel = createButton("Hủy", new Color(200, 200, 200), Color.BLACK);
        JButton btnOk = createButton("Lưu", new Color(33, 150, 243), Color.WHITE);

        panel.add(btnCancel);
        panel.add(btnOk);

        btnCancel.addActionListener(e -> {
            isConfirm = false;
            dispose();
        });

        btnOk.addActionListener(e -> {
            if (!validateInput()) return;
            isConfirm = true;
            dispose();
        });

        return panel;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(100, 36));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bg, 1, true));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    // ===== VALIDATE =====
    private boolean validateInput() {
        if (cbCccd.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập CCCD!");
            return false;
        }

        if (cbNganh.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập ngành!");
            return false;
        }

        if (txtThuTu.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập nguyện vọng!");
            return false;
        }

        try {
            Integer.parseInt(txtThuTu.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Nguyện vọng phải là số!");
            return false;
        }

        return true;
    }

    // ===== GETTER =====
    public boolean isConfirm() {
        return isConfirm;
    }

    public String getCccd() {
        return cbCccd.getText();
    }

    public String getMaNganh() {
        return cbNganh.getText();
    }

    public int getThuTu() {
        return Integer.parseInt(txtThuTu.getText());
    }

    public String getPhuongThuc() {
        return cbPhuongThuc.getSelectedItem().toString();
    }

    // ===== MOCK =====
    private List<String> mockSearchCccd(String keyword) {
        return Arrays.asList(
                "089307000001 - Nguyễn Văn A",
                "084307000002 - Trần Thị B",
                "079307000003 - Lê Văn C"
        ).stream()
                .filter(s -> s.toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    private List<String> mockSearchNganh(String keyword) {
        return Arrays.asList(
                "7140201 - Giáo dục Tiểu học",
                "7140211 - Sư phạm Vật lý",
                "7340201 - Tài chính ngân hàng"
        ).stream()
                .filter(s -> s.toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }
}