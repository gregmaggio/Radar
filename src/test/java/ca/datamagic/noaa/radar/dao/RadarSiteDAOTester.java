package ca.datamagic.noaa.radar.dao;

import java.io.FileOutputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.google.gson.Gson;

import ca.datamagic.noaa.radar.dto.RadarSiteDTO;

public class RadarSiteDAOTester {
	private static final Logger logger = LogManager.getLogger(RadarSiteDAOTester.class);
	
	@Test
	public void test1() throws Exception {
		double latitude = 33.828704833984375;
	    double longitude = -84.51777648925781;
	    double distance = 100;
	    String units = "statute miles";
		RadarSiteDAO dao = new RadarSiteDAO();
		RadarSiteDTO site = dao.nearest(latitude, longitude, distance, units);
		Gson gson = new Gson();
		logger.info(gson.toJson(site));
		List<String> urls = dao.listUrls(site.getICAO());
		logger.info(gson.toJson(urls));
		byte[] imageBytes = dao.readImageBytes(urls.get(urls.size() - 1));
		FileOutputStream output = new FileOutputStream("C:/Temp/Radar.png");
		output.write(imageBytes);
		output.close();
	}
	
	@Test
	public void test2() throws Exception {
		RadarSiteDAO dao = new RadarSiteDAO();
		List<RadarSiteDTO> sites = dao.list();
		Gson gson = new Gson();
		logger.info(gson.toJson(sites));
	}
	
	@Test
	public void test3() throws Exception {
		RadarSiteDAO dao = new RadarSiteDAO();
		List<String> urls = dao.listUrls("KRIW");
		Gson gson = new Gson();
		logger.info(gson.toJson(urls));
	}
}
