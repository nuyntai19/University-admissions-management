package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.math.BigDecimal;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import vn.edu.sgu.phanmemtuyensinh.bus.NganhBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.Nganh;

public class NganhGUI extends JPanel {

    private final NganhBUS bus = new NganhBUS();
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnImport;
    private JButton btnLamMoi;
    private JButton btnTim;
    private JTextField txtTimKiem;
    private JTable table;
    private DefaultTableModel tableModel;
    private int currentId = -1;
    private String currentKeyword = "";

    public NganhGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("QUẢN LÝ NGÀNH TUYỂN SINH", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlButtons.setOpaque(false);
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnImport = new JButton("Import");
        btnLamMoi = new JButton("Làm Mới");
        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnImport);
        pnlButtons.add(btnLamMoi);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.add(new JLabel("Tìm mã/tên ngành:"));
        txtTimKiem = new JTextField(24);
        btnTim = new JButton("Tìm");
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTim);

        JPanel pnlActionSearch = new JPanel(new BorderLayout(8, 8));
        pnlActionSearch.setOpaque(false);
        pnlActionSearch.add(pnlButtons, BorderLayout.WEST);
        pnlActionSearch.add(pnlSearch, BorderLayout.EAST);

        JPanel pnlTop = new JPanel(new BorderLayout(0, 10));
        pnlTop.setOpaque(false);
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        pnlTop.add(pnlActionSearch, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        String[] columns = {
                "ID", "Mã Ngành", "Tên Ngành", "Tổ Hợp Gốc", "Chỉ Tiêu", "Điểm Sàn", "Điểm Trúng Tuyển",
                "N_TuyểnThẳng", "N_DGNL", "N_THPT", "N_VSAT", "SL_XTT", "SL_DGNL", "SL_VSAT", "SL_THPT"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        configureColumnWidths();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        btnThem.addActionListener(e -> themNganh());
        btnSua.addActionListener(e -> suaNganh());
        btnXoa.addActionListener(e -> xoaNganh());
        btnImport.addActionListener(e -> importNganh());
        btnTim.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> lamMoi());
        table.getSelectionModel().addListSelectionListener(e -> chonDong());

        loadDuLieu();
    }

    private void loadDuLieu() {
        tableModel.setRowCount(0);
        List<Nganh> list = (currentKeyword == null || currentKeyword.isBlank())
                ? bus.getAll()
                : bus.searchByKeyword(currentKeyword);

        for (Nganh n : list) {
            tableModel.addRow(new Object[]{
                    n.getIdNganh(),
                    n.getMaNganh(),
                    n.getTenNganh(),
                    n.getToHopGoc(),
                    n.getChiTieu(),
                    n.getDiemSan(),
                    n.getDiemTrungTuyen(),
                    n.getTuyenThang(),
                    n.getDgnl(),
                    n.getThpt(),
                    n.getVsat(),
                    n.getSlXtt(),
                    n.getSlDgnl(),
                    n.getSlVsat(),
                    n.getSlThpt()
            });
        }
    }

    private void themNganh() {
        Nganh n = hienThiFormNganh(null);
        if (n == null) {
            return;
        }
        if (bus.add(n)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadDuLieu();
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError());
        }
    }

    private void xoaNganh() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngành!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa ngành này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (bus.delete(currentId)) {
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadDuLieu();
            lamMoi();
        }
    }

    private void suaNganh() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngành cần sửa!");
            return;
        }

        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }

        Nganh current = bus.getById(currentId);
        if (current == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu ngành để sửa!");
            return;
        }

        Nganh n = hienThiFormNganh(current);
        if (n == null) {
            return;
        }
        n.setIdNganh(currentId);

        if (bus.update(n)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDuLieu();
            lamMoi();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError());
        }
    }

    private void importNganh() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file ngành tuyển sinh (.xlsx/.txt/.csv)");
        chooser.setFileFilter(new FileNameExtensionFilter("File dữ liệu", "xlsx", "txt", "csv"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        if (selected == null || !selected.exists()) {
            JOptionPane.showMessageDialog(this, "File không tồn tại!");
            return;
        }

        try {
            int success = bus.importAndSaveToDatabase(selected.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Import ngành hoàn tất.\n"
                            + bus.getLastImportSummary()
                            + "\nSố bản ghi thành công: " + success);
            currentKeyword = "";
            txtTimKiem.setText("");
            loadDuLieu();
            lamMoi();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Import thất bại: " + ex.getMessage());
        }
    }

    private void timKiem() {
        currentKeyword = txtTimKiem.getText().trim();
        loadDuLieu();
        lamMoi();
    }

    private void chonDong() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }

        Object idValue = tableModel.getValueAt(row, 0);
        if (idValue instanceof Integer) {
            currentId = (Integer) idValue;
        } else {
            currentId = Integer.parseInt(String.valueOf(idValue));
        }
    }

    private void lamMoi() {
        currentId = -1;
        table.clearSelection();
        if (txtTimKiem != null && txtTimKiem.getText().isBlank()) {
            currentKeyword = "";
        }
    }

    private Nganh hienThiFormNganh(Nganh source) {
        JTextField txtMaNganh = new JTextField();
        JTextField txtTenNganh = new JTextField();
        JTextField txtToHopGoc = new JTextField();
        JTextField txtChiTieu = new JTextField();
        JTextField txtDiemSan = new JTextField();
        JTextField txtDiemTrungTuyen = new JTextField();
        JTextField txtTuyenThang = new JTextField();
        JTextField txtDgnl = new JTextField();
        JTextField txtThpt = new JTextField();
        JTextField txtVsat = new JTextField();
        JTextField txtSlXtt = new JTextField();
        JTextField txtSlDgnl = new JTextField();
        JTextField txtSlVsat = new JTextField();
        JTextField txtSlThpt = new JTextField();

        if (source != null) {
            txtMaNganh.setText(source.getMaNganh());
            txtTenNganh.setText(source.getTenNganh());
            txtToHopGoc.setText(nullToEmpty(source.getToHopGoc()));
            txtChiTieu.setText(String.valueOf(source.getChiTieu()));
            txtDiemSan.setText(source.getDiemSan() == null ? "" : source.getDiemSan().toPlainString());
            txtDiemTrungTuyen.setText(source.getDiemTrungTuyen() == null ? "" : source.getDiemTrungTuyen().toPlainString());
            txtTuyenThang.setText(nullToEmpty(source.getTuyenThang()));
            txtDgnl.setText(nullToEmpty(source.getDgnl()));
            txtThpt.setText(nullToEmpty(source.getThpt()));
            txtVsat.setText(nullToEmpty(source.getVsat()));
            txtSlXtt.setText(String.valueOf(source.getSlXtt()));
            txtSlDgnl.setText(String.valueOf(source.getSlDgnl()));
            txtSlVsat.setText(String.valueOf(source.getSlVsat()));
            txtSlThpt.setText(nullToEmpty(source.getSlThpt()));
        }

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(245, 249, 255));
        panel.setPreferredSize(new Dimension(760, 560));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(source == null ? new Color(30, 136, 229) : new Color(243, 156, 18));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel lblHeader = new JLabel(source == null ? "THÊM NGÀNH TUYỂN SINH" : "CẬP NHẬT NGÀNH TUYỂN SINH");
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
        form.add(new JLabel("Mã ngành:"));
        form.add(txtMaNganh);
        form.add(new JLabel("Tên ngành:"));
        form.add(txtTenNganh);
        form.add(new JLabel("Tổ hợp gốc:"));
        form.add(txtToHopGoc);
        form.add(new JLabel("Chỉ tiêu:"));
        form.add(txtChiTieu);
        form.add(new JLabel("Điểm sàn:"));
        form.add(txtDiemSan);
        form.add(new JLabel("Điểm trúng tuyển:"));
        form.add(txtDiemTrungTuyen);
        form.add(new JLabel("N_TuyểnThẳng (0/1):"));
        form.add(txtTuyenThang);
        form.add(new JLabel("N_DGNL (0/1):"));
        form.add(txtDgnl);
        form.add(new JLabel("N_THPT (0/1):"));
        form.add(txtThpt);
        form.add(new JLabel("N_VSAT (0/1):"));
        form.add(txtVsat);
        form.add(new JLabel("SL_XTT:"));
        form.add(txtSlXtt);
        form.add(new JLabel("SL_DGNL:"));
        form.add(txtSlDgnl);
        form.add(new JLabel("SL_VSAT:"));
        form.add(txtSlVsat);
        form.add(new JLabel("SL_THPT:"));
        form.add(txtSlThpt);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        pnlFormCard.add(formScroll, BorderLayout.CENTER);
        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(pnlFormCard, BorderLayout.CENTER);
        ModernTheme.styleDialogContent(panel);

        int result = JOptionPane.showConfirmDialog(this, panel,
                source == null ? "Thêm ngành" : "Sửa ngành",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Nganh n = new Nganh();
        n.setMaNganh(txtMaNganh.getText().trim());
        n.setTenNganh(txtTenNganh.getText().trim());
        n.setToHopGoc(txtToHopGoc.getText().trim());
        try {
            n.setChiTieu(Integer.parseInt(txtChiTieu.getText().trim()));
            n.setDiemSan(parseDecimal(txtDiemSan.getText().trim()));
            n.setDiemTrungTuyen(parseDecimal(txtDiemTrungTuyen.getText().trim()));
            n.setTuyenThang(txtTuyenThang.getText().trim());
            n.setDgnl(txtDgnl.getText().trim());
            n.setThpt(txtThpt.getText().trim());
            n.setVsat(txtVsat.getText().trim());
            n.setSlXtt(parseInt(txtSlXtt.getText().trim()));
            n.setSlDgnl(parseInt(txtSlDgnl.getText().trim()));
            n.setSlVsat(parseInt(txtSlVsat.getText().trim()));
            n.setSlThpt(txtSlThpt.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu số không hợp lệ!");
            return null;
        }

        return n;
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.contains(",") && !value.contains(".")) {
            value = value.replace(',', '.');
        }
        return new BigDecimal(value);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void configureColumnWidths() {
        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(0).setPreferredWidth(70);
        columns.getColumn(1).setPreferredWidth(120);
        columns.getColumn(2).setPreferredWidth(260);
        columns.getColumn(3).setPreferredWidth(100);
        columns.getColumn(4).setPreferredWidth(90);
        columns.getColumn(5).setPreferredWidth(100);
        columns.getColumn(6).setPreferredWidth(130);
        columns.getColumn(7).setPreferredWidth(110);
        columns.getColumn(8).setPreferredWidth(90);
        columns.getColumn(9).setPreferredWidth(90);
        columns.getColumn(10).setPreferredWidth(90);
        columns.getColumn(11).setPreferredWidth(90);
        columns.getColumn(12).setPreferredWidth(90);
        columns.getColumn(13).setPreferredWidth(90);
        columns.getColumn(14).setPreferredWidth(110);
    }
}
