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

        JPanel centerRow = new JPanel(new GridLayout(1, 2, 12, 12));
        centerRow.setOpaque(false);
        centerRow.add(createBarChartPanel(stats.scoreDistribution));
        centerRow.add(createPieChartPanel(stats.topMajors));

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

    private JPanel createBarChartPanel(int[] dist) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 240)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Phổ điểm xét tuyển");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ModernTheme.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                int max = java.util.Arrays.stream(dist).max().orElse(1);
                if (max == 0) max = 1;
                
                String[] labels = {"< 15", "15 - 20", "20 - 25", "25 - 27", ">= 27"};
                int barWidth = (w - 40) / 5 - 20;
                if (barWidth < 10) barWidth = 10;
                
                for (int i = 0; i < 5; i++) {
                    int barHeight = (int) (((double) dist[i] / max) * (h - 60));
                    int x = 20 + i * (barWidth + 20);
                    int y = h - 30 - barHeight;
                    
                    GradientPaint gp = new GradientPaint(x, y, new Color(52, 152, 219), x, h - 30, new Color(41, 128, 185));
                    g2.setPaint(gp);
                    g2.fillRoundRect(x, y, barWidth, Math.max(barHeight, 5), 8, 8);
                    
                    g2.setColor(new Color(100, 100, 100));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    int stringWidth = g2.getFontMetrics().stringWidth(labels[i]);
                    g2.drawString(labels[i], x + (barWidth - stringWidth) / 2, h - 10);
                    
                    String val = String.valueOf(dist[i]);
                    int valWidth = g2.getFontMetrics().stringWidth(val);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2.setColor(ModernTheme.TEXT_DARK);
                    g2.drawString(val, x + (barWidth - valWidth) / 2, y - 5);
                }
            }
        };
        chart.setOpaque(false);
        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPieChartPanel(List<java.util.Map.Entry<String, Integer>> topMajors) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 240)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Top ngành được đăng ký nhiều nhất");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ModernTheme.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                int total = topMajors.stream().mapToInt(java.util.Map.Entry::getValue).sum();
                if (total == 0) return;
                
                int d = Math.min((int)(w * 0.45), h - 40);
                int x = 20;
                int y = (h - d) / 2;
                
                Color[] colors = {new Color(231, 76, 60), new Color(241, 196, 15), new Color(46, 204, 113), new Color(52, 152, 219), new Color(155, 89, 182)};
                
                int startAngle = 90;
                int legendX = x + d + 30;
                int legendY = Math.max(20, (h - (topMajors.size() * 35)) / 2 + 10);
                
                for (int i = 0; i < topMajors.size(); i++) {
                    java.util.Map.Entry<String, Integer> entry = topMajors.get(i);
                    int angle = (int) Math.round(((double) entry.getValue() / total) * 360);
                    if (i == topMajors.size() - 1) {
                        angle = 360 - (startAngle - 90); 
                    }
                    
                    g2.setColor(colors[i % colors.length]);
                    g2.fillArc(x, y, d, d, startAngle, angle);
                    
                    g2.fillRoundRect(legendX, legendY - 10, 12, 12, 4, 4);
                    g2.setColor(new Color(80, 80, 80));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    String lbl = entry.getKey();
                    if (lbl.length() > 22) lbl = lbl.substring(0, 19) + "...";
                    g2.drawString(lbl, legendX + 20, legendY);
                    
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    String valText = entry.getValue() + " NV (" + String.format(Locale.US, "%.1f", ((double)entry.getValue() / total) * 100) + "%)";
                    g2.drawString(valText, legendX + 20, legendY + 16);
                    
                    legendY += 35;
                    startAngle += angle;
                }
                
                // Draw Donut Hole
                g2.setColor(Color.WHITE);
                int innerD = (int)(d * 0.6);
                g2.fillOval(x + (d - innerD)/2, y + (d - innerD)/2, innerD, innerD);
            }
        };
        chart.setOpaque(false);
        panel.add(chart, BorderLayout.CENTER);
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

        java.util.Map<String, Integer> majorCount = new java.util.HashMap<>();
        int[] dist = new int[5];

        for (NguyenVongXetTuyen nv : allResults) {
            String result = normalizeText(nv.getNvKetQua());
            if (isExactFailedResult(result)) {
                rot++;
            } else if (isExactPassedResult(result)) {
                dat++;
            }
            
            if (nv.getDiemXetTuyen() != null) {
                double score = nv.getDiemXetTuyen().doubleValue();
                if (score < 15) dist[0]++;
                else if (score < 20) dist[1]++;
                else if (score < 25) dist[2]++;
                else if (score < 27) dist[3]++;
                else dist[4]++;
            }

            String mName = nv.getNvTenMaNganh();
            if (mName != null && !mName.isBlank()) {
                majorCount.put(mName, majorCount.getOrDefault(mName, 0) + 1);
            }
        }

        stats.datCount = dat;
        stats.rotCount = rot;
        long evaluated = dat + rot;
        stats.passRate = evaluated == 0 ? 0d : (double) dat / (double) evaluated;
        stats.rateHint = String.format(Locale.US, "Đậu: %s • Rớt: %s",
                formatPercent(stats.passRate),
                formatPercent(evaluated == 0 ? 0d : (double) rot / (double) evaluated));

        stats.scoreDistribution = dist;
        stats.topMajors = majorCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
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
        int[] scoreDistribution = new int[5];
        List<java.util.Map.Entry<String, Integer>> topMajors = new ArrayList<>();
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
