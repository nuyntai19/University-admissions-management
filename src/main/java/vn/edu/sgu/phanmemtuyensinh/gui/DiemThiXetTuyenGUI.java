package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

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
import vn.edu.sgu.phanmemtuyensinh.bus.ThiSinhBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;

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
    private String currentMode = "THPT"; // THPT, DGNL, V-SAT

    private JButton btnDauTrang, btnCuoiTrang;

    public DiemThiXetTuyenGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buildTop();
        buildTable();
        buildBottom();

        loadPage();
    }

    private void buildTop() {
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 10));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel lblTitle = new JLabel("Qu\u1ea3n l\u00fd \u0110i\u1ec3m thi", JLabel.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pnlHeader.add(lblTitle, BorderLayout.NORTH);

        // Mode Switcher (THPT, DGNL, V-SAT)
        JPanel pnlModes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlModes.setOpaque(false);
        
        javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();
        javax.swing.JToggleButton btnModeTHPT = createModeTab("THPT");
        javax.swing.JToggleButton btnModeDGNL = createModeTab("DGNL");
        javax.swing.JToggleButton btnModeVSAT = createModeTab("V-SAT");
        
        group.add(btnModeTHPT);
        group.add(btnModeDGNL);
        group.add(btnModeVSAT);
        btnModeTHPT.setSelected(true);

        pnlModes.add(btnModeTHPT);
        pnlModes.add(btnModeDGNL);
        pnlModes.add(btnModeVSAT);

        ActionListener modeListener = e -> {
            currentMode = e.getActionCommand();
            currentPage = 1;
            buildTable(); 
            loadPage();
        };
        btnModeTHPT.addActionListener(modeListener);
        btnModeDGNL.addActionListener(modeListener);
        btnModeVSAT.addActionListener(modeListener);

        // Build Action Buttons row
        JPanel pnlActionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActionButtons.setOpaque(false);
        pnlActionButtons.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        btnImport = btnStyled("Import", new Color(0, 105, 92));
        btnImport.setPreferredSize(new Dimension(100, 40));
        btnThem = btnStyled("Th\u00eam", new Color(46, 125, 50));
        btnThem.setPreferredSize(new Dimension(100, 40));
        btnSua = btnStyled("S\u1eeda", new Color(255, 152, 0));
        btnSua.setPreferredSize(new Dimension(100, 40));
        btnXoa = btnStyled("X\u00f3a", new Color(211, 47, 47));
        btnXoa.setPreferredSize(new Dimension(100, 40));
        btnLamMoi = btnStyled("L\u00e0m m\u1edbi", new Color(108, 117, 125));
        btnLamMoi.setPreferredSize(new Dimension(120, 40));
        pnlActionButtons.add(btnImport);
        pnlActionButtons.add(btnThem);
        pnlActionButtons.add(btnSua);
        pnlActionButtons.add(btnXoa);
        pnlActionButtons.add(btnLamMoi);

        // Build Search row
        JPanel pnlSearchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSearchRow.setOpaque(false);
        pnlSearchRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        JLabel lblSearch = new JLabel("T\u00ecm CCCD/SBD:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTimKiem = new JTextField(25);
        txtTimKiem.setPreferredSize(new Dimension(350, 40));
        btnTim = btnStyled("T\u00ecm", new Color(13, 110, 253));
        btnTim.setPreferredSize(new Dimension(100, 40));
        btnTim.addActionListener(e -> timKiem());
        pnlSearchRow.add(lblSearch);
        pnlSearchRow.add(txtTimKiem);
        pnlSearchRow.add(btnTim);

        // Combine Row 1 and Row 2 into a single top row
        JPanel pnlTopRow = new JPanel(new BorderLayout());
        pnlTopRow.setOpaque(false);
        pnlTopRow.add(pnlModes, BorderLayout.WEST);
        pnlTopRow.add(pnlActionButtons, BorderLayout.EAST);

        // Assembly
        JPanel pnlToolbar = new JPanel();
        pnlToolbar.setLayout(new BoxLayout(pnlToolbar, BoxLayout.Y_AXIS));
        pnlToolbar.setOpaque(false);
        pnlToolbar.add(pnlTopRow);
        pnlToolbar.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
        pnlToolbar.add(pnlSearchRow);

        add(pnlToolbar, BorderLayout.NORTH);

        pnlHeader.add(pnlToolbar, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        btnThem.addActionListener(e -> themDiem());
        btnSua.addActionListener(e -> suaDiem());
        btnXoa.addActionListener(e -> xoaDiem());
        btnImport.addActionListener(e -> importDiem());
        btnLamMoi.addActionListener(e -> lamMoi());
        txtTimKiem.addActionListener(e -> timKiem());
    }

    private javax.swing.JToggleButton createModeTab(String text) {
        javax.swing.JToggleButton btn = new javax.swing.JToggleButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setPreferredSize(new Dimension(120, 45));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(230, 230, 230)));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        
        btn.addChangeListener(e -> {
            if (btn.isSelected()) {
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(13, 110, 253)));
                btn.setForeground(new Color(13, 110, 253));
                btn.setBackground(new Color(240, 247, 255));
            } else {
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(230, 230, 230)));
                btn.setForeground(Color.BLACK);
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    private JButton btnStyled(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return b;
    }


    private void buildTable() {
        String[] columns;
        if ("DGNL".equals(currentMode)) {
            columns = new String[]{"STT", "ID", "CCCD", "S\u1ed1 B\u00e1o Danh", "\u0110i\u1ec3m \u0110GNL"};
        } else if ("V-SAT".equals(currentMode)) {
            columns = new String[]{"STT", "ID", "CCCD", "S\u1ed1 B\u00e1o Danh", "To\u00e1n", "V\u0103n", "Ti\u1ebfng Anh", "V\u1eadt l\u00fd", "H\u00f3a h\u1ecdc", "Sinh h\u1ecdc", "L\u1ecbch s\u1eed", "\u0110\u1ecba l\u00fd"};
        } else {
            // THPT
            columns = new String[]{
                "STT", "ID", "CCCD", "S\u1ed1 B\u00e1o Danh", "To\u00e1n", "V\u0103n", "L\u00fd", "H\u00f3a", "Sinh", "S\u1eed", "\u0110\u1ecba", "GDCD", "N.Ng\u1eef", "KTPL", "\u0110i\u1ec3m x\u00e9t TN"
            };
        }

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        if (table == null) {
            table = new JTable(tableModel);
            table.getSelectionModel().addListSelectionListener(e -> chonDong());
            table.setRowHeight(35);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            // Blue header style
            table.getTableHeader().setBackground(new Color(13, 110, 253));
            table.getTableHeader().setForeground(Color.WHITE);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            table.getTableHeader().setPreferredSize(new Dimension(0, 40));
            
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            add(scrollPane, BorderLayout.CENTER);
        } else {
            table.setModel(tableModel);
        }
        
        configureTableColumns();
    }

    private void configureTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(50); // STT
        table.getColumnModel().getColumn(1).setMinWidth(0);        // ID
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(2).setPreferredWidth(140); // CCCD
        table.getColumnModel().getColumn(3).setPreferredWidth(110); // SBD
        
        for (int i = 4; i < table.getColumnCount(); i++) {
            // Increase width for the last column in THPT mode
            if ("THPT".equals(currentMode) && i == 14) {
                table.getColumnModel().getColumn(i).setPreferredWidth(130);
            } else {
                table.getColumnModel().getColumn(i).setPreferredWidth(85);
            }
            table.getColumnModel().getColumn(i).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                { setHorizontalAlignment(JLabel.CENTER); }
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                    Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                    if (v == null || v.toString().isEmpty() || v.toString().equals("---")) {
                        setText("---");
                        setForeground(new Color(180, 180, 180));
                    } else {
                        setForeground(Color.BLACK);
                    }
                    return comp;
                }
            });
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void buildBottom() {
        JPanel pnlPaging = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlPaging.setOpaque(false);

        btnDauTrang = btnStyled("<<", new Color(13, 110, 253));
        btnDauTrang.setPreferredSize(new Dimension(80, 35));
        
        btnCuoiTrang = btnStyled(">>", new Color(13, 110, 253));
        btnCuoiTrang.setPreferredSize(new Dimension(80, 35));

        btnTrangTruoc = btnStyled("Trang tr\u01b0\u1edbc", new Color(13, 110, 253));
        btnTrangTruoc.setPreferredSize(new Dimension(160, 35));
        
        btnTrangSau = btnStyled("Trang sau", new Color(13, 110, 253));
        btnTrangSau.setPreferredSize(new Dimension(160, 35));

        lblThongTinTrang = new JLabel("Trang 1/1");
        lblThongTinTrang.setFont(new Font("Segoe UI", Font.BOLD, 13));

        pnlPaging.add(btnDauTrang);
        pnlPaging.add(btnTrangTruoc);
        pnlPaging.add(lblThongTinTrang);
        pnlPaging.add(btnTrangSau);
        pnlPaging.add(btnCuoiTrang);

        btnDauTrang.addActionListener(e -> {
            currentPage = 1;
            loadPage();
        });

        btnCuoiTrang.addActionListener(e -> {
            currentPage = getTotalPages();
            loadPage();
        });

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

        int stt = (currentPage - 1) * PAGE_SIZE + 1;
        for (DiemThiXetTuyen d : list) {
            if ("DGNL".equals(currentMode)) {
                tableModel.addRow(new Object[]{ stt++, d.getIdDiemThi(), d.getCccd(), d.getSoBaoDanh(), d.getNl1() });
            } else if ("V-SAT".equals(currentMode)) {
                tableModel.addRow(new Object[]{ 
                    stt++, d.getIdDiemThi(), d.getCccd(), d.getSoBaoDanh(),
                    d.getVsatTo(), d.getVsatVa(), d.getVsatAnh(), d.getVsatLi(), d.getVsatHo(), d.getVsatSi(), d.getVsatSu(), d.getVsatDi()
                });
            } else {
                tableModel.addRow(new Object[]{
                    stt++, d.getIdDiemThi(), d.getCccd(), d.getSoBaoDanh(),
                    d.getTo(), d.getVa(), d.getLi(), d.getHo(), d.getSi(), d.getSu(), d.getDi(),
                    d.getGdcd(), d.getN1Thi(), d.getKtpl(), d.getDiemXetTotNghiep()
                });
            }
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
        currentId = Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 1))); // Column 1 is ID
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
        JTextField txtCccdOrSbd = new JTextField();
        final String[] selectedData = new String[2]; // [0]: cccd, [1]: soBaoDanh
        
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

        applyNumericFilters(
            txtTo, txtLi, txtHo, txtSi, txtSu, txtDi, txtVa, txtGdcd, txtN1Thi, txtN1Cc, txtCncn,
            txtCnnn, txtTi, txtKtpl, txtNl1, txtNk1, txtNk2, txtNk3, txtNk4, txtNk5, txtNk6,
            txtNk7, txtNk8, txtNk9, txtNk10, txtDiemXetTotNghiep
        );

        if (source != null) {
            String cccdVal = nullToEmpty(source.getCccd());
            String sbdVal = nullToEmpty(source.getSoBaoDanh());
            txtCccdOrSbd.setText(cccdVal + (sbdVal.isEmpty() ? "" : " / " + sbdVal));
            txtCccdOrSbd.setEditable(false); 
            selectedData[0] = cccdVal;
            selectedData[1] = sbdVal;
            txtTo.setText(source.getTo() == null ? "" : source.getTo().toPlainString());
            txtLi.setText(source.getLi() == null ? "" : source.getLi().toPlainString());
            txtHo.setText(source.getHo() == null ? "" : source.getHo().toPlainString());
            txtSi.setText(source.getSi() == null ? "" : source.getSi().toPlainString());
            txtSu.setText(source.getSu() == null ? "" : source.getSu().toPlainString());
            txtDi.setText(source.getDi() == null ? "" : source.getDi().toPlainString());
            txtVa.setText(source.getVa() == null ? "" : source.getVa().toPlainString());
            txtGdcd.setText(source.getGdcd() == null ? "" : source.getGdcd().toPlainString());
            txtN1Thi.setText(source.getN1Thi() == null ? "" : source.getN1Thi().toPlainString());

            // Load V-SAT specific values if in V-SAT mode or just load them anyway
            if ("V-SAT".equals(currentMode)) {
                txtTo.setText(source.getVsatTo() == null ? "" : source.getVsatTo().toPlainString());
                txtVa.setText(source.getVsatVa() == null ? "" : source.getVsatVa().toPlainString());
                txtN1Thi.setText(source.getVsatAnh() == null ? "" : source.getVsatAnh().toPlainString());
                txtLi.setText(source.getVsatLi() == null ? "" : source.getVsatLi().toPlainString());
                txtHo.setText(source.getVsatHo() == null ? "" : source.getVsatHo().toPlainString());
                txtSi.setText(source.getVsatSi() == null ? "" : source.getVsatSi().toPlainString());
                txtSu.setText(source.getVsatSu() == null ? "" : source.getVsatSu().toPlainString());
                txtDi.setText(source.getVsatDi() == null ? "" : source.getVsatDi().toPlainString());
            }
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

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(source == null ? new Color(13, 110, 253) : new Color(255, 152, 0));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel lblHeader = new JLabel(source == null ? "TH\u00caM \u0110I\u1ec2M THI" : "C\u1eacP NH\u1eacT \u0110I\u1ec2M THI");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pnlHeader.add(lblHeader, BorderLayout.WEST);

        JPanel pnlBody = new JPanel(new BorderLayout(0, 20));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Info Section
        JPanel pnlInfo = new JPanel(new BorderLayout());
        pnlInfo.setOpaque(false);
        pnlInfo.add(createFieldGroup("CCCD/S\u1ed1 b\u00e1o danh (*)", txtCccdOrSbd), BorderLayout.CENTER);

        if (!isUpdate) {
            setupAutoComplete(txtCccdOrSbd, selectedData);
        }

        // Scores Section
        JPanel pnlScores = new JPanel(new GridLayout(0, 2, 20, 15));
        pnlScores.setOpaque(false);
        String title = "B\u1ea2NG \u0110I\u1ec2M CH\u00cdNH (" + currentMode + ")";
        pnlScores.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            title, 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(41, 128, 185)
        ));
        ((javax.swing.border.TitledBorder)pnlScores.getBorder()).setTitlePosition(javax.swing.border.TitledBorder.TOP);

        if ("DGNL".equals(currentMode)) {
            pnlScores.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
            pnlScores.add(new JLabel("\u0110i\u1ec3m \u0110GNL:"));
            txtNl1.setPreferredSize(new Dimension(200, 35));
            pnlScores.add(txtNl1);
        } else if ("V-SAT".equals(currentMode)) {
            pnlScores.add(new JLabel("To\u00e1n:")); pnlScores.add(txtTo);
            pnlScores.add(new JLabel("Ng\u1eef v\u0103n:")); pnlScores.add(txtVa);
            pnlScores.add(new JLabel("Ti\u1ebfng Anh:")); pnlScores.add(txtN1Thi);
            pnlScores.add(new JLabel("V\u1eadt l\u00fd:")); pnlScores.add(txtLi);
            pnlScores.add(new JLabel("H\u00f3a h\u1ecdc:")); pnlScores.add(txtHo);
            pnlScores.add(new JLabel("Sinh h\u1ecdc:")); pnlScores.add(txtSi);
            pnlScores.add(new JLabel("L\u1ecbch s\u1eed:")); pnlScores.add(txtSu);
            pnlScores.add(new JLabel("\u0110\u1ecba l\u00fd:")); pnlScores.add(txtDi);
        } else {
            pnlScores.add(new JLabel("To\u00e1n:")); pnlScores.add(txtTo);
            pnlScores.add(new JLabel("Ng\u1eef v\u0103n:")); pnlScores.add(txtVa);
            pnlScores.add(new JLabel("V\u1eadt l\u00fd:")); pnlScores.add(txtLi);
            pnlScores.add(new JLabel("H\u00f3a h\u1ecdc:")); pnlScores.add(txtHo);
            pnlScores.add(new JLabel("Sinh h\u1ecdc:")); pnlScores.add(txtSi);
            pnlScores.add(new JLabel("L\u1ecbch s\u1eed:")); pnlScores.add(txtSu);
            pnlScores.add(new JLabel("\u0110\u1ecba l\u00fd:")); pnlScores.add(txtDi);
            pnlScores.add(new JLabel("GDCD:")); pnlScores.add(txtGdcd);
            pnlScores.add(new JLabel("Ngo\u1ea1i ng\u1eef:")); pnlScores.add(txtN1Thi);
            pnlScores.add(new JLabel("KTPL:")); pnlScores.add(txtKtpl);
            pnlScores.add(new JLabel("\u0110i\u1ec3m x\u00e9t TN:")); pnlScores.add(txtDiemXetTotNghiep);
        }

        // Talent Scores Section (Always show)
        JPanel pnlTalent = new JPanel(new GridLayout(0, 4, 15, 10));
        pnlTalent.setOpaque(false);
        pnlTalent.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            "\u0110I\u1ec2M N\u0102NG KHI\u1ebeU", 0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(46, 125, 50)
        ));
        pnlTalent.add(new JLabel("NK 1:")); pnlTalent.add(txtNk1);
        pnlTalent.add(new JLabel("NK 2:")); pnlTalent.add(txtNk2);
        pnlTalent.add(new JLabel("NK 3:")); pnlTalent.add(txtNk3);
        pnlTalent.add(new JLabel("NK 4:")); pnlTalent.add(txtNk4);
        pnlTalent.add(new JLabel("NK 5:")); pnlTalent.add(txtNk5);
        pnlTalent.add(new JLabel("NK 6:")); pnlTalent.add(txtNk6);
        pnlTalent.add(new JLabel("NK 7:")); pnlTalent.add(txtNk7);
        pnlTalent.add(new JLabel("NK 8:")); pnlTalent.add(txtNk8);
        pnlTalent.add(new JLabel("NK 9:")); pnlTalent.add(txtNk9);
        pnlTalent.add(new JLabel("NK 10:")); pnlTalent.add(txtNk10);

        pnlBody.add(pnlInfo, BorderLayout.NORTH);
        
        JPanel pnlScoresWrapper = new JPanel();
        pnlScoresWrapper.setLayout(new BoxLayout(pnlScoresWrapper, BoxLayout.Y_AXIS));
        pnlScoresWrapper.setOpaque(false);
        pnlScoresWrapper.add(pnlScores);
        
        if (!"DGNL".equals(currentMode) && !"V-SAT".equals(currentMode)) {
            pnlScoresWrapper.add(javax.swing.Box.createVerticalStrut(15));
            pnlScoresWrapper.add(pnlTalent);
        }
        
        JScrollPane scroll = new JScrollPane(pnlScoresWrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        pnlBody.add(scroll, BorderLayout.CENTER);

        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(pnlBody, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 15));
        JButton btnHuy = new JButton("Đóng");
        JButton btnLuu = new JButton(isUpdate ? "Lưu cập nhật" : "Lưu thêm");
        pnlFooter.add(btnHuy);
        pnlFooter.add(btnLuu);
        panel.add(pnlFooter, BorderLayout.SOUTH);

        JDialog formDialog = new JDialog((java.awt.Frame) null,
                isUpdate ? "Sửa điểm thi" : "Thêm điểm thi", true);
        formDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        formDialog.setLayout(new BorderLayout());
        formDialog.add(panel, BorderLayout.CENTER);
        formDialog.setSize(750, 750);
        formDialog.setLocationRelativeTo(this);

        btnHuy.addActionListener(e -> formDialog.dispose());
        btnLuu.addActionListener(e -> {
            DiemThiXetTuyen d = source == null ? new DiemThiXetTuyen() : source;
            if (isUpdate) {
                d.setIdDiemThi(targetId);
            }

            String inputVal = txtCccdOrSbd.getText().trim();
            if (!isUpdate && selectedData[0] == null && !inputVal.isEmpty()) {
                ThiSinhBUS tsBus = new ThiSinhBUS();
                ThiSinh ts = tsBus.getByCccd(inputVal);
                if (ts == null) ts = tsBus.getBySoBaoDanh(inputVal);
                
                if (ts != null) {
                    selectedData[0] = ts.getCccd();
                    selectedData[1] = ts.getSoBaoDanh();
                } else {
                    selectedData[0] = inputVal;
                }
            }

            d.setCccd(selectedData[0]);
            d.setSoBaoDanh(selectedData[1]);
            // Set phuong thuc based on current mode if adding new
            if (!isUpdate) d.setPhuongThuc(currentMode);

            String maxError = validateMaxScores(txtTo, txtVa, txtN1Thi, txtLi, txtHo, txtSi, txtSu, txtDi, txtNl1);
            if (maxError != null) {
                JOptionPane.showMessageDialog(formDialog, maxError);
                return;
            }

            try {
                if ("DGNL".equals(currentMode)) {
                    d.setNl1(parseDecimal(txtNl1.getText().trim()));
                } else if ("V-SAT".equals(currentMode)) {
                    d.setVsatTo(parseDecimal(txtTo.getText().trim()));
                    d.setVsatVa(parseDecimal(txtVa.getText().trim()));
                    d.setVsatAnh(parseDecimal(txtN1Thi.getText().trim()));
                    d.setVsatLi(parseDecimal(txtLi.getText().trim()));
                    d.setVsatHo(parseDecimal(txtHo.getText().trim()));
                    d.setVsatSi(parseDecimal(txtSi.getText().trim()));
                    d.setVsatSu(parseDecimal(txtSu.getText().trim()));
                    d.setVsatDi(parseDecimal(txtDi.getText().trim()));
                } else {
                    d.setTo(parseDecimal(txtTo.getText().trim()));
                    d.setVa(parseDecimal(txtVa.getText().trim()));
                    d.setLi(parseDecimal(txtLi.getText().trim()));
                    d.setHo(parseDecimal(txtHo.getText().trim()));
                    d.setSi(parseDecimal(txtSi.getText().trim()));
                    d.setSu(parseDecimal(txtSu.getText().trim()));
                    d.setDi(parseDecimal(txtDi.getText().trim()));
                    d.setGdcd(parseDecimal(txtGdcd.getText().trim()));
                    d.setN1Thi(parseDecimal(txtN1Thi.getText().trim()));
                    d.setKtpl(parseDecimal(txtKtpl.getText().trim()));
                    d.setDiemXetTotNghiep(parseDecimal(txtDiemXetTotNghiep.getText().trim()));
                }

                // Luôn cập nhật điểm năng khiếu nếu có nhập
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

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(formDialog, " \u0110i\u1ec3m ph\u1ea3i l\u00e0 s\u1ed1 h\u1ee3p l\u1ec7!");
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

    private JPanel createFieldGroup(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(l, BorderLayout.NORTH);
        field.setPreferredSize(new Dimension(0, 35));
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value.replace(',', '.'));
    }

    private String validateMaxScores(
            JTextField txtTo,
            JTextField txtVa,
            JTextField txtN1Thi,
            JTextField txtLi,
            JTextField txtHo,
            JTextField txtSi,
            JTextField txtSu,
            JTextField txtDi,
            JTextField txtNl1
    ) {
        if ("DGNL".equals(currentMode)) {
            return validateMaxField(txtNl1, new BigDecimal("1200"), "\u0110GNL");
        }

        if ("V-SAT".equals(currentMode)) {
            BigDecimal max = new BigDecimal("150");
            String error;
            error = validateMaxField(txtTo, max, "To\u00e1n (V-SAT)");
            if (error != null) return error;
            error = validateMaxField(txtVa, max, "Ng\u1eef v\u0103n (V-SAT)");
            if (error != null) return error;
            error = validateMaxField(txtN1Thi, max, "Ti\u1ebfng Anh (V-SAT)");
            if (error != null) return error;
            error = validateMaxField(txtLi, max, "V\u1eadt l\u00fd (V-SAT)");
            if (error != null) return error;
            error = validateMaxField(txtHo, max, "H\u00f3a h\u1ecdc (V-SAT)");
            if (error != null) return error;
            error = validateMaxField(txtSi, max, "Sinh h\u1ecdc (V-SAT)");
            if (error != null) return error;
            error = validateMaxField(txtSu, max, "L\u1ecbch s\u1eed (V-SAT)");
            if (error != null) return error;
            error = validateMaxField(txtDi, max, "\u0110\u1ecba l\u00fd (V-SAT)");
            if (error != null) return error;
        }

        return null;
    }

    private String validateMaxField(JTextField field, BigDecimal max, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            return null;
        }

        BigDecimal parsed;
        try {
            parsed = parseDecimal(value);
        } catch (NumberFormatException ex) {
            return "\u0110i\u1ec3m " + label + " ph\u1ea3i l\u00e0 s\u1ed1 h\u1ee3p l\u1ec7!";
        }

        if (parsed != null && parsed.compareTo(max) > 0) {
            return "\u0110i\u1ec3m " + label + " t\u1ed1i \u0111a l\u00e0 " + max.stripTrailingZeros().toPlainString() + ".";
        }
        return null;
    }

    private void applyNumericFilters(JTextField... fields) {
        NumericDocumentFilter filter = new NumericDocumentFilter();
        for (JTextField field : fields) {
            if (field.getDocument() instanceof AbstractDocument) {
                ((AbstractDocument) field.getDocument()).setDocumentFilter(filter);
            }
        }
    }

    private static class NumericDocumentFilter extends DocumentFilter {
        private static final Pattern VALID_NUMBER = Pattern.compile("^[0-9]*([\\.,][0-9]*)?$");

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = new StringBuilder(current)
                    .replace(offset, offset + length, text == null ? "" : text)
                    .toString();
            if (next.isEmpty() || VALID_NUMBER.matcher(next).matches()) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
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
    private void setupAutoComplete(JTextField textField, String[] selectedData) {
        JPopupMenu popup = new JPopupMenu();
        JList<String> list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        popup.add(scroll);

        ThiSinhBUS tsBus = new ThiSinhBUS();

        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    popup.setVisible(false);
                    return;
                }
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                    if (popup.isVisible()) {
                        list.requestFocus();
                        if (list.getModel().getSize() > 0) list.setSelectedIndex(0);
                    }
                    return;
                }
                
                String text = textField.getText().trim();
                if (text.length() < 1) {
                    popup.setVisible(false);
                    return;
                }

                List<ThiSinh> results = tsBus.searchByKeyword(text, 1, 10);
                if (results.isEmpty()) {
                    popup.setVisible(false);
                    return;
                }

                DefaultListModel<String> model = new DefaultListModel<>();
                for (ThiSinh ts : results) {
                    String sbd = ts.getSoBaoDanh() == null ? "" : ts.getSoBaoDanh();
                    model.addElement(ts.getCccd() + " - " + ts.getHo() + " " + ts.getTen() + (sbd.isEmpty() ? "" : " (" + sbd + ")"));
                }
                list.setModel(model);
                
                scroll.setPreferredSize(new Dimension(textField.getWidth(), Math.min(250, results.size() * 32 + 5)));
                popup.pack();
                if (!popup.isVisible()) {
                    popup.show(textField, 0, textField.getHeight());
                }
                textField.requestFocus();
            }
        });

        ActionListener selectAction = ev -> {
            String selected = list.getSelectedValue();
            if (selected != null) {
                String cccd = selected.split(" - ")[0];
                ThiSinh ts = tsBus.getByCccd(cccd);
                if (ts != null) {
                    selectedData[0] = ts.getCccd();
                    selectedData[1] = ts.getSoBaoDanh();
                    textField.setText(ts.getCccd() + (ts.getSoBaoDanh() == null || ts.getSoBaoDanh().isEmpty() ? "" : " / " + ts.getSoBaoDanh()));
                }
                popup.setVisible(false);
            }
        };

        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    selectAction.actionPerformed(null);
                }
            }
        });
        
        list.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    selectAction.actionPerformed(null);
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    popup.setVisible(false);
                    textField.requestFocus();
                }
            }
        });
    }
}

