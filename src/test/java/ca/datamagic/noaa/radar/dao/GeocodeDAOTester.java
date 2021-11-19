package ca.datamagic.noaa.radar.dao;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import ca.datamagic.noaa.radar.dto.GeocodeDTO;
import ca.datamagic.util.IOUtils;

public class GeocodeDAOTester {
	private static final Logger logger = LogManager.getLogger(GeocodeDAOTester.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		InputStream secureStream = null;
		try {
			secureStream = new FileInputStream("src/test/resources/secure.properties");
			Properties secureProperties = new Properties();
			secureProperties.load(secureStream);
			GeocodeDAO.setAPIKey(secureProperties.getProperty("google_maps_api_key"));
		} finally {
			if (secureStream != null) {
				IOUtils.closeQuietly(secureStream);
			}
		}
	}

	@Test
	public void test1() throws Exception {
		GeocodeDAO dao = new GeocodeDAO();
		GeocodeDTO geocode = dao.geocode("College Park, MD");
		Assert.assertNotNull(geocode);
		Gson gson = new Gson();
		logger.debug(gson.toJson(geocode));
	}

}
