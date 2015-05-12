package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.*;

/**
 * Created by Michal on 27.4.2015.
 */
public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        configureLogging();
        // Inicializaci GUI provedeme ve vlákně message dispatcheru,
        // ne v hlavním vlákně (bude vysvětleno později)!
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                DataSource dataSource = DBUtils.connectToDB();

                try {
                    DBUtils.tryCreateTables(dataSource, DBUtils.class.getClassLoader().getResource("dragon_schema.sql"));
                    log.log(Level.INFO, "Tables created/already exist");
                } catch (SQLException ex){
                    log.log(Level.SEVERE, "Creating tables unsuccessful: " + ex.getMessage());
                    throw new ServiceFailureException("Creating tables unsuccessful",ex);
                }

                TimeService timeService= new TimeServiceImpl();
                CustomerManager customerManager =  new CustomerManagerImpl(dataSource);
                DragonManager dragonManager = new DragonManagerImpl(dataSource, timeService);
                LeaseManager leaseManager = new LeaseManagerImpl(dataSource, timeService);
                MainWindow mainWindow = new MainWindow(dragonManager, customerManager, leaseManager);
                mainWindow.setVisible(true);
            }
        });
    }

    private static void configureLogging() {
        Handler fileHandler = null;
        Properties prop = new Properties();
        String propFileName = "config.properties";

        InputStream inputStream = DragonManager.class.getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ex){
                log.log(Level.SEVERE, "Cannot load property from input stream.");
                throw new ServiceFailureException("Cannot load property from input stream.");
            }
        } else {
            log.log(Level.SEVERE, "property file '" + propFileName + "' not found in the classpath");
            throw new ServiceFailureException("property file '" + propFileName + "' not found in the classpath");
        }
        try {
            fileHandler = new FileHandler(prop.getProperty("logFile"));
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Unable to initialize FileHandler", ex);
        } catch (SecurityException ex) {
            log.log(Level.SEVERE, "Unable to initialize FileHandler.", ex);
        }

        Logger.getLogger("").addHandler(fileHandler);
    }
}
