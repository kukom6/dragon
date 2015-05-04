package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;

/**
 * Created by Michal on 27.4.2015.
 */
public class Main {
    public static void main(String[] args) {
        // Inicializaci GUI provedeme ve vlákně message dispatcheru,
        // ne v hlavním vlákně (bude vysvětleno později)!
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
                CustomerManager customerManager =  springContext.getBean("customerManager", CustomerManager.class);
                DragonManager dragonManager = springContext.getBean("dragonManager", DragonManager.class);
                LeaseManager leaseManager = springContext.getBean("leaseManager", LeaseManager.class);
                MainWindow g = new MainWindow(dragonManager, customerManager, leaseManager);
                g.setVisible(true);
            }
        });
    }
}
