package ca.datamagic.noaa.radar.dao;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import ca.datamagic.noaa.radar.dto.GeocodeDTO;
import ca.datamagic.noaa.radar.dto.GeocodeResultDTO;
import ca.datamagic.noaa.radar.dto.GeometryDTO;
import ca.datamagic.noaa.radar.dto.LocationDTO;
import ca.datamagic.noaa.radar.dto.RadarSiteDTO;
import ca.datamagic.util.IOUtils;

public class RadarSiteDAOTester {
	private static final Logger logger = LogManager.getLogger(RadarSiteDAOTester.class);
	
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
	public void importAll() throws Exception {
		RadarSiteDAO radarSiteDAO = new RadarSiteDAO();
		GeocodeDAO geocodeDAO = new GeocodeDAO();
		RadarSiteDTO[] sites = radarSiteDAO.loadFromWeb();
		for (RadarSiteDTO site : sites) {
			String radLocation = site.getRDALocation();
			if ((radLocation != null) && (radLocation.length() > 0)) {
				GeocodeDTO geocode = geocodeDAO.geocode(radLocation);
				if (geocode != null) {
					GeocodeResultDTO[] geocodeResults = geocode.getResults();
					if ((geocodeResults != null) && (geocodeResults.length > 0)) {
						for (GeocodeResultDTO geocodeResult : geocodeResults) {
							GeometryDTO geometry = geocodeResult.getGeometry();
							if (geometry != null) {
								LocationDTO location = geometry.getLocation();
								if (location != null) {
									site.setLatitude(location.getLatitude());
									site.setLongitude(location.getLongitude());
									break;
								}
							}
						}
					}
				}
			}
		}
		radarSiteDAO.createShapeFile();
		radarSiteDAO.add(sites);		
	}

	@Test
	public void exportAll() throws Exception {
		RadarSiteDAO radarSiteDAO = new RadarSiteDAO();
		radarSiteDAO.openShapeFile();
		List<RadarSiteDTO> sites = radarSiteDAO.list();
		Gson gson = new Gson();
		String json = gson.toJson(sites);
		IOUtils.writeEntireByteArray(json.getBytes(), MessageFormat.format("{0}/radar.json", RadarSiteDAO.getDataPath()));
	}
	
	@Test
	public void test1() throws Exception {
		double latitude = 32.53300575745244;
	    double longitude = -117.58666254580021;
	    double distance = 100;
	    String units = "statute miles";
		RadarSiteDAO dao = new RadarSiteDAO();
		dao.openShapeFile();
		RadarSiteDTO site = dao.nearest(latitude, longitude, distance, units);
		Gson gson = new Gson();
		logger.info(gson.toJson(site));
		List<String> urls = dao.listUrls(site.getICAO());
		logger.info(gson.toJson(urls));
	}
	
	@Test
	public void test2() throws Exception {
		RadarSiteDAO dao = new RadarSiteDAO();
		dao.openShapeFile();
		List<RadarSiteDTO> sites = dao.list();
		Gson gson = new Gson();
		logger.info(gson.toJson(sites));
	}
	
	//32.53300575745244/-117.58666254580021
	
}
