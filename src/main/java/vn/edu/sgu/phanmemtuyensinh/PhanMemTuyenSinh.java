package vn.edu.sgu.phanmemtuyensinh;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguoiDung;
import vn.edu.sgu.phanmemtuyensinh.gui.BangQuyDoiGUI;
import vn.edu.sgu.phanmemtuyensinh.gui.DangNhapDialog;
import vn.edu.sgu.phanmemtuyensinh.gui.DashboardGUI;
import vn.edu.sgu.phanmemtuyensinh.gui.DiemCongXetTuyenGUI;
import vn.edu.sgu.phanmemtuyensinh.gui.DiemThiXetTuyenGUI;
import vn.edu.sgu.phanmemtuyensinh.gui.ModernTheme;
import vn.edu.sgu.phanmemtuyensinh.gui.NganhGUI;
import vn.edu.sgu.phanmemtuyensinh.gui.NganhToHopGUI;
import vn.edu.sgu.phanmemtuyensinh.gui.NguoiDungGUI;
import vn.edu.sgu.phanmemtuyensinh.gui.NguyenVongXetTuyenGUI;
import vn.edu.sgu.phanmemtuyensinh.gui.ThiSinhGUI;
import vn.edu.sgu.phanmemtuyensinh.gui.ToHopMonGUI;

public class PhanMemTuyenSinh extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final NguoiDung currentUser;
    private JLabel userLabel;

    public PhanMemTuyenSinh(NguoiDung currentUser) {
        this.currentUser = currentUser;
        ModernTheme.applyGlobalTheme();

        setTitle("Hệ Thống Quản Lý Tuyển Sinh - SGU");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1480, 860);
        setMinimumSize(new Dimension(1300, 780));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ModernTheme.APP_BG);

        JPanel sidebar = buildSidebar();
        JPanel mainArea = buildMainArea();

        root.add(sidebar, BorderLayout.WEST);
        root.add(mainArea, BorderLayout.CENTER);
        setContentPane(root);

        setVisible(true);

        applyRolePermissions();

        showPage("dashboard");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(ModernTheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(230, 0));

        JPanel brand = new JPanel(new BorderLayout());
        brand.setBackground(new Color(22, 31, 104));
        brand.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("SGU Tuyển sinh");
        title.setForeground(ModernTheme.TEXT_LIGHT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel sub = new JLabel("Năm 2026");
        sub.setForeground(new Color(170, 188, 255));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        brand.add(title, BorderLayout.NORTH);
        brand.add(sub, BorderLayout.SOUTH);

        JPanel nav = new JPanel(new GridLayout(0, 1, 0, 2));
        nav.setBackground(ModernTheme.SIDEBAR_BG);
        nav.setBorder(new EmptyBorder(10, 10, 10, 10));

        addNavItem(nav, "dashboard", "Tổng quan", new DashboardGUI(), "TQ", new Color(40, 167, 69));
        addNavItem(nav, "nguoidung", "Người dùng", new NguoiDungGUI(), "ND", new Color(0, 150, 136));
        addNavItem(nav, "thisinh", "Thí sinh", new ThiSinhGUI(), "TS", new Color(33, 150, 243));
        addNavItem(nav, "nganh", "Ngành tuyển sinh", new NganhGUI(), "NG", new Color(156, 39, 176));
        addNavItem(nav, "tohop", "Tổ hợp môn", new ToHopMonGUI(), "TH", new Color(255, 152, 0));
        addNavItem(nav, "nganh_tohop", "Ngành - Tổ hợp", new NganhToHopGUI(), "NT", new Color(103, 58, 183));
        addNavItem(nav, "diemthi", "Điểm thi", new DiemThiXetTuyenGUI(), "DT", new Color(3, 169, 244));
        addNavItem(nav, "diemcong", "Điểm cộng", new DiemCongXetTuyenGUI(), "DC", new Color(255, 87, 34));
        addNavItem(nav, "nguyenvong", "Nguyện vọng & XT", new NguyenVongXetTuyenGUI(), "NV", new Color(233, 30, 99));
        addNavItem(nav, "bangquydoi", "Bảng quy đổi", new BangQuyDoiGUI(), "QD", new Color(96, 125, 139));

        JButton logoutButton = ModernTheme.createSidebarButton("Đăng xuất");
        logoutButton.addActionListener(e -> {
            int choose = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn đăng xuất?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (choose == JOptionPane.YES_OPTION) {
                dispose();
                openAppWithLogin();
            }
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(ModernTheme.SIDEBAR_BG);
        bottom.setBorder(new EmptyBorder(6, 10, 12, 10));
        bottom.add(logoutButton, BorderLayout.CENTER);

        JScrollPane navScroll = new JScrollPane(nav,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.setBorder(null);
        navScroll.getViewport().setOpaque(false);
        navScroll.getViewport().setBackground(ModernTheme.SIDEBAR_BG);
        navScroll.setBackground(ModernTheme.SIDEBAR_BG);

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(navScroll, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(ModernTheme.APP_BG);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER_LIGHT),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel header = new JLabel("Hệ thống Quản lý Tuyển sinh SGU 2026");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(ModernTheme.TEXT_DARK);

        userLabel = new JLabel(buildUserText());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(87, 101, 130));

        top.add(header, BorderLayout.WEST);
        top.add(userLabel, BorderLayout.EAST);

        contentPanel.setBackground(ModernTheme.APP_BG);

        main.add(top, BorderLayout.NORTH);
        main.add(contentPanel, BorderLayout.CENTER);
        return main;
    }

    private void addNavItem(JPanel nav, String key, String label, JComponent page, String iconText, Color iconColor) {
        JButton button = ModernTheme.createSidebarButton(label, iconText, iconColor);
        button.addActionListener(e -> showPage(key));
        nav.add(button);
        navButtons.put(key, button);
        contentPanel.add(ModernTheme.createPageContainer(page), key);
    }

    private void showPage(String key) {
        cardLayout.show(contentPanel, key);
        navButtons.forEach((k, b) -> ModernTheme.setSidebarButtonActive(b, k.equals(key)));
    }

    private String buildUserText() {
        if (currentUser == null) {
            return "Chưa đăng nhập";
        }
        String name = currentUser.getHoTen() == null || currentUser.getHoTen().isBlank()
                ? currentUser.getTaiKhoan()
                : currentUser.getHoTen();
        String role = "admin".equalsIgnoreCase(currentUser.getPhanQuyen()) ? "Admin" : "User";
        return name + " (" + role + ")";
    }

    private void applyRolePermissions() {
        if (currentUser == null || "admin".equalsIgnoreCase(currentUser.getPhanQuyen())) {
            return;
        }
        setMutationButtonsEnabled(contentPanel, false);
    }

    private void setMutationButtonsEnabled(Component component, boolean enabled) {
        if (component instanceof JButton button) {
            String text = button.getText() == null ? "" : button.getText().toLowerCase();
            if (text.contains("thêm") || text.contains("sửa") || text.contains("xóa") || text.contains("import")) {
                button.setEnabled(enabled);
                if (!enabled) {
                    button.setToolTipText("Tài khoản user chỉ có quyền xem dữ liệu");
                }
            }
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                setMutationButtonsEnabled(child, enabled);
            }
        }
    }

    private static void openAppWithLogin() {
        NguoiDung loggedIn = DangNhapDialog.showLogin(null);
        if (loggedIn != null) {
            PhanMemTuyenSinh app = new PhanMemTuyenSinh(loggedIn);
            app.toFront();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PhanMemTuyenSinh::openAppWithLogin);
    }
}