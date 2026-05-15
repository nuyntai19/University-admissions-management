package vn.edu.sgu.phanmemtuyensinh.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class GroupableTableHeaderUI extends BasicTableHeaderUI {
    @Override
    public void paint(Graphics g, JComponent c) {
        Rectangle clipBounds = g.getClipBounds();
        if (header.getColumnModel() == null) return;
        ((GroupableTableHeader) header).setColumnMargin();
        int column = 0;
        Dimension size = header.getSize();
        Rectangle cellRect = new Rectangle(0, 0, size.width, size.height);
        Hashtable<ColumnGroup, Rectangle> h = new Hashtable<ColumnGroup, Rectangle>();
        Enumeration<TableColumn> enumeration = header.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
            cellRect.height = size.height;
            cellRect.y = 0;
            TableColumn aColumn = enumeration.nextElement();
            Enumeration<ColumnGroup> cGroups = ((GroupableTableHeader) header).getColumnGroups(aColumn);
            if (cGroups != null) {
                int numGroups = 0;
                Enumeration<ColumnGroup> cGroups2 = ((GroupableTableHeader) header).getColumnGroups(aColumn);
                while (cGroups2.hasMoreElements()) { cGroups2.nextElement(); numGroups++; }
                
                int levelHeight = size.height / (numGroups + 1);
                int currentY = 0;
                
                Enumeration<ColumnGroup> cGroupsPaint = ((GroupableTableHeader) header).getColumnGroups(aColumn);
                while (cGroupsPaint.hasMoreElements()) {
                    ColumnGroup cGroup = cGroupsPaint.nextElement();
                    Rectangle groupRect = h.get(cGroup);
                    if (groupRect == null) {
                        groupRect = new Rectangle(cellRect);
                        Dimension d = cGroup.getSize(header.getTable());
                        groupRect.width = d.width;
                        groupRect.height = levelHeight;
                        groupRect.y = currentY;
                        h.put(cGroup, groupRect);
                    }
                    paintCell(g, groupRect, cGroup);
                    currentY += levelHeight;
                }
                cellRect.y = currentY;
                cellRect.height = size.height - currentY;
            }
            cellRect.width = aColumn.getWidth();
            if (cellRect.intersects(clipBounds)) {
                paintCell(g, cellRect, column);
            }
            cellRect.x += cellRect.width;
            column++;
        }
    }

    private void paintCell(Graphics g, Rectangle cellRect, int columnIndex) {
        TableColumn aColumn = header.getColumnModel().getColumn(columnIndex);
        TableCellRenderer renderer = aColumn.getHeaderRenderer();
        if (renderer == null) {
            renderer = header.getDefaultRenderer();
        }
        
        if (renderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer) renderer).setHorizontalAlignment(JLabel.CENTER);
        }
        
        Component comp = renderer.getTableCellRendererComponent(
                header.getTable(), aColumn.getHeaderValue(), false, false,
                -1, columnIndex);
                
        comp.setBackground(header.getBackground());
        comp.setForeground(header.getForeground());
        comp.setFont(header.getFont());
        if (comp instanceof JComponent) {
            ((JComponent) comp).setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        }
        rendererPane.paintComponent(g, comp, header, cellRect.x, cellRect.y,
                cellRect.width, cellRect.height, true);
    }

    private void paintCell(Graphics g, Rectangle cellRect, ColumnGroup cGroup) {
        TableCellRenderer renderer = cGroup.getHeaderRenderer();
        Component comp = renderer.getTableCellRendererComponent(
                header.getTable(), cGroup.getHeaderValue(), false, false, -1, -1);
        
        comp.setBackground(header.getBackground());
        comp.setForeground(header.getForeground());
        comp.setFont(header.getFont());
        rendererPane.paintComponent(g, comp, header, cellRect.x, cellRect.y,
                cellRect.width, cellRect.height, true);
    }

    private int getHeaderHeight() {
        int height = 0;
        TableColumnModel columnModel = header.getColumnModel();
        for (int column = 0; column < columnModel.getColumnCount(); column++) {
            TableColumn aColumn = columnModel.getColumn(column);
            TableCellRenderer renderer = aColumn.getHeaderRenderer();
            if (renderer == null) {
                renderer = header.getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(
                    header.getTable(), aColumn.getHeaderValue(), false, false, -1, column);
            int cHeight = Math.max(comp.getPreferredSize().height, 35);
            Enumeration<ColumnGroup> e = ((GroupableTableHeader) header).getColumnGroups(aColumn);
            if (e != null) {
                while (e.hasMoreElements()) {
                    ColumnGroup cGroup = e.nextElement();
                    Dimension d = cGroup.getSize(header.getTable());
                    cHeight += Math.max(d.height, 35);
                }
            }
            height = Math.max(height, cHeight);
        }
        return height;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        long width = 0;
        Enumeration<TableColumn> enumeration = header.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = enumeration.nextElement();
            width = width + aColumn.getPreferredWidth();
        }
        return new Dimension((int) width, getHeaderHeight());
    }
}
