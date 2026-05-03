package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import vn.edu.sgu.phanmemtuyensinh.bus.DiemCongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemCongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;
import vn.edu.sgu.phanmemtuyensinh.utils.AutoSuggestComboBox;

public class DiemCongXetTuyenDialog extends JDialog {

    private AutoSuggestComboBox cbCccd;
    private JComboBox<String> cbCC;
    private JTextField txtMucCC;
    private JTextField txtCapGiai;
    private JTextField txtDoiTuongGiai;
    private JTextField txtMaMonGiai;
    private JTextField txtLoaiGiai;

    private JTextField txtDiemQuyDoi;
    private JTextField txtDiemCc;
    private JTextField txtDiemMon;
    private JTextField txtDiemKoMon;
    private JTextField txtDiemUt;
    private JTextField txtTongKet;

    private boolean isConfirm = false;
    private DiemCongXetTuyen data;
    private DiemCongXetTuyenBUS bus = new DiemCongXetTuyenBUS();

    public DiemCongXetTuyenDialog(Frame parent, DiemCongXetTuyen initData) {
        super(parent, initData == null ? "Thêm Điểm Cộng" : "Sửa Điểm Cộng", true);
        this.data = initData;

        setSize(850, 750);
        setMinimumSize(new Dimension(800, 700));
        setLocationRelativeTo(parent);

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 249, 255));

        add(createContent(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);

        if (initData != null) {
            fillData();
        }
    }

    private JPanel createContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(30, 136, 229));
        pnlHeader.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblHeader = new JLabel(data == null ? "THÊM ĐIỂM CỘNG" : "SỬA ĐIỂM CỘNG");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pnlHeader.add(lblHeader, BorderLayout.WEST);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(213, 223, 240)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        Dimension inputSize = new Dimension(220, 36);

        // Group 1: Cơ bản
        cbCccd = new AutoSuggestComboBox(this::searchCccd);
        styleInput(cbCccd, inputSize);

        g.gridx = 0; g.gridy = 0; g.gridwidth = 1;
        pnlForm.add(label("CCCD/Số báo danh (*):"), g);
        g.gridx = 1; g.gridwidth = 3;
        pnlForm.add(cbCccd, g);

        // Group 2: Chứng chỉ ngoại ngữ
        JPanel pnlNgoaiNgu = new JPanel(new GridBagLayout());
        pnlNgoaiNgu.setBackground(Color.WHITE);
        pnlNgoaiNgu.setBorder(createTitledBorder("Chứng chỉ Ngoại ngữ"));
        GridBagConstraints gN = new GridBagConstraints();
        gN.insets = new Insets(5, 5, 5, 5); gN.fill = GridBagConstraints.HORIZONTAL;

        cbCC = new JComboBox<>(new String[]{"None", "IELTS", "TOEFL ITP", "TOEFL iBT", "TOEIC", "PTE", "Linguaskill", "Aptis", "VSTEP"});
        txtMucCC = new JTextField();
        styleInput(cbCC, inputSize);
        styleInput(txtMucCC, inputSize);

        gN.gridx = 0; gN.gridy = 0; pnlNgoaiNgu.add(label("Loại chứng chỉ:"), gN);
        gN.gridx = 1; pnlNgoaiNgu.add(cbCC, gN);
        gN.gridx = 2; pnlNgoaiNgu.add(label("Điểm/Bậc CC:"), gN);
        gN.gridx = 3; pnlNgoaiNgu.add(txtMucCC, gN);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 4;
        pnlForm.add(pnlNgoaiNgu, g);

        // Group 3: Giải thưởng
        JPanel pnlGiai = new JPanel(new GridBagLayout());
        pnlGiai.setBackground(Color.WHITE);
        pnlGiai.setBorder(createTitledBorder("Giải thưởng & Ưu tiên khác"));
        GridBagConstraints gG = new GridBagConstraints();
        gG.insets = new Insets(5, 5, 5, 5); gG.fill = GridBagConstraints.HORIZONTAL;

        txtCapGiai = new JTextField();
        txtDoiTuongGiai = new JTextField();
        txtMaMonGiai = new JTextField();
        txtLoaiGiai = new JTextField();
        
        styleInput(txtCapGiai, inputSize);
        styleInput(txtDoiTuongGiai, inputSize);
        styleInput(txtMaMonGiai, inputSize);
        styleInput(txtLoaiGiai, inputSize);

        gG.gridx = 0; gG.gridy = 0; pnlGiai.add(label("Cấp giải:"), gG);
        gG.gridx = 1; pnlGiai.add(txtCapGiai, gG);
        gG.gridx = 2; pnlGiai.add(label("Đối tượng giải:"), gG);
        gG.gridx = 3; pnlGiai.add(txtDoiTuongGiai, gG);

        gG.gridx = 0; gG.gridy = 1; pnlGiai.add(label("Mã môn giải:"), gG);
        gG.gridx = 1; pnlGiai.add(txtMaMonGiai, gG);
        gG.gridx = 2; pnlGiai.add(label("Loại giải:"), gG);
        gG.gridx = 3; pnlGiai.add(txtLoaiGiai, gG);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 4;
        pnlForm.add(pnlGiai, g);

        // Group 4: Kết quả (Tự động tính)
        JPanel pnlKetQua = new JPanel(new GridBagLayout());
        pnlKetQua.setBackground(Color.WHITE);
        pnlKetQua.setBorder(createTitledBorder("Kết quả Quy đổi & Cộng điểm (Hệ thống tự động tính)"));
        GridBagConstraints gK = new GridBagConstraints();
        gK.insets = new Insets(5, 5, 5, 5); gK.fill = GridBagConstraints.HORIZONTAL;

        txtDiemQuyDoi = createReadOnlyField();
        txtDiemCc = createReadOnlyField();
        txtDiemMon = createReadOnlyField();
        txtDiemKoMon = createReadOnlyField();
        txtDiemUt = createReadOnlyField();
        txtTongKet = createReadOnlyField();

        styleInput(txtDiemQuyDoi, inputSize);
        styleInput(txtDiemCc, inputSize);
        styleInput(txtDiemMon, inputSize);
        styleInput(txtDiemKoMon, inputSize);
        styleInput(txtDiemUt, inputSize);
        styleInput(txtTongKet, inputSize);

        gK.gridx = 0; gK.gridy = 0; pnlKetQua.add(label("Điểm quy đổi:"), gK);
        gK.gridx = 1; pnlKetQua.add(txtDiemQuyDoi, gK);
        gK.gridx = 2; pnlKetQua.add(label("Điểm cộng CC:"), gK);
        gK.gridx = 3; pnlKetQua.add(txtDiemCc, gK);

        gK.gridx = 0; gK.gridy = 1; pnlKetQua.add(label("Điểm cộng môn:"), gK);
        gK.gridx = 1; pnlKetQua.add(txtDiemMon, gK);
        gK.gridx = 2; pnlKetQua.add(label("Điểm cộng ko môn:"), gK);
        gK.gridx = 3; pnlKetQua.add(txtDiemKoMon, gK);

        gK.gridx = 0; gK.gridy = 2; pnlKetQua.add(label("Điểm UT:"), gK);
        gK.gridx = 1; pnlKetQua.add(txtDiemUt, gK);
        gK.gridx = 2; pnlKetQua.add(label("Tổng kết:"), gK);
        gK.gridx = 3; pnlKetQua.add(txtTongKet, gK);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 4;
        pnlForm.add(pnlKetQua, g);

        // Blank space
        g.gridy = 4; g.weighty = 1.0;
        pnlForm.add(new JLabel(""), g);

        JScrollPane scroll = new JScrollPane(pnlForm);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private TitledBorder createTitledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), title);
        tb.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        tb.setTitleColor(new Color(30, 136, 229));
        return tb;
    }

    private void fillData() {
        cbCccd.setText(data.getTsCccd());
        String cc = data.getChungChi();
        if (cc != null && cc.startsWith("Tiếng Anh - ")) {
            cc = cc.replace("Tiếng Anh - ", "");
        }
        boolean found = false;
        for (int i = 0; i < cbCC.getItemCount(); i++) {
            if (cbCC.getItemAt(i).equalsIgnoreCase(cc)) {
                cbCC.setSelectedIndex(i);
                found = true; break;
            }
        }
        if (!found) cbCC.setSelectedItem(data.getChungChi() != null ? data.getChungChi() : "None");
        
        txtMucCC.setText(data.getMucDatDuoc());
        
        txtCapGiai.setText(data.getCapGiai());
        txtDoiTuongGiai.setText(data.getDoiTuongGiai());
        txtMaMonGiai.setText(data.getMaMonGiai());
        txtLoaiGiai.setText(data.getLoaiGiai());
        
        int mucCC = getMucChungChi(data.getChungChi(), data.getMucDatDuoc());
        String diemQuyDoiStr = "";
        String diemCongCcStr = "";
        if (mucCC > 0) {
            diemQuyDoiStr = mucCC == 1 ? "8.0" : mucCC == 2 ? "9.0" : "10.0";
            diemCongCcStr = mucCC == 1 ? "1.0" : mucCC == 2 ? "1.5" : "2.0";
        }
        
        txtDiemQuyDoi.setText(diemQuyDoiStr);
        txtDiemCc.setText(diemCongCcStr);
        txtDiemMon.setText(data.getDiemCongMonGiai() != null ? data.getDiemCongMonGiai().toString() : "0.0");
        txtDiemKoMon.setText(data.getDiemCongKhongMon() != null ? data.getDiemCongKhongMon().toString() : "0.0");
        txtDiemUt.setText(data.getDiemUtxt() != null ? data.getDiemUtxt().toString() : "0.0");
        txtTongKet.setText(data.getDiemTong() != null ? data.getDiemTong().toString() : "0.0");
    }
    
    private int getMucChungChi(String loaiCC, String diemStr) {
        if (loaiCC == null || loaiCC.isEmpty() || loaiCC.equalsIgnoreCase("None")) return 0;
        if (diemStr == null || diemStr.isEmpty()) return 0;

        String cc = loaiCC.trim().toLowerCase(java.util.Locale.ROOT);
        double diem = 0;
        try { diem = Double.parseDouble(diemStr.trim().replace(',', '.')); } catch (Exception e) {}
        String diemUpper = diemStr.trim().toUpperCase(java.util.Locale.ROOT);

        if (cc.contains("ielts")) {
            if (diem >= 7.0) return 3;
            if (diem >= 5.5) return 2;
            if (diem >= 4.0) return 1;
        } else if (cc.contains("toefl itp")) {
            if (diem >= 627) return 3;
            if (diem >= 500) return 2;
            if (diem >= 450) return 1;
        } else if (cc.contains("toefl ibt")) {
            if (diem >= 94) return 3;
            if (diem >= 46) return 2;
            if (diem >= 30) return 1;
        } else if (cc.contains("toeic")) {
            if (diem >= 490) return 3;
            if (diem >= 400) return 2;
            if (diem >= 275) return 1;
        } else if (cc.contains("pte")) {
            if (diem >= 76) return 3;
            if (diem >= 59) return 2;
            if (diem >= 43) return 1;
        } else if (cc.contains("linguaskill")) {
            if (diem >= 180) return 3;
            if (diem >= 160) return 2;
            if (diem >= 140) return 1;
        } else if (cc.contains("aptis")) {
            if (diemUpper.equals("C") || diemUpper.equals("C1")) return 3;
            if (diemUpper.equals("B2")) return 2;
            if (diemUpper.equals("B1")) return 1;
        } else if (cc.contains("vstep")) {
            if (diemUpper.contains("5")) return 3;
            if (diemUpper.contains("4")) return 2;
            if (diemUpper.contains("3")) return 1;
        }
        return 0;
    }

    private JTextField createReadOnlyField() {
        JTextField txt = new JTextField();
        txt.setEditable(false);
        txt.setBackground(new Color(245, 245, 245));
        txt.setForeground(new Color(0, 100, 0));
        txt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return txt;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void styleInput(JComponent comp, Dimension size) {
        comp.setPreferredSize(size);
        comp.setBackground(Color.WHITE);
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        Border line = new LineBorder(new Color(200, 200, 200), 1, true);
        Border padding = new EmptyBorder(6, 12, 6, 12);

        comp.setBorder(new CompoundBorder(line, padding));
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(new Color(245, 249, 255));

        JButton btnCancel = createButton("Hủy", new Color(220, 53, 69), Color.WHITE);
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
        btn.setPreferredSize(new Dimension(120, 38));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bg, 1, true));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return btn;
    }

    private boolean validateInput() {
        if (cbCccd.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD/SBD!");
            return false;
        }
        return true;
    }

    public boolean isConfirm() { return isConfirm; }
    
    public DiemCongXetTuyen getData() {
        if (data == null) {
            data = new DiemCongXetTuyen();
        }
        data.setTsCccd(cbCccd.getText().split(" - ")[0].trim());
        
        String cc = cbCC.getSelectedItem().toString();
        data.setChungChi("None".equals(cc) ? null : cc);
        data.setMucDatDuoc(txtMucCC.getText().trim());
        
        data.setCapGiai(txtCapGiai.getText().trim());
        data.setDoiTuongGiai(txtDoiTuongGiai.getText().trim());
        data.setMaMonGiai(txtMaMonGiai.getText().trim());
        data.setLoaiGiai(txtLoaiGiai.getText().trim());
        
        // Tạo dcKeys giả lập nếu chưa có
        if (data.getDcKeys() == null || data.getDcKeys().isEmpty()) {
            data.setDcKeys("KEY_" + data.getTsCccd() + "_" + System.currentTimeMillis());
        }
        
        return data;
    }

    public String getChungChiStr() { return cbCC.getSelectedItem().toString(); }
    public String getMucDatDuocStr() { return txtMucCC.getText().trim(); }
    
    // Gửi tạm dữ liệu về GUI tính toán nếu cần, tuy nhiên trong version này 
    // ta lấy trực tiếp từ data (loaiGiai, capGiai) cho vào BUS xử lý.
    public String getGiaiThuongStr() { 
        return !txtLoaiGiai.getText().trim().isEmpty() ? txtLoaiGiai.getText().trim() : "None"; 
    }
    public String getKhuVucStr() { return ""; } // Đã bỏ nhập khu vực, bus tự lấy từ DB
    public String getDoiTuongStr() { return ""; } // Đã bỏ nhập đối tượng, bus tự lấy từ DB

    private List<String> searchCccd(String keyword) {
        List<ThiSinh> res = bus.timKiemThiSinh(keyword);
        return res.stream()
                .map(ts -> ts.getCccd() + " - " + ts.getHo() + " " + ts.getTen())
                .collect(Collectors.toList());
    }
}
