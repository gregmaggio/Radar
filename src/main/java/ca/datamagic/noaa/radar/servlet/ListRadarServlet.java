/**
 * 
 */
package ca.datamagic.noaa.radar.servlet;

import java.io.IOError;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import ca.datamagic.noaa.radar.dao.RadarSiteDAO;
import ca.datamagic.noaa.radar.dto.RadarSiteDTO;

/**
 * @author Greg
 *
 */
public class ListRadarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(ListRadarServlet.class);

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			RadarSiteDAO dao = new RadarSiteDAO();
			dao.openShapeFile();
			List<RadarSiteDTO> sites = dao.list();
			String json = (new Gson()).toJson(sites);
			response.setContentType("application/json");
			response.getWriter().println(json);
		} catch (Throwable t) {
			logger.error("Exception", t);
			throw new IOError(t);
		}
	}
}
