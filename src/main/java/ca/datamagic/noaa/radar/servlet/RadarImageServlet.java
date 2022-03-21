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
import ca.datamagic.util.IOUtils;

/**
 * @author Greg
 *
 */
public class RadarImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(RadarImageServlet.class.getName());
	private static final Pattern metaDataPattern = Pattern.compile("/metadata", Pattern.CASE_INSENSITIVE);
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String pathInfo = request.getPathInfo();
			logger.info("pathInfo: " + pathInfo);
			String urlSpec = IOUtils.readEntireStream(request.getInputStream());
			logger.info("urlSpec: " + urlSpec);
			if ((pathInfo != null) && (pathInfo.length() > 0)) {
				Matcher metaDataMatcher = metaDataPattern.matcher(pathInfo);
				if (metaDataMatcher.find()) {
					logger.info("metaData");				
					RadarSiteDAO dao = new RadarSiteDAO();
					String icao = dao.readICAOFromUrl(urlSpec);
					if ((icao != null) && (icao.length() > 0)) {
						RadarSiteDTO site = dao.read(icao);
						if ((site != null) && (site.getSiteInfo() != null)) {
							String json = (new Gson()).toJson(site.getSiteInfo());
							response.setContentType("application/json");
							response.getWriter().println(json);
						}
					}
					return;
				}
			}
			if ((pathInfo == null) || (pathInfo.length() < 1)) {
				logger.info("image");				
				RadarSiteDAO dao = new RadarSiteDAO();
				byte[] imageBytes = dao.readImageBytes(urlSpec);
				response.setContentType("image/png");
				if (imageBytes != null) {
					response.getOutputStream().write(imageBytes);
				}
				return;
			}
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);			
		} catch (Throwable t) {
			logger.severe("Throwable: " + t.getMessage());
			throw new IOError(t);
		}
	}
}
