package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.LeaseManagerImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Matej on 8. 5. 2015.
 */
public class ErrorWindow extends JFrame{
    private JPanel jPanel1;
    private JTextArea textArea1;
    private JButton OKButton;
    private static final Logger log = Logger.getLogger(LeaseManagerImpl.class.getCanonicalName());

    public ErrorWindow(String error){
        log.log(Level.SEVERE, "Error windows start up, error: "+error);
        setContentPane(jPanel1);
        textArea1.setText(error);
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
