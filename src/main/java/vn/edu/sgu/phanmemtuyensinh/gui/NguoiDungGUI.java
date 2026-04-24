package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import vn.edu.sgu.phanmemtuyensinh.bus.NguoiDungBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguoiDung;

public class NguoiDungGUI extends JPanel {

    private final NguoiDungBUS bus = new NguoiDungBUS();
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;
    private JTable table;
    private DefaultTableModel tableModel;
    private int currentId = -1;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{0,9}$");

    public NguoiDungGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ NGƯỜI DÙNG", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);
        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.setOpaque(false);
        pnlTop.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlButtons.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm Mới");
        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);
        pnlTop.add(pnlButtons, BorderLayout.CENTER);

        add(pnlTop, BorderLayout.NORTH);

        // Table Panel
        String[] columns = {
            "ID", "Tài Khoản", "Mật Khẩu", "Họ Tên", "Email", "Điện Thoại",
            "Phân Quyền", "Trạng Thái", "Ngày Tạo", "Ngày Sửa"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        configureColumnWidths();
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Events
        btnThem.addActionListener(e -> themNguoiDung());
        btnSua.addActionListener(e -> suaNguoiDung());
        btnXoa.addActionListener(e -> xoaNguoiDung());
        btnLamMoi.addActionListener(e -> lamMoi());
        table.getSelectionModel().addListSelectionListener(e -> chonDong());

        loadDuLieu();
    }

    private void loadDuLieu() {
        tableModel.setRowCount(0);
        List<NguoiDung> list = bus.getAll();
        for (NguoiDung nd : list) {
            tableModel.addRow(new Object[]{
                    nd.getIdNguoiDung(),
                    nd.getTaiKhoan(),
                    nd.getMatKhau(),
                    nd.getHoTen(),
                    nd.getEmail(),
                    nd.getDienThoai(),
                    nd.getPhanQuyen(),
                    nd.getTrangThaiHoatDong() == 1 ? "Hoạt động" : "Khóa",
                    nd.getNgayTao(),
                    nd.getNgaySua()
            });
        }
    }

    private void themNguoiDung() {
        NguoiDung nd = hienThiFormNguoiDung(null);
        if (nd == null) {
            return;
        }

        if (bus.getByTaiKhoan(nd.getTaiKhoan()) != null) {
            JOptionPane.showMessageDialog(this, "Tài khoản đã tồn tại, vui lòng chọn tài khoản khác!");
            return;
        }

        nd.setNgayTao(LocalDateTime.now());

        if (bus.add(nd)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadDuLieu();
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError());
        }
    }

    private void suaNguoiDung() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng!");
            return;
        }

        String taiKhoan = String.valueOf(tableModel.getValueAt(table.getSelectedRow(), 1));
        NguoiDung current = bus.getByTaiKhoan(taiKhoan);
        if (current == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu người dùng để sửa!");
            return;
        }

        NguoiDung updated = hienThiFormNguoiDung(current);
        if (updated == null) {
            return;
        }

        NguoiDung duplicate = bus.getByTaiKhoan(updated.getTaiKhoan());
        if (duplicate != null && duplicate.getIdNguoiDung() != current.getIdNguoiDung()) {
            JOptionPane.showMessageDialog(this, "Tài khoản đã tồn tại, vui lòng chọn tài khoản khác!");
            return;
        }

        updated.setIdNguoiDung(currentId);
        if (updated.getMatKhau() == null || updated.getMatKhau().isBlank()) {
            updated.setMatKhau(current.getMatKhau());
        }
        updated.setNgayTao(current.getNgayTao());
        updated.setNgaySua(LocalDateTime.now());

        if (bus.update(updated)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDuLieu();
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError());
        }
    }

    private void xoaNguoiDung() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa?");
        if (confirm == JOptionPane.YES_OPTION) {
            if (bus.delete(currentId)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadDuLieu();
                lamMoi();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError());
            }
        }
    }

    private void chonDong() {
        if (table.getSelectedRow() != -1) {
            currentId = (int) tableModel.getValueAt(table.getSelectedRow(), 0);
        }
    }

    private void lamMoi() {
        currentId = -1;
        table.clearSelection();
    }

    private NguoiDung hienThiFormNguoiDung(NguoiDung source) {
        JTextField txtTaiKhoan = new JTextField();
        JPasswordField txtMatKhau = new JPasswordField();
        JTextField txtHoTen = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtDienThoai = new JTextField();
        JComboBox<String> comboTrangThai = new JComboBox<>(new String[]{"Hoạt động", "Khóa"});
        
        // Phân quyền - sử dụng ComboBox với 2 lựa chọn: admin và user
        JComboBox<String> comboQuyen = new JComboBox<>(new String[]{"admin", "user"});

        if (source != null) {
            txtTaiKhoan.setText(source.getTaiKhoan());
            txtHoTen.setText(source.getHoTen() == null ? "" : source.getHoTen());
            txtEmail.setText(source.getEmail() == null ? "" : source.getEmail());
            txtDienThoai.setText(source.getDienThoai() == null ? "" : source.getDienThoai());
            comboQuyen.setSelectedItem(source.getPhanQuyen() != null ? source.getPhanQuyen() : "user");
            comboTrangThai.setSelectedItem(source.getTrangThaiHoatDong() == 1 ? "Hoạt động" : "Khóa");
            txtTaiKhoan.setEditable(false);
        } else {
            comboQuyen.setSelectedItem("user");
            comboTrangThai.setSelectedItem("Hoạt động");
        }

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(245, 249, 255));
        panel.setPreferredSize(new Dimension(560, 330));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(source == null ? new Color(30, 136, 229) : new Color(243, 156, 18));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel lblHeader = new JLabel(source == null ? "THÊM NGƯỜI DÙNG" : "CẬP NHẬT NGƯỜI DÙNG");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlHeader.add(lblHeader, BorderLayout.WEST);

        JPanel pnlFormCard = new JPanel(new BorderLayout());
        pnlFormCard.setOpaque(true);
        pnlFormCard.setBackground(Color.WHITE);
        pnlFormCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(213, 223, 240)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setOpaque(false);
        form.add(new JLabel("Tài khoản:"));
        form.add(txtTaiKhoan);
        form.add(new JLabel("Mật khẩu (để trống nếu giữ nguyên):"));
        form.add(txtMatKhau);
        form.add(new JLabel("Họ tên:"));
        form.add(txtHoTen);
        form.add(new JLabel("Email:"));
        form.add(txtEmail);
        form.add(new JLabel("Điện thoại:"));
        form.add(txtDienThoai);
        form.add(new JLabel("Phân quyền:"));
        form.add(comboQuyen);
        if (source != null) {
            form.add(new JLabel("Trạng thái:"));
            form.add(comboTrangThai);
        }

        pnlFormCard.add(form, BorderLayout.CENTER);
        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(pnlFormCard, BorderLayout.CENTER);
        ModernTheme.styleDialogContent(panel);

        // Vòng lặp xử lý với validation
        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel,
                    source == null ? "Thêm người dùng" : "Sửa người dùng",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            // Lấy giá trị từ các trường
            String taiKhoan = txtTaiKhoan.getText().trim();
            String matKhau = new String(txtMatKhau.getPassword()).trim();
            String hoTen = txtHoTen.getText().trim();
            String email = txtEmail.getText().trim();
            String dienThoai = txtDienThoai.getText().trim();

            // === VALIDATION ===
            if (taiKhoan.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Tài khoản không được để trống!", 
                    "Lỗi Validation", 
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            if (source == null && matKhau.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Mật khẩu không được để trống khi thêm người dùng!", 
                    "Lỗi Validation", 
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            if (hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Họ tên không được để trống!", 
                    "Lỗi Validation", 
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Email không được để trống!", 
                    "Lỗi Validation", 
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                JOptionPane.showMessageDialog(this,
                    "❌ Email không hợp lệ!",
                    "Lỗi Validation",
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            if (dienThoai.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Điện thoại không được để trống!", 
                    "Lỗi Validation", 
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!PHONE_PATTERN.matcher(dienThoai).matches()) {
                JOptionPane.showMessageDialog(this,
                    "❌ Số điện thoại phải bắt đầu bằng số 0 và tối đa 10 chữ số!",
                    "Lỗi Validation",
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }

            // Nếu validation pass, tạo object NguoiDung
            NguoiDung nd = new NguoiDung();
            nd.setTaiKhoan(taiKhoan);
            nd.setMatKhau(matKhau);
            nd.setHoTen(hoTen);
            nd.setEmail(email);
            nd.setDienThoai(dienThoai);
                nd.setPhanQuyen((String) comboQuyen.getSelectedItem());
                nd.setTrangThaiHoatDong(source == null
                    ? 1
                    : ("Hoạt động".equals(comboTrangThai.getSelectedItem()) ? 1 : 0));

            return nd;
        }
    }

    private void configureColumnWidths() {
        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(0).setPreferredWidth(60);
        columns.getColumn(1).setPreferredWidth(120);
        columns.getColumn(2).setPreferredWidth(120);
        columns.getColumn(3).setPreferredWidth(170);
        columns.getColumn(4).setPreferredWidth(170);
        columns.getColumn(5).setPreferredWidth(120);
        columns.getColumn(6).setPreferredWidth(100);
        columns.getColumn(7).setPreferredWidth(90);
        columns.getColumn(8).setPreferredWidth(160);
        columns.getColumn(9).setPreferredWidth(160);
    }
}
