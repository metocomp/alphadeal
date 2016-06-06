package com.wc.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.wc.bean.AlphaDealUser;
import com.wc.dao.AlphaDealUserDAO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

@Produces("application/json;charset=UTF-8")
//@Consumes(MediaType.MULTIPART_FORM_DATA)
@Path("alphadeal")
public class AlphaDealActions {

	//常量
	public static final String androidVersion="1.0.0";
	public static final String iosVersion="4.0.0";
	private static final long RUN_TIMEOUT = 1000 * 60 * 60 * 10; // 10 hours
	private final String USER_AGENT = "Mozilla/5.0";

	private String item;
	private String upc;
	private String userId;
	private String isTrigger;
	private JSONObject res = new JSONObject();
	private AlphaDealUserDAO uDao = new AlphaDealUserDAO();
	private AlphaDealUser user;
	private AlphaDealUser admin;
	
	private static final Logger logger;
	
	static {
		logger = Logger.getLogger("AlphaDealActions");
		logger.setLevel(Level.ALL);
	}
	
	private class Location {
		private double lat;
		private double lng;
	}

	// Gets the location with the zip code.
	private Location getLocation(String zip) {

		String url = "http://dev.virtualearth.net/REST/v1/Locations/" + zip
				+ "?maxResults=1&key=AmV3iQQoSXvLS4UkW5zDQdzjGBUZlEe6UQOHLHSpyPXFNQTFsXAwno8xPbGCHnc_";
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	
			// optional default is GET
			con.setRequestMethod("GET");
	
			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
	
			int responseCode = con.getResponseCode();
			switch (responseCode) {
		        case 200:
		        case 201:
		            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		            StringBuilder sb = new StringBuilder();
		            String line;
		            while ((line = br.readLine()) != null) {
		                sb.append(line);
		            }
		            br.close();
		            String s = sb.toString().replaceAll("http.?", "")
		            		.replaceAll(".?\\/", "")
		            		.replace('©', ' ');
		            JSONObject json = new JSONObject(s);
		            Location location = new Location();
		            JSONArray resourcesSet = json.getJSONArray("resourceSets");
		            JSONArray resources = resourcesSet.getJSONObject(0).getJSONArray("resources");
		            JSONArray bboxs = resources.getJSONObject(0).getJSONArray("bbox");
		            location.lat = (bboxs.getDouble(0) + bboxs.getDouble(2)) / 2;
		            location.lng = (bboxs.getDouble(1) + bboxs.getDouble(3)) / 2;
		            return location;
		         default:
		        	 System.out.println("failed from location response code:" + responseCode); 
			}
		} catch (Exception e) {
			System.out.println("failed from location:" + e);
		}
		return null;
	}
	
	// Gets the store jsons with the location and item id.
	private JSONObject getStoreData(String item, Location location) throws Exception {
		if (location == null) {
			System.out.println("location is null");
		}

		String url = "https://mobile.walmart.com/m/j?service=Slap&method=getByItemsAndLatLong&p1=[" + item
				+ "]&p2=" + location.lat + "&p3=" + location.lng + "&p4=c4tch4spyder&version=3&e=1";
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		switch (responseCode) {
	        case 200:
	        case 201:
	            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            StringBuilder sb = new StringBuilder();
	            String line;
	            while ((line = br.readLine()) != null) {
	                sb.append(line);
	            }
	            br.close();
	            String s = sb.toString();
	            if (s.startsWith("[")) {
	            	s = sb.toString().substring(1, sb.length() - 1);
	            }
//	            System.out.println("data: " + s);  
	            return new JSONObject(s);
	         default:
	        	break; 
		}
		return null;
	}
	
	// Sends the result back to web app.
	private void sendResult(Map<String, JSONObject> storesMap) throws Exception {
		
		// Reset the bit
		user.setRunning(0);
		uDao.update(user);

		String url="https://script.google.com/macros/s/AKfycbwYLOqsych0STQVsrtKo6-f0vEEZIheGfq4S60yllYtgaVX5KQ/exec";
		URL object=new URL(url);

		HttpURLConnection con = (HttpURLConnection) object.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("POST");

		res.put("item", item);
		res.put("upc", upc);
		res.put("stores", storesMap.values());
		res.put("user", userId);
		res.put("isTrigger", isTrigger);
		System.out.println("item :" + item);  
		System.out.println("upc :" + upc); 
		System.out.println("isTrigger :" + isTrigger); 
		System.out.println("store size:" + storesMap.values().size());  
		OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
		wr.write(res.toString());
		wr.flush();

		//display what returns the POST request

		StringBuilder sb = new StringBuilder();  
		int HttpResult = con.getResponseCode(); 
		if (HttpResult == HttpURLConnection.HTTP_OK) {
		    BufferedReader br = new BufferedReader(
		            new InputStreamReader(con.getInputStream(), "utf-8"));
		    String line = null;  
		    while ((line = br.readLine()) != null) {  
		        sb.append(line + "\n");  
		    }
		    br.close();
		    System.out.println("Post response from web app:" + sb.toString());  
		} else {
		    System.out.println(con.getResponseMessage());  
		}  
	}
	
	@POST
	@Path("getstores.do")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String scan(
			@FormParam("item")String item,
			@FormParam("zips")String zips,
			@FormParam("user")String userInput,
			@FormParam("isTrigger")String isTrigger) throws Exception {
		this.item = item;
		this.userId = userInput;
		this.isTrigger = isTrigger;
		user = uDao.findById(userInput);
		admin = uDao.findById("metocomp@gmail.com");
		if (user == null) {
			res.put("error", "USER_NOT_FOUND");
			return res.toString();
		}
		user.setLastRun(new Timestamp(System.currentTimeMillis()));
		// Starting a scan asynchronously.
		new Thread(new ScanRunnable(zips)).start();
		
		return res.toString();
	}
	
	private class ScanRunnable implements Runnable {
		private final String zipsCallable;
		
		ScanRunnable(String zipsCallable) {
			this.zipsCallable = zipsCallable;
		}

		@Override
		public void run() {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			while ((user.getRunning() > 0 || admin.getRunning() > 0) && now.getTime()
					- user.getLastRun().getTime() < RUN_TIMEOUT) {
				logger.log(Level.INFO, "waiting for the job to complete... running: " + user.getRunning(), user);
				// Periodicaly check
				try {
					Thread.sleep(1000 * 30);
				} catch (InterruptedException e) {
					logger.log(Level.INFO, "Failed to sleep and skipping... ", e);
				} // check every 30 seconds.
				AlphaDealUser newUser = uDao.findById(userId);
				logger.log(Level.INFO, "See if running in new user: " + newUser.getRunning(), newUser);
				user.setRunning(newUser.getRunning());
				admin = uDao.findById("metocomp@gmail.com");
				now = new Timestamp(System.currentTimeMillis());
			}
			user.setUserId(userId);
			user.setRunning(1);
			// Update the running bit for user
			uDao.update(user);
			String[] zipsArr = zipsCallable.split(",");
			System.out.println(String.format("Start geting the store for item: %s, zip size: %s",
					item, zipsArr.length));  
			int i = 0;
			ListeningExecutorService service =
					MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(20));
			List<String> subZips = new ArrayList<String>();
			List<ListenableFuture<Map<String, JSONObject>>> futureList =
					new ArrayList<ListenableFuture<Map<String, JSONObject>>>();
			for (String zip : zipsArr) {
				if (i < 100) {
					subZips.add(zip);
					if (i == zipsArr.length - 1) {
						ListenableFuture<Map<String, JSONObject>> future =
								service.submit(new GetStoresCallable(subZips));
						futureList.add(future);
				     }
					i++;
				} else {
					ListenableFuture<Map<String, JSONObject>> future =
							service.submit(new GetStoresCallable(subZips));
					futureList.add(future);
					subZips = new ArrayList<String>();
					i = 0;
				}
			}
			ListenableFuture<List<Map<String, JSONObject>>> successfulFutures =
					Futures.allAsList(futureList);
			Futures.addCallback(successfulFutures, new SendResultCallback());
		}
		
	}
	
	private class SendResultCallback implements FutureCallback<List<Map<String, JSONObject>>> {

		@Override
		public void onFailure(Throwable e1) {
			// Still send result;
			try {
				logger.log(Level.WARNING, "Failed to send result", e1);
				sendResult(new HashMap<String, JSONObject>());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onSuccess(List<Map<String, JSONObject>> result) {
			// Sends the result to web app.
			try {
				Map<String, JSONObject> storesMap = new HashMap<String, JSONObject>();
				for (Map<String, JSONObject> m : result) {
					storesMap.putAll(m);	
				}
				sendResult(storesMap);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private class GetStoresCallable implements Callable<Map<String, JSONObject>> {
		
		private final List<String> zips;
		
		private GetStoresCallable(List<String> zips) {
			this.zips = new ArrayList<String>(zips);
		}

		@Override
		public Map<String, JSONObject> call() throws Exception {
			Map<String, JSONObject> m = new HashMap<String, JSONObject>();
			for (String zip : zips) {
				try {
					JSONObject data = getStoreData(item, getLocation(zip));
					if (upc == null) {
						upc = data.getJSONObject("item").getString("upc");
					}
					JSONArray stores = data.getJSONArray("stores");
					for (int i = 0; i < stores.length(); i++) {
						String status = stores.getJSONObject(i).getString("stockStatus");
						if (status != null && status.indexOf("Out of stock") < 0) {
							m.put(stores.getJSONObject(i).getString("storeId"), stores.getJSONObject(i));
						}
					}
				} catch (Exception e) {
					if (e.getMessage() != null && !e.getMessage().contains("A JSONObject text must begin with")) {
						System.out.println("error get data: " + e.getMessage());
					}
				}
				Thread.sleep(500);
			}
			return m;
		}
	}

}