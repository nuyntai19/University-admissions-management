package vn.edu.sgu.phanmemtuyensinh.gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class SimpleDocumentListener {
    private SimpleDocumentListener() {
    }

    public static DocumentListener onChange(Runnable handler) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handler.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handler.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handler.run();
            }
        };
    }
}
