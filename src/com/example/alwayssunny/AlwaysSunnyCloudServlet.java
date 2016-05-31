package com.example.alwayssunny;

import java.io.IOException;
import java.io.PrintWriter;
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
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		@SuppressWarnings("unchecked")
		List<WeatherStation> sunnyStations = (List<WeatherStation>) syncCache.get("sun");
		jsonString = formatAsJson(sunnyStations);
		out.println(jsonString);
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
	
	
}
