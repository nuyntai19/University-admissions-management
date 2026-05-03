package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import vn.edu.sgu.phanmemtuyensinh.bus.DiemThiXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen;

public class DiemThiXetTuyenGUI extends JPanel {

    private static final int PAGE_SIZE = 20;
    private static final String[] EXPORT_HEADERS = {
            "ID", "CCCD", "Số Báo Danh", "Phương Thức",
            "TO", "LI", "HO", "SI", "SU", "DI", "VA", "GDCD",
            "N1_THI", "N1_CC", "CNCN", "CNNN", "TI", "KTPL", "NL1",
            "NK1", "NK2", "NK3", "NK4", "NK5", "NK6", "NK7", "NK8", "NK9", "NK10",
            "Điểm xét TN"
    };

    private final DiemThiXetTuyenBUS bus = new DiemThiXetTuyenBUS();

    private JTextField txtTimKiem;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnImport;
    private JButton btnExport;
    private JButton btnThongKe;
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
    private String currentSortOrder = "ASC";

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
        btnExport = new JButton("Export");
        btnThongKe = new JButton("Thống Kê");
        btnLamMoi = new JButton("Làm Mới");
        pnlActions.add(btnThem);
        pnlActions.add(btnSua);
        pnlActions.add(btnXoa);
        pnlActions.add(btnLamMoi);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.add(new JLabel("Tìm CCCD / SBD:"));
        txtTimKiem = new JTextField(18);
        btnTim = new JButton("Tìm");
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTim);
        pnlSearch.add(btnThongKe);
        pnlSearch.add(btnImport);
        pnlSearch.add(btnExport);


        JPanel pnlActionSearchSort = new JPanel(new BorderLayout(8, 8));
        pnlActionSearchSort.setOpaque(false);
        pnlActionSearchSort.add(pnlActions, BorderLayout.WEST);
        pnlActionSearchSort.add(pnlSearch, BorderLayout.EAST);

        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.setOpaque(false);
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        pnlTop.add(pnlActionSearchSort, BorderLayout.CENTER);

        btnThem.addActionListener(e -> themDiem());
        btnSua.addActionListener(e -> suaDiem());
        btnXoa.addActionListener(e -> xoaDiem());
        btnImport.addActionListener(e -> importDiem());
        btnExport.addActionListener(e -> exportDiem());
        btnThongKe.addActionListener(e -> thongKeDiem());
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
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID - gọn lại
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // CCCD
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Số Báo Danh
        table.getColumnModel().getColumn(3).setPreferredWidth(90);  // Phương Thức
        
        // Các cột điểm - đặt width nhỏ và không cho phép resize
        for (int i = 4; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(55);
            table.getColumnModel().getColumn(i).setResizable(false);
        }
        
        // Cột cuối "Điểm xét TN" rộng hơn để hiển thị đầy đủ
        table.getColumnModel().getColumn(columns.length - 1).setPreferredWidth(110);
        table.getColumnModel().getColumn(columns.length - 1).setResizable(true);
        
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
        hienThiFormDiem(null, false, -1);
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

        hienThiFormDiem(current, true, currentId);
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
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError());
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

        final String importPath = selectedFile.getAbsolutePath();

        JDialog progressDialog = new JDialog((java.awt.Frame) null, "Đang import điểm thi...", true);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setLayout(new BorderLayout(10, 10));

        JLabel lblStatus = new JLabel("Đang chuẩn bị import...");
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(lblStatus, BorderLayout.NORTH);
        content.add(progressBar, BorderLayout.CENTER);
        progressDialog.add(content, BorderLayout.CENTER);
        progressDialog.setSize(420, 120);
        progressDialog.setLocationRelativeTo(this);

        SwingWorker<Integer, String> worker = new SwingWorker<>() {
            private String errorMessage;

            @Override
            protected Integer doInBackground() {
                try {
                    return bus.importAndSaveToDatabase(importPath, (percent, message) -> {
                        setProgress(percent);
                        publish(message);
                    });
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                    return -1;
                }
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    lblStatus.setText(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(DiemThiXetTuyenGUI.this,
                            "Import thất bại: " + errorMessage,
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int success = get();
                    JOptionPane.showMessageDialog(DiemThiXetTuyenGUI.this,
                            "Import điểm thi hoàn tất.\n"
                                    + bus.getLastImportSummary()
                                    + "\nSố bản ghi ghi nhận thành công: " + success);
                    currentPage = 1;
                    currentId = -1;
                    table.clearSelection();
                    loadPage();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DiemThiXetTuyenGUI.this,
                            "Import thất bại: " + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();
        progressDialog.setVisible(true);
    }

    private void hienThiFormDiem(DiemThiXetTuyen source, boolean isUpdate, int targetId) {
        JTextField txtCccd = new JTextField();
        JComboBox<String> cbPhuongThuc = new JComboBox<>(new String[]{"", "THPT", "V-SAT", "DGNL"});
        JTextField txtSoBaoDanh = new JTextField();
        JTextField txtTo = new JTextField();
        JTextField txtLi = new JTextField();
        JTextField txtHo = new JTextField();
        JTextField txtSi = new JTextField();
        JTextField txtSu = new JTextField();
        JTextField txtDi = new JTextField();
        JTextField txtVa = new JTextField();
        JTextField txtGdcd = new JTextField();
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

        if (source != null) {
            txtCccd.setText(nullToEmpty(source.getCccd()));
            txtCccd.setEditable(false); // Không cho sửa CCCD
            cbPhuongThuc.setSelectedItem(nullToEmpty(source.getPhuongThuc()));
            txtSoBaoDanh.setText(nullToEmpty(source.getSoBaoDanh()));
            txtTo.setText(source.getTo() == null ? "" : source.getTo().toPlainString());
            txtLi.setText(source.getLi() == null ? "" : source.getLi().toPlainString());
            txtHo.setText(source.getHo() == null ? "" : source.getHo().toPlainString());
            txtSi.setText(source.getSi() == null ? "" : source.getSi().toPlainString());
            txtSu.setText(source.getSu() == null ? "" : source.getSu().toPlainString());
            txtDi.setText(source.getDi() == null ? "" : source.getDi().toPlainString());
            txtVa.setText(source.getVa() == null ? "" : source.getVa().toPlainString());
            txtGdcd.setText(source.getGdcd() == null ? "" : source.getGdcd().toPlainString());
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
        }

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(245, 249, 255));
        panel.setPreferredSize(new Dimension(750, 750));

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
        
        // Các field cơ bản
        form.add(new JLabel("CCCD:"));
        form.add(txtCccd);
        form.add(new JLabel("Số báo danh:"));
        form.add(txtSoBaoDanh);
        form.add(new JLabel("Phương thức: *"));
        form.add(cbPhuongThuc);
        
        // Các field điểm THPT
        form.add(new JLabel("Điểm TO (0-10 cho THPT, 0-150 cho V-SAT):"));
        form.add(txtTo);
        form.add(new JLabel("Điểm LI (0-10 cho THPT, 0-150 cho V-SAT):"));
        form.add(txtLi);
        form.add(new JLabel("Điểm HO (0-10 cho THPT, 0-150 cho V-SAT):"));
        form.add(txtHo);
        
        // Các field V-SAT riêng
        form.add(new JLabel("Điểm SI (0-150 cho V-SAT):"));
        form.add(txtSi);
        form.add(new JLabel("Điểm SU (0-150 cho V-SAT):"));
        form.add(txtSu);
        form.add(new JLabel("Điểm DI (0-150 cho V-SAT):"));
        form.add(txtDi);
        form.add(new JLabel("Điểm VA (0-150 cho V-SAT):"));
        form.add(txtVa);
        form.add(new JLabel("Điểm GDCD (0-150 cho V-SAT):"));
        form.add(txtGdcd);
        
        // Các field DGNL
        form.add(new JLabel("Điểm NL1 (0-1200 cho DGNL):"));
        form.add(txtNl1);
        
        // Các field chung
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

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlFooter.setOpaque(false);
        JButton btnHuy = new JButton("Đóng");
        JButton btnLuu = new JButton(isUpdate ? "Lưu cập nhật" : "Lưu thêm");
        pnlFooter.add(btnHuy);
        pnlFooter.add(btnLuu);
        pnlCard.add(pnlFooter, BorderLayout.SOUTH);

        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(pnlCard, BorderLayout.CENTER);
        ModernTheme.styleDialogContent(panel);

        JDialog formDialog = new JDialog((java.awt.Frame) null,
                isUpdate ? "Sửa điểm thi" : "Thêm điểm thi", true);
        formDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        formDialog.setLayout(new BorderLayout());
        formDialog.add(panel, BorderLayout.CENTER);
        formDialog.setSize(750, 750);
        formDialog.setLocationRelativeTo(this);

        btnHuy.addActionListener(e -> formDialog.dispose());
        btnLuu.addActionListener(e -> {
            if (cbPhuongThuc.getSelectedItem() == null || cbPhuongThuc.getSelectedItem().toString().isEmpty()) {
                JOptionPane.showMessageDialog(formDialog, "Vui lòng chọn phương thức thi!");
                return;
            }

            DiemThiXetTuyen d = source == null ? new DiemThiXetTuyen() : source;
            if (isUpdate) {
                d.setIdDiemThi(targetId);
            }

            d.setCccd(txtCccd.getText().trim());
            d.setSoBaoDanh(txtSoBaoDanh.getText().trim());
            d.setPhuongThuc(cbPhuongThuc.getSelectedItem().toString().trim());

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
                JOptionPane.showMessageDialog(formDialog, "Điểm phải là số hợp lệ!");
                return;
            }

            boolean success = isUpdate ? bus.update(d) : bus.add(d);
            if (success) {
                JOptionPane.showMessageDialog(formDialog, isUpdate ? "Cập nhật thành công!" : "Thêm thành công!");
                currentPage = 1;
                loadPage();
                table.clearSelection();
                currentId = -1;
                formDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(formDialog,
                        (isUpdate ? "Cập nhật thất bại: " : "Thêm thất bại: ") + bus.getLastError());
            }
        });

        formDialog.setVisible(true);
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

    private void exportDiem() {
        showExportDialog();
    }

    private void showExportDialog() {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Chọn loại xuất Excel", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(this);

        JPanel pnlContent = new JPanel(new GridLayout(3, 1, 10, 10));
        pnlContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Chọn cách xuất file Excel:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlContent.add(lblTitle);

        JButton btnTemplate = new JButton("Xuất file trắng (chỉ định dạng cột)");
        btnTemplate.addActionListener(e -> {
            dialog.dispose();
            exportToExcelTemplate();
        });
        pnlContent.add(btnTemplate);

        JButton btnWithData = new JButton("Xuất file có đầy đủ dữ liệu");
        btnWithData.addActionListener(e -> {
            dialog.dispose();
            exportToExcelWithData();
        });
        pnlContent.add(btnWithData);

        dialog.add(pnlContent, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void exportToExcelTemplate() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Excel template");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel file", "xlsx"));
        chooser.setSelectedFile(new File("DiemThiXetTuyen_Template.xlsx"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vị trí lưu file!");
            return;
        }

        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Điểm Thi Xét Tuyển");

            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(EXPORT_HEADERS[i]);
                headerRow.getCell(i).setCellStyle(headerStyle);
                sheet.setColumnWidth(i, i < 4 ? 18 * 256 : 12 * 256);
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, EXPORT_HEADERS.length - 1));

            // Ghi file
            try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
                workbook.write(fos);
            }
            workbook.close();

            JOptionPane.showMessageDialog(this, "Xuất file template thành công!\nĐường dẫn: " + selectedFile.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToExcelWithData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Excel có dữ liệu");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel file", "xlsx"));
        chooser.setSelectedFile(new File("DiemThiXetTuyen_" + System.currentTimeMillis() + ".xlsx"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vị trí lưu file!");
            return;
        }

        // Chạy trong background
        SwingWorker<Boolean, String> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    XSSFWorkbook workbook = new XSSFWorkbook();
                    XSSFSheet sheet = workbook.createSheet("Điểm Thi Xét Tuyển");

                    XSSFCellStyle headerStyle = createHeaderStyle(workbook);
                    XSSFCellStyle dataStyle = createDataStyle(workbook);

                    XSSFRow headerRow = sheet.createRow(0);
                    for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                        headerRow.createCell(i).setCellValue(EXPORT_HEADERS[i]);
                        headerRow.getCell(i).setCellStyle(headerStyle);
                        sheet.setColumnWidth(i, i < 4 ? 18 * 256 : 12 * 256);
                    }

                    sheet.createFreezePane(0, 1);
                    sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, EXPORT_HEADERS.length - 1));

                    // Lấy toàn bộ dữ liệu từ DB
                    List<DiemThiXetTuyen> allData = bus.getAll();
                    int rowNum = 1;

                    for (DiemThiXetTuyen d : allData) {
                        XSSFRow row = sheet.createRow(rowNum++);

                        row.createCell(0).setCellValue(d.getIdDiemThi());
                        row.createCell(1).setCellValue(nullToEmpty(d.getCccd()));
                        row.createCell(2).setCellValue(nullToEmpty(d.getSoBaoDanh()));
                        row.createCell(3).setCellValue(nullToEmpty(d.getPhuongThuc()));
                        setCellDecimalValue(row.createCell(4), d.getTo());
                        setCellDecimalValue(row.createCell(5), d.getLi());
                        setCellDecimalValue(row.createCell(6), d.getHo());
                        setCellDecimalValue(row.createCell(7), d.getSi());
                        setCellDecimalValue(row.createCell(8), d.getSu());
                        setCellDecimalValue(row.createCell(9), d.getDi());
                        setCellDecimalValue(row.createCell(10), d.getVa());
                        setCellDecimalValue(row.createCell(11), d.getGdcd());
                        setCellDecimalValue(row.createCell(12), d.getN1Thi());
                        setCellDecimalValue(row.createCell(13), d.getN1Cc());
                        setCellDecimalValue(row.createCell(14), d.getCncn());
                        setCellDecimalValue(row.createCell(15), d.getCnnn());
                        setCellDecimalValue(row.createCell(16), d.getTi());
                        setCellDecimalValue(row.createCell(17), d.getKtpl());
                        setCellDecimalValue(row.createCell(18), d.getNl1());
                        setCellDecimalValue(row.createCell(19), d.getNk1());
                        setCellDecimalValue(row.createCell(20), d.getNk2());
                        setCellDecimalValue(row.createCell(21), d.getNk3());
                        setCellDecimalValue(row.createCell(22), d.getNk4());
                        setCellDecimalValue(row.createCell(23), d.getNk5());
                        setCellDecimalValue(row.createCell(24), d.getNk6());
                        setCellDecimalValue(row.createCell(25), d.getNk7());
                        setCellDecimalValue(row.createCell(26), d.getNk8());
                        setCellDecimalValue(row.createCell(27), d.getNk9());
                        setCellDecimalValue(row.createCell(28), d.getNk10());
                        setCellDecimalValue(row.createCell(29), d.getDiemXetTotNghiep());

                        // Apply data style
                        for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                            row.getCell(i).setCellStyle(dataStyle);
                        }
                    }

                    // Ghi file
                    try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
                        workbook.write(fos);
                    }
                    workbook.close();

                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(DiemThiXetTuyenGUI.this,
                                "Xuất dữ liệu thành công!\nĐường dẫn: " + selectedFile.getAbsolutePath());
                    } else {
                        JOptionPane.showMessageDialog(DiemThiXetTuyenGUI.this,
                                "Lỗi khi xuất dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DiemThiXetTuyenGUI.this,
                            "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private XSSFCellStyle createDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(false);
        return style;
    }

    private void setCellDecimalValue(org.apache.poi.xssf.usermodel.XSSFCell cell, BigDecimal value) {
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue("");
        }
    }

    private void thongKeDiem() {
        JDialog dialog = new JDialog(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                "Thống Kê Điểm Theo Môn Thi", true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Tiêu đề
        JLabel lblTitle = new JLabel("THỐNG KÊ ĐIỂM THI THEO TỪNG MÔN", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(0x1565C0));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(14, 10, 6, 10));
        dialog.add(lblTitle, BorderLayout.NORTH);

        // Bảng thống kê
        String[] cols = {"Môn Thi", "Số có điểm", "Điểm TB", "Điểm Max", "Điểm Min", "Số bỏ trống"};
        DefaultTableModel statModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        java.util.LinkedHashMap<String, double[]> thongKe = bus.getThongKeDiemTheoMon();
        for (java.util.Map.Entry<String, double[]> entry : thongKe.entrySet()) {
            double[] s = entry.getValue();
            long soCoDiem = (long) s[0];
            String tb   = soCoDiem > 0 ? String.format("%.2f", s[1]) : "-";
            String max  = soCoDiem > 0 ? String.format("%.2f", s[2]) : "-";
            String min  = soCoDiem > 0 ? String.format("%.2f", s[3]) : "-";
            long soTrong = (long) s[4];
            statModel.addRow(new Object[]{entry.getKey(), soCoDiem, tb, max, min, soTrong});
        }

        JTable statTable = new JTable(statModel);
        statTable.setRowHeight(26);
        statTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        statTable.getTableHeader().setBackground(new Color(0x1565C0));
        statTable.getTableHeader().setForeground(Color.WHITE);
        statTable.setSelectionBackground(new Color(0xBBDEFB));
        statTable.setGridColor(new Color(0xCFD8DC));

        // Căn giữa các cột số
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < cols.length; i++) {
            statTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        statTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        for (int i = 1; i < cols.length; i++) {
            statTable.getColumnModel().getColumn(i).setPreferredWidth(100);
        }

        JScrollPane scroll = new JScrollPane(statTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        dialog.add(scroll, BorderLayout.CENTER);

        // Footer
        long tongBanGhi = bus.countAll();
        JLabel lblFooter = new JLabel("Tổng số bản ghi điểm thi: " + tongBanGhi, JLabel.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblFooter.setForeground(new Color(0x555555));
        lblFooter.setBorder(BorderFactory.createEmptyBorder(6, 10, 4, 10));

        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(e -> dialog.dispose());
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        pnlFooter.add(lblFooter, BorderLayout.NORTH);
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBtn.add(btnDong);
        pnlFooter.add(pnlBtn, BorderLayout.SOUTH);
        dialog.add(pnlFooter, BorderLayout.SOUTH);

        dialog.setSize(740, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}

