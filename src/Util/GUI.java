package Util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class GUI {
    private final JDialog mainDialog;
    private boolean submitted;
    public static int userInput = -1;
    private static GUI guiInstance;

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
        panel.setLayout(new GridLayout(3, 1));

        JPanel horizPanel = new JPanel();
        JLabel label = new JLabel("Menu Selection (int or empty):");
        JTextField textField = new JTextField(5);
        horizPanel.add(label);
        horizPanel.add(textField);

        panel.add(horizPanel);
        JLabel errorLabel = new JLabel("Entry must be [1-9] or empty");
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);
        panel.add(errorLabel);
        JButton submitBtn = new JButton("Submit");
        panel.add(submitBtn);

        mainDialog.getContentPane().add(panel);

        submitBtn.addActionListener(e -> {
            try {
                String text = textField.getText();
                if(text == null || text.isEmpty()) {
                    userInput = -1;
                } else {
                    userInput = Integer.parseInt(textField.getText());
                    submitted = true;
                    mainDialog.setVisible(false);
                    mainDialog.dispose();
                }
            } catch (NumberFormatException nfe) {
                errorLabel.setVisible(true);
            }
        });

        mainDialog.pack();
    }



    public static void startAndAwaitInput() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                guiInstance = new GUI();
                guiInstance.mainDialog.setVisible(true);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            userInput = -1;
        }
        if(!guiInstance.submitted) {
            userInput = -1;
        }
    }
}
