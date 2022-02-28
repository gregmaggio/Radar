/**
 * 
 */
package ca.datamagic.noaa.radar.dto;

import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

/**
 * @author Greg
 *
 */
public class RadarSiteDTO {
	private String nexradSystem = null;
	private String icao = null;
	private String rdaLocation = null;
	private String responsibleWFO = null;
	private String wfo = null;
	private String equipment = null;
	private Double latitude = null;
	private Double longitude = null;
	private RadarSiteInfoDTO siteInfo = null;
	
	public RadarSiteDTO() {
		
	}
	
	public RadarSiteDTO(SimpleFeature feature) {
		Point point = (Point)feature.getDefaultGeometry();
		this.nexradSystem = (String)feature.getAttribute("NEXRAD");
		this.icao = (String)feature.getAttribute("ICAO");
		this.rdaLocation = (String)feature.getAttribute("RDA");
		this.responsibleWFO = (String)feature.getAttribute("RESP_WFO");
		this.wfo = (String)feature.getAttribute("WFO");
		this.equipment = (String)feature.getAttribute("EQUIP");
		this.latitude = point.getY();
		this.longitude = point.getX();
		String crs = (String)feature.getAttribute("CRS");
		Double lowerX = (Double)feature.getAttribute("LOWER_X");
		Double lowerY = (Double)feature.getAttribute("LOWER_Y");
		Double upperX = (Double)feature.getAttribute("UPPER_X");
		Double upperY = (Double)feature.getAttribute("UPPER_Y");
		Integer width = (Integer)feature.getAttribute("WIDTH");
		Integer height = (Integer)feature.getAttribute("HEIGHT");
		if ((crs != null) && (lowerX != null) && (lowerY != null) && (upperX != null) && (upperY != null) && (width != null) && (height != null)) {
			RadarSiteInfoDTO siteInfo = new RadarSiteInfoDTO();
			siteInfo.setCrs(crs);
			siteInfo.setLowerCorner(new double[] { lowerX, lowerY });
			siteInfo.setUpperCorner(new double[] { upperX, upperY });
			siteInfo.setHeight(height);
			siteInfo.setWidth(width);
			this.siteInfo = siteInfo;
		}
	}
	
	public String getNexradSystem() {
		return this.nexradSystem;
	}
	
	public void setNexradSystem(String newVal) {
		this.nexradSystem = newVal;
	}
	
	public String getICAO() {
		return this.icao;
	}
	
	public void setICAO(String newVal) {
		this.icao = newVal;
	}
	
	public String getRDALocation() {
		return this.rdaLocation;
	}
	
	public void setRDALocation(String newVal) {
		this.rdaLocation = newVal;
	}
	
	public String getResponsibleWFO() {
		return this.responsibleWFO;
	}
	
	public void setResponsibleWFO(String newVal) {
		this.responsibleWFO = newVal;
	}
	
	public String getWFO() {
		return this.wfo;
	}
	
	public void setWFO(String newVal) {
		this.wfo = newVal;
	}
	
	public String getEquipment() {
		return this.equipment;
	}
	
	public void setEquipment(String newVal) {
		this.equipment = newVal;
	}
	
	public Double getLatitude() {
		return this.latitude;
	}
	
	public void setLatitude(Double newVal) {
		this.latitude = newVal;
	}
	
	public Double getLongitude() {
		return this.longitude;
	}
	
	public void setLongitude(Double newVal) {
		this.longitude = newVal;
	}
	
	public RadarSiteInfoDTO getSiteInfo() {
		return this.siteInfo;
	}
	
	public void setSiteInfo(RadarSiteInfoDTO newVal) {
		this.siteInfo = newVal;
	}
}
