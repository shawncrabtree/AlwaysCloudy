package com.example.alwayssunny;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@PersistenceCapable
public class WeatherStation {
	@PrimaryKey
	private Long Id;

	@Persistent
	private String Name;

	@Persistent
	private Double Latitude;
	
	@Persistent
	private Double Longitude;
	
	@Persistent
	private Boolean IsSunny;
	
	@Persistent
	private Date UpdatedDate;

    public WeatherStation() {}

    public WeatherStation(Long Id, String name, Double lat, Double lng, Boolean issun) {
        this.Id = Id;
    	this.Name = name;
        this.Latitude = lat;
        this.Longitude = lng;
        this.IsSunny = issun;
        this.UpdatedDate = new Date();
    }

    public String getStationName() { return this.Name; }

    public Double getLat() { return this.Latitude; }

    public Double getLng() { return this.Longitude; }
    
    public Boolean isSunny() { return this.IsSunny; }
    
    public Date UpdatedDate() { return this.UpdatedDate; }
    
	@SuppressWarnings("unchecked")
	public static List<WeatherStation> loadSunny(PersistenceManager pm) {
		Query query = pm.newQuery(WeatherStation.class, "IsSunny == :true");
		List<WeatherStation> rv = (List<WeatherStation>) query.execute(true);
		rv.size(); // forces all records to load into memory
		query.closeAll();
		return rv;
	}
	
	public String formatAsJson() {
		HashMap<String, String> obj = new HashMap<String, String>();
		obj.put("id", Long.toString(Id));
		obj.put("name", Name);
		obj.put("lat", Double.toString(Latitude));
		obj.put("lng", Double.toString(Longitude));
		obj.put("sunny", Boolean.toString(IsSunny));
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		return gson.toJson(obj);
	}
    
    

}
