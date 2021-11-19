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
		//this.latitude = (Double)feature.getAttribute("LAT");
		//this.longitude = (Double)feature.getAttribute("LON");
		this.latitude = point.getY();
		this.longitude = point.getX();
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
}
