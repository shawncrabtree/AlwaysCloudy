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

import com.google.appengine.api.datastore.GeoPt;
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
		GeoPt center = new GeoPt(0,0);
		GeoPt left = new GeoPt(0,-180);
		GeoPt right = new GeoPt(0,180);
		GeoPt bot = new GeoPt(-90,0);
		GeoPt top = new GeoPt(90,0);
		GeoPt topleft = new GeoPt(90,-180);
		GeoPt botright = new GeoPt(-90,180);
		//top left to bottom right
		Pair<GeoPt, GeoPt> box1 = new Pair<GeoPt, GeoPt>(topleft, center);
		Pair<GeoPt, GeoPt> box2 = new Pair<GeoPt, GeoPt>(top, right);
		Pair<GeoPt, GeoPt> box3 = new Pair<GeoPt, GeoPt>(left, bot);
		Pair<GeoPt, GeoPt> box4 = new Pair<GeoPt, GeoPt>(center, botright);
		
		ArrayList<WeatherStation> stations = new ArrayList<WeatherStation>();
		stations.addAll(searchWeatherStations(box1));
		stations.addAll(searchWeatherStations(box2));
		stations.addAll(searchWeatherStations(box3));
		stations.addAll(searchWeatherStations(box4));
		return stations;
	}
	
	public ArrayList<WeatherStation> searchWeatherStations(Pair<GeoPt, GeoPt> box){
		JSONObject reader;
        JSONArray jArray = new JSONArray();
        GeoPt tl = box.getElement0();
        GeoPt br = box.getElement1();
        String jsonString = getWeatherStringFromURL(tl.getLongitude(), tl.getLatitude(), br.getLongitude(), br.getLatitude());
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
	
	public String getWeatherStringFromURL(float tllon, float tllat, float brlon, float brlat){
        URL url;
        try {
        	//lon of the top left point, 
        	//lat of the top left point, 
        	//lon of the bottom right point,
        	//lat of the bottom right point, 
            url = new URL("http://api.openweathermap.org/data/2.5/box/city?bbox="+
            					String.valueOf(tllon)+","+
            					String.valueOf(tllat)+","+
            					String.valueOf(brlon)+","+
            					String.valueOf(brlat)+",100000&cluster=no&units=imperial&appid=2ab91d37d2983284cd0e8a970e078544");
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
