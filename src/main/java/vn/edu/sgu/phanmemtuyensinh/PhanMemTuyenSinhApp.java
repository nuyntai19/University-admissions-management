package vn.edu.sgu.phanmemtuyensinh;

import vn.edu.sgu.phanmemtuyensinh.gui.*;
import javax.swing.*;
import java.awt.*;

public class PhanMemTuyenSinhApp extends JFrame {

    public PhanMemTuyenSinhApp() {
        setTitle("Phần Mềm Quản Lí Tuyển Sinh");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Thêm Dashboard
        tabbedPane.addTab("Dashboard", new DashboardGUI());

        // Thêm các module
        tabbedPane.addTab("Quản Lý Người Dùng", new NguoiDungGUI());
        tabbedPane.addTab("Quản Lý Thí Sinh", new ThiSinhGUI());
        tabbedPane.addTab("Quản Lý Ngành", new NganhGUI());
        tabbedPane.addTab("Quản Lý Tổ Hợp Môn", new ToHopMonGUI());
        tabbedPane.addTab("Quản Lý Ngành-Tổ Hợp", new NganhToHopGUI());
        tabbedPane.addTab("Quản Lý Điểm Thi", new DiemThiXetTuyenGUI());
        tabbedPane.addTab("Quản Lý Điểm Cộng", new DiemCongXetTuyenGUI());
        tabbedPane.addTab("Quản Lý Nguyện Vọng", new NguyenVongXetTuyenGUI());
        tabbedPane.addTab("Quản Lý Bảng Quy Đổi", new BangQuyDoiGUI());

        add(tabbedPane);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PhanMemTuyenSinhApp());
    }
}
