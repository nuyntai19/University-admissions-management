package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import vn.edu.sgu.phanmemtuyensinh.bus.NguoiDungBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguoiDung;

public class DangNhapDialog extends JDialog {

    private static final String BACKGROUND_IMAGE_PATH = "D:\\PhanMemPhanLop\\PhanMemTuyenSinh\\data\\truong-dai-hoc-sai-gon-1.jpg";

    private final NguoiDungBUS nguoiDungBUS = new NguoiDungBUS();
    private final JTextField txtTaiKhoan = new JTextField();
    private final JPasswordField txtMatKhau = new JPasswordField();
    private NguoiDung loggedInUser;
    private final Image backgroundImage;

    public DangNhapDialog(Frame owner) {
        super(owner, "Đăng nhập hệ thống", true);
    backgroundImage = loadBackgroundImage();
        buildUI();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
    setSize(980, 620);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    BackgroundPanel root = new BackgroundPanel(backgroundImage);
    root.setLayout(new BorderLayout());

    JPanel veil = new JPanel(new GridBagLayout());
    veil.setOpaque(false);
    veil.setBorder(new EmptyBorder(28, 28, 28, 28));

    RoundedPanel shell = new RoundedPanel(
        new BorderLayout(0, 0),
        new Color(255, 255, 255, 28),
        34,
        new Color(255, 255, 255, 110),
        1.2f);
    shell.setOpaque(false);
    shell.setBorder(new EmptyBorder(18, 18, 18, 18));
    shell.setPreferredSize(new Dimension(900, 540));

    RoundedPanel leftHero = new RoundedPanel(new BorderLayout(0, 12), new Color(255, 255, 255, 22), 30);
    leftHero.setOpaque(false);
    leftHero.setBorder(new EmptyBorder(18, 18, 18, 28));

    JLabel heroTitle = new JLabel("HỆ THỐNG TUYỂN SINH SGU");
    heroTitle.setForeground(Color.WHITE);
    heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));

    JLabel heroSubtitle = new JLabel("Quản lý tuyển sinh, thí sinh, điểm thi và xét tuyển");
    heroSubtitle.setForeground(new Color(240, 245, 255));
    heroSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

    JPanel heroBadge = new JPanel();
    heroBadge.setOpaque(false);
    JLabel badge = new JLabel("University Admissions Management");
    badge.setForeground(new Color(224, 236, 255));
    badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
    heroBadge.add(badge);

    JPanel heroTextWrap = new JPanel(new GridBagLayout());
    heroTextWrap.setOpaque(false);
    GridBagConstraints heroGbc = new GridBagConstraints();
    heroGbc.gridx = 0;
    heroGbc.gridy = 0;
    heroGbc.anchor = GridBagConstraints.WEST;
    heroGbc.insets = new Insets(0, 0, 8, 0);
    heroTextWrap.add(heroTitle, heroGbc);
    heroGbc.gridy++;
    heroTextWrap.add(heroSubtitle, heroGbc);
    heroGbc.gridy++;
    heroTextWrap.add(heroBadge, heroGbc);

    RoundedPanel heroNote = new RoundedPanel(
        new BorderLayout(),
        new Color(255, 255, 255, 18),
        24,
        new Color(255, 255, 255, 70),
        1f);
    heroNote.setBorder(new EmptyBorder(14, 16, 14, 16));

    JLabel note = new JLabel("Đăng nhập để tiếp tục vào hệ thống quản lý tuyển sinh.");
    note.setForeground(Color.WHITE);
    note.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    heroNote.add(note, BorderLayout.CENTER);

    leftHero.add(heroTextWrap, BorderLayout.NORTH);
    leftHero.add(heroNote, BorderLayout.SOUTH);

    JPanel rightPanel = new JPanel(new GridBagLayout());
    rightPanel.setOpaque(false);

    RoundedPanel formCard = new RoundedPanel(
        new BorderLayout(0, 16),
        new Color(255, 255, 255, 242),
        34,
        new Color(255, 255, 255, 185),
        1f);
    formCard.setBorder(new EmptyBorder(22, 22, 22, 22));
    formCard.setPreferredSize(new Dimension(370, 420));

    JLabel formTitle = new JLabel("Đăng nhập hệ thống", SwingConstants.LEFT);
    formTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
    formTitle.setForeground(new Color(30, 50, 92));

    JLabel formDesc = new JLabel("Sử dụng tài khoản quản trị hoặc người dùng đã được cấp.");
    formDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    formDesc.setForeground(new Color(92, 106, 132));

    JPanel formHeader = new JPanel(new BorderLayout(0, 6));
    formHeader.setOpaque(false);
    formHeader.add(formTitle, BorderLayout.NORTH);
    formHeader.add(formDesc, BorderLayout.SOUTH);

    JPanel form = new JPanel(new GridBagLayout());
    form.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 0, 8, 10);

    JLabel lblTaiKhoan = new JLabel("Tài khoản");
    lblTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblTaiKhoan.setForeground(new Color(54, 68, 88));
    form.add(lblTaiKhoan, gbc);
    gbc.gridx = 1;
    gbc.weightx = 1;
    gbc.insets = new Insets(0, 0, 8, 0);
    styleInput(txtTaiKhoan);
    form.add(txtTaiKhoan, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0;
    gbc.insets = new Insets(0, 0, 8, 10);
    JLabel lblMatKhau = new JLabel("Mật khẩu");
    lblMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblMatKhau.setForeground(new Color(54, 68, 88));
    form.add(lblMatKhau, gbc);
    gbc.gridx = 1;
    gbc.weightx = 1;
    gbc.insets = new Insets(0, 0, 8, 0);
    styleInput(txtMatKhau);
    form.add(txtMatKhau, gbc);

    JPanel cardBody = new JPanel(new BorderLayout(0, 16));
    cardBody.setOpaque(false);
    cardBody.add(formHeader, BorderLayout.NORTH);
    cardBody.add(form, BorderLayout.CENTER);

    formCard.add(cardBody, BorderLayout.CENTER);

    JPanel actions = new JPanel(new BorderLayout());
    actions.setOpaque(false);

        JButton btnDangNhap = new JButton("Đăng nhập");
        JButton btnThoat = new JButton("Thoát");
    btnDangNhap.setPreferredSize(new Dimension(160, 40));
    btnThoat.setPreferredSize(new Dimension(120, 40));

    JPanel buttonWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    buttonWrap.setOpaque(false);
        buttonWrap.add(btnDangNhap);
        buttonWrap.add(btnThoat);

        actions.add(buttonWrap, BorderLayout.EAST);

    rightPanel.add(formCard);

    JPanel center = new JPanel(new GridBagLayout());
    center.setOpaque(false);
    GridBagConstraints centerGbc = new GridBagConstraints();
    centerGbc.gridx = 0;
    centerGbc.gridy = 0;
    centerGbc.weightx = 0.58;
    centerGbc.weighty = 1;
    centerGbc.fill = GridBagConstraints.BOTH;
    centerGbc.insets = new Insets(0, 0, 0, 20);
    center.add(leftHero, centerGbc);
    centerGbc.gridx = 1;
    centerGbc.weightx = 0.42;
    centerGbc.insets = new Insets(0, 0, 0, 0);
    center.add(rightPanel, centerGbc);

    JPanel inner = new JPanel(new BorderLayout(0, 14));
    inner.setOpaque(false);
    inner.add(center, BorderLayout.CENTER);
    inner.add(actions, BorderLayout.SOUTH);

    GridBagConstraints shellGbc = new GridBagConstraints();
    shellGbc.gridx = 0;
    shellGbc.gridy = 0;
    shellGbc.weightx = 1;
    shellGbc.weighty = 1;
    shellGbc.fill = GridBagConstraints.BOTH;
    veil.add(shell, shellGbc);

    shell.add(inner, BorderLayout.CENTER);
    root.add(veil, BorderLayout.CENTER);

        ModernTheme.styleDialogContent(root);
    ModernTheme.styleDialogContent(shell);
    ModernTheme.styleDialogContent(formCard);
    ModernTheme.styleDialogContent(form);
    ModernTheme.styleDialogContent(buttonWrap);

        // Apply rounded button style after theme pass to avoid being overridden.
    styleRoundedButton(btnDangNhap, new Color(35, 108, 224));
    styleRoundedButton(btnThoat, new Color(71, 92, 122));

        btnDangNhap.addActionListener(e -> dangNhap());
        btnThoat.addActionListener(e -> {
            loggedInUser = null;
            dispose();
        });
        getRootPane().setDefaultButton(btnDangNhap);

        setContentPane(root);
    }

    private void styleInput(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(220, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 220, 235), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        field.setBackground(Color.WHITE);
    }

    private void styleRoundedButton(JButton button, Color background) {
        button.setIcon(null);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.putClientProperty("bgColor", background);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.putClientProperty("hover", Boolean.TRUE);
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.putClientProperty("hover", Boolean.FALSE);
                button.repaint();
            }
        });
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, javax.swing.JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = Boolean.TRUE.equals(((JButton) c).getClientProperty("hover"));
                Color bg = (Color) ((JButton) c).getClientProperty("bgColor");
                g2.setColor(hover ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 24, 24);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private Image loadBackgroundImage() {
        try {
            File file = new File(BACKGROUND_IMAGE_PATH);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void dangNhap() {
        String taiKhoan = txtTaiKhoan.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword());

        if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ tài khoản và mật khẩu!");
            return;
        }

        NguoiDung nd = nguoiDungBUS.authenticateAndGetUser(taiKhoan, matKhau);
        if (nd == null) {
            JOptionPane.showMessageDialog(this,
                    "Đăng nhập thất bại hoặc tài khoản đã bị khóa!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        loggedInUser = nd;
        dispose();
    }

    public NguoiDung getLoggedInUser() {
        return loggedInUser;
    }

    public static NguoiDung showLogin(Frame owner) {
        DangNhapDialog dialog = new DangNhapDialog(owner);
        dialog.setVisible(true);
        return dialog.getLoggedInUser();
    }

    private static class BackgroundPanel extends JPanel {
        private final Image image;

        private BackgroundPanel(Image image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (image != null) {
                g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(new Color(18, 44, 92));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.setColor(new Color(14, 31, 67, 120));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 255, 255, 28));
            g2.fillRoundRect(36, 42, 220, 120, 24, 24);
            g2.fillRoundRect(getWidth() - 280, getHeight() - 180, 210, 120, 24, 24);
            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color fillColor;
        private final int arc;
        private final Color borderColor;
        private final float borderWidth;

        private RoundedPanel(java.awt.LayoutManager layout, Color fillColor, int arc) {
            this(layout, fillColor, arc, null, 0f);
        }

        private RoundedPanel(java.awt.LayoutManager layout, Color fillColor, int arc, Color borderColor, float borderWidth) {
            super(layout);
            this.fillColor = fillColor;
            this.arc = arc;
            this.borderColor = borderColor;
            this.borderWidth = borderWidth;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(fillColor);

            int inset = Math.max(1, Math.round(borderWidth));
            g2.fillRoundRect(inset / 2, inset / 2, getWidth() - inset, getHeight() - inset, arc, arc);

            if (borderColor != null && borderWidth > 0f) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderWidth));
                g2.drawRoundRect(inset / 2, inset / 2, getWidth() - inset - 1, getHeight() - inset - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
