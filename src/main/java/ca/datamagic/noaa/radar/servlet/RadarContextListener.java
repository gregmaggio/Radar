/**
 * 
 */
package ca.datamagic.noaa.radar.servlet;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Greg
 *
 */
public class RadarContextListener implements ServletContextListener {
	private static Logger logger = Logger.getLogger(RadarContextListener.class.getName());
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("contextInitialized");		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("contextDestroyed");
	}
}
