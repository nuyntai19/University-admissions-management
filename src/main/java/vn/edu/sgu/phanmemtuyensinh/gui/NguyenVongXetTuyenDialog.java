package vn.edu.sgu.phanmemtuyensinh.gui;

import vn.edu.sgu.phanmemtuyensinh.utils.AutoSuggestComboBox;
import vn.edu.sgu.phanmemtuyensinh.bus.NguyenVongXetTuyenBUS;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NguyenVongXetTuyenDialog extends JDialog {

    private AutoSuggestComboBox cbCccd;
    private AutoSuggestComboBox cbNganh;
    private JComboBox<String> cbPhuongThuc;

    private JTextField txtThuTu;
    private JTextField txtDiemThxt;
    private JTextField txtDiemUtqd;
    private JTextField txtDiemCong;
    private JTextField txtDiemXet;

    private boolean isConfirm = false;

    private NguyenVongXetTuyenBUS bus = new NguyenVongXetTuyenBUS();

    public NguyenVongXetTuyenDialog(Frame parent) {
        super(parent, "Thêm nguyện vọng", true);

        setSize(950, 700);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(parent);

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 249, 255));

        add(createContent(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);
    }

    private JPanel createContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(30, 136, 229));
        pnlHeader.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblHeader = new JLabel("THÊM NGUYỆN VỌNG");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pnlHeader.add(lblHeader, BorderLayout.WEST);

        JPanel pnlCard = new JPanel(new BorderLayout());
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(213, 223, 240)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel form = new JPanel(new GridLayout(0, 2, 15, 15));
        form.setOpaque(false);

        Dimension inputSize = new Dimension(380, 36);

        cbCccd = new AutoSuggestComboBox(this::mockSearchCccd);
        cbNganh = new AutoSuggestComboBox(this::mockSearchNganh);

        cbPhuongThuc = new JComboBox<>(new String[]{"THPT", "DGNL", "V-SAT"});

        txtThuTu = new JTextField();
        txtThuTu.setEditable(false);

        txtDiemThxt = createReadOnlyField();
        txtDiemUtqd = createReadOnlyField();
        txtDiemCong = createReadOnlyField();
        txtDiemXet = createReadOnlyField();

        styleInput(cbCccd, inputSize);
        styleInput(cbNganh, inputSize);
        styleInput(cbPhuongThuc, inputSize);
        styleInput(txtThuTu, inputSize);
        styleInput(txtDiemThxt, inputSize);
        styleInput(txtDiemUtqd, inputSize);
        styleInput(txtDiemCong, inputSize);
        styleInput(txtDiemXet, inputSize);

        // ===== FORM =====
        form.add(label("CCCD/Số báo danh:"));
        form.add(cbCccd);

        form.add(label("Nguyện vọng:"));
        form.add(txtThuTu);

        form.add(label("Phương thức:"));
        form.add(cbPhuongThuc);

        form.add(label("Ngành (Mã - Tên - Tổ hợp):"));
        form.add(cbNganh);

        form.add(label("Điểm THXT:"));
        form.add(txtDiemThxt);

        form.add(label("Điểm UTQD:"));
        form.add(txtDiemUtqd);

        form.add(label("Điểm cộng:"));
        form.add(txtDiemCong);

        form.add(label("Điểm xét tuyển:"));
        form.add(txtDiemXet);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        pnlCard.add(scroll, BorderLayout.CENTER);

        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(pnlCard, BorderLayout.CENTER);

        return panel;
    }

    private JTextField createReadOnlyField() {
        JTextField txt = new JTextField();
        txt.setEditable(false);
        txt.setBackground(new Color(245, 245, 245));
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

        Border line = new LineBorder(new Color(200, 200, 200), 1, true);
        Border padding = new EmptyBorder(6, 12, 6, 12);

        comp.setBorder(new CompoundBorder(line, padding));
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(new Color(245, 249, 255));

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
        btn.setPreferredSize(new Dimension(120, 38));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bg, 1, true));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    private boolean validateInput() {
        if (cbCccd.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn CCCD!");
            return false;
        }

        if (cbNganh.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn ngành!");
            return false;
        }

        return true;
    }

    // ===== GETTER =====
    public boolean isConfirm() { return isConfirm; }

    public String getCccd() {
        return cbCccd.getText().split(" - ")[0];
    }

    public String getMaNganh() {
        return cbNganh.getText().split(" - ")[0];
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
                "079307000003 - Lê Văn C",
                "077307000004 - Phạm Thị D",
                "078307000005 - Lê Minh E"
        ).stream()
                .filter(s -> s.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> mockSearchNganh(String keyword) {
        return Arrays.asList(
                "7140201 - Giáo dục Tiểu học - A00",
                "7140201 - Giáo dục Tiểu học - A01",

                "7140211 - Sư phạm Vật lý - A00",
                "7140211 - Sư phạm Vật lý - A01",
                "7140211 - Sư phạm Vật lý - A02",

                "7340201 - Tài chính ngân hàng - A00",
                "7340201 - Tài chính ngân hàng - D01",

                "7480201 - Công nghệ thông tin - A00",
                "7480201 - Công nghệ thông tin - A01",
                "7480201 - Công nghệ thông tin - D01",

                "7480101 - Khoa học máy tính - A00",
                "7480101 - Khoa học máy tính - A01",

                "7220201 - Ngôn ngữ Anh - D01",
                "7220201 - Ngôn ngữ Anh - D14",

                "7310101 - Kinh tế - A00",
                "7310101 - Kinh tế - D01",

                "7340101 - Quản trị kinh doanh - A00",
                "7340101 - Quản trị kinh doanh - D01",
                "7340101 - Quản trị kinh doanh - C01",

                "7510201 - Kỹ thuật cơ khí - A00",
                "7510201 - Kỹ thuật cơ khí - A01",

                "7510301 - Kỹ thuật điện - A00",
                "7510301 - Kỹ thuật điện - A01",
                "7510301 - Kỹ thuật điện - A02"
        ).stream()
                .filter(s -> s.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}