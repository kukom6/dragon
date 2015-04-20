package cz.muni.fi.pv168.web;

import cz.muni.fi.pv168.dragon.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class StartListener implements ServletContextListener {

    final static Logger log = LoggerFactory.getLogger(StartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("aplikace inicializována");
        ServletContext servletContext = ev.getServletContext();
        ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        servletContext.setAttribute("customerManager", springContext.getBean("customerManager", CustomerManager.class));
        servletContext.setAttribute("dragonManager", springContext.getBean("dragonManager", DragonManager.class));
        servletContext.setAttribute("leaseManager", springContext.getBean("leaseManager", LeaseManager.class));
        log.info("vytvořeny manažery");
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("aplikace končí");
    }
}