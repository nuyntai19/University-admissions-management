package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.*;
import vn.edu.sgu.phanmemtuyensinh.bus.NguyenVongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.NganhToHopDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.ThiSinhDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.DiemThiXetTuyenDAO;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.utils.AutoSuggestComboBox;

public class NguyenVongXetTuyenDialog extends JDialog {

    private static final Color ACCENT      = new Color(25, 118, 210);
    private static final Color BG          = new Color(245, 248, 252);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color BORDER_COL  = new Color(207, 216, 220);
    private static final Color LABEL_COL   = new Color(60, 70, 80);
    private static final Color READONLY_BG = new Color(240, 243, 247);
    private static final Font  FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font  FONT_INPUT  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_SEC    = new Font("Segoe UI", Font.BOLD, 12);

    private final ThiSinhDAO thiSinhDAO         = new ThiSinhDAO();
    private final NganhToHopDAO nganhToHopDAO   = new NganhToHopDAO();
    private final DiemThiXetTuyenDAO diemDao    = new DiemThiXetTuyenDAO();
    private final NguyenVongXetTuyenBUS bus     = new NguyenVongXetTuyenBUS();

    private AutoSuggestComboBox cbCccd;
    private AutoSuggestComboBox cbNganh;
    private JComboBox<String> cbPhuongThuc;
    private JTextField txtThuTu;
    private JTextField txtDiemThxt, txtDiemUtqd, txtDiemCong, txtDiemXet;
    private JLabel lblHeaderTitle;

    private boolean isConfirm  = false;
    private boolean isEditMode = false;
    private int editIdNv       = -1;

    public NguyenVongXetTuyenDialog(Frame parent) {
        super(parent, "Nguyện vọng", true);
        setSize(750, 650); // Tăng kích thước cửa sổ
        setMinimumSize(new Dimension(700, 600));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(ACCENT);
        p.setBorder(new EmptyBorder(14, 20, 14, 20));
        lblHeaderTitle = new JLabel("THÊM NGUYỆN VỌNG");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeaderTitle.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Điền thông tin nguyện vọng xét tuyển");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(180, 215, 255));
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        textPanel.setOpaque(false);
        textPanel.add(lblHeaderTitle);
        textPanel.add(sub);
        p.add(textPanel, BorderLayout.WEST);
        return p;
    }

    private JScrollPane buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG);
        form.setBorder(new EmptyBorder(14, 16, 8, 16));

        form.add(sectionLabel("1. Thí sinh"));
        form.add(Box.createVerticalStrut(6));
        JPanel card1 = card();
        card1.setLayout(new GridBagLayout());
        GridBagConstraints gbc = gbc();
        cbCccd = new AutoSuggestComboBox(this::searchCccd);
        styleCombo(cbCccd);
        cbCccd.addActionListener(e -> onCccdChanged());
        txtThuTu = new JTextField();
        txtThuTu.setEditable(false);
        txtThuTu.setBackground(READONLY_BG);
        styleField(txtThuTu);
        cbPhuongThuc = new JComboBox<>(new String[]{"THPT", "ĐGNL", "V-SAT"});
        styleCbPt(cbPhuongThuc);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        card1.add(label("CCCD / Số báo danh:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        card1.add(cbCccd, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        card1.add(label("Thứ tự NV (tự động):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        card1.add(txtThuTu, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        card1.add(label("Phương thức xét tuyển:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        card1.add(cbPhuongThuc, gbc);
        form.add(card1);
        form.add(Box.createVerticalStrut(12));

        form.add(sectionLabel("2. Ngành đăng ký"));
        form.add(Box.createVerticalStrut(6));
        JPanel card2 = card();
        card2.setLayout(new GridBagLayout());
        GridBagConstraints gbc2 = gbc();
        cbNganh = new AutoSuggestComboBox(this::searchNganh);
        styleCombo(cbNganh);
        gbc2.gridx = 0; gbc2.gridy = 0; gbc2.weightx = 0.3;
        card2.add(label("Ngành (Mã - Tên - Tổ hợp):"), gbc2);
        gbc2.gridx = 1; gbc2.weightx = 0.7;
        card2.add(cbNganh, gbc2);
        form.add(card2);
        form.add(Box.createVerticalStrut(12));

        form.add(sectionLabel("3. Kết quả điểm (do hệ thống tính sau khi Xét tuyển)"));
        form.add(Box.createVerticalStrut(6));
        txtDiemThxt = readonlyField();
        txtDiemUtqd = readonlyField();
        txtDiemCong = readonlyField();
        txtDiemXet  = readonlyField();
        JPanel card3 = card();
        card3.setLayout(new GridBagLayout());
        GridBagConstraints gbc3 = gbc();
        gbc3.insets = new Insets(5, 5, 5, 5);
        // Hàng 1
        gbc3.gridx = 0; gbc3.gridy = 0; gbc3.weightx = 0.2;
        card3.add(label("Điểm THXT:"), gbc3);
        gbc3.gridx = 1; gbc3.weightx = 0.3;
        card3.add(txtDiemThxt, gbc3);
        gbc3.gridx = 2; gbc3.weightx = 0.2;
        card3.add(label("Điểm UTQD:"), gbc3);
        gbc3.gridx = 3; gbc3.weightx = 0.3;
        card3.add(txtDiemUtqd, gbc3);
        // Hàng 2
        gbc3.gridx = 0; gbc3.gridy = 1; gbc3.weightx = 0.2;
        card3.add(label("Điểm cộng:"), gbc3);
        gbc3.gridx = 1; gbc3.weightx = 0.3;
        card3.add(txtDiemCong, gbc3);
        gbc3.gridx = 2; gbc3.weightx = 0.2;
        card3.add(label("Điểm xét tuyển:"), gbc3);
        gbc3.gridx = 3; gbc3.weightx = 0.3;
        card3.add(txtDiemXet, gbc3);
        form.add(card3);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BG);
        return scroll;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        p.setBackground(new Color(236, 240, 245));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));
        JButton btnCancel = actionButton("Hủy", new Color(108, 117, 125));
        JButton btnSave   = actionButton("Lưu", ACCENT);
        btnCancel.addActionListener(e -> { isConfirm = false; dispose(); });
        btnSave.addActionListener(e -> { if (validate2()) { isConfirm = true; dispose(); } });
        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SEC);
        lbl.setForeground(ACCENT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL, 1, true),
                new EmptyBorder(12, 14, 12, 14)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return p;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 5, 5, 5);
        g.weightx = 1.0;
        return g;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(LABEL_COL);
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(FONT_INPUT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL, 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        f.setPreferredSize(new Dimension(200, 34)); // Giảm preferred width để tránh cuộn ngang
    }

    private JTextField readonlyField() {
        JTextField f = new JTextField();
        f.setEditable(false);
        f.setBackground(READONLY_BG);
        styleField(f);
        return f;
    }

    private void styleCombo(AutoSuggestComboBox cb) {
        cb.setFont(FONT_INPUT);
        cb.setPreferredSize(new Dimension(300, 34));
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL, 1, true),
                new EmptyBorder(3, 8, 3, 8)));
    }

    private void styleCbPt(JComboBox<String> cb) {
        cb.setFont(FONT_INPUT);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(200, 34));
    }

    private JButton actionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(110, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private List<String> searchCccd(String keyword) {
        return thiSinhDAO.searchByKeyword(keyword, 25)
                .stream()
                .map(ts -> ts.getCccd() + " - " + ts.getHo() + " " + ts.getTen())
                .collect(Collectors.toList());
    }

    private List<String> searchNganh(String keyword) {
        return nganhToHopDAO.searchNganh(keyword, 30)
                .stream()
                .map(nth -> nth.getMaNganh() + " - " + nth.getTenNganhChuan() + " - " + nth.getMaToHop())
                .collect(Collectors.toList());
    }

    private void onCccdChanged() {
        if (isEditMode) return;
        String raw = cbCccd.getText().trim();
        if (raw.isEmpty()) return;
        String cccd = raw.contains(" - ") ? raw.split(" - ")[0].trim() : raw;
        if (!cccd.isEmpty()) {
            int next = bus.getNextThuTu(cccd);
            txtThuTu.setText(String.valueOf(next));
        }
    }

    public void setData(NguyenVongXetTuyen nv) {
        isEditMode = true;
        editIdNv   = nv.getIdNv();
        setTitle("Sửa nguyện vọng");
        lblHeaderTitle.setText("SỬA NGUYỆN VỌNG");
        cbCccd.setText(nv.getNvCccd());
        cbCccd.setEnabled(false);
        txtThuTu.setText(String.valueOf(nv.getNvTt()));
        String pm = nv.getTtPhuongThuc() != null ? nv.getTtPhuongThuc() : "THPT";
        cbPhuongThuc.setSelectedItem(pm);
        String nganhTxt = nv.getNvMaNganh() != null ? nv.getNvMaNganh() : "";
        if (nv.getNvTenMaNganh() != null) nganhTxt += " - " + nv.getNvTenMaNganh();
        if (nv.getTtThm() != null && !nv.getTtThm().isEmpty()) nganhTxt += " - " + nv.getTtThm();
        cbNganh.setText(nganhTxt);
        if (nv.getDiemThxt()    != null) txtDiemThxt.setText(nv.getDiemThxt().toPlainString());
        if (nv.getDiemUtqd()    != null) txtDiemUtqd.setText(nv.getDiemUtqd().toPlainString());
        if (nv.getDiemCong()    != null) txtDiemCong.setText(nv.getDiemCong().toPlainString());
        if (nv.getDiemXetTuyen()!= null) txtDiemXet.setText(nv.getDiemXetTuyen().toPlainString());
    }

    public void resetForm() {
        isEditMode = false;
        editIdNv   = -1;
        setTitle("Thêm nguyện vọng");
        lblHeaderTitle.setText("THÊM NGUYỆN VỌNG");
        cbCccd.setText("");
        cbCccd.setEnabled(true);
        cbNganh.setText("");
        txtThuTu.setText("");
        cbPhuongThuc.setSelectedIndex(0);
        txtDiemThxt.setText(""); txtDiemUtqd.setText("");
        txtDiemCong.setText(""); txtDiemXet.setText("");
        isConfirm = false;
    }

    private boolean validate2() {
        String rawCccd = cbCccd.getText().trim();
        if (rawCccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        String cccd = getCccd();
        // 1. Kiểm tra thí sinh tồn tại
        if (thiSinhDAO.getByCccd(cccd) == null) {
            JOptionPane.showMessageDialog(this, "CCCD này không tồn tại trong danh sách thí sinh!", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 2. Kiểm tra điểm thi tồn tại cho phương thức đã chọn
        String pt = getPhuongThuc();
        if (diemDao.getByCcqdAndPhuongThuc(cccd, pt) == null) {
            JOptionPane.showMessageDialog(this, "Thí sinh này chưa có dữ liệu điểm thi cho phương thức " + pt + "!", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String nganhRaw = cbNganh.getText().trim();
        if (nganhRaw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngành!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Kiểm tra định dạng Mã - Tên - Tổ hợp (phải có ít nhất 2 dấu gạch ngang phân cách)
        String[] parts = nganhRaw.split(" - ");
        if (parts.length < 3 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty() || parts[2].trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn ngành từ danh sách gợi ý hoặc nhập đúng định dạng:\nMã ngành - Tên ngành - Tổ hợp môn", 
                "Sai định dạng", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isConfirm()  { return isConfirm; }
    public boolean isEditMode() { return isEditMode; }
    public int getEditIdNv()    { return editIdNv; }

    public String getCccd() {
        String t = cbCccd.getText().trim();
        return t.contains(" - ") ? t.split(" - ")[0].trim() : t;
    }

    public String getMaNganh() {
        String[] parts = cbNganh.getText().trim().split(" - ");
        return parts.length > 0 ? parts[0].trim() : "";
    }

    public String getTenNganh() {
        String[] parts = cbNganh.getText().trim().split(" - ");
        return parts.length > 1 ? parts[1].trim() : "";
    }

    public String getToHop() {
        String[] parts = cbNganh.getText().trim().split(" - ");
        return parts.length > 2 ? parts[2].trim() : "";
    }

    public int getThuTu() {
        try { return Integer.parseInt(txtThuTu.getText().trim()); }
        catch (NumberFormatException e) { return 1; }
    }

    public String getPhuongThuc() {
        return cbPhuongThuc.getSelectedItem().toString();
    }
}