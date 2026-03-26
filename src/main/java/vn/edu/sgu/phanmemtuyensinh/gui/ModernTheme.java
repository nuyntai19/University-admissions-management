package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public final class ModernTheme {

    public static final Color APP_BG = new Color(246, 248, 253);
    public static final Color SIDEBAR_BG = new Color(24, 37, 111);
    public static final Color SIDEBAR_ACTIVE = new Color(39, 91, 221);
    public static final Color TEXT_LIGHT = new Color(243, 246, 255);
    public static final Color TEXT_DARK = new Color(35, 43, 62);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_LIGHT = new Color(219, 227, 243);

    private ModernTheme() {
    }

    public static void applyGlobalTheme() {
        UIManager.put("Panel.background", APP_BG);
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("TitledBorder.font", new Font("Segoe UI", Font.BOLD, 13));
    }

    public static JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBackground(SIDEBAR_BG);
        button.setForeground(TEXT_LIGHT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(12, 18, 12, 12));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton createSidebarButton(String text, String iconText, Color iconColor) {
        JButton button = createSidebarButton(text);
        button.setIcon(new CircleGlyphIcon(iconText, iconColor));
        button.setIconTextGap(10);
        return button;
    }

    public static void styleModuleTitle(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(TEXT_DARK);
    }

    public static void setSidebarButtonActive(JButton button, boolean active) {
        button.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        button.setForeground(TEXT_LIGHT);
    }

    public static JPanel createPageContainer(JComponent content) {
        styleComponentTree(content);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(APP_BG);
        wrapper.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            new EmptyBorder(12, 12, 12, 12)
        ));

        card.add(content, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    public static void styleDialogContent(JComponent content) {
        styleComponentTree(content);
    }

    private static void styleComponentTree(Component component) {
        if (component instanceof JTable) {
            styleTable((JTable) component);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            String text = button.getText() == null ? "" : button.getText().toLowerCase();

            boolean isSidebarButton = button.getParent() != null
                    && (SIDEBAR_BG.equals(button.getParent().getBackground())
                    || SIDEBAR_ACTIVE.equals(button.getParent().getBackground()));

            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setBorder(new EmptyBorder(8, 14, 8, 14));
            button.setOpaque(true);

            if (text.contains("thêm")) {
                button.setBackground(new Color(40, 167, 69));
                button.setForeground(Color.WHITE);
            } else if (text.contains("sửa")) {
                button.setBackground(new Color(243, 156, 18));
                button.setForeground(Color.WHITE);
            } else if (text.contains("xóa")) {
                button.setBackground(new Color(231, 76, 60));
                button.setForeground(Color.WHITE);
            } else if (text.contains("import")) {
                button.setBackground(new Color(255, 153, 0));
                button.setForeground(Color.WHITE);
            } else if (text.contains("tìm")) {
                button.setBackground(new Color(30, 136, 229));
                button.setForeground(Color.WHITE);
            } else if (text.contains("làm mới")) {
                button.setBackground(new Color(94, 114, 228));
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(new Color(36, 111, 217));
                button.setForeground(Color.WHITE);
            }

            if (!isSidebarButton) {
                decorateActionButtonIcon(button, text);
            }
        } else if (component instanceof JTextField) {
            JTextField field = (JTextField) component;
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(212, 220, 236)),
                    new EmptyBorder(6, 8, 6, 8)
            ));
            field.setBackground(Color.WHITE);
        } else if (component instanceof JCheckBox) {
            component.setBackground(Color.WHITE);
        } else if (component instanceof JComboBox) {
            ((JComboBox<?>) component).setBackground(Color.WHITE);
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                styleComponentTree(child);
            }
        }
    }

    private static void decorateActionButtonIcon(JButton button, String lowerText) {
        ActionIconType type = ActionIconType.DEFAULT;
        if (lowerText.contains("thêm")) {
            type = ActionIconType.ADD;
        } else if (lowerText.contains("sửa")) {
            type = ActionIconType.EDIT;
        } else if (lowerText.contains("xóa")) {
            type = ActionIconType.DELETE;
        } else if (lowerText.contains("làm mới")) {
            type = ActionIconType.REFRESH;
        } else if (lowerText.contains("tìm")) {
            type = ActionIconType.SEARCH;
        } else if (lowerText.contains("import")) {
            type = ActionIconType.IMPORT;
        }

        button.setIcon(new ActionGlyphIcon(type));
        button.setIconTextGap(8);
    }

    public static void styleTable(JTable table) {
        table.setGridColor(new Color(232, 236, 245));
        table.setSelectionBackground(new Color(229, 240, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer zebraRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 255));
                    c.setForeground(TEXT_DARK);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };

        StatusTagRenderer statusRenderer = new StatusTagRenderer();

        for (int i = 0; i < table.getColumnCount(); i++) {
            String headerName = table.getColumnName(i).toLowerCase();
            if (headerName.contains("trạng thái") || headerName.contains("trang thai")
                    || headerName.contains("kết quả") || headerName.contains("ket qua")) {
                table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(zebraRenderer);
            }
        }

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setBackground(new Color(37, 114, 222));
            header.setForeground(Color.WHITE);
            header.setBorder(BorderFactory.createLineBorder(new Color(30, 99, 196)));
            header.setReorderingAllowed(false);
            header.setPreferredSize(new Dimension(header.getWidth(), 34));
        }
    }

    private static class CircleGlyphIcon implements Icon {
        private final String text;
        private final Color color;

        private CircleGlyphIcon(String text, Color color) {
            this.text = text;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);
            g2.fillOval(x, y, 18, 18);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (18 - fm.stringWidth(text)) / 2;
            int ty = y + ((18 - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, tx, ty);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }
    }

    private static class StatusTagRenderer extends DefaultTableCellRenderer {
        private Color tagBg = new Color(226, 232, 240);
        private Color tagFg = new Color(51, 65, 85);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String raw = value == null ? "" : value.toString().trim();
            String normalized = raw.toLowerCase();

            if (normalized.contains("đạt") || normalized.contains("trúng") || normalized.contains("active")
                    || normalized.equals("1") || normalized.contains("enable") || normalized.contains("còn")) {
                tagBg = new Color(220, 252, 231);
                tagFg = new Color(22, 101, 52);
            } else if (normalized.contains("rớt") || normalized.contains("không") || normalized.contains("inactive")
                    || normalized.equals("0") || normalized.contains("disable")) {
                tagBg = new Color(254, 226, 226);
                tagFg = new Color(153, 27, 27);
            } else {
                tagBg = new Color(226, 232, 240);
                tagFg = new Color(51, 65, 85);
            }

            setText(raw.isEmpty() ? "-" : raw);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setOpaque(false);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int textWidth = g2.getFontMetrics(getFont()).stringWidth(getText());
            int pillWidth = Math.min(getWidth() - 12, textWidth + 24);
            int x = (getWidth() - pillWidth) / 2;
            int y = 4;
            int height = Math.max(20, getHeight() - 8);

            g2.setColor(tagBg);
            g2.fillRoundRect(x, y, pillWidth, height, 14, 14);
            g2.dispose();

            setForeground(tagFg);
            super.paintComponent(g);
        }
    }

    private enum ActionIconType {
        ADD,
        EDIT,
        DELETE,
        REFRESH,
        SEARCH,
        IMPORT,
        DEFAULT
    }

    private static class ActionGlyphIcon implements Icon {
        private final ActionIconType type;

        private ActionGlyphIcon(ActionIconType type) {
            this.type = type;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = x + 7;
            int cy = y + 7;

            switch (type) {
                case ADD:
                    g2.drawLine(cx, y + 3, cx, y + 11);
                    g2.drawLine(x + 3, cy, x + 11, cy);
                    break;
                case EDIT:
                    g2.drawLine(x + 3, y + 11, x + 11, y + 3);
                    g2.drawLine(x + 2, y + 12, x + 5, y + 12);
                    break;
                case DELETE:
                    g2.drawRect(x + 3, y + 4, 8, 8);
                    g2.drawLine(x + 2, y + 4, x + 12, y + 4);
                    g2.drawLine(x + 5, y + 2, x + 9, y + 2);
                    break;
                case REFRESH:
                    g2.drawArc(x + 2, y + 2, 10, 10, 35, 290);
                    g2.drawLine(x + 10, y + 2, x + 12, y + 2);
                    g2.drawLine(x + 12, y + 2, x + 12, y + 4);
                    break;
                case SEARCH:
                    g2.drawOval(x + 2, y + 2, 7, 7);
                    g2.drawLine(x + 8, y + 8, x + 12, y + 12);
                    break;
                case IMPORT:
                    g2.drawLine(cx, y + 2, cx, y + 8);
                    g2.drawLine(cx, y + 8, x + 5, y + 6);
                    g2.drawLine(cx, y + 8, x + 9, y + 6);
                    g2.drawLine(x + 3, y + 11, x + 11, y + 11);
                    break;
                default:
                    g2.fillOval(x + 5, y + 5, 4, 4);
                    break;
            }

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }
    }
}
