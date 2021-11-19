/**
 * 
 */
package ca.datamagic.noaa.radar.dao;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.gson.Gson;

import ca.datamagic.noaa.radar.dto.RadarImageDTO;
import ca.datamagic.noaa.radar.dto.RadarSiteDTO;
import ca.datamagic.util.IOUtils;

/**
 * @author Greg
 *
 */
public class RadarSiteDAO {
	private static final Logger logger = LogManager.getLogger(RadarSiteDAO.class);
	private static final String RADAR_SITE_URL = "https://www.roc.noaa.gov/WSR88D/Program/SiteID/Network_Sites_iframe.asp";	
	private static final String RADAR_DATA_URL = "https://mrms.ncep.noaa.gov/data";
	private static String dataPath = "C:/Dev/Applications/Radar/src/main/resources";	
	private String typeName = null; 
	private GeometryFactory geometryFactory = null;
	private SimpleFeatureBuilder featureBuilder = null;
	private SimpleFeatureSource featureSource = null;
	
	public static String getDataPath() {
		return dataPath;
	}
	
	public static void setDataPath(String newVal) {
		dataPath = newVal;
	}
	
	private static InputStream getRequestStream(String urlSpec) throws IOException {
		URL url = new URL(urlSpec);
		HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(2000);
        connection.setRequestProperty("accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
        connection.setRequestProperty("accept-encoding", "gzip, deflate, br");
        connection.setRequestProperty("accept-language", "en-US,en;q=0.9");
        connection.setRequestProperty("sec-ch-ua", "\"Google Chrome\";v=\"89\", \"Chromium\";v=\"89\", \";Not A Brand\";v=\"99\"");
        connection.setRequestProperty("sec-ch-ua-mobile", "?0");
        connection.setRequestProperty("sec-fetch-dest", "image");
        connection.setRequestProperty("sec-fetch-mode", "no-cors");
        connection.setRequestProperty("sec-fetch-site", "cross-site");
        connection.setRequestProperty("User-Agent", "(datamagic.ca,gregorymaggio@gmail.com)");
        connection.connect();
        return connection.getInputStream();
	}
	
	public void createShapeFile() throws IOException, SchemaException {
		String fileName = MessageFormat.format("{0}/radar.shp", getDataPath());
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
		String dbfFileName = MessageFormat.format("{0}/radar.dbf", getDataPath());
		File dbfFile = new File(dbfFileName);
		if (dbfFile.exists()) {
			dbfFile.delete();
		}
		String fixFileName = MessageFormat.format("{0}/radar.fix", getDataPath());
		File fixFile = new File(fixFileName);
		if (fixFile.exists()) {
			fixFile.delete();
		}
		String prjFileName = MessageFormat.format("{0}/radar.prj", getDataPath());
		File prjFile = new File(prjFileName);
		if (prjFile.exists()) {
			prjFile.delete();
		}
		String shxFileName = MessageFormat.format("{0}/radar.shx", getDataPath());
		File shxFile = new File(shxFileName);
		if (shxFile.exists()) {
			shxFile.delete();
		}
		SimpleFeatureType featureType = DataUtilities.createType("radar",
                "location:Point:srid=4326," + // <- the geometry attribute: Point type
                        "NEXRAD:String," + // <- a String attribute
                        "ICAO:String," + // <- a String attribute
                        "RDA:String," + // <- a String attribute
                        "RESP_WFO:String," + // <- a String attribute
                        "WFO:String," + // <- a String attribute
                        "EQUIP:String," + // <- a String attribute
                        "LAT:Double," + // a number attribute
                        "LON:Double" // a number attribute
        );
		Map<String, Object> connect = new HashMap<String, Object>();
		connect.put("url", file.toURI().toURL());
		connect.put("create spatial index", Boolean.TRUE);
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		ShapefileDataStore dataStore = (ShapefileDataStore)dataStoreFactory.createDataStore(connect);
		dataStore.createSchema(featureType);
		
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("radar");
		builder.setCRS(DefaultGeographicCRS.WGS84);
		builder.add("the_geom", Point.class);
		builder.add("NEXRAD", String.class);
		builder.add("ICAO", String.class);
		builder.add("RDA", String.class);
		builder.add("RESP_WFO", String.class);
		builder.add("WFO", String.class);
		builder.add("EQUIP", String.class);
		builder.add("LAT", Double.class);
		builder.add("LON", Double.class);
		SimpleFeatureType point = builder.buildFeatureType();

		this.typeName = featureType.getTypeName();
		this.geometryFactory = JTSFactoryFinder.getGeometryFactory();
		this.featureBuilder = new SimpleFeatureBuilder(point);
		this.featureSource = dataStore.getFeatureSource(featureType.getTypeName());
	}
	
	public void openShapeFile() throws IOException {
		String fileName = MessageFormat.format("{0}/radar.shp", getDataPath());
		Map<String, Object> connect = new HashMap<String, Object>();
		connect.put("url", "file://" + fileName);
		DataStore dataStore = DataStoreFinder.getDataStore(connect);
		String[] typeNames = dataStore.getTypeNames();			
		String typeName = typeNames[0];
		
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("radar");
		builder.setCRS(DefaultGeographicCRS.WGS84);
		builder.add("the_geom", Point.class);
		builder.add("NEXRAD", String.class);
		builder.add("ICAO", String.class);
		builder.add("RDA", String.class);
		builder.add("RESP_WFO", String.class);
		builder.add("WFO", String.class);
		builder.add("EQUIP", String.class);
		builder.add("LAT", Double.class);
		builder.add("LON", Double.class);		
		SimpleFeatureType point = builder.buildFeatureType();
		
		SimpleFeatureType featureType = dataStore.getSchema(typeName);
		SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(point);
		this.typeName = featureType.getTypeName();
		this.geometryFactory = geometryFactory;
		this.featureBuilder = featureBuilder;
		this.featureSource = featureSource;
	}
	
	public void add(RadarSiteDTO[] sites) throws IOException {		
		DefaultFeatureCollection collection = new DefaultFeatureCollection();
		for (RadarSiteDTO site : sites) {
			Double longitude = site.getLongitude();
			Double latitude = site.getLatitude();
			if ((longitude != null) && (latitude != null)) {
				Point point = this.geometryFactory.createPoint(new Coordinate(longitude, latitude));
				this.featureBuilder.reset();
				this.featureBuilder.set("the_geom", point);
				this.featureBuilder.set("NEXRAD", site.getNexradSystem());
				this.featureBuilder.set("ICAO", site.getICAO());
				this.featureBuilder.set("RDA", site.getRDALocation());
				this.featureBuilder.set("RESP_WFO", site.getResponsibleWFO());
				this.featureBuilder.set("WFO", site.getWFO());
				this.featureBuilder.set("EQUIP", site.getEquipment());
				this.featureBuilder.set("LAT", latitude);
				this.featureBuilder.set("LON", longitude);
				/*
				this.featureBuilder.add(point);
				this.featureBuilder.add(site.getNexradSystem());
				this.featureBuilder.add(site.getICAO());
				this.featureBuilder.add(site.getRDALocation());
				this.featureBuilder.add(site.getResponsibleWFO());
				this.featureBuilder.add(site.getWFO());
				this.featureBuilder.add(site.getEquipment());
				this.featureBuilder.add(latitude);
				this.featureBuilder.add(longitude);
				*/
				//typeBuilder.add("the_geom", Point.class);
				SimpleFeature feature = this.featureBuilder.buildFeature(null);
				collection.add(feature);
			}
		}
		Transaction transaction = new DefaultTransaction("create");
		SimpleFeatureStore featureStore = (SimpleFeatureStore)this.featureSource;
		featureStore.setTransaction(transaction);
		try {
			featureStore.addFeatures(collection);
			transaction.commit();
		} catch (Throwable t) {
			logger.error("Exception", t);
			transaction.rollback();
		} finally {
			transaction.close();
		}
	}
	
	public void add(RadarSiteDTO site) throws IOException {
		Transaction transaction = new DefaultTransaction("create");		
		Point point = this.geometryFactory.createPoint(new Coordinate(site.getLongitude(), site.getLatitude()));
		this.featureBuilder.add(point);
		this.featureBuilder.add(site.getNexradSystem());
		this.featureBuilder.add(site.getICAO());
		this.featureBuilder.add(site.getRDALocation());
		this.featureBuilder.add(site.getResponsibleWFO());
		this.featureBuilder.add(site.getWFO());
		this.featureBuilder.add(site.getEquipment());
		this.featureBuilder.add(site.getLatitude());
		this.featureBuilder.add(site.getLongitude());
		SimpleFeature feature = this.featureBuilder.buildFeature(null);
		DefaultFeatureCollection collection = new DefaultFeatureCollection();
		collection.add(feature);
		SimpleFeatureStore featureStore = (SimpleFeatureStore)this.featureSource;
		featureStore.setTransaction(transaction);
		try {
			featureStore.addFeatures(collection);
			transaction.commit();
		} catch (Throwable t) {
			logger.error("Exception", t);
			transaction.rollback();
		} finally {
			transaction.close();
		}
	}
	
	private static String getTextValue(Node node) {
		if (node instanceof TextNode) {
			TextNode textNode = (TextNode)node;
			return textNode.text();
		}		
		List<Node> childNodes = node.childNodes();
		if (childNodes.size() > 0) {
			return getTextValue(childNodes.get(0));
		}
		return null;
	}
	
	public RadarSiteDTO[] loadFromWeb() throws IOException {
		List<RadarSiteDTO> sites = new ArrayList<RadarSiteDTO>();
		Document document = Jsoup.parse(new URL(RADAR_SITE_URL), 10000);
		Elements tables = document.getElementsByTag("table");
		Element table1 = tables.get(0);
		Element tBody1 = table1.child(0);
		List<Node> rows1 = tBody1.childNodes();
		for (int ii = 0; ii < rows1.size(); ii++) {
			Node row = rows1.get(ii);
			if (row.nodeName().compareToIgnoreCase("tr") == 0) {
				List<Node> childNodes = row.childNodes();
				List<Node> cells = new ArrayList<Node>();
				for (int jj = 0; jj < childNodes.size(); jj++) {
					if (childNodes.get(jj).nodeName().compareToIgnoreCase("td") == 0) {
						cells.add(childNodes.get(jj));
					}
				}
				if (cells.size() == 6) {
					Node cell1 = cells.get(0);								
					if (cell1.attr("class").compareToIgnoreCase("DATA") == 0) {
						String nexradSystem = getTextValue(cell1);
						String icao = getTextValue(cells.get(1));
						String rdaLocation = getTextValue(cells.get(2));
						String responsibleWFO = getTextValue(cells.get(3));
						String wfo = getTextValue(cells.get(4));
						String equipment = getTextValue(cells.get(5));
						RadarSiteDTO site = new RadarSiteDTO();
						site.setNexradSystem(nexradSystem);
						site.setICAO(icao);
						site.setRDALocation(rdaLocation);
						site.setResponsibleWFO(responsibleWFO);
						site.setWFO(wfo);
						site.setEquipment(equipment);
						sites.add(site);
					}
				}
			}
		}
		
		Element table2 = tables.get(1);
		Element tBody2 = table2.child(0);
		List<Node> rows2 = tBody2.childNodes();
		for (int ii = 0; ii < rows2.size(); ii++) {
			Node row = rows2.get(ii);
			if (row.nodeName().compareToIgnoreCase("tr") == 0) {
				List<Node> childNodes = row.childNodes();
				List<Node> cells = new ArrayList<Node>();
				for (int jj = 0; jj < childNodes.size(); jj++) {
					if (childNodes.get(jj).nodeName().compareToIgnoreCase("td") == 0) {
						cells.add(childNodes.get(jj));
					}
				}
				if (cells.size() == 5) {
					Node cell1 = cells.get(0);								
					if (cell1.attr("class").compareToIgnoreCase("DATA") == 0) {
						String nexradSystem = getTextValue(cell1);
						String icao = getTextValue(cells.get(1));
						String rdaLocation = getTextValue(cells.get(2));
						String responsibleWFO = getTextValue(cells.get(3));
						String wfo = getTextValue(cells.get(4));
						RadarSiteDTO site = new RadarSiteDTO();
						site.setNexradSystem(nexradSystem);
						site.setICAO(icao);
						site.setRDALocation(rdaLocation);
						site.setResponsibleWFO(responsibleWFO);
						site.setWFO(wfo);
						sites.add(site);
					}
				}
			}
		}
		
		Element table3 = tables.get(2);
		Element tBody3 = table3.child(0);
		List<Node> rows3 = tBody3.childNodes();
		for (int ii = 0; ii < rows3.size(); ii++) {
			Node row = rows3.get(ii);
			if (row.nodeName().compareToIgnoreCase("tr") == 0) {
				List<Node> childNodes = row.childNodes();
				List<Node> cells = new ArrayList<Node>();
				for (int jj = 0; jj < childNodes.size(); jj++) {
					if (childNodes.get(jj).nodeName().compareToIgnoreCase("td") == 0) {
						cells.add(childNodes.get(jj));
					}
				}
				if (cells.size() == 5) {
					Node cell1 = cells.get(0);								
					if (cell1.attr("class").compareToIgnoreCase("DATA") == 0) {
						String nexradSystem = getTextValue(cell1);
						String icao = getTextValue(cells.get(1));
						String responsibleWFO = getTextValue(cells.get(3));
						String wfo = getTextValue(cells.get(4));
						RadarSiteDTO site = new RadarSiteDTO();
						site.setNexradSystem(nexradSystem);
						site.setICAO(icao);
						site.setResponsibleWFO(responsibleWFO);
						site.setWFO(wfo);
						sites.add(site);
					}
				}
			}
		}
		
		Element table4 = tables.get(3);
		Element tBody4 = table4.child(0);
		List<Node> rows4 = tBody4.childNodes();
		String currWFO = null;
		String currResponsibleWFO = null;
		for (int ii = 0; ii < rows4.size(); ii++) {
			Node row = rows4.get(ii);
			if (row.nodeName().compareToIgnoreCase("tr") == 0) {
				List<Node> childNodes = row.childNodes();
				List<Node> cells = new ArrayList<Node>();
				for (int jj = 0; jj < childNodes.size(); jj++) {
					if (childNodes.get(jj).nodeName().compareToIgnoreCase("td") == 0) {
						cells.add(childNodes.get(jj));
					}
				}
				if (cells.size() > 0) {
					Node cell1 = cells.get(0);	
					if (cell1.attr("class").compareToIgnoreCase("DATA") == 0) {
						if (cells.size() == 5) {
							currWFO = getTextValue(cell1);
							currResponsibleWFO = getTextValue(cells.get(1));
							String icao = getTextValue(cells.get(2));
							String nexradSystem = getTextValue(cells.get(3));
							String rdaLocation = getTextValue(cells.get(4));
							RadarSiteDTO site = new RadarSiteDTO();
							site.setNexradSystem(nexradSystem);
							site.setICAO(icao);
							site.setRDALocation(rdaLocation);
							site.setResponsibleWFO(currResponsibleWFO);
							site.setWFO(currWFO);
							sites.add(site);
						} else if (cells.size() == 4) {
							String icao = getTextValue(cells.get(1));
							String nexradSystem = getTextValue(cells.get(2));
							String rdaLocation = getTextValue(cells.get(3));
							RadarSiteDTO site = new RadarSiteDTO();
							site.setNexradSystem(nexradSystem);
							site.setICAO(icao);
							site.setRDALocation(rdaLocation);
							site.setResponsibleWFO(currResponsibleWFO);
							site.setWFO(currWFO);
							sites.add(site);
						}
					}
				}
			}
		}
		RadarSiteDTO[] array = new RadarSiteDTO[sites.size()];
		return sites.toArray(array);
	}
	
	public RadarSiteDTO[] loadFromJson() throws IOException {
		byte[] bytes = IOUtils.readEntireByteArray(MessageFormat.format("{0}/radar.json", getDataPath()));
		Gson gson = new Gson();
		return (RadarSiteDTO[])gson.fromJson(new String(bytes), RadarSiteDTO[].class);
	}
	
	public RadarSiteDTO nearest(double latitude, double longitude, double distance, String units) throws CQLException, IOException {
		String filter = MessageFormat.format("DWITHIN(the_geom, POINT({0} {1}), {2}, {3})", Double.toString(longitude), Double.toString(latitude), Double.toString(distance), units);
		logger.debug("filter: " + filter);
		Query query = new Query(typeName, CQL.toFilter(filter));
		SimpleFeatureCollection collection = this.featureSource.getFeatures(query);
		SimpleFeatureIterator iterator = null;
		GeodeticCalculator calculator = new GeodeticCalculator();
		calculator.setStartingGeographicPoint(longitude, latitude);
		SimpleFeature nearestFeature = null;
		double minDistanceToPoint = 0.0;
		try {
			iterator = collection.features();
			while (iterator.hasNext()) {		
				SimpleFeature feature = iterator.next();
				Point point = (Point)feature.getAttribute(0);
				calculator.setDestinationGeographicPoint(point.getX(), point.getY());
				double distanceToPoint = calculator.getOrthodromicDistance();
				if (nearestFeature == null) {
					nearestFeature = feature;
					minDistanceToPoint = distanceToPoint;
				} else {
					if (distanceToPoint < minDistanceToPoint) {
						nearestFeature = feature;
						minDistanceToPoint = distanceToPoint;
					}
				}
			}
			if (nearestFeature != null) {
				return new RadarSiteDTO(nearestFeature);
			}
			return null;
		} finally {
			if (iterator != null) {
				iterator.close();
			}
		}
	}
	
	public RadarSiteDTO read(String icao) throws IOException, CQLException {
		String filter = MessageFormat.format("ICAO = {0}", "'" + icao.toUpperCase() + "'");
		logger.debug("filter: " + filter);
		Query query = new Query(typeName, CQL.toFilter(filter));
		SimpleFeatureCollection collection = this.featureSource.getFeatures(query);
		SimpleFeatureIterator iterator = null;
		try {
			iterator = collection.features();
			if (iterator.hasNext()) {
				return new RadarSiteDTO(iterator.next());
			}
			return null;
		} finally {
			if (iterator != null) {
				iterator.close();
			}
		}
	}
	
	public List<RadarSiteDTO> list() throws IOException {		
		SimpleFeatureCollection collection = this.featureSource.getFeatures();
		SimpleFeatureIterator iterator = null;
		try {
			iterator = collection.features();
			List<RadarSiteDTO> sites = new ArrayList<RadarSiteDTO>();
			while (iterator.hasNext()) {
				sites.add(new RadarSiteDTO(iterator.next()));
			}
			return sites;
		} finally {
			if (iterator != null) {
				iterator.close();
			}
		}		
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
	
	public RadarImageDTO readImageMetaData(String urlSpec) throws IOException {
		InputStream inputStream = null;
		GZIPInputStream gzipInputStream = null;
		File tifFile = null;
		try {
			String tempPath = MessageFormat.format("{0}/temp", dataPath);
			logger.debug("tempPath: " + tempPath);
			String gzFile = urlSpec.substring(urlSpec.lastIndexOf("/") + 1);
			logger.debug("gzFile: " + gzFile);
			String tifFileName = gzFile.replace(".gz", "");
			logger.debug("tifFileName: " + tifFileName);
			tifFile = new File(tifFileName);
			inputStream = getRequestStream(urlSpec);
			gzipInputStream = new GZIPInputStream(inputStream);
			byte[] inputBytes = IOUtils.readEntireByteArray(gzipInputStream);
			gzipInputStream.close();
			gzipInputStream = null;
			inputStream.close();
			inputStream = null;
			IOUtils.writeEntireByteArray(inputBytes, tifFile.getAbsolutePath());
			AbstractGridFormat format = new GeoTiffFormat();
			GeoTiffReader reader = (GeoTiffReader)format.getReader(tifFile);
			CoordinateReferenceSystem crs = reader.getCoordinateReferenceSystem();
			//System.out.println("CRS: " + crs.toWKT());
			GridCoverage2D coverage = reader.read(null);
			Envelope env = coverage.getEnvelope();
			double[] lowerCorner = env.getLowerCorner().getCoordinate();
			double[] upperCorner = env.getUpperCorner().getCoordinate();
	        RenderedImage image = coverage.getRenderedImage();
	        RadarImageDTO dto = new RadarImageDTO();
	        dto.setCRS(crs.toWKT());
	        dto.setLowerCorner(lowerCorner);
	        dto.setUpperCorner(upperCorner);
	        dto.setWidth(image.getWidth());
	        dto.setHeight(image.getHeight());
			return dto;
		} finally {			
			if (gzipInputStream != null) {
				IOUtils.closeQuietly(gzipInputStream);
			}
			if (inputStream != null) {
				IOUtils.closeQuietly(inputStream);
			}
			if (tifFile != null) {
				if (tifFile.exists()) {
					tifFile.delete();
				}
			}
		}
	}
	
	public byte[] readImageBytes(String urlSpec) throws IOException {
		InputStream inputStream = null;
		GZIPInputStream gzipInputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		File tifFile = null;
		try {
			String tempPath = MessageFormat.format("{0}/temp", dataPath);
			logger.debug("tempPath: " + tempPath);
			String gzFile = urlSpec.substring(urlSpec.lastIndexOf("/") + 1);
			logger.debug("gzFile: " + gzFile);
			String tifFileName = gzFile.replace(".gz", "");
			logger.debug("tifFileName: " + tifFileName);
			tifFile = new File(tifFileName);
			inputStream = getRequestStream(urlSpec);
			gzipInputStream = new GZIPInputStream(inputStream);
			byte[] inputBytes = IOUtils.readEntireByteArray(gzipInputStream);
			gzipInputStream.close();
			gzipInputStream = null;
			inputStream.close();
			inputStream = null;
			IOUtils.writeEntireByteArray(inputBytes, tifFile.getAbsolutePath());
			AbstractGridFormat format = new GeoTiffFormat();
			GeoTiffReader reader = (GeoTiffReader)format.getReader(tifFile);
			GridCoverage2D coverage = reader.read(null);
	        RenderedImage image = coverage.getRenderedImage();
	        byteArrayOutputStream = new ByteArrayOutputStream();
	        ImageIO.write(image, "png", byteArrayOutputStream);	        
			return byteArrayOutputStream.toByteArray();
		} finally {			
			if (gzipInputStream != null) {
				IOUtils.closeQuietly(gzipInputStream);
			}
			if (inputStream != null) {
				IOUtils.closeQuietly(inputStream);
			}
			if (byteArrayOutputStream != null) {
				IOUtils.closeQuietly(byteArrayOutputStream);
			}
			if (tifFile != null) {
				if (tifFile.exists()) {
					tifFile.delete();
				}
			}
		}
	}
}
