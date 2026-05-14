package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vn.edu.sgu.phanmemtuyensinh.bus.NguyenVongXetTuyenBUS;
import vn.edu.sgu.phanmemtuyensinh.bus.NganhBUS;
import vn.edu.sgu.phanmemtuyensinh.bus.ThiSinhBUS;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.Nganh;
import vn.edu.sgu.phanmemtuyensinh.dal.entity.NguyenVongXetTuyen;

public class DashboardGUI extends JPanel {

    private final ThiSinhBUS thiSinhBUS = new ThiSinhBUS();
    private final NguyenVongXetTuyenBUS nguyenVongBUS = new NguyenVongXetTuyenBUS();
    private final NganhBUS nganhBUS = new NganhBUS();

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
        centerRow.add(createTopMajorsChartPanel(stats.topMajors), BorderLayout.CENTER);

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

    private JPanel createTopMajorsChartPanel(List<MajorCount> majors) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("Top 5 ngành xét tuyển nhiều nhất");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ModernTheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Số lượng thí sinh xét tuyển theo ngành");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(103, 116, 143));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);

        if (majors == null || majors.isEmpty()) {
            JLabel empty = new JLabel("Chưa có dữ liệu xét tuyển.", JLabel.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            empty.setForeground(new Color(123, 134, 156));
            panel.add(empty, BorderLayout.CENTER);
            return panel;
        }

        long max = majors.stream().mapToLong(m -> m.count).max().orElse(1);
        Color[] palette = {
                new Color(52, 152, 219),
                new Color(46, 204, 113),
                new Color(241, 196, 15),
                new Color(155, 89, 182),
                new Color(231, 76, 60)
        };

        JPanel rows = new JPanel(new GridLayout(majors.size(), 1, 0, 10));
        rows.setOpaque(false);

        for (int i = 0; i < majors.size(); i++) {
            rows.add(createMajorRow(majors.get(i), max, palette[i % palette.length]));
        }

        panel.add(rows, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMajorRow(MajorCount major, long max, Color color) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        String majorLabel = buildMajorLabel(major);
        String majorName = major.tenNganh == null ? "" : major.tenNganh.trim();
        String majorCode = major.maNganh == null ? "" : major.maNganh.trim();
        if (majorName.isBlank()) {
            majorName = majorCode;
            majorCode = "";
        } else if (majorName.equalsIgnoreCase(majorCode)) {
            majorCode = "";
        }

        JPanel labelPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        labelPanel.setOpaque(false);
        labelPanel.setPreferredSize(new Dimension(240, 36));

        JLabel lblName = new JLabel(majorName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(ModernTheme.TEXT_DARK);
        lblName.setToolTipText(majorLabel);

        JLabel lblCode = new JLabel(majorCode.isBlank() ? "" : "(" + majorCode + ")");
        lblCode.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCode.setForeground(new Color(103, 116, 143));

        labelPanel.add(lblName);
        labelPanel.add(lblCode);

        BarComponent bar = new BarComponent(major.count, max, color);
        bar.setPreferredSize(new Dimension(220, 16));

        JLabel lblValue = new JLabel(formatCount(major.count) + " thí sinh");
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblValue.setForeground(new Color(103, 116, 143));

        row.add(labelPanel, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(lblValue, BorderLayout.EAST);
        return row;
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

        Map<String, String> majorNameMap = new HashMap<>();
        List<Nganh> nganhList = nganhBUS.getAll();
        if (nganhList != null) {
            for (Nganh nganh : nganhList) {
                if (nganh == null) {
                    continue;
                }
                String ma = nganh.getMaNganh() == null ? "" : nganh.getMaNganh().trim();
                String ten = nganh.getTenNganh() == null ? "" : nganh.getTenNganh().trim();
                if (!ma.isBlank() && !ten.isBlank()) {
                    majorNameMap.put(ma, ten);
                }
            }
        }

        Map<String, Set<String>> majorStudents = new HashMap<>();
        for (NguyenVongXetTuyen nv : allResults) {
            String maNganh = nv.getNvMaNganh() == null ? "" : nv.getNvMaNganh().trim();
            if (maNganh.isBlank()) {
                continue;
            }
            String cccd = nv.getNvCccd() == null ? "" : nv.getNvCccd().trim();
            if (cccd.isBlank()) {
                cccd = "NV-" + nv.getIdNv();
            }
            majorStudents.computeIfAbsent(maNganh, k -> new HashSet<>()).add(cccd);
        }

        stats.topMajors = majorStudents.entrySet().stream()
            .map(entry -> {
                String ma = entry.getKey();
                String ten = majorNameMap.getOrDefault(ma, ma);
                return new MajorCount(ma, ten, entry.getValue().size());
            })
            .sorted(Comparator.comparingLong((MajorCount m) -> m.count).reversed()
                .thenComparing(m -> m.tenNganh))
                .limit(5)
                .toList();
        return stats;
    }

    private String formatCount(long value) {
        return NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(value);
    }

    private String formatPercent(double ratio) {
        return String.format(Locale.US, "%.1f%%", ratio * 100d);
    }

    private String buildMajorLabel(MajorCount major) {
        if (major.tenNganh == null || major.tenNganh.isBlank()) {
            return major.maNganh == null ? "" : major.maNganh;
        }
        if (major.maNganh == null || major.maNganh.isBlank()) {
            return major.tenNganh;
        }
        if (major.tenNganh.equalsIgnoreCase(major.maNganh)) {
            return major.maNganh;
        }
        return major.tenNganh + " (" + major.maNganh + ")";
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

    private static class DashboardStats {
        long totalThiSinh;
        long totalNguyenVong;
        long datCount;
        long rotCount;
        double passRate;
        String rateHint;
        List<MajorCount> topMajors = List.of();
    }

    private static class MajorCount {
        private final String maNganh;
        private final String tenNganh;
        private final long count;

        private MajorCount(String maNganh, String tenNganh, long count) {
            this.maNganh = maNganh;
            this.tenNganh = tenNganh;
            this.count = count;
        }
    }

    private static class BarComponent extends JPanel {
        private final long value;
        private final long max;
        private final Color barColor;
        private final Color trackColor = new Color(231, 235, 245);

        private BarComponent(long value, long max, Color barColor) {
            this.value = value;
            this.max = max;
            this.barColor = barColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int arc = Math.max(8, height);

            g2.setColor(trackColor);
            g2.fillRoundRect(0, 0, width, height, arc, arc);

            double ratio = max == 0 ? 0d : (double) value / (double) max;
            int barWidth = (int) Math.round(ratio * width);
            if (barWidth > 0) {
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, barWidth, height, arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
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
