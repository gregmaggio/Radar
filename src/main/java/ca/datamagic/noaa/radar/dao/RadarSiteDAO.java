/**
 * 
 */
package ca.datamagic.noaa.radar.dao;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import ca.datamagic.noaa.radar.dto.RadarSiteDTO;
import ca.datamagic.noaa.radar.image.ImageConverter;
import ca.datamagic.util.DistanceUtils;
import ca.datamagic.util.IOUtils;

/**
 * @author Greg
 *
 */
public class RadarSiteDAO {
	private static final Logger logger = Logger.getLogger(RadarSiteDAO.class.getName());
	private static final String RADAR_DATA_URL = "https://mrms.ncep.noaa.gov/data";
	private static RadarSiteDTO[] sites = null;
	
	static {
		try {
			InputStream inputStream = RadarSiteDAO.class.getClassLoader().getResourceAsStream("radar.json");
			Gson gson = new Gson();			
			sites = (RadarSiteDTO[])gson.fromJson(new InputStreamReader(inputStream), RadarSiteDTO[].class);
			inputStream.close();
		} catch (IOException ex) {
			logger.severe("IOException: " + ex.getMessage());
		}
	}
	
	public List<RadarSiteDTO> list() {
		List<RadarSiteDTO> items = new ArrayList<RadarSiteDTO>();
		for (int ii = 0; ii < sites.length; ii++) {
			RadarSiteDTO site = sites[ii];
			if ((site.getSiteInfo() != null) && (site.getLatitude() != null) && (site.getLongitude() != null)) {
				items.add(site);
			}
		}
		return items;
	}
	
	public RadarSiteDTO nearest(double latitude, double longitude, double distance, String units) {
		RadarSiteDTO nearest = null;
        double nearestDistance = Double.NaN;
        distance = DistanceUtils.distanceToMeters(distance, units);
        for (int ii = 0; ii < sites.length; ii++) {
        	RadarSiteDTO site = sites[ii];
        	if ((site.getSiteInfo() != null) && (site.getLatitude() != null) && (site.getLongitude() != null)) {
	            double distanceToStation = DistanceUtils.computeDistance(latitude, longitude, site.getLatitude(), site.getLongitude());
	            if (distanceToStation <= distance) {
	                if (nearest == null) {
	                    nearest = site;
	                    nearestDistance = distanceToStation;
	                } else if (nearestDistance > distanceToStation) {
	                    nearest = site;
	                    nearestDistance = distanceToStation;
	                }
	            }
        	}
        }
        return nearest;
	}
	
	public RadarSiteDTO read(String icao) {
		for (int ii = 0; ii < sites.length; ii++) {
			RadarSiteDTO site = sites[ii];
			if (site.getICAO().compareToIgnoreCase(icao) == 0) {
				return site;
			}
		}
		return null;
	}
	
	public List<String> listUrls(String icao) throws IOException {
		List<String> urls = new ArrayList<String>();
		String baseUrl = MessageFormat.format("{0}/RIDGEII/L3/{1}/CREF/", RADAR_DATA_URL, icao);
		Document document = Jsoup.parse(new URL(baseUrl), 10000);
		Elements anchors = document.getElementsByTag("a");
		if (anchors != null) {
			for (int ii = 0; ii < anchors.size(); ii++) {
				Element anchor = anchors.get(ii);
				String href = anchor.attr("href");
				if ((href != null) && (href.length() > 0)) {
					if (href.endsWith(".tif.gz")) {
						urls.add(baseUrl + href);
					}
				}
			}
		}
		return urls;
	}
	
	public byte[] readImageBytes(String urlSpec) throws IOException, ImageReadException {
		InputStream inputStream = null;
		GZIPInputStream gzipInputStream = null;
		ByteArrayOutputStream outputStream = null;
		try {
			int index = urlSpec.lastIndexOf("/");
			if (index < 0) {
				return null;
			}
			String gzFile = urlSpec.substring(index + 1);
			logger.info("gzFile: " + gzFile);
			String tifFileName = gzFile.replace(".gz", "");
			logger.info("tifFileName: " + tifFileName);
			
			inputStream = IOUtils.getRequestStream(urlSpec);
			gzipInputStream = new GZIPInputStream(inputStream);
			byte[] inputBytes = IOUtils.readEntireByteArray(gzipInputStream);
			gzipInputStream.close();
			gzipInputStream = null;
			inputStream.close();
			inputStream = null;
			
			TiffImageParser parser = new TiffImageParser();
			List<BufferedImage> images = parser.getAllBufferedImages(inputBytes);
			BufferedImage image = images.get(0);
			BufferedImage transparent = ImageConverter.convertToTransparent(image);
			outputStream = new ByteArrayOutputStream();
			ImageIO.write(transparent, "png", outputStream);
			return outputStream.toByteArray();
		} finally {			
			if (gzipInputStream != null) {
				IOUtils.closeQuietly(gzipInputStream);
			}
			if (inputStream != null) {
				IOUtils.closeQuietly(inputStream);
			}
			if (outputStream != null) {
				IOUtils.closeQuietly(outputStream);
			}
		}
	}
	
	public String readICAOFromUrl(String urlSpec) {
		int index = urlSpec.lastIndexOf("/");
		if (index < 0) {
			return null;
		}
		String gzFile = urlSpec.substring(index + 1);
		logger.info("gzFile: " + gzFile);
		if ((gzFile != null) && (gzFile.length() > 3)) {
			return gzFile.substring(0, 4);
		}
		return null;
	}
}
