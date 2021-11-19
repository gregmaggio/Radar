/**
 * 
 */
package ca.datamagic.noaa.radar.servlet;

import java.text.MessageFormat;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.datamagic.noaa.radar.Importer;
import ca.datamagic.noaa.radar.dao.RadarSiteDAO;
import ca.datamagic.noaa.radar.dto.RadarSiteDTO;

/**
 * @author Greg
 *
 */
public class RadarContextListener implements ServletContextListener {
	private static Logger logger = LogManager.getLogger(RadarContextListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String realPath = sce.getServletContext().getRealPath("/");
		String dataPath = MessageFormat.format("{0}/WEB-INF/classes", realPath);
		RadarSiteDAO.setDataPath(dataPath);		
		logger.debug("contextInitialized");
		boolean shapeFileGood = false;
		try {
			RadarSiteDAO dao = new RadarSiteDAO();
			List<RadarSiteDTO> sites = dao.list();
			shapeFileGood = sites.size() > 0;
		} catch (Throwable t) {
			logger.warn("Throwable", t);
			shapeFileGood = false;
		}
		if (!shapeFileGood) {
			try {
				Importer.importFromJson();
			} catch (Throwable t) {
				logger.error("Throwable", t);
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.debug("contextDestroyed");
	}
}
