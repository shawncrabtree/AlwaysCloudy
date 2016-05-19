package com.example.alwayssunny;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class AlwaysSunnyCloudServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		String latstr = (String) req.getParameter("lat");
		String lngstr = (String) req.getParameter("lng");
		String sunstr = (String) req.getParameter("sun");
		String jsonString = "Must supply lat and lng get args";
		if(latstr != null && lngstr != null && latstr.length() > 0 && lngstr.length() > 0){
			double lat = Double.valueOf(latstr);
			double lng = Double.valueOf(lngstr);
			boolean sun = (sunstr != "false");
			PersistenceManager pm = PMF.getPMF().getPersistenceManager();
			List<WeatherStation> sunnyStations = WeatherStation.loadSunnyNear(pm, lat, lng, sun);
			jsonString = formatAsJson(sunnyStations);
		}
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
