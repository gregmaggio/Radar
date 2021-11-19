/**
 * 
 */
package ca.datamagic.noaa.radar.servlet;

import java.io.IOError;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import ca.datamagic.noaa.radar.dao.RadarSiteDAO;
import ca.datamagic.noaa.radar.dto.RadarImageDTO;
import ca.datamagic.util.IOUtils;

/**
 * @author Greg
 *
 */
public class RadarImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(RadarImageServlet.class);
	private static final Pattern metaDataPattern = Pattern.compile("/metadata", Pattern.CASE_INSENSITIVE);
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String pathInfo = request.getPathInfo();
			logger.debug("pathInfo: " + pathInfo);
			String urlSpec = IOUtils.readEntireStream(request.getInputStream());
			logger.debug("urlSpec: " + urlSpec);
			if ((pathInfo != null) && (pathInfo.length() > 0)) {
				Matcher metaDataMatcher = metaDataPattern.matcher(pathInfo);
				if (metaDataMatcher.find()) {
					logger.debug("metaData");				
					RadarSiteDAO dao = new RadarSiteDAO();
					RadarImageDTO radarImage = dao.readImageMetaData(urlSpec);
					String json = (new Gson()).toJson(radarImage);
					response.setContentType("application/json");
					response.getWriter().println(json);
					return;
				}
			}
			if ((pathInfo == null) || (pathInfo.length() < 1)) {
				logger.debug("image");				
				RadarSiteDAO dao = new RadarSiteDAO();
				byte[] imageBytes = dao.readImageBytes(urlSpec);
				response.setContentType("image/png");
				response.getOutputStream().write(imageBytes);
				return;
			}
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);			
		} catch (Throwable t) {
			logger.error("Exception", t);
			throw new IOError(t);
		}
	}
}
