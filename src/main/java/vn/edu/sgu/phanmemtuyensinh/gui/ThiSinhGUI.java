package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.toedter.calendar.JDateChooser;

import vn.edu.sgu.phanmemtuyensinh.bus.ThiSinhBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.DiemThiXetTuyen;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ThiSinh;

public class ThiSinhGUI extends JPanel {

    private static final int PAGE_SIZE = 20;
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern SO_BAO_DANH_PATTERN = Pattern.compile("^TS_\\d{1,20}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");

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
    private JPanel pnlThongKe;
    private JLabel lblTongSo;
    private JTable tableThongKeKhuVuc;
    private JTable tableThongKeDoiTuong;
    private DefaultTableModel modelThongKeKhuVuc;
    private DefaultTableModel modelThongKeDoiTuong;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<ThiSinh> currentDataList;

    private int currentId = -1;
    private int currentPage = 1;
    private long totalItems = 0;
    private String currentKeyword = "";

    public ThiSinhGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buildTop();
        buildThongKePanel();
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
        pnlTop.add(buildThongKePanel(), BorderLayout.SOUTH);

        btnThem.addActionListener(e -> themThiSinh());
        btnSua.addActionListener(e -> suaThiSinh());
        btnXoa.addActionListener(e -> xoaThiSinh());
        btnImport.addActionListener(e -> importThiSinh());
        btnTim.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> lamMoi());

        add(pnlTop, BorderLayout.NORTH);
    }

    public void setActionButtonsEnabled(boolean enabled) {
        btnThem.setEnabled(enabled);
        btnSua.setEnabled(enabled);
        btnXoa.setEnabled(enabled);
        btnImport.setEnabled(enabled);
        btnLamMoi.setEnabled(true);
        btnTim.setEnabled(true);
    }


    private void buildTable() {
        String[] columns = {
                "ID", "CCCD", "Số Báo Danh", "Họ Tên", "Ngày Sinh", "Điện Thoại",
                "Giới Tính", "Email", "Đối Tượng", "Khu Vực", "Chi tiết"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép edit bất kỳ ô nào
            }
        };

        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setRowHeight(34);
        configureColumnWidths();
        int detailCol = tableModel.getColumnCount() - 1;
        table.getColumnModel().getColumn(detailCol).setCellRenderer(new EyeButtonRenderer());
        table.getSelectionModel().addListSelectionListener(e -> chonDong());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                // Click đơn vào cột Chi tiết → mở dialog
                if (col == detailCol && row >= 0 && currentDataList != null && row < currentDataList.size()) {
                    showThiSinhDetail(currentDataList.get(row));
                    return;
                }
                // Double-click bất kỳ cột nào khác → cũng mở dialog
                if (e.getClickCount() == 2 && row >= 0 && currentDataList != null && row < currentDataList.size()) {
                    showThiSinhDetail(currentDataList.get(row));
                }
            }
        });
        // Đổi cursor thành HAND khi hover vào cột Chi tiết
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col == detailCol) {
                    table.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                } else {
                    table.setCursor(java.awt.Cursor.getDefaultCursor());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void buildBottom() {
        JPanel pnlPaging = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlPaging.setOpaque(false);

        JButton btnDauTrang = new JButton("<<");
        btnTrangTruoc = new JButton("Trang tr\u01b0\u1edbc");
        lblThongTinTrang = new JLabel("Trang 1/1");
        btnTrangSau = new JButton("Trang sau");
        JButton btnCuoiTrang = new JButton(">>");

        pnlPaging.add(btnDauTrang);
        pnlPaging.add(btnTrangTruoc);
        pnlPaging.add(lblThongTinTrang);
        pnlPaging.add(btnTrangSau);
        pnlPaging.add(btnCuoiTrang);

        btnDauTrang.addActionListener(e -> { currentPage = 1; loadPage(); });
        btnCuoiTrang.addActionListener(e -> { currentPage = getTotalPages(); loadPage(); });
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

    private JPanel buildThongKePanel() {
        if (pnlThongKe != null) {
            return pnlThongKe;
        }

        pnlThongKe = new JPanel(new BorderLayout(10, 10));
        pnlThongKe.setOpaque(false);
        pnlThongKe.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        pnlThongKe.setPreferredSize(new Dimension(0, 190));

        JPanel pnlSummary = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlSummary.setOpaque(false);
        lblTongSo = createStatChip("Tổng thí sinh: 0");
        pnlSummary.add(lblTongSo);

        JPanel pnlTables = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlTables.setOpaque(false);

        modelThongKeKhuVuc = createThongKeTableModel("Khu vực");
        modelThongKeDoiTuong = createThongKeTableModel("Đối tượng");
        tableThongKeKhuVuc = new JTable(modelThongKeKhuVuc);
        tableThongKeDoiTuong = new JTable(modelThongKeDoiTuong);
        ModernTheme.styleTable(tableThongKeKhuVuc);
        ModernTheme.styleTable(tableThongKeDoiTuong);
        tableThongKeKhuVuc.setRowHeight(24);
        tableThongKeDoiTuong.setRowHeight(24);

        pnlTables.add(wrapThongKeTable("Thống kê theo khu vực", tableThongKeKhuVuc));
        pnlTables.add(wrapThongKeTable("Thống kê theo đối tượng", tableThongKeDoiTuong));

        pnlThongKe.add(pnlSummary, BorderLayout.NORTH);
        pnlThongKe.add(pnlTables, BorderLayout.CENTER);
        return pnlThongKe;
    }

    private DefaultTableModel createThongKeTableModel(String firstColumnName) {
        return new DefaultTableModel(new String[] { firstColumnName, "Số lượng" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel wrapThongKeTable(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(45, 62, 80));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JLabel createStatChip(String text) {
        JLabel chip = new JLabel(text);
        chip.setOpaque(true);
        chip.setBackground(new Color(32, 129, 226));
        chip.setForeground(Color.WHITE);
        chip.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        chip.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return chip;
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

        currentDataList = list;

        for (ThiSinh ts : list) {
            String hoTen = (ts.getHo() == null ? "" : ts.getHo()) + (ts.getTen() == null ? "" : " " + ts.getTen());
            tableModel.addRow(new Object[] {
                    ts.getIdThiSinh(),
                    ts.getCccd(),
                    ts.getSoBaoDanh(),
                    hoTen.trim(),
                    ts.getNgaySinh(),
                    ts.getDienThoai(),
                    ts.getGioiTinh(),
                    ts.getEmail(),
                    ts.getDoiTuong(),
                    ts.getKhuVuc(),
                    "Chi tiết"
            });
        }

        if (currentPage > getTotalPages()) {
            currentPage = Math.max(1, getTotalPages());
            loadPage();
            return;
        }

        updatePagingInfo();
        refreshThongKe();
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

    private void refreshThongKe() {
        if (lblTongSo == null || modelThongKeKhuVuc == null || modelThongKeDoiTuong == null) {
            return;
        }

        lblTongSo.setText("Tổng thí sinh: " + bus.countAll());
        fillThongKeTable(modelThongKeKhuVuc, bus.countByKhuVuc());
        fillThongKeTable(modelThongKeDoiTuong, bus.countByDoiTuong());
    }

    private void fillThongKeTable(DefaultTableModel model, List<Object[]> data) {
        model.setRowCount(0);
        if (data == null) {
            return;
        }
        for (Object[] row : data) {
            if (row == null || row.length < 2) {
                continue;
            }
            model.addRow(new Object[] { String.valueOf(row[0]), String.valueOf(row[1]) });
        }
    }

    private void showThiSinhDetail(ThiSinh ts) {
        if (ts == null) {
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết thí sinh",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(980, 720);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(new Color(236, 242, 250));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(32, 129, 226));
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JLabel title = new JLabel("CHI TIẾT THÔNG TIN THÍ SINH", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        summary.setOpaque(false);
        summary.add(createInfoChip("CCCD", nullToEmpty(ts.getCccd()), new Color(52, 152, 219)));
        summary.add(createInfoChip("SBD", nullToEmpty(ts.getSoBaoDanh()), new Color(46, 204, 113)));
        summary.add(createInfoChip("ĐT", nullToEmpty(ts.getDoiTuong()), new Color(241, 196, 15)));
        summary.add(createInfoChip("KV", nullToEmpty(ts.getKhuVuc()), new Color(155, 89, 182)));

        JPanel infoCard = new JPanel(new GridBagLayout());
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(213, 223, 240)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        infoCard.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        addInfoRow(infoCard, gbc, "ID", String.valueOf(ts.getIdThiSinh()));
        addInfoRow(infoCard, gbc, "CCCD", nullToEmpty(ts.getCccd()));
        addInfoRow(infoCard, gbc, "Số báo danh", nullToEmpty(ts.getSoBaoDanh()));
        addInfoRow(infoCard, gbc, "Họ", nullToEmpty(ts.getHo()));
        addInfoRow(infoCard, gbc, "Tên", nullToEmpty(ts.getTen()));
        addInfoRow(infoCard, gbc, "Họ và tên",
                ((ts.getHo() == null ? "" : ts.getHo()) + " " + (ts.getTen() == null ? "" : ts.getTen())).trim());
        addInfoRow(infoCard, gbc, "Ngày sinh", nullToEmpty(ts.getNgaySinh()));
        addInfoRow(infoCard, gbc, "Điện thoại", nullToEmpty(ts.getDienThoai()));
        addInfoRow(infoCard, gbc, "Mật khẩu", nullToEmpty(ts.getPassword()));
        addInfoRow(infoCard, gbc, "Giới tính", nullToEmpty(ts.getGioiTinh()));
        addInfoRow(infoCard, gbc, "Email", nullToEmpty(ts.getEmail()));
        addInfoRow(infoCard, gbc, "Nơi sinh", nullToEmpty(ts.getNoiSinh()));
        addInfoRow(infoCard, gbc, "Updated At", nullToEmpty(ts.getUpdatedAt()));
        addInfoRow(infoCard, gbc, "Đối tượng", nullToEmpty(ts.getDoiTuong()));
        addInfoRow(infoCard, gbc, "Khu vực", nullToEmpty(ts.getKhuVuc()));
        addInfoRow(infoCard, gbc, "Dân tộc", nullToEmpty(ts.getDanToc()));
        addInfoRow(infoCard, gbc, "Mã dân tộc", nullToEmpty(ts.getMaDanToc()));
        addInfoRow(infoCard, gbc, "Chương trình học", nullToEmpty(ts.getChuongTrinhHoc()));
        addInfoRow(infoCard, gbc, "Mã môn NN", nullToEmpty(ts.getMaMonNn()));

        List<DiemThiXetTuyen> diemList = bus.getDiemThiByCccd(ts.getCccd());
        JPanel scorePanel = buildScorePanel(diemList);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("Thông tin thí sinh",
                wrapSection("THÔNG TIN CÁ NHÂN", new JScrollPane(infoCard), new Color(245, 249, 255)));
        tabs.addTab("Điểm thi", wrapSection("XEM CHI TIẾT CÁC ĐIỂM CỦA THÍ SINH", new JScrollPane(scorePanel),
                new Color(250, 248, 240)));

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        body.add(summary, BorderLayout.NORTH);
        body.add(tabs, BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnClose);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel buildScorePanel(List<DiemThiXetTuyen> diemList) {
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JPanel note = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        note.setOpaque(false);
        note.add(createInfoChip("THPT", "Điểm thi phổ thông", new Color(52, 152, 219)));
        note.add(createInfoChip("ĐGNL", "Đánh giá năng lực", new Color(46, 204, 113)));
        note.add(createInfoChip("V-SAT", "Kỳ thi V-SAT", new Color(231, 76, 60)));
        panel.add(note);
        panel.add(Box.createVerticalStrut(10));

        if (diemList == null || diemList.isEmpty()) {
            JLabel empty = new JLabel("Thí sinh chưa có dữ liệu điểm thi.", JLabel.CENTER);
            empty.setOpaque(true);
            empty.setBackground(new Color(255, 250, 240));
            empty.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(empty);
            return panel;
        }

        for (DiemThiXetTuyen d : diemList) {
            panel.add(createScoreCard(d));
            panel.add(Box.createVerticalStrut(15));
        }
        return panel;
    }

    private JPanel createScoreCard(DiemThiXetTuyen d) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(213, 223, 240)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        // Tiêu đề card (Phương thức chính của bản ghi này)
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        String ptName = nullToEmpty(d.getPhuongThuc());
        if (ptName.isEmpty()) ptName = "Chưa xác định";
        JLabel method = new JLabel("Bản ghi điểm - Phương thức chính: " + ptName);
        method.setFont(new Font("Segoe UI", Font.BOLD, 15));
        method.setForeground(new Color(32, 129, 226));

        JLabel total = new JLabel("Điểm xét TN: " + formatBigDecimal(d.getDiemXetTotNghiep()));
        total.setFont(new Font("Segoe UI", Font.BOLD, 14));
        total.setForeground(new Color(192, 57, 43));
        head.add(method, BorderLayout.WEST);
        head.add(total, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Nhóm 1: Điểm THPT & Học bạ
        body.add(createSubSectionTitle("1. Nhóm điểm THPT / Học bạ"));
        JPanel gridThpt = new JPanel(new GridLayout(0, 5, 8, 8));
        gridThpt.setOpaque(false);
        addScoreCell(gridThpt, "Toán", d.getTo());
        addScoreCell(gridThpt, "Văn", d.getVa());
        addScoreCell(gridThpt, "Lý", d.getLi());
        addScoreCell(gridThpt, "Hóa", d.getHo());
        addScoreCell(gridThpt, "Sinh", d.getSi());
        addScoreCell(gridThpt, "Sử", d.getSu());
        addScoreCell(gridThpt, "Địa", d.getDi());
        addScoreCell(gridThpt, "GDCD", d.getGdcd());
        addScoreCell(gridThpt, "N.Ngữ (Thi)", d.getN1Thi());
        addScoreCell(gridThpt, "N.Ngữ (CC)", d.getN1Cc());
        addScoreCell(gridThpt, "Tin học", d.getTi());
        addScoreCell(gridThpt, "KTPL", d.getKtpl());
        addScoreCell(gridThpt, "Công nghệ CN", d.getCncn());
        addScoreCell(gridThpt, "Công nghệ NN", d.getCnnn());
        body.add(gridThpt);
        body.add(Box.createVerticalStrut(10));

        // Nhóm 2: Năng khiếu (NK1 - NK10)
        body.add(createSubSectionTitle("2. Nhóm điểm Năng khiếu"));
        JPanel gridNk = new JPanel(new GridLayout(0, 5, 8, 8));
        gridNk.setOpaque(false);
        addScoreCell(gridNk, "NK 1", d.getNk1());
        addScoreCell(gridNk, "NK 2", d.getNk2());
        addScoreCell(gridNk, "NK 3", d.getNk3());
        addScoreCell(gridNk, "NK 4", d.getNk4());
        addScoreCell(gridNk, "NK 5", d.getNk5());
        addScoreCell(gridNk, "NK 6", d.getNk6());
        addScoreCell(gridNk, "NK 7", d.getNk7());
        addScoreCell(gridNk, "NK 8", d.getNk8());
        addScoreCell(gridNk, "NK 9", d.getNk9());
        addScoreCell(gridNk, "NK 10", d.getNk10());
        body.add(gridNk);
        body.add(Box.createVerticalStrut(10));

        // Nhóm 3: Đánh giá năng lực (NL1)
        if (d.getNl1() != null && d.getNl1().compareTo(BigDecimal.ZERO) > 0) {
            body.add(createSubSectionTitle("3. Nhóm điểm Đánh giá năng lực (ĐGNL)"));
            JPanel gridDgnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            gridDgnl.setOpaque(false);
            addScoreCell(gridDgnl, "ĐGNL (NL1)", d.getNl1());
            body.add(gridDgnl);
            body.add(Box.createVerticalStrut(10));
        }

        // Nhóm 4: V-SAT
        if (hasVsatScore(d)) {
            body.add(createSubSectionTitle("4. Nhóm điểm V-SAT"));
            JPanel gridVsat = new JPanel(new GridLayout(0, 4, 8, 8));
            gridVsat.setOpaque(false);
            addScoreCell(gridVsat, "V-SAT Toán", d.getVsatTo());
            addScoreCell(gridVsat, "V-SAT Văn", d.getVsatVa());
            addScoreCell(gridVsat, "V-SAT Anh", d.getVsatAnh());
            addScoreCell(gridVsat, "V-SAT Lý", d.getVsatLi());
            addScoreCell(gridVsat, "V-SAT Hóa", d.getVsatHo());
            addScoreCell(gridVsat, "V-SAT Sử", d.getVsatSu());
            addScoreCell(gridVsat, "V-SAT Địa", d.getVsatDi());
            addScoreCell(gridVsat, "V-SAT Sinh", d.getVsatSi());
            body.add(gridVsat);
        }

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JLabel createSubSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(41, 128, 185));
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 225, 240)),
            BorderFactory.createEmptyBorder(10, 0, 5, 0)
        ));
        return lbl;
    }

    private boolean hasVsatScore(DiemThiXetTuyen d) {
        return (d.getVsatTo() != null && d.getVsatTo().compareTo(BigDecimal.ZERO) > 0)
                || (d.getVsatVa() != null && d.getVsatVa().compareTo(BigDecimal.ZERO) > 0)
                || (d.getVsatAnh() != null && d.getVsatAnh().compareTo(BigDecimal.ZERO) > 0)
                || (d.getVsatLi() != null && d.getVsatLi().compareTo(BigDecimal.ZERO) > 0);
    }

    private void addScoreCell(JPanel panel, String label, BigDecimal value) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setOpaque(true);
        cell.setBackground(new Color(250, 252, 255));
        cell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 235, 245)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblKey.setForeground(new Color(100, 110, 120));

        JLabel lblValue = new JLabel(formatBigDecimal(value), JLabel.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValue.setForeground(new Color(41, 128, 185));

        cell.add(lblKey, BorderLayout.NORTH);
        cell.add(lblValue, BorderLayout.CENTER);
        
        cell.setPreferredSize(new Dimension(130, 45));
        panel.add(cell);
    }


    private JPanel wrapSection(String sectionTitle, JComponent innerComponent, Color background) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setBackground(background);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblTitle = new JLabel(sectionTitle);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(38, 50, 70));
        wrapper.add(lblTitle, BorderLayout.NORTH);
        wrapper.add(innerComponent, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createInfoChip(String label, String value, Color color) {
        JPanel chip = new JPanel(new BorderLayout());
        chip.setBackground(color);
        chip.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JLabel lbl = new JLabel(label + ": " + (value == null || value.isBlank() ? "-" : value));
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chip.add(lbl, BorderLayout.CENTER);
        return chip;
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, String label, String value) {
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lblKey = new JLabel(label + ":");
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblKey.setForeground(new Color(52, 73, 94));
        panel.add(lblKey, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel lblValue = new JLabel(value == null || value.isBlank() ? "-" : value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblValue.setForeground(new Color(45, 62, 80));
        panel.add(lblValue, gbc);

        gbc.gridy++;
    }

    private String formatBigDecimal(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }

    private class EyeButtonRenderer extends JPanel implements TableCellRenderer {
    private final JLabel labelBtn; // Dùng JLabel thay vì JButton

    EyeButtonRenderer() {
        // Dùng GridBagLayout để bọc nhãn luôn nằm ngay ngắn ở giữa ô
        setLayout(new GridBagLayout());
        setOpaque(true);

        // Cấu hình JLabel giả làm Button
        labelBtn = new JLabel("Chi tiết", JLabel.CENTER);
        labelBtn.setOpaque(true); // BẮT BUỘC phải bật true để nhãn hiển thị màu nền
        labelBtn.setForeground(Color.WHITE); // Màu chữ trắng
        labelBtn.setBackground(new Color(0, 123, 255)); // Màu nền xanh chuẩn
        labelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelBtn.setPreferredSize(new Dimension(100, 28));

        add(labelBtn);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Đồng bộ màu nền của Panel chứa button với nền của dòng được chọn
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            labelBtn.setBackground(new Color(0, 104, 214)); // Nút sậm màu hơn chút khi row được chọn
        } else {
            setBackground(table.getBackground());
            labelBtn.setBackground(new Color(0, 123, 255)); // Trả về màu xanh dương
        }
        return this;
    }

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

        final String importPath = chooser.getSelectedFile().getAbsolutePath();

        JDialog progressDialog = new JDialog((java.awt.Frame) null, "Đang import thí sinh...", true);
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
            private final String[] errorMessage = new String[1];

            @Override
            protected Integer doInBackground() {
                try {
                    return bus.importAndSaveToDatabase(importPath, (percent, message) -> {
                        setProgress(percent);
                        publish(message);
                    });
                } catch (IOException ex) {
                    errorMessage[0] = ex.getMessage();
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
                if (errorMessage[0] != null) {
                    JOptionPane.showMessageDialog(ThiSinhGUI.this,
                            "Lỗi import: " + errorMessage[0],
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int imported = get();
                    JOptionPane.showMessageDialog(ThiSinhGUI.this,
                            "Import hoàn tất!\n"
                                    + bus.getLastImportSummary()
                                    + "\n(Đã thêm vào DB: " + imported + ")");
                    currentPage = 1;
                    loadPage();
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(ThiSinhGUI.this,
                            "Lỗi import: " + ex.getMessage(),
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
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError());
        }
    }

    private ThiSinh hienThiFormThiSinh(ThiSinh source) {
        JTextField txtCccd = new JTextField();
        JTextField txtSoBaoDanh = new JTextField();
        JTextField txtHo = new JTextField();
        JTextField txtTen = new JTextField();
        JDateChooser dcNgaySinh = new JDateChooser();
        dcNgaySinh.setDateFormatString("dd/MM/yyyy");
        ((JTextField) dcNgaySinh.getDateEditor().getUiComponent()).setEditable(false);
        JTextField txtDienThoai = new JTextField();
        JTextField txtMatKhau = new JTextField();
        JComboBox<String> cboGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ", "Khác" });
        JTextField txtEmail = new JTextField();
        JTextField txtNoiSinh = new JTextField();
        JTextField txtDoiTuong = new JTextField();
        JTextField txtKhuVuc = new JTextField();
        JTextField txtDanToc = new JTextField();
        JTextField txtMaDanToc = new JTextField();
        JTextField txtChuongTrinhHoc = new JTextField();
        JTextField txtMaMonNn = new JTextField();

        Dimension inputSize = new Dimension(300, 30);
        txtCccd.setPreferredSize(inputSize);
        txtSoBaoDanh.setPreferredSize(inputSize);
        txtHo.setPreferredSize(inputSize);
        txtTen.setPreferredSize(inputSize);
        dcNgaySinh.setPreferredSize(inputSize);
        txtDienThoai.setPreferredSize(inputSize);
        txtMatKhau.setPreferredSize(inputSize);
        cboGioiTinh.setPreferredSize(inputSize);
        txtEmail.setPreferredSize(inputSize);
        txtNoiSinh.setPreferredSize(inputSize);
        txtDoiTuong.setPreferredSize(inputSize);
        txtKhuVuc.setPreferredSize(inputSize);
        txtDanToc.setPreferredSize(inputSize);
        txtMaDanToc.setPreferredSize(inputSize);
        txtChuongTrinhHoc.setPreferredSize(inputSize);
        txtMaMonNn.setPreferredSize(inputSize);

        if (source != null) {
            txtCccd.setText(nullToEmpty(source.getCccd()));
            txtSoBaoDanh.setText(nullToEmpty(source.getSoBaoDanh()));
            txtHo.setText(nullToEmpty(source.getHo()));
            txtTen.setText(nullToEmpty(source.getTen()));
            Date ngaySinh = parseDateFlexible(source.getNgaySinh());
            if (ngaySinh != null) {
                dcNgaySinh.setDate(ngaySinh);
            }
            txtDienThoai.setText(nullToEmpty(source.getDienThoai()));
            txtEmail.setText(nullToEmpty(source.getEmail()));
            txtNoiSinh.setText(nullToEmpty(source.getNoiSinh()));
            txtDoiTuong.setText(nullToEmpty(source.getDoiTuong()));
            txtKhuVuc.setText(nullToEmpty(source.getKhuVuc()));
            txtDanToc.setText(nullToEmpty(source.getDanToc()));
            txtMaDanToc.setText(nullToEmpty(source.getMaDanToc()));
            txtChuongTrinhHoc.setText(nullToEmpty(source.getChuongTrinhHoc()));
            txtMaMonNn.setText(nullToEmpty(source.getMaMonNn()));
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
        form.add(new JLabel("Ngày sinh (dd/MM/yyyy):"));
        form.add(dcNgaySinh);
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
        form.add(new JLabel("Dân tộc:"));
        form.add(txtDanToc);
        form.add(new JLabel("Mã dân tộc:"));
        form.add(txtMaDanToc);
        form.add(new JLabel("Chương trình học:"));
        form.add(txtChuongTrinhHoc);
        form.add(new JLabel("Mã môn NN:"));
        form.add(txtMaMonNn);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);

        pnlCard.add(formScroll, BorderLayout.CENTER);
        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(pnlCard, BorderLayout.CENTER);
        ModernTheme.styleDialogContent(panel);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel,
                    source == null ? "Thêm thí sinh" : "Sửa thí sinh",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            ThiSinh ts = new ThiSinh();
            ts.setCccd(txtCccd.getText().trim());
            ts.setSoBaoDanh(txtSoBaoDanh.getText().trim());
            ts.setHo(txtHo.getText().trim());
            ts.setTen(txtTen.getText().trim());
            Date selectedNgaySinh = dcNgaySinh.getDate();
            ts.setNgaySinh(selectedNgaySinh == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(selectedNgaySinh));
            ts.setDienThoai(txtDienThoai.getText().trim());
            ts.setPassword(txtMatKhau.getText().trim());
            ts.setGioiTinh(String.valueOf(cboGioiTinh.getSelectedItem()));
            ts.setEmail(txtEmail.getText().trim());
            ts.setNoiSinh(txtNoiSinh.getText().trim());
            ts.setDoiTuong(txtDoiTuong.getText().trim());
            ts.setKhuVuc(txtKhuVuc.getText().trim());
            ts.setDanToc(txtDanToc.getText().trim());
            ts.setMaDanToc(txtMaDanToc.getText().trim());
            ts.setChuongTrinhHoc(txtChuongTrinhHoc.getText().trim());
            ts.setMaMonNn(txtMaMonNn.getText().trim());

            if (validateFormInput(ts)) {
                return ts;
            }
        }
    }

    private boolean validateFormInput(ThiSinh ts) {
        if (ts.getCccd().isBlank()) {
            JOptionPane.showMessageDialog(this, "CCCD không được để trống!");
            return false;
        }
        if (!CCCD_PATTERN.matcher(ts.getCccd()).matches()) {
            JOptionPane.showMessageDialog(this, "CCCD chỉ được phép gồm đúng 12 chữ số!");
            return false;
        }
        if (ts.getSoBaoDanh().isBlank()) {
            JOptionPane.showMessageDialog(this, "Số báo danh không được để trống!");
            return false;
        }
        if (!SO_BAO_DANH_PATTERN.matcher(ts.getSoBaoDanh()).matches()) {
            JOptionPane.showMessageDialog(this, "Số báo danh phải theo mẫu TS_<số> (ví dụ: TS_20260001)!");
            return false;
        }
        if (ts.getHo().isBlank() || ts.getTen().isBlank()) {
            JOptionPane.showMessageDialog(this, "Họ và Tên không được để trống!");
            return false;
        }
        if (isNumericOnly(ts.getHo()) || isNumericOnly(ts.getTen())) {
            JOptionPane.showMessageDialog(this, "Họ và Tên không được chỉ gồm chữ số!");
            return false;
        }
        if (ts.getNgaySinh().isBlank()) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không được để trống!");
            return false;
        }
        if (!ts.getDienThoai().isBlank() && !PHONE_PATTERN.matcher(ts.getDienThoai()).matches()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ!");
            return false;
        }
        if (!ts.getEmail().isBlank() && !EMAIL_PATTERN.matcher(ts.getEmail()).matches()) {
            JOptionPane.showMessageDialog(this, "Email không đúng định dạng!");
            return false;
        }
        String kv = normalizeKhuVuc(ts.getKhuVuc());
        if (kv == null) {
            JOptionPane.showMessageDialog(this, "Khu vực chỉ chấp nhận: KV1, KV2-NT, KV2, KV3!");
            return false;
        }
        ts.setKhuVuc(kv);

        String dt = normalizeDoiTuong(ts.getDoiTuong());
        if (dt == null) {
            JOptionPane.showMessageDialog(this,
                    "Đối tượng phải thuộc: 01, 02, 03, 04, 05, 06, 06a, 07, 07a (hoặc để trống)!");
            return false;
        }
        ts.setDoiTuong(dt);
        return true;
    }

    private boolean isNumericOnly(String value) {
        String compact = value == null ? "" : value.replaceAll("\\s+", "");
        return !compact.isBlank() && NUMERIC_PATTERN.matcher(compact).matches();
    }

    private String normalizeKhuVuc(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim().toUpperCase(Locale.ROOT).replace("_", "").replace(" ", "");
        if ("KV1".equals(v) || "1".equals(v)) {
            return "KV1";
        }
        if ("KV2NT".equals(v) || "KV2-NT".equals(v) || "2NT".equals(v)) {
            return "KV2-NT";
        }
        if ("KV2".equals(v) || "2".equals(v)) {
            return "KV2";
        }
        if ("KV3".equals(v) || "3".equals(v)) {
            return "KV3";
        }
        return null;
    }

    private String normalizeDoiTuong(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String v = value.trim().toUpperCase(Locale.ROOT).replace(" ", "");
        if (v.startsWith("DT")) {
            v = v.substring(2);
        }
        if ("06A".equals(v) || "6A".equals(v)) {
            return "06a";
        }
        if ("07A".equals(v) || "7A".equals(v)) {
            return "07a";
        }
        if (v.matches("\\d{1,2}")) {
            int code = Integer.parseInt(v);
            if (code >= 1 && code <= 7) {
                return String.format("%02d", code);
            }
        }
        return null;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Date parseDateFlexible(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String text = value.trim();
        String[] patterns = { "dd/MM/yyyy", "dd/MM/yy", "yyyy-MM-dd" };
        for (String pattern : patterns) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            ParsePosition pos = new ParsePosition(0);
            Date parsed = sdf.parse(text, pos);
            if (parsed != null && pos.getIndex() == text.length()) {
                return parsed;
            }
        }
        return null;
    }

    private void configureColumnWidths() {
        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(0).setPreferredWidth(70);
        columns.getColumn(1).setPreferredWidth(120);
        columns.getColumn(2).setPreferredWidth(120);
        columns.getColumn(3).setPreferredWidth(180);
        columns.getColumn(4).setPreferredWidth(120);
        columns.getColumn(5).setPreferredWidth(120);
        columns.getColumn(6).setPreferredWidth(100);
        columns.getColumn(7).setPreferredWidth(180);
        columns.getColumn(8).setPreferredWidth(100);
        columns.getColumn(9).setPreferredWidth(110);
        columns.getColumn(10).setMinWidth(120);
        columns.getColumn(10).setPreferredWidth(150);
        columns.getColumn(10).setMaxWidth(180);
    }
}
