package vn.edu.sgu.phanmemtuyensinh.gui;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class GroupableTableHeader extends JTableHeader {
    protected Vector<ColumnGroup> columnGroups = null;

    public GroupableTableHeader(TableColumnModel model) {
        super(model);
        setUI(new GroupableTableHeaderUI());
        setReorderingAllowed(false);
    }

    @Override
    public void updateUI() {
        setUI(new GroupableTableHeaderUI());
    }

    @Override
    public void setReorderingAllowed(boolean b) {
        super.setReorderingAllowed(false);
    }

    public void addColumnGroup(ColumnGroup g) {
        if (columnGroups == null) {
            columnGroups = new Vector<ColumnGroup>();
        }
        columnGroups.addElement(g);
    }

    public Enumeration<ColumnGroup> getColumnGroups(TableColumn col) {
        if (columnGroups == null) return null;
        Enumeration<ColumnGroup> e = columnGroups.elements();
        while (e.hasMoreElements()) {
            ColumnGroup cGroup = e.nextElement();
            Vector<Object> v_ret = cGroup.getColumnGroups(col, new Vector<Object>());
            if (v_ret != null) {
                Vector<ColumnGroup> casted = new Vector<>();
                for (Object o : v_ret) casted.add((ColumnGroup) o);
                return casted.elements();
            }
        }
        return null;
    }

    public void setColumnMargin() {
        if (columnGroups == null) return;
        int columnMargin = getColumnModel().getColumnMargin();
        Enumeration<ColumnGroup> e = columnGroups.elements();
        while (e.hasMoreElements()) {
            ColumnGroup cGroup = e.nextElement();
            cGroup.setColumnMargin(columnMargin);
        }
    }
}
