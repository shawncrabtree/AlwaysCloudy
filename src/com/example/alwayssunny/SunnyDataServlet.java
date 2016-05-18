package com.example.alwayssunny;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class SunnyDataServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();

		PersistenceManager pm = PMF.getPMF().getPersistenceManager();
		ArrayList<WeatherStation> stations = searchWeatherStations();
		try {
			pm.makePersistentAll(stations);
			
		} catch (IllegalArgumentException iae) {
			out.write(iae.getMessage());
		} finally {
			pm.close();
		}
		out.write("done");
	}
	
	public ArrayList<WeatherStation> searchWeatherStations(){
		ArrayList<ArrayList<String>> boxes = new ArrayList<ArrayList<String>>();
		ArrayList<String> latcoords = new ArrayList<String>() {{
		    add("-90");
		    add("-60");
		    add("-30");
		    add("0");
		    add("30");
		    add("60");
		}};
		ArrayList<String> lngcoords = new ArrayList<String>() {{
		    add("-180");
		    add("-120");
		    add("-60");
		    add("0");
		    add("60");
		    add("120");
		    add("180");
		}};
		ArrayList<WeatherStation> stations = new ArrayList<WeatherStation>();
		for(int i = 0; i < latcoords.size()-1; i++){
			for(int j = 0; j < latcoords.size()-1; j++){
				ArrayList<WeatherStation> s = searchWeatherStations(latcoords.get(i), latcoords.get(i+1), lngcoords.get(j), lngcoords.get(j+1));
				stations.addAll(s);
			}
		}
		return stations;
	}
	
	public ArrayList<WeatherStation> searchWeatherStations(String lat1, String lng1, String lat2, String lng2){
		JSONObject reader;
        JSONArray jArray = new JSONArray();
        String jsonString = getWeatherStringFromURL(lat1, lng1, lat2, lng2);
        try {
            reader = new JSONObject(jsonString);
            jArray = reader.getJSONArray("list");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<WeatherStation> stations = new ArrayList<WeatherStation>();
        for(int i = 0; i < jArray.length(); i++) {
            JSONObject jObj = null;
			try {
				jObj = jArray.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
            WeatherStation station = getWeatherStationFromJSONObject(jObj);
            stations.add(station);
        }

        return stations;
	}
	
	public String getWeatherStringFromURL(String lat1, String lng1, String lat2, String lng2){
        URL url;
        try {
            url = new URL("http://api.openweathermap.org/data/2.5/box/city?bbox="+lat1+","+lng1+","+lat2+","+lng2+",100000&cluster=no&units=imperial&appid=2ab91d37d2983284cd0e8a970e078544");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String jsonString = "";
        try {
        	HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        	conn.setConnectTimeout(60000);  //60 Seconds
        	conn.setReadTimeout(60000);  //60 Seconds
        	BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                jsonString += inputLine;

            in.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        return jsonString;
    }
	
	public WeatherStation getWeatherStationFromJSONObject(JSONObject json) {
        try {
        	Long stationId = json.getLong("id");
            String stationName = json.getString("name");
            JSONObject coord = json.getJSONObject("coord");
            Double stationLat = coord.getDouble("lat");
            Double stationLng = coord.getDouble("lon");
            int cloudLevel = json.getJSONObject("clouds").getInt("all");
            int weatherId = json.getJSONArray("weather").getJSONObject(0).getInt("id");
            Boolean isSunny = cloudLevel == 0 && weatherId == 800;
            WeatherStation station = new WeatherStation(stationId, stationName, stationLat, stationLng, isSunny);
            return station;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
