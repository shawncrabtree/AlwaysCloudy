package com.example.alwayssunny;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class WeatherStation implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
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
	public static List<WeatherStation> loadSunnyNear(PersistenceManager pm, final double lat, final double lng, boolean sun) {
		Query query = pm.newQuery(WeatherStation.class, "IsSunny == :sun");
		query.getFetchPlan().setFetchSize(300);
		List<WeatherStation> rv = (List<WeatherStation>) query.execute(sun);
		Collections.sort(rv, new Comparator<WeatherStation>() {
            @Override
            public int compare(WeatherStation w1, WeatherStation w2) {
                double w1dist = distance(w1.Latitude, w1.Longitude, lat, lng);
                double w2dist = distance(w2.Latitude, w2.Longitude, lat, lng);
                return w1dist > w2dist ? 1 : -1;
            }
        });
		
		ArrayList<WeatherStation> retlist = new ArrayList<WeatherStation>();
		if(rv.size() >= 3){
			retlist.add(rv.get(0));
			retlist.add(rv.get(1));
			retlist.add(rv.get(2));
		}
		
		query.closeAll();
		return retlist;
	}
	
	public String formatAsJson() {
		HashMap<String, String> obj = new HashMap<String, String>();
		obj.put("id", Long.toString(Id));
		obj.put("name", Name);
		obj.put("lat", Double.toString(Latitude));
		obj.put("lng", Double.toString(Longitude));
		obj.put("sunny", Boolean.toString(IsSunny));
		obj.put("updateddate", UpdatedDate.toString());
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		return gson.toJson(obj);
	}
	
	private static double distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }
    
    

}
