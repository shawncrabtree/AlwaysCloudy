package com.example.alwayssunny;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.*;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

@SuppressWarnings("serial")
public class AlwaysSunnyCloudServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		String jsonString = "Must supply lat and lng get args";
		String latstr = (String) req.getParameter("lat");
		String lngstr = (String) req.getParameter("lng");
		if(latstr != null && lngstr != null && latstr.length() > 0 && lngstr.length() > 0){
			latstr = SecurityService.decode(latstr);
			lngstr = SecurityService.decode(lngstr);
			final double lat = Double.valueOf(latstr);
			final double lng = Double.valueOf(lngstr);
			
			MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
			@SuppressWarnings("unchecked")
			List<WeatherStation> sunnyStations = (List<WeatherStation>) syncCache.get("sun");
			
			Collections.sort(sunnyStations, new Comparator<WeatherStation>() {
	            @Override
	            public int compare(WeatherStation w1, WeatherStation w2) {
	                double w1dist = distance(w1.getLat(), w1.getLng(), lat, lng);
	                double w2dist = distance(w2.getLat(), w2.getLng(), lat, lng);
	                return w1dist > w2dist ? 1 : -1;
	            }
	        });
			sunnyStations = sunnyStations.subList(0, 3);
			jsonString = formatAsJson(sunnyStations);
			out.println(jsonString);
		}
	}
	
	public String formatAsJson(List<WeatherStation> stations) {
		String jsonString = "[";
		for(WeatherStation s : stations){
			jsonString += s.formatAsJson() + ",";
		}
		if(stations.size() > 0){
			jsonString = jsonString.substring(0, jsonString.length() - 1);
		}
		return jsonString + "]";
	}
	
	public double distance(double lat1, double lng1, double lat2, double lng2) {
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
