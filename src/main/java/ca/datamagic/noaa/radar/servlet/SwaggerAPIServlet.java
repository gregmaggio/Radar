/**
 * 
 */
package ca.datamagic.noaa.radar.servlet;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * @author Greg
 *
 */
public class SwaggerAPIServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SwaggerAPIServlet.class.getName());
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String requestUrl = request.getRequestURL().toString();
			logger.info("requestUrl: " + requestUrl);
			URL url = new URL(requestUrl);
			StringBuffer host = new StringBuffer();
			host.append(url.getHost());
			if ((url.getPort() != -1) && (url.getPort() != 80) && (url.getPort() != 443)) {
				host.append(":");
				host.append(Integer.toString(url.getPort()));
			}
			String basePath = "";
			if (requestUrl.toLowerCase().contains("/radar/")) {
				basePath = "/Radar";
			}
			InputStream input = getServletContext().getResourceAsStream("/WEB-INF/classes/swagger.json");
			String theString = IOUtils.toString(input, "UTF-8").replace("{{host}}", host.toString()).replace("{{basePath}}", basePath);
			input.close();
			response.setContentType("application/json");
			response.getWriter().println(theString);
		} catch (Throwable t) {
			logger.severe("Throwable: " + t.getMessage());
			throw new IOError(t);
		}
	}
}
