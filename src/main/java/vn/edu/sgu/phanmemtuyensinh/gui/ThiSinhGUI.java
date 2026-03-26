package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import vn.edu.sgu.phanmemtuyensinh.bus.ThiSinhBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;

public class ThiSinhGUI extends JPanel {

    private static final int PAGE_SIZE = 20;

    private final ThiSinhBUS bus = new ThiSinhBUS();

    private JTextField txtTimKiem;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnImport;
    private JButton btnTim;
    private JButton btnLamMoi;
    private JButton btnTrangTruoc;
    private JButton btnTrangSau;
    private JLabel lblThongTinTrang;
    private JTable table;
    private DefaultTableModel tableModel;

    private int currentId = -1;
    private int currentPage = 1;
    private long totalItems = 0;
    private String currentKeyword = "";

    public ThiSinhGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buildTop();
        buildTable();
        buildBottom();

        loadPage();
    }

    private void buildTop() {
        JLabel lblTitle = new JLabel("QUẢN LÝ THÍ SINH", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlActions.setOpaque(false);
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnImport = new JButton("Import");
        btnLamMoi = new JButton("Làm Mới");
        pnlActions.add(btnThem);
        pnlActions.add(btnSua);
        pnlActions.add(btnXoa);
        pnlActions.add(btnImport);
        pnlActions.add(btnLamMoi);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.add(new JLabel("Tìm CCCD / Họ tên:"));
        txtTimKiem = new JTextField(22);
        btnTim = new JButton("Tìm");
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTim);

        JPanel pnlActionSearch = new JPanel(new BorderLayout(8, 8));
        pnlActionSearch.setOpaque(false);
        pnlActionSearch.add(pnlActions, BorderLayout.WEST);
        pnlActionSearch.add(pnlSearch, BorderLayout.EAST);

        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.setOpaque(false);
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        pnlTop.add(pnlActionSearch, BorderLayout.CENTER);

        btnThem.addActionListener(e -> themThiSinh());
        btnSua.addActionListener(e -> suaThiSinh());
        btnXoa.addActionListener(e -> xoaThiSinh());
        btnImport.addActionListener(e -> importThiSinh());
        btnTim.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> lamMoi());

        add(pnlTop, BorderLayout.NORTH);
    }

    private void buildTable() {
        String[] columns = {
                "ID", "CCCD", "Số Báo Danh", "Họ", "Tên", "Ngày Sinh", "Điện Thoại",
                "Mật Khẩu", "Giới Tính", "Email", "Nơi Sinh", "Updated At", "Đối Tượng", "Khu Vực"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        configureColumnWidths();
        table.getSelectionModel().addListSelectionListener(e -> chonDong());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void buildBottom() {
        JPanel pnlPaging = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlPaging.setOpaque(false);

        btnTrangTruoc = new JButton("Trang trước");
        btnTrangSau = new JButton("Trang sau");
        lblThongTinTrang = new JLabel("Trang 1/1");

        pnlPaging.add(btnTrangTruoc);
        pnlPaging.add(lblThongTinTrang);
        pnlPaging.add(btnTrangSau);

        btnTrangTruoc.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadPage();
            }
        });

        btnTrangSau.addActionListener(e -> {
            if (currentPage < getTotalPages()) {
                currentPage++;
                loadPage();
            }
        });

        add(pnlPaging, BorderLayout.SOUTH);
    }

    private void loadPage() {
        tableModel.setRowCount(0);

        List<ThiSinh> list;
        if (currentKeyword == null || currentKeyword.isBlank()) {
            totalItems = bus.countAll();
            list = bus.getPage(currentPage, PAGE_SIZE);
        } else {
            totalItems = bus.countByKeyword(currentKeyword);
            list = bus.searchByKeyword(currentKeyword, currentPage, PAGE_SIZE);
        }

        for (ThiSinh ts : list) {
            tableModel.addRow(new Object[]{
                    ts.getIdThiSinh(),
                    ts.getCccd(),
                    ts.getSoBaoDanh(),
                    ts.getHo(),
                    ts.getTen(),
                    ts.getNgaySinh(),
                    ts.getDienThoai(),
                    ts.getPassword(),
                    ts.getGioiTinh(),
                    ts.getEmail(),
                    ts.getNoiSinh(),
                    ts.getUpdatedAt(),
                    ts.getDoiTuong(),
                    ts.getKhuVuc()
            });
        }

        if (currentPage > getTotalPages()) {
            currentPage = Math.max(1, getTotalPages());
            loadPage();
            return;
        }

        updatePagingInfo();
    }

    private int getTotalPages() {
        if (totalItems <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalItems / PAGE_SIZE);
    }

    private void updatePagingInfo() {
        int totalPages = getTotalPages();
        lblThongTinTrang.setText("Trang " + currentPage + "/" + totalPages + " (" + totalItems + " dòng)");
        btnTrangTruoc.setEnabled(currentPage > 1);
        btnTrangSau.setEnabled(currentPage < totalPages);
    }

    private void timKiem() {
        currentKeyword = txtTimKiem.getText().trim();
        currentPage = 1;
        currentId = -1;
        table.clearSelection();
        loadPage();
    }

    private void lamMoi() {
        currentId = -1;
        currentPage = 1;
        currentKeyword = "";
        txtTimKiem.setText("");
        table.clearSelection();
        loadPage();
    }

    private void chonDong() {
        int row = table.getSelectedRow();
        if (row == -1) {
            currentId = -1;
            return;
        }
        Object id = tableModel.getValueAt(row, 0);
        currentId = Integer.parseInt(String.valueOf(id));
    }

    private void themThiSinh() {
        ThiSinh ts = hienThiFormThiSinh(null);
        if (ts == null) {
            return;
        }

        ts.setUpdatedAt(LocalDate.now().toString());
        if (bus.add(ts)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            currentPage = 1;
            loadPage();
            currentId = -1;
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError());
        }
    }

    private void suaThiSinh() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thí sinh cần sửa!");
            return;
        }

        ThiSinh current = bus.getById(currentId);
        if (current == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu thí sinh!");
            return;
        }

        ThiSinh updated = hienThiFormThiSinh(current);
        if (updated == null) {
            return;
        }

        updated.setIdThiSinh(currentId);
        if (updated.getPassword() == null || updated.getPassword().isBlank()) {
            updated.setPassword(current.getPassword());
        }
        updated.setUpdatedAt(LocalDate.now().toString());

        if (bus.update(updated)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadPage();
            table.clearSelection();
            currentId = -1;
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError());
        }
    }

    private void importThiSinh() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Dữ liệu thí sinh (*.xlsx, *.csv, *.txt)", "xlsx", "csv", "txt"));

        int choose = chooser.showOpenDialog(this);
        if (choose != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            int imported = bus.importAndSaveToDatabase(chooser.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                "Import hoàn tất!\n"
                    + bus.getLastImportSummary()
                    + "\n(Đã thêm vào DB: " + imported + ")");
            currentPage = 1;
            loadPage();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi import: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaThiSinh() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thí sinh!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa thí sinh này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (bus.delete(currentId)) {
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadPage();
            table.clearSelection();
            currentId = -1;
        }
    }

    private ThiSinh hienThiFormThiSinh(ThiSinh source) {
        JTextField txtCccd = new JTextField();
        JTextField txtSoBaoDanh = new JTextField();
        JTextField txtHo = new JTextField();
        JTextField txtTen = new JTextField();
        JTextField txtNgaySinh = new JTextField();
        JTextField txtDienThoai = new JTextField();
        JTextField txtMatKhau = new JTextField();
        JComboBox<String> cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        JTextField txtEmail = new JTextField();
        JTextField txtNoiSinh = new JTextField();
        JTextField txtDoiTuong = new JTextField();
        JTextField txtKhuVuc = new JTextField();

        Dimension inputSize = new Dimension(300, 30);
        txtCccd.setPreferredSize(inputSize);
        txtSoBaoDanh.setPreferredSize(inputSize);
        txtHo.setPreferredSize(inputSize);
        txtTen.setPreferredSize(inputSize);
        txtNgaySinh.setPreferredSize(inputSize);
        txtDienThoai.setPreferredSize(inputSize);
        txtMatKhau.setPreferredSize(inputSize);
        cboGioiTinh.setPreferredSize(inputSize);
        txtEmail.setPreferredSize(inputSize);
        txtNoiSinh.setPreferredSize(inputSize);
        txtDoiTuong.setPreferredSize(inputSize);
        txtKhuVuc.setPreferredSize(inputSize);

        if (source != null) {
            txtCccd.setText(nullToEmpty(source.getCccd()));
            txtSoBaoDanh.setText(nullToEmpty(source.getSoBaoDanh()));
            txtHo.setText(nullToEmpty(source.getHo()));
            txtTen.setText(nullToEmpty(source.getTen()));
            txtNgaySinh.setText(nullToEmpty(source.getNgaySinh()));
            txtDienThoai.setText(nullToEmpty(source.getDienThoai()));
            txtEmail.setText(nullToEmpty(source.getEmail()));
            txtNoiSinh.setText(nullToEmpty(source.getNoiSinh()));
            txtDoiTuong.setText(nullToEmpty(source.getDoiTuong()));
            txtKhuVuc.setText(nullToEmpty(source.getKhuVuc()));
            if (source.getGioiTinh() != null && !source.getGioiTinh().isBlank()) {
                cboGioiTinh.setSelectedItem(source.getGioiTinh());
            }
            txtCccd.setEditable(false);
        }

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(245, 249, 255));
        panel.setPreferredSize(new Dimension(740, 560));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(source == null ? new Color(30, 136, 229) : new Color(243, 156, 18));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel lblHeader = new JLabel(source == null ? "THÊM THÍ SINH" : "CẬP NHẬT THÍ SINH");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlHeader.add(lblHeader, BorderLayout.WEST);

        JPanel pnlCard = new JPanel(new BorderLayout());
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(213, 223, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setOpaque(false);
        form.add(new JLabel("CCCD:"));
        form.add(txtCccd);
        form.add(new JLabel("Số báo danh:"));
        form.add(txtSoBaoDanh);
        form.add(new JLabel("Họ:"));
        form.add(txtHo);
        form.add(new JLabel("Tên:"));
        form.add(txtTen);
        form.add(new JLabel("Ngày sinh:"));
        form.add(txtNgaySinh);
        form.add(new JLabel("Điện thoại:"));
        form.add(txtDienThoai);
        form.add(new JLabel("Mật khẩu (để trống nếu giữ nguyên):"));
        form.add(txtMatKhau);
        form.add(new JLabel("Giới tính:"));
        form.add(cboGioiTinh);
        form.add(new JLabel("Email:"));
        form.add(txtEmail);
        form.add(new JLabel("Nơi sinh:"));
        form.add(txtNoiSinh);
        form.add(new JLabel("Đối tượng:"));
        form.add(txtDoiTuong);
        form.add(new JLabel("Khu vực:"));
        form.add(txtKhuVuc);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);

        pnlCard.add(formScroll, BorderLayout.CENTER);
        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(pnlCard, BorderLayout.CENTER);
        ModernTheme.styleDialogContent(panel);

        int result = JOptionPane.showConfirmDialog(this, panel,
                source == null ? "Thêm thí sinh" : "Sửa thí sinh",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        ThiSinh ts = source == null ? new ThiSinh() : source;
        ts.setCccd(txtCccd.getText().trim());
        ts.setSoBaoDanh(txtSoBaoDanh.getText().trim());
        ts.setHo(txtHo.getText().trim());
        ts.setTen(txtTen.getText().trim());
        ts.setNgaySinh(txtNgaySinh.getText().trim());
        ts.setDienThoai(txtDienThoai.getText().trim());
        ts.setPassword(txtMatKhau.getText().trim());
        ts.setGioiTinh(String.valueOf(cboGioiTinh.getSelectedItem()));
        ts.setEmail(txtEmail.getText().trim());
        ts.setNoiSinh(txtNoiSinh.getText().trim());
        ts.setDoiTuong(txtDoiTuong.getText().trim());
        ts.setKhuVuc(txtKhuVuc.getText().trim());

        if (ts.getCccd().isBlank() || ts.getHo().isBlank() || ts.getTen().isBlank()) {
            JOptionPane.showMessageDialog(this, "CCCD, Họ, Tên không được để trống!");
            return null;
        }

        return ts;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void configureColumnWidths() {
        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(0).setPreferredWidth(70);
        columns.getColumn(1).setPreferredWidth(120);
        columns.getColumn(2).setPreferredWidth(120);
        columns.getColumn(3).setPreferredWidth(120);
        columns.getColumn(4).setPreferredWidth(120);
        columns.getColumn(5).setPreferredWidth(120);
        columns.getColumn(6).setPreferredWidth(120);
        columns.getColumn(7).setPreferredWidth(120);
        columns.getColumn(8).setPreferredWidth(100);
        columns.getColumn(9).setPreferredWidth(170);
        columns.getColumn(10).setPreferredWidth(150);
        columns.getColumn(11).setPreferredWidth(130);
        columns.getColumn(12).setPreferredWidth(110);
        columns.getColumn(13).setPreferredWidth(100);
    }
}
