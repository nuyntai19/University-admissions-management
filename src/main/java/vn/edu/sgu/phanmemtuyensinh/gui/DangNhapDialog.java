package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import vn.edu.sgu.phanmemtuyensinh.bus.NguoiDungBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguoiDung;

public class DangNhapDialog extends JDialog {

    private final NguoiDungBUS nguoiDungBUS = new NguoiDungBUS();
    private final JTextField txtTaiKhoan = new JTextField();
    private final JPasswordField txtMatKhau = new JPasswordField();
    private NguoiDung loggedInUser;

    public DangNhapDialog(Frame owner) {
        super(owner, "Đăng nhập hệ thống", true);
        buildUI();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setSize(430, 270);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(new Color(245, 249, 255));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 87, 199));
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel title = new JLabel("HỆ THỐNG TUYỂN SINH SGU");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.add(title, BorderLayout.WEST);

        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 224, 242)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setOpaque(false);
        form.add(new JLabel("Tài khoản:"));
        form.add(txtTaiKhoan);
        form.add(new JLabel("Mật khẩu:"));
        form.add(txtMatKhau);
        formCard.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);

        JButton btnDangNhap = new JButton("Đăng nhập");
        JButton btnThoat = new JButton("Thoát");

        JPanel buttonWrap = new JPanel();
        buttonWrap.setOpaque(false);
        buttonWrap.add(btnDangNhap);
        buttonWrap.add(btnThoat);

        actions.add(buttonWrap, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);
        root.add(formCard, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        ModernTheme.styleDialogContent(root);

        btnDangNhap.addActionListener(e -> dangNhap());
        btnThoat.addActionListener(e -> {
            loggedInUser = null;
            dispose();
        });
        getRootPane().setDefaultButton(btnDangNhap);

        setContentPane(root);
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
}
