/**
 * 
 */
package ca.datamagic.noaa.radar.servlet;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import ca.datamagic.noaa.radar.dao.RadarSiteDAO;

/**
 * @author Greg
 *
 */
public class RadarContextListener implements ServletContextListener {
	private static Logger logger = Logger.getLogger(RadarContextListener.class.getName());
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String realPath = sce.getServletContext().getRealPath("/");
		if (realPath.endsWith("/")) {
			realPath = realPath.substring(0, realPath.length() - 1);
		}
		String dataPath = MessageFormat.format("{0}/WEB-INF/classes", realPath);
		try {
			RadarSiteDAO.initialize(dataPath);
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}		
		logger.info("contextInitialized");		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("contextDestroyed");
	}
}
