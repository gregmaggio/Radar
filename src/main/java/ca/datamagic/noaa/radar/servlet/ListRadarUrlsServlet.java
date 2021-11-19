/**
 * 
 */
package ca.datamagic.noaa.radar.servlet;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import ca.datamagic.noaa.radar.dao.RadarSiteDAO;

/**
 * @author Greg
 *
 */
public class ListRadarUrlsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(ListRadarUrlsServlet.class);
	private static final Pattern listPattern = Pattern.compile("/(?<identifier>\\w+)", Pattern.CASE_INSENSITIVE);
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String pathInfo = request.getPathInfo();
			logger.debug("pathInfo: " + pathInfo);
			Matcher listMatcher = listPattern.matcher(pathInfo);
			if (listMatcher.find()) {
				logger.debug("list");
				String identifier = listMatcher.group("identifier");
				logger.debug("identifier: " + identifier);
				RadarSiteDAO dao = new RadarSiteDAO();
				dao.openShapeFile();
				List<String> urls = dao.listUrls(identifier);
				String json = (new Gson()).toJson(urls);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);			
		} catch (Throwable t) {
			logger.error("Exception", t);
			throw new IOError(t);
		}
	}
}
