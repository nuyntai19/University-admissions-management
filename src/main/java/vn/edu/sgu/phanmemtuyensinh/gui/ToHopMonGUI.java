package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import vn.edu.sgu.phanmemtuyensinh.bus.ToHopMonBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.ToHopMon;

public class ToHopMonGUI extends JPanel {

    private ToHopMonBUS bus = new ToHopMonBUS();

    // Các thành phần giao diện
    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnImport;
    private JButton btnLoc, btnBoLoc;
    private JTextField txtBoLoc;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<ToHopMon> danhSachGoc = new ArrayList<>();
    
    // Biến lưu ID đang được chọn
    private int currentId = -1;

    public ToHopMonGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        loadDataToTable();
    }

    private void initComponents() {
        // --- PHẦN TIÊU ĐỀ ---
        JLabel lblTitle = new JLabel("QUẢN LÝ TỔ HỢP MÔN", JLabel.CENTER);
        ModernTheme.styleModuleTitle(lblTitle);
        
        // --- NÚT BẤM ---
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm mới");
        btnImport = new JButton("Import Excel");
        btnImport.setBackground(new Color(34, 139, 34));
        btnImport.setForeground(Color.WHITE);
        
        pnlButtons.add(btnThem);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnSua);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnXoa);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnLamMoi);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnImport);

        JPanel pnlLoc = new JPanel();
        pnlLoc.setLayout(new BoxLayout(pnlLoc, BoxLayout.X_AXIS));
        JLabel lblBoLoc = new JLabel("Bộ lọc:");
        txtBoLoc = new JTextField();
        txtBoLoc.setPreferredSize(new Dimension(240, 30));
        txtBoLoc.setMaximumSize(new Dimension(260, 30));
        txtBoLoc.setToolTipText("Nhập mã tổ hợp (VD: A01) hoặc tên môn (VD: Toán, Tiếng Anh, Sinh học)");
        btnLoc = new JButton("Lọc");
        btnBoLoc = new JButton("Bỏ lọc");

        pnlLoc.add(lblBoLoc);
        pnlLoc.add(Box.createHorizontalStrut(8));
        pnlLoc.add(txtBoLoc);
        pnlLoc.add(Box.createHorizontalStrut(8));
        pnlLoc.add(btnLoc);
        pnlLoc.add(Box.createHorizontalStrut(8));
        pnlLoc.add(btnBoLoc);

        // Combine title + actions into one north panel
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        JPanel pnlActions = new JPanel(new BorderLayout());
        pnlActions.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pnlActions.add(pnlButtons, BorderLayout.WEST);
        pnlActions.add(pnlLoc, BorderLayout.EAST);
        pnlTop.add(pnlActions, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        // --- PHẦN BẢNG DỮ LIỆU (Bên dưới) ---
        String[] columns = {"ID", "Mã Tổ Hợp", "Môn 1", "Môn 2", "Môn 3", "Tên Tổ Hợp"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Không cho sửa trực tiếp trên bảng
        };
        table = new JTable(tableModel);
        configureColumnWidths();
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- BẮT SỰ KIỆN (EVENTS) ---
        
        // Sự kiện click vào dòng trong bảng
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                currentId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            }
        });

        // Nút Làm mới
        btnLamMoi.addActionListener(e -> clearForm());

        // Bộ lọc bên phải
        btnLoc.addActionListener(e -> applyFilter());
        btnBoLoc.addActionListener(e -> {
            txtBoLoc.setText("");
            loadDataToTable();
        });
        txtBoLoc.addActionListener(e -> applyFilter());

        // Nút Thêm
        btnThem.addActionListener(e -> {
            ToHopMon th = showToHopFormDialog(null);
            if (th == null) {
                return;
            }

            if (bus.add(th)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Nút Sửa
        btnSua.addActionListener(e -> {
            if (currentId == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
                return;
            }

            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
                return;
            }

            ToHopMon source = new ToHopMon();
            source.setIdToHop(currentId);
            source.setMaToHop(String.valueOf(tableModel.getValueAt(row, 1)));
            source.setMon1(String.valueOf(tableModel.getValueAt(row, 2)));
            source.setMon2(String.valueOf(tableModel.getValueAt(row, 3)));
            source.setMon3(String.valueOf(tableModel.getValueAt(row, 4)));
            Object tenValue = tableModel.getValueAt(row, 5);
            source.setTenToHop(tenValue == null ? "" : String.valueOf(tenValue));

            ToHopMon th = showToHopFormDialog(source);
            if (th == null) {
                return;
            }
            th.setIdToHop(currentId);

            if (bus.update(th)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Nút Xóa
        btnXoa.addActionListener(e -> {
            if (currentId == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (bus.delete(currentId)) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadDataToTable();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại: " + bus.getLastError(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Nút Import dữ liệu (xlsx/txt)
        btnImport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel hoặc Text (*.xlsx, *.txt)", "xlsx", "txt"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    bus.importAndSaveToDatabase(filePath);
                    JOptionPane.showMessageDialog(this, bus.getLastImportSummary(), "Kết quả import", JOptionPane.INFORMATION_MESSAGE);
                    loadDataToTable();
                    clearForm();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi import: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
    }

    // Hàm load dữ liệu từ Database lên Bảng
    private void loadDataToTable() {
        danhSachGoc = bus.getAll();
        applyFilter();
    }

    // Hàm xóa trắng các ô nhập liệu
    private void clearForm() {
        currentId = -1;
        table.clearSelection();
    }

    private void applyFilter() {
        String keyword = txtBoLoc == null ? "" : txtBoLoc.getText().trim();
        if (keyword.isEmpty()) {
            renderTableData(danhSachGoc);
            return;
        }

        String keywordUpper = keyword.toUpperCase(Locale.ROOT);
        String keywordAscii = toAscii(keyword);
        String monCode = mapSubjectKeywordToCode(keyword);

        List<ToHopMon> filtered = new ArrayList<>();
        for (ToHopMon th : danhSachGoc) {
            String ma = safe(th.getMaToHop()).toUpperCase(Locale.ROOT);
            String m1 = safe(th.getMon1()).toUpperCase(Locale.ROOT);
            String m2 = safe(th.getMon2()).toUpperCase(Locale.ROOT);
            String m3 = safe(th.getMon3()).toUpperCase(Locale.ROOT);
            String ten = safe(th.getTenToHop());
            String tenAscii = toAscii(ten);

            boolean matchMa = ma.contains(keywordUpper);
            boolean matchMonCode = m1.contains(keywordUpper) || m2.contains(keywordUpper) || m3.contains(keywordUpper);
            boolean matchMonName = tenAscii.contains(keywordAscii);
            boolean matchMappedCode = !monCode.isEmpty() && (m1.equals(monCode) || m2.equals(monCode) || m3.equals(monCode));

            if (matchMa || matchMonCode || matchMonName || matchMappedCode) {
                filtered.add(th);
            }
        }

        renderTableData(filtered);
    }

    private void renderTableData(List<ToHopMon> list) {
        tableModel.setRowCount(0);
        for (ToHopMon th : list) {
            tableModel.addRow(new Object[]{
                    th.getIdToHop(), th.getMaToHop(),
                    th.getMon1(), th.getMon2(), th.getMon3(), th.getTenToHop()
            });
        }
        clearForm();
    }

    private String mapSubjectKeywordToCode(String keyword) {
        String k = toAscii(keyword).replaceAll("[^A-Z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        String compact = k.replace(" ", "");

        if (compact.equals("TOAN")) return "TO";
        if (compact.equals("VATLI") || compact.equals("VATLY") || compact.equals("LY") || compact.equals("LI")) return "LI";
        if (compact.equals("HOA") || compact.equals("HOAHOC")) return "HO";
        if (compact.equals("SINH") || compact.equals("SINHHOC")) return "SI";
        if (compact.equals("LICHSU") || compact.equals("SU")) return "SU";
        if (compact.equals("DIALI") || compact.equals("DIALY") || compact.equals("DIA")) return "DI";
        if (compact.equals("NGUVAN") || compact.equals("VAN")) return "VA";
        if (compact.contains("TIENGANH") || compact.contains("NGOAINGU") || compact.equals("ANH")) return "N1";
        if (compact.contains("GDKTPL") || compact.equals("GDCD")) return "KTPL";
        if (compact.equals("TIN") || compact.equals("TINHOC")) return "TI";
        if (compact.contains("CONGNGHECONGNGHIEP")) return "CNCN";
        if (compact.contains("CONGNGHENONGNGHIEP")) return "CNNN";
        if (compact.equals("TO") || compact.equals("LI") || compact.equals("HO") || compact.equals("SI")
                || compact.equals("SU") || compact.equals("DI") || compact.equals("VA") || compact.equals("N1")
                || compact.equals("KTPL") || compact.equals("TI") || compact.equals("CNCN") || compact.equals("CNNN")) {
            return compact;
        }
        return "";
    }

    private String toAscii(String text) {
        String input = safe(text);
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('Đ', 'D')
                .replace('đ', 'd');
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void configureColumnWidths() {
        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(0).setPreferredWidth(55);  // ID nhỏ hơn
        columns.getColumn(1).setPreferredWidth(95);
        columns.getColumn(2).setPreferredWidth(90);
        columns.getColumn(3).setPreferredWidth(90);
        columns.getColumn(4).setPreferredWidth(90);
        columns.getColumn(5).setPreferredWidth(230); // Bù rộng cho Tên tổ hợp
    }

    private ToHopMon showToHopFormDialog(ToHopMon source) {
        JTextField txtMa = createDialogTextField();
        JTextField txtMon1 = createDialogTextField();
        JTextField txtMon2 = createDialogTextField();
        JTextField txtMon3 = createDialogTextField();
        JTextField txtTen = createDialogTextField();
        txtTen.setPreferredSize(new Dimension(280, 34));

        if (source != null) {
            txtMa.setText(source.getMaToHop() == null ? "" : source.getMaToHop());
            txtMon1.setText(source.getMon1() == null ? "" : source.getMon1());
            txtMon2.setText(source.getMon2() == null ? "" : source.getMon2());
            txtMon3.setText(source.getMon3() == null ? "" : source.getMon3());
            txtTen.setText(source.getTenToHop() == null ? "" : source.getTenToHop());
        }

        JLabel lblHeading = new JLabel(source == null ? "THÊM TỔ HỢP MÔN" : "SỬA TỔ HỢP MÔN");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblHeading.setForeground(new Color(32, 55, 96));

        JLabel lblHint = new JLabel("Mã môn ví dụ: TO, LI, HO, SI, SU, DI, VA, N1...");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(new Color(92, 106, 132));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 4));
        pnlHeader.setOpaque(false);
        pnlHeader.add(lblHeading, BorderLayout.NORTH);
        pnlHeader.add(lblHint, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 226, 241)),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(form, gbc, 0, "Mã tổ hợp:", txtMa);
        addFormRow(form, gbc, 1, "Môn 1:", txtMon1);
        addFormRow(form, gbc, 2, "Môn 2:", txtMon2);
        addFormRow(form, gbc, 3, "Môn 3:", txtMon3);
        addFormRow(form, gbc, 4, "Tên tổ hợp:", txtTen);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(pnlHeader, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        ModernTheme.styleDialogContent(content);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                content,
                    source == null ? "Thêm tổ hợp môn" : "Sửa tổ hợp môn",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            String ma = txtMa.getText().trim().toUpperCase();
            String mon1 = txtMon1.getText().trim().toUpperCase();
            String mon2 = txtMon2.getText().trim().toUpperCase();
            String mon3 = txtMon3.getText().trim().toUpperCase();
            String ten = txtTen.getText().trim();

            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mã tổ hợp không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (mon1.isEmpty() || mon2.isEmpty() || mon3.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ 3 môn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            ToHopMon toHopMon = new ToHopMon();
            toHopMon.setMaToHop(ma);
            toHopMon.setMon1(mon1);
            toHopMon.setMon2(mon2);
            toHopMon.setMon3(mon3);
            toHopMon.setTenToHop(ten);
            return toHopMon;
        }
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField textField) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label, SwingConstants.LEFT);
        lbl.setPreferredSize(new Dimension(95, 20));
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        textField.setColumns(22);
        form.add(textField, gbc);
    }

    private JTextField createDialogTextField() {
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(200, 34));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return textField;
    }
}