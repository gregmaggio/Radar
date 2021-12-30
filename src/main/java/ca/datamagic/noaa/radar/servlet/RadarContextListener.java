/**
 * 
 */
package ca.datamagic.noaa.radar.servlet;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.geotools.referencing.factory.DeferredAuthorityFactory;
import org.geotools.util.WeakCollectionCleaner;

import ca.datamagic.noaa.radar.Importer;
import ca.datamagic.noaa.radar.dao.RadarSiteDAO;
import ca.datamagic.noaa.radar.dto.RadarSiteDTO;

/**
 * @author Greg
 *
 */
public class RadarContextListener implements ServletContextListener {
	private static Logger logger = Logger.getLogger(RadarContextListener.class.getName());
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String realPath = sce.getServletContext().getRealPath("/");
		String dataPath = MessageFormat.format("{0}/WEB-INF/classes", realPath);
		RadarSiteDAO.setDataPath(dataPath);		
		logger.info("contextInitialized");
		boolean shapeFileGood = false;
		try {
			RadarSiteDAO dao = new RadarSiteDAO();
			List<RadarSiteDTO> sites = dao.list();
			shapeFileGood = sites.size() > 0;
		} catch (Throwable t) {
			logger.warning("Throwable: " + t.getMessage());
			shapeFileGood = false;
		}
		if (!shapeFileGood) {
			try {
				Importer.importFromJson();
			} catch (Throwable t) {
				logger.severe("Throwable: " + t.getMessage());
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("contextDestroyed");
		Set<Thread> threads = Thread.getAllStackTraces().keySet();
		for (Thread thread : threads) {
			logger.info("Thread: " + thread.getId() + ", " + thread.getName() + ", " + thread.getState());
		}
		try {
			DeferredAuthorityFactory.exit();
		} catch (Throwable t) {
			logger.severe("Throwable: " + t.getMessage());
		}
		if (WeakCollectionCleaner.DEFAULT != null) {
			try {
				WeakCollectionCleaner.DEFAULT.exit();
			} catch (Throwable t) {
				logger.severe("Throwable: " + t.getMessage());
			}
		}
		System.exit(0);
	}
}
