/**
 * 
 */
package ca.datamagic.noaa.radar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.geotools.feature.SchemaException;

import ca.datamagic.noaa.radar.dao.GeocodeDAO;
import ca.datamagic.noaa.radar.dao.RadarSiteDAO;
import ca.datamagic.noaa.radar.dto.GeocodeDTO;
import ca.datamagic.noaa.radar.dto.GeocodeResultDTO;
import ca.datamagic.noaa.radar.dto.GeometryDTO;
import ca.datamagic.noaa.radar.dto.LocationDTO;
import ca.datamagic.noaa.radar.dto.RadarSiteDTO;
import ca.datamagic.util.IOUtils;

/**
 * @author Greg
 *
 */
public class Importer {
	public static void importFromWeb() throws IOException, SchemaException {
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
	
	public static void importFromJson() throws IOException, SchemaException {
		RadarSiteDAO radarSiteDAO = new RadarSiteDAO();
		RadarSiteDTO[] sites = radarSiteDAO.loadFromJson();		
		radarSiteDAO.createShapeFile();
		radarSiteDAO.add(sites);
	}
	
	public static void main(String[] args) {		
		try {
			boolean importFromWeb = true;
			boolean importFromJson = false;
			for (int ii = 0; ii < args.length; ii++) {
				String arg = args[ii];
				if (arg.compareToIgnoreCase("--importFromWeb") == 0) {
					importFromWeb = true;
				} else if (arg.compareToIgnoreCase("--importFromJson") == 0) {
					importFromJson = true;
				}
			}
			File securePropertiesFile = new File("secure.properties");
			if (!securePropertiesFile.exists()) {
				securePropertiesFile = new File("src/main/resources/secure.properties");
			}
			if (!securePropertiesFile.exists()) {
				System.err.println("secure.properties not found!");
				return;
			}
			
			InputStream secureStream = null;
			try {
				secureStream = new FileInputStream(securePropertiesFile);
				Properties secureProperties = new Properties();
				secureProperties.load(secureStream);
				GeocodeDAO.setAPIKey(secureProperties.getProperty("google_maps_api_key"));
			} finally {
				if (secureStream != null) {
					IOUtils.closeQuietly(secureStream);
				}
			}
			String dataPath = securePropertiesFile.getParent();
			System.out.println("dataPath: " + dataPath);
			RadarSiteDAO.setDataPath(dataPath);
			if (importFromWeb) {
				importFromWeb();
			} else if (importFromJson) {
				importFromJson();
			}			
		} catch (Throwable t) {
			System.out.println("Throwable: " + t.getMessage());
			t.printStackTrace();
		}
	}
}
