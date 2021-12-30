/**
 * 
 */
package ca.datamagic.noaa.radar.servlet;

import java.io.IOError;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import ca.datamagic.noaa.radar.dao.RadarSiteDAO;
import ca.datamagic.noaa.radar.dto.RadarSiteDTO;

/**
 * @author Greg
 *
 */
public class ReadRadarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ReadRadarServlet.class.getName());
	private static final Pattern readNearestPattern = Pattern.compile("/(?<latitude>[+-]?([0-9]*[.])?[0-9]+)/(?<longitude>[+-]?([0-9]*[.])?[0-9]+)/nearest", Pattern.CASE_INSENSITIVE);
	private static final Pattern readPattern = Pattern.compile("/(?<identifier>\\w+)", Pattern.CASE_INSENSITIVE);

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String pathInfo = request.getPathInfo();
			logger.info("pathInfo: " + pathInfo);
			Matcher readNearestMatcher = readNearestPattern.matcher(pathInfo);
			if (readNearestMatcher.find()) {
				logger.info("readNearest");
				String latitude = readNearestMatcher.group("latitude");
				logger.info("latitude: " + latitude);
				String longitude = readNearestMatcher.group("longitude");
				logger.info("longitude: " + longitude);
				double doubleLatitude = Double.parseDouble(latitude);
				double doubleLongitude = Double.parseDouble(longitude);
				RadarSiteDAO dao = new RadarSiteDAO();
				dao.openShapeFile();
				RadarSiteDTO site = dao.nearest(doubleLatitude, doubleLongitude, 100, "statute miles");
				String json = (new Gson()).toJson(site);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			
			Matcher readMatcher = readPattern.matcher(pathInfo);
			if (readMatcher.find()) {
				logger.info("read");
				String identifier = readMatcher.group("identifier");
				logger.info("identifier: " + identifier);
				RadarSiteDAO dao = new RadarSiteDAO();
				dao.openShapeFile();
				RadarSiteDTO site = dao.read(identifier);
				String json = (new Gson()).toJson(site);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} catch (Throwable t) {
			logger.severe("Throwable: " + t.getMessage());
			throw new IOError(t);
		}
	}
}
