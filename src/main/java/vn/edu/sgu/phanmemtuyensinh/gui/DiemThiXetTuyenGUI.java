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
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import vn.edu.sgu.phanmemtuyensinh.bus.DiemThiXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen;

public class DiemThiXetTuyenGUI extends JPanel {

    private static final int PAGE_SIZE = 20;

    private final DiemThiXetTuyenBUS bus = new DiemThiXetTuyenBUS();

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
    private JComboBox<String> cbbSort;

    private int currentId = -1;
    private int currentPage = 1;
    private long totalItems = 0;
    private String currentKeyword = "";
    private String currentSortOrder = "DESC";

    public DiemThiXetTuyenGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buildTop();
        buildTable();
        buildBottom();

        loadPage();
    }

    private void buildTop() {
        JLabel lblTitle = new JLabel("QUẢN LÝ ĐIỂM THI XÉT TUYỂN", JLabel.CENTER);
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

        JPanel pnlSort = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pnlSort.setOpaque(false);
        cbbSort = new JComboBox<>(new String[]{"Lớn nhất đến bé nhất", "Bé nhất đến lớn nhất"});
        cbbSort.setSelectedIndex(0);
        cbbSort.addActionListener(e -> {
            String selected = (String) cbbSort.getSelectedItem();
            currentSortOrder = "Bé nhất đến lớn nhất".equals(selected) ? "ASC" : "DESC";
            currentPage = 1;
            currentId = -1;
            table.clearSelection();
            loadPage();
        });
        pnlSort.add(cbbSort);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.add(new JLabel("Tìm CCCD / SBD:"));
        txtTimKiem = new JTextField(22);
        btnTim = new JButton("Tìm");
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTim);

        JPanel pnlActionSearchSort = new JPanel(new BorderLayout(8, 8));
        pnlActionSearchSort.setOpaque(false);
        pnlActionSearchSort.add(pnlActions, BorderLayout.WEST);
        pnlActionSearchSort.add(pnlSort, BorderLayout.CENTER);
        pnlActionSearchSort.add(pnlSearch, BorderLayout.EAST);

        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.setOpaque(false);
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        pnlTop.add(pnlActionSearchSort, BorderLayout.CENTER);

        btnThem.addActionListener(e -> themDiem());
        btnSua.addActionListener(e -> suaDiem());
        btnXoa.addActionListener(e -> xoaDiem());
        btnImport.addActionListener(e -> importDiem());
        btnTim.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> lamMoi());

        add(pnlTop, BorderLayout.NORTH);
    }

    private void buildTable() {
        String[] columns = {
                "ID", "CCCD", "Số Báo Danh", "Phương Thức",
                "TO", "LI", "HO", "SI", "SU", "DI", "VA", "GDCD",
                "N1_THI", "N1_CC", "CNCN", "CNNN", "TI", "KTPL", "NL1", 
                "NK1", "NK2", "NK3", "NK4", "NK5", "NK6", "NK7", "NK8", "NK9", "NK10",
                "Điểm xét TN"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> chonDong());
        
        // Đặt độ rộng cột mặc định
        table.getColumnModel().getColumn(0).setPreferredWidth(120); // ID - ngang với CCCD
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // CCCD
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Số Báo Danh
        table.getColumnModel().getColumn(3).setPreferredWidth(90);  // Phương Thức
        
        // Các cột điểm - đặt width nhỏ và không cho phép resize
        for (int i = 4; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(55);
            table.getColumnModel().getColumn(i).setResizable(false);
        }
        
        // Tắt tính năng auto-resize để giữ width cố định
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
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

        List<DiemThiXetTuyen> list;
        if (currentKeyword == null || currentKeyword.isBlank()) {
            totalItems = bus.countAll();
            list = bus.getPageWithSort(currentPage, PAGE_SIZE, currentSortOrder);
        } else {
            totalItems = bus.countByKeyword(currentKeyword);
            list = bus.searchByKeyword(currentKeyword, currentPage, PAGE_SIZE);
        }

        for (DiemThiXetTuyen d : list) {
            tableModel.addRow(new Object[]{
                    d.getIdDiemThi(),
                    d.getCccd(),
                    d.getSoBaoDanh(),
                    d.getPhuongThuc(),
                    d.getTo(),
                    d.getLi(),
                    d.getHo(),
                    d.getSi(),
                    d.getSu(),
                    d.getDi(),
                    d.getVa(),
                    d.getGdcd(),
                    d.getN1Thi(),
                    d.getN1Cc(),
                    d.getCncn(),
                    d.getCnnn(),
                    d.getTi(),
                    d.getKtpl(),
                    d.getNl1(),
                    d.getNk1(),
                    d.getNk2(),
                    d.getNk3(),
                    d.getNk4(),
                    d.getNk5(),
                    d.getNk6(),
                    d.getNk7(),
                    d.getNk8(),
                    d.getNk9(),
                    d.getNk10(),
                    d.getDiemXetTotNghiep()
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
        currentId = Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 0)));
    }

    private void themDiem() {
        DiemThiXetTuyen diem = hienThiFormDiem(null);
        if (diem == null) {
            return;
        }

        if (bus.add(diem)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            currentPage = 1;
            loadPage();
            currentId = -1;
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError());
        }
    }

    private void suaDiem() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi điểm cần sửa!");
            return;
        }

        DiemThiXetTuyen current = bus.getById(currentId);
        if (current == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu điểm thi!");
            return;
        }

        DiemThiXetTuyen updated = hienThiFormDiem(current);
        if (updated == null) {
            return;
        }

        updated.setIdDiemThi(currentId);
        if (bus.update(updated)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadPage();
            table.clearSelection();
            currentId = -1;
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError());
        }
    }

    private void xoaDiem() {
        if (currentId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi điểm!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa?", "Xác nhận",
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

    private void importDiem() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file điểm thi (.xlsx/.txt/.csv)");
        chooser.setFileFilter(new FileNameExtensionFilter("File dữ liệu", "xlsx", "txt", "csv"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(this, "File không tồn tại!");
            return;
        }

        try {
            int success = bus.importAndSaveToDatabase(selectedFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Import điểm thi hoàn tất.\n"
                            + bus.getLastImportSummary()
                            + "\nSố bản ghi ghi nhận thành công: " + success);
            currentPage = 1;
            currentId = -1;
            table.clearSelection();
            loadPage();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Import thất bại: " + ex.getMessage());
        }
    }

    private DiemThiXetTuyen hienThiFormDiem(DiemThiXetTuyen source) {
        JTextField txtCccd = new JTextField();
        JTextField txtSoBaoDanh = new JTextField();
        JTextField txtPhuongThuc = new JTextField();
        JTextField txtTo = new JTextField();
        JTextField txtLi = new JTextField();
        JTextField txtHo = new JTextField();
        JTextField txtSi = new JTextField();
        JTextField txtSu = new JTextField();
        JTextField txtDi = new JTextField();
        JTextField txtVa = new JTextField();
        JTextField txtN1Thi = new JTextField();
        JTextField txtN1Cc = new JTextField();
        JTextField txtCncn = new JTextField();
        JTextField txtCnnn = new JTextField();
        JTextField txtTi = new JTextField();
        JTextField txtKtpl = new JTextField();
        JTextField txtNl1 = new JTextField();
        JTextField txtNk1 = new JTextField();
        JTextField txtNk2 = new JTextField();
        JTextField txtNk3 = new JTextField();
        JTextField txtNk4 = new JTextField();
        JTextField txtNk5 = new JTextField();
        JTextField txtNk6 = new JTextField();
        JTextField txtNk7 = new JTextField();
        JTextField txtNk8 = new JTextField();
        JTextField txtNk9 = new JTextField();
        JTextField txtNk10 = new JTextField();
        JTextField txtDiemXetTotNghiep = new JTextField();
        JTextField txtGdcd = new JTextField();

        if (source != null) {
            txtCccd.setText(nullToEmpty(source.getCccd()));
            txtSoBaoDanh.setText(nullToEmpty(source.getSoBaoDanh()));
            txtPhuongThuc.setText(nullToEmpty(source.getPhuongThuc()));
            txtTo.setText(source.getTo() == null ? "" : source.getTo().toPlainString());
            txtLi.setText(source.getLi() == null ? "" : source.getLi().toPlainString());
            txtHo.setText(source.getHo() == null ? "" : source.getHo().toPlainString());
            txtSi.setText(source.getSi() == null ? "" : source.getSi().toPlainString());
            txtSu.setText(source.getSu() == null ? "" : source.getSu().toPlainString());
            txtDi.setText(source.getDi() == null ? "" : source.getDi().toPlainString());
            txtVa.setText(source.getVa() == null ? "" : source.getVa().toPlainString());
            txtN1Thi.setText(source.getN1Thi() == null ? "" : source.getN1Thi().toPlainString());
            txtN1Cc.setText(source.getN1Cc() == null ? "" : source.getN1Cc().toPlainString());
            txtCncn.setText(source.getCncn() == null ? "" : source.getCncn().toPlainString());
            txtCnnn.setText(source.getCnnn() == null ? "" : source.getCnnn().toPlainString());
            txtTi.setText(source.getTi() == null ? "" : source.getTi().toPlainString());
            txtKtpl.setText(source.getKtpl() == null ? "" : source.getKtpl().toPlainString());
            txtNl1.setText(source.getNl1() == null ? "" : source.getNl1().toPlainString());
            txtNk1.setText(source.getNk1() == null ? "" : source.getNk1().toPlainString());
            txtNk2.setText(source.getNk2() == null ? "" : source.getNk2().toPlainString());
            txtNk3.setText(source.getNk3() == null ? "" : source.getNk3().toPlainString());
            txtNk4.setText(source.getNk4() == null ? "" : source.getNk4().toPlainString());
            txtNk5.setText(source.getNk5() == null ? "" : source.getNk5().toPlainString());
            txtNk6.setText(source.getNk6() == null ? "" : source.getNk6().toPlainString());
            txtNk7.setText(source.getNk7() == null ? "" : source.getNk7().toPlainString());
            txtNk8.setText(source.getNk8() == null ? "" : source.getNk8().toPlainString());
            txtNk9.setText(source.getNk9() == null ? "" : source.getNk9().toPlainString());
            txtNk10.setText(source.getNk10() == null ? "" : source.getNk10().toPlainString());
            txtDiemXetTotNghiep.setText(source.getDiemXetTotNghiep() == null ? "" : source.getDiemXetTotNghiep().toPlainString());
            txtGdcd.setText(source.getGdcd() == null ? "" : source.getGdcd().toPlainString());
        }

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(245, 249, 255));
        panel.setPreferredSize(new Dimension(720, 700));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(source == null ? new Color(30, 136, 229) : new Color(243, 156, 18));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel lblHeader = new JLabel(source == null ? "THÊM ĐIỂM THI" : "CẬP NHẬT ĐIỂM THI");
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
        form.add(new JLabel("Phương thức:"));
        form.add(txtPhuongThuc);
        form.add(new JLabel("Điểm TO:"));
        form.add(txtTo);
        form.add(new JLabel("Điểm LI:"));
        form.add(txtLi);
        form.add(new JLabel("Điểm HO:"));
        form.add(txtHo);
        form.add(new JLabel("Điểm SI:"));
        form.add(txtSi);
        form.add(new JLabel("Điểm SU:"));
        form.add(txtSu);
        form.add(new JLabel("Điểm DI:"));
        form.add(txtDi);
        form.add(new JLabel("Điểm VA:"));
        form.add(txtVa);
        form.add(new JLabel("Điểm GDCD:"));
        form.add(txtGdcd);
        form.add(new JLabel("Điểm N1_THI:"));
        form.add(txtN1Thi);
        form.add(new JLabel("Điểm N1_CC:"));
        form.add(txtN1Cc);
        form.add(new JLabel("Điểm CNCN:"));
        form.add(txtCncn);
        form.add(new JLabel("Điểm CNNN:"));
        form.add(txtCnnn);
        form.add(new JLabel("Điểm TI:"));
        form.add(txtTi);
        form.add(new JLabel("Điểm KTPL:"));
        form.add(txtKtpl);
        form.add(new JLabel("Điểm NL1:"));
        form.add(txtNl1);
        form.add(new JLabel("Điểm NK1:"));
        form.add(txtNk1);
        form.add(new JLabel("Điểm NK2:"));
        form.add(txtNk2);
        form.add(new JLabel("Điểm NK3:"));
        form.add(txtNk3);
        form.add(new JLabel("Điểm NK4:"));
        form.add(txtNk4);
        form.add(new JLabel("Điểm NK5:"));
        form.add(txtNk5);
        form.add(new JLabel("Điểm NK6:"));
        form.add(txtNk6);
        form.add(new JLabel("Điểm NK7:"));
        form.add(txtNk7);
        form.add(new JLabel("Điểm NK8:"));
        form.add(txtNk8);
        form.add(new JLabel("Điểm NK9:"));
        form.add(txtNk9);
        form.add(new JLabel("Điểm NK10:"));
        form.add(txtNk10);
        form.add(new JLabel("Điểm xét TN:"));
        form.add(txtDiemXetTotNghiep);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(18);
        pnlCard.add(formScroll, BorderLayout.CENTER);
        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(pnlCard, BorderLayout.CENTER);
        ModernTheme.styleDialogContent(panel);

        int result = JOptionPane.showConfirmDialog(this, panel,
                source == null ? "Thêm điểm thi" : "Sửa điểm thi",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        DiemThiXetTuyen d = source == null ? new DiemThiXetTuyen() : source;
        d.setCccd(txtCccd.getText().trim());
        d.setSoBaoDanh(txtSoBaoDanh.getText().trim());
        d.setPhuongThuc(txtPhuongThuc.getText().trim());

        try {
            d.setTo(parseDecimal(txtTo.getText().trim()));
            d.setLi(parseDecimal(txtLi.getText().trim()));
            d.setHo(parseDecimal(txtHo.getText().trim()));
            d.setSi(parseDecimal(txtSi.getText().trim()));
            d.setSu(parseDecimal(txtSu.getText().trim()));
            d.setDi(parseDecimal(txtDi.getText().trim()));
            d.setVa(parseDecimal(txtVa.getText().trim()));
            d.setGdcd(parseDecimal(txtGdcd.getText().trim()));
            d.setN1Thi(parseDecimal(txtN1Thi.getText().trim()));
            d.setN1Cc(parseDecimal(txtN1Cc.getText().trim()));
            d.setCncn(parseDecimal(txtCncn.getText().trim()));
            d.setCnnn(parseDecimal(txtCnnn.getText().trim()));
            d.setTi(parseDecimal(txtTi.getText().trim()));
            d.setKtpl(parseDecimal(txtKtpl.getText().trim()));
            d.setNl1(parseDecimal(txtNl1.getText().trim()));
            d.setNk1(parseDecimal(txtNk1.getText().trim()));
            d.setNk2(parseDecimal(txtNk2.getText().trim()));
            d.setNk3(parseDecimal(txtNk3.getText().trim()));
            d.setNk4(parseDecimal(txtNk4.getText().trim()));
            d.setNk5(parseDecimal(txtNk5.getText().trim()));
            d.setNk6(parseDecimal(txtNk6.getText().trim()));
            d.setNk7(parseDecimal(txtNk7.getText().trim()));
            d.setNk8(parseDecimal(txtNk8.getText().trim()));
            d.setNk9(parseDecimal(txtNk9.getText().trim()));
            d.setNk10(parseDecimal(txtNk10.getText().trim()));
            d.setDiemXetTotNghiep(parseDecimal(txtDiemXetTotNghiep.getText().trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Điểm phải là số hợp lệ!");
            return null;
        }

        return d;
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
