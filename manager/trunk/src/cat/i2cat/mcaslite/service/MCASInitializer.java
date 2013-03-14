package cat.i2cat.mcaslite.service;


import javax.servlet.ServletContextEvent;

import javax.servlet.ServletContextListener;


public class MCASInitializer implements ServletContextListener{

    @Override

    public void contextDestroyed(ServletContextEvent arg0) {

    }

    @Override

    public void contextInitialized(ServletContextEvent arg0) {

        System.setProperty("mcas.home", arg0.getServletContext().getRealPath("/"));

    }   
}
