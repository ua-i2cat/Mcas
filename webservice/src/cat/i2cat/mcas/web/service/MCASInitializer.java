package cat.i2cat.mcas.web.service;


import java.io.File;
import java.nio.file.Paths;

import javax.servlet.ServletContextEvent;

import javax.servlet.ServletContextListener;


public class MCASInitializer implements ServletContextListener{

    @Override

    public void contextDestroyed(ServletContextEvent arg0) {

    }

    @Override

    public void contextInitialized(ServletContextEvent arg0) {

        System.setProperty("mcas.home", Paths.get(arg0.getServletContext().getRealPath(File.separator)).toString());

    }   
}
