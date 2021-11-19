/**
 * 
 */
package ca.datamagic.noaa.radar;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import com.google.gson.Gson;

import ca.datamagic.noaa.radar.dao.RadarSiteDAO;
import ca.datamagic.noaa.radar.dto.RadarSiteDTO;
import ca.datamagic.util.IOUtils;

/**
 * @author Greg
 *
 */
public class Exporter {
	public static void main(String[] args) {
		try {
			File securePropertiesFile = new File("secure.properties");
			if (!securePropertiesFile.exists()) {
				securePropertiesFile = new File("src/main/resources/secure.properties");
			}
			if (!securePropertiesFile.exists()) {
				System.err.println("secure.properties not found!");
				return;
			}
			String dataPath = securePropertiesFile.getParent();
			System.out.println("dataPath: " + dataPath);
			RadarSiteDAO.setDataPath(dataPath);
			RadarSiteDAO radarSiteDAO = new RadarSiteDAO();
			radarSiteDAO.openShapeFile();
			List<RadarSiteDTO> sites = radarSiteDAO.list();
			Gson gson = new Gson();
			String json = gson.toJson(sites);
			IOUtils.writeEntireByteArray(json.getBytes(), MessageFormat.format("{0}/radar.json", RadarSiteDAO.getDataPath()));
		} catch (Throwable t) {
			System.out.println("Throwable: " + t.getMessage());
			t.printStackTrace();
		}
	}
}
