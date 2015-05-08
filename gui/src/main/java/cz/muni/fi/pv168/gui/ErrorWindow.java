package cz.muni.fi.pv168.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Matej on 8. 5. 2015.
 */
public class ErrorWindow extends JFrame{
    private JPanel jPanel1;
    private JTextArea textArea1;
    private JButton OKButton;

    public ErrorWindow(String chyba){
        setContentPane(jPanel1);
        textArea1.setText(chyba);
        OKButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            dispose();
            }
        });
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }
}
