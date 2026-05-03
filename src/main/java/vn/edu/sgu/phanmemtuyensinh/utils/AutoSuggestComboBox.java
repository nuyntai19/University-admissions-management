package vn.edu.sgu.phanmemtuyensinh.utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class AutoSuggestComboBox extends JPanel {

    private JTextField txtInput;
    private JPopupMenu popup;
    private JList<String> list;
    private DefaultListModel<String> listModel;

    private Timer debounceTimer;

    // Hàm callback để search (truyền từ BUS vào)
    private Function<String, List<String>> searchFunction;

    public AutoSuggestComboBox(Function<String, List<String>> searchFunction) {
        this.searchFunction = searchFunction;

        setLayout(new BorderLayout());

        txtInput = new JTextField();
        add(txtInput, BorderLayout.CENTER);

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);

        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        popup.add(new JScrollPane(list));

        // debounce 300ms
        debounceTimer = new Timer(300, e -> search());
        debounceTimer.setRepeats(false);

        txtInput.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { debounceTimer.restart(); }
            public void removeUpdate(DocumentEvent e) { debounceTimer.restart(); }
            public void changedUpdate(DocumentEvent e) { debounceTimer.restart(); }
        });

        // click chọn
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!list.isSelectionEmpty()) {
                    txtInput.setText(list.getSelectedValue());
                    popup.setVisible(false);
                    // Trigger action listeners
                    for (java.awt.event.ActionListener al : txtInput.getActionListeners()) {
                        al.actionPerformed(new java.awt.event.ActionEvent(txtInput, java.awt.event.ActionEvent.ACTION_PERFORMED, null));
                    }
                }
            }
        });
    }

    private void search() {
        String keyword = txtInput.getText().trim();

        if (keyword.isEmpty()) {
            popup.setVisible(false);
            return;
        }

        new SwingWorker<List<String>, Void>() {
            protected List<String> doInBackground() {
                return searchFunction.apply(keyword);
            }

            protected void done() {
                try {
                    List<String> result = get();

                    listModel.clear();

                    if (result.isEmpty()) {
                        popup.setVisible(false);
                        return;
                    }

                    for (String s : result) {
                        listModel.addElement(s);
                    }

                    list.setVisibleRowCount(Math.min(8, result.size()));

                    popup.show(txtInput, 0, txtInput.getHeight());

                } catch (Exception ignored) {}
            }
        }.execute();
    }

    public String getText() {
        return txtInput.getText();
    }

    public void setText(String text) {
        txtInput.setText(text);
    }

    public void addActionListener(java.awt.event.ActionListener l) {
        txtInput.addActionListener(l);
    }

    public JTextField getTextField() {
        return txtInput;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        txtInput.setEnabled(enabled);
    }
}