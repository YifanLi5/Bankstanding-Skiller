package Util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GUI {
    private final JDialog mainDialog;
    private boolean started;

    public GUI() {
        mainDialog = new JDialog();
        mainDialog.setTitle("What to make?");
        mainDialog.setModal(true);
        mainDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainDialog.getContentPane().add(mainPanel);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel label = new JLabel("Menu Selection (int):");
        JTextField textField = new JTextField();
        JButton button = new JButton("Submit");
        panel.add(label);
        panel.add(textField);
        panel.add(button);

        mainDialog.getContentPane().add(panel);

        button.addActionListener(e -> {
            started = true;
            close();
        });

        mainDialog.pack();
    }

    public void open() {
        mainDialog.setVisible(true);
    }

    public void close() {
        mainDialog.setVisible(false);
        mainDialog.dispose();
    }
}
