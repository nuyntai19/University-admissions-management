package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import vn.edu.sgu.phanmemtuyensinh.bus.NguyenVongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.bus.ThiSinhBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;

public class DashboardGUI extends JPanel {

    private final ThiSinhBUS thiSinhBUS = new ThiSinhBUS();
    private final NguyenVongXetTuyenBUS nguyenVongBUS = new NguyenVongXetTuyenBUS();

    public DashboardGUI() {
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(ModernTheme.APP_BG);

        JLabel lblTitle = new JLabel("Dashboard - Hệ thống Quản lý Tuyển sinh", JLabel.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(ModernTheme.TEXT_DARK);

        JLabel lblSub = new JLabel("Tổng quan số liệu hệ thống theo thời gian thực", JLabel.LEFT);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(103, 116, 143));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        JPanel pnlMain = new JPanel(new BorderLayout(14, 14));
        pnlMain.setOpaque(false);

    DashboardStats stats = loadDashboardStats();

        JPanel statRow = new JPanel(new GridLayout(1, 4, 12, 12));
        statRow.setOpaque(false);
    statRow.add(createStatCard("Thí sinh", formatCount(stats.totalThiSinh), new Color(52, 152, 219), "Tổng số thí sinh trong hệ thống"));
    statRow.add(createStatCard("Nguyện vọng", formatCount(stats.totalNguyenVong), new Color(46, 204, 113), "Tổng số nguyện vọng xét tuyển"));
    statRow.add(createStatCard("Trúng tuyển", formatCount(stats.datCount), new Color(155, 89, 182), "Số nguyện vọng đạt / trúng tuyển"));
    statRow.add(createStatCard("Tỷ lệ đậu/rớt", formatPercent(stats.passRate), new Color(26, 188, 156), stats.rateHint));

        JPanel centerRow = new JPanel(new BorderLayout(12, 12));
        centerRow.setOpaque(false);
    centerRow.add(createQuickActionPanel(), BorderLayout.WEST);
    centerRow.add(createRecentTablePanel(stats.latestResults), BorderLayout.CENTER);

        pnlMain.add(statRow, BorderLayout.NORTH);
        pnlMain.add(centerRow, BorderLayout.CENTER);

        add(pnlMain, BorderLayout.CENTER);

        JLabel lblFooter = new JLabel("Hệ thống quản lý tuyển sinh - Phiên bản 1.0", JLabel.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblFooter.setForeground(new Color(118, 128, 150));
        add(lblFooter, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, String value, Color bgColor, String hintText) {
        JPanel card = new GradientCard(bgColor.darker(), bgColor);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblValue.setForeground(Color.WHITE);

        JLabel lblHint = new JLabel(hintText);
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(new Color(240, 244, 255));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblHint, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createQuickActionPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setPreferredSize(new java.awt.Dimension(300, 0));

        JLabel title = new JLabel("Tác vụ nhanh");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ModernTheme.TEXT_DARK);

        JPanel actions = new JPanel(new GridLayout(0, 1, 8, 8));
        actions.setOpaque(false);

        List<JButton> buttons = Arrays.asList(
                new JButton("Thêm thí sinh"),
                new JButton("Import điểm thi"),
                new JButton("Chạy xét tuyển"),
                new JButton("Xuất báo cáo")
        );
        for (JButton btn : buttons) {
            actions.add(btn);
        }

        panel.add(title, BorderLayout.NORTH);
        panel.add(actions, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRecentTablePanel(List<NguyenVongXetTuyen> recentResults) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Hoạt động gần đây");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ModernTheme.TEXT_DARK);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chips.add(createChip("Tất cả (34)", new Color(55, 65, 81)));
        chips.add(createChip("Cần tư vấn (26)", new Color(16, 185, 129)));
        chips.add(createChip("Đã hẹn test (6)", new Color(34, 197, 94)));
        chips.add(createChip("Ưu tiên (1)", new Color(236, 72, 153)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(new JButton("Lọc"));
        actions.add(new JButton("Tìm"));
        actions.add(new JButton("Export"));
        actions.add(new JButton("Import"));
        actions.add(new JButton("Thêm"));

        JPanel topMeta = new JPanel(new BorderLayout(0, 8));
        topMeta.setOpaque(false);
        topMeta.add(chips, BorderLayout.NORTH);
        topMeta.add(actions, BorderLayout.SOUTH);

        String[] cols = {"CCCD", "Mã ngành", "Phương thức", "Điểm XT", "Kết quả"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (NguyenVongXetTuyen nv : recentResults) {
            model.addRow(new Object[]{
                safe(nv.getNvCccd()),
                safe(nv.getNvMaNganh()),
                safe(nv.getTtPhuongThuc()),
                formatScore(nv),
                displayResult(nv.getNvKetQua())
            });
        }

        JTable table = new JTable(model);
        ModernTheme.styleTable(table);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(topMeta, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private DashboardStats loadDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.totalThiSinh = thiSinhBUS.countAll();

        List<NguyenVongXetTuyen> allResults = nguyenVongBUS.getAll();
        if (allResults == null) {
            allResults = new ArrayList<>();
        }

        stats.totalNguyenVong = allResults.size();
        long dat = 0;
        long rot = 0;

        for (NguyenVongXetTuyen nv : allResults) {
            String result = normalizeText(nv.getNvKetQua());
            if (isExactFailedResult(result)) {
                rot++;
            } else if (isExactPassedResult(result)) {
                dat++;
            }
        }

        stats.datCount = dat;
        stats.rotCount = rot;
        long evaluated = dat + rot;
        stats.passRate = evaluated == 0 ? 0d : (double) dat / (double) evaluated;
        stats.rateHint = String.format(Locale.US, "Đậu: %s • Rớt: %s",
                formatPercent(stats.passRate),
                formatPercent(evaluated == 0 ? 0d : (double) rot / (double) evaluated));

        allResults.sort(Comparator.comparingInt(NguyenVongXetTuyen::getIdNv).reversed());
        stats.latestResults = allResults.stream().limit(4).toList();
        return stats;
    }

    private String formatCount(long value) {
        return NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(value);
    }

    private String formatPercent(double ratio) {
        return String.format(Locale.US, "%.1f%%", ratio * 100d);
    }

    private String formatScore(NguyenVongXetTuyen nv) {
        if (nv.getDiemXetTuyen() == null) {
            return "-";
        }
        return nv.getDiemXetTuyen().stripTrailingZeros().toPlainString();
    }

    private String displayResult(String result) {
        String normalized = normalizeText(result);
        if (isExactFailedResult(normalized)) {
            return "Rớt";
        }
        if (isExactPassedResult(normalized)) {
            return "Trúng tuyển";
        }
        return result == null || result.isBlank() ? "Chưa có" : result.trim();
    }

    private boolean isExactPassedResult(String normalizedResult) {
        return "dat".equals(normalizedResult)
                || "trungtuyen".equals(normalizedResult)
                || "dattuyen".equals(normalizedResult);
    }

    private boolean isExactFailedResult(String normalizedResult) {
        return "truot".equals(normalizedResult)
                || "rot".equals(normalizedResult)
                || "khongdat".equals(normalizedResult)
                || "khongtrungtuyen".equals(normalizedResult);
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        normalized = normalized.replace('đ', 'd').replace('Đ', 'D');
        return normalized.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    private static class DashboardStats {
        long totalThiSinh;
        long totalNguyenVong;
        long datCount;
        long rotCount;
        double passRate;
        String rateHint;
        List<NguyenVongXetTuyen> latestResults = List.of();
    }

    private JLabel createChip(String text, Color bg) {
        JLabel chip = new JLabel(text);
        chip.setOpaque(true);
        chip.setBackground(bg);
        chip.setForeground(Color.WHITE);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chip.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return chip;
    }

    private static class GradientCard extends JPanel {

        private final Color start;
        private final Color end;

        private GradientCard(Color start, Color end) {
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint paint = new GradientPaint(0, 0, start, getWidth(), getHeight(), end);
            g2.setPaint(paint);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

            g2.setColor(new Color(255, 255, 255, 38));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            g2.dispose();

            super.paintComponent(g);
        }
    }
}
