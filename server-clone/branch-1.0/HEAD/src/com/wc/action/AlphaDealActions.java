package com.wc.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

	/** {product}, {nearby} */
	private static final String TARGET_URL = "https://api.target.com/available_to_promise/v2/%s/"
			+ "search?key=adaptive-pdp&nearby=%s&inventory_type=stores&multichannel_option=none&"
			+ "field_groups=location_summary&requested_quantity=1&radius=100";

	private String item;
	private String upc;
	private String userId;
	private String isTrigger;
	private AlphaDealUserDAO uDao = new AlphaDealUserDAO();
	private AlphaDealUser user;
	private AlphaDealUser admin;
	private enum StoreType {
		WALMART,
		TARGET
	};
	
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
		        	 logger.warning("failed from location response code:" + responseCode); 
			}
		} catch (Exception e) {
			logger.warning("failed from location:" + e);
		}
		return null;
	}
	
	// Gets the store jsons with the location and item id.
	private JSONObject getTargetStoreData(String item, String zip) throws Exception {
		if (zip == null) {
			logger.warning("zip is null");
		}

		String url = String.format(TARGET_URL, item, zip);
		
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
	
	// Gets the store jsons with the location and item id.
	private JSONObject getWalmartStoreData(String item, Location location) throws Exception {
		if (location == null) {
			logger.warning("location is null");
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
	
	/**
	 * Sends batch records in the size of 1000. The pageToken is used to identify the chunks. Once finished
	 * 'finished' bit is set.
	 */
	private void sendResult(Map<String, JSONObject> storesMap) throws Exception {
		Collection<JSONObject> allValues = storesMap.values();
		int size = allValues.size();
		logger.info("All size: " + size);
		JSONObject res = new JSONObject();
		if (size > 1000) {
			res.put("partial", "true");
			res.put("pageToken", UUID.randomUUID());
			JSONObject[] objArr = allValues.toArray(new JSONObject[size]);
			for (int i = 0; i < size; i += 1000) {
				int toIndex = i + 1000;
				if (i + 1000 > size) {
					toIndex = size;
					res.put("finished", "true");
				}
				res.put("start", i + 2);
				res.put("end", toIndex + 2);
				sendPartialResult(Arrays.copyOfRange(objArr, i, toIndex), res);
				Thread.sleep(10000);
			}
		} else {
			JSONObject[] objArr = allValues.toArray(new JSONObject[size]);
			sendPartialResult(objArr, res);
		}
	}
	
	// Sends the result back to web app.
	private void sendPartialResult(JSONObject[] stores, JSONObject res) throws Exception {
		
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

		try {
			res.put("item", item);
			res.put("upc", upc);
			res.put("stores", stores);
			res.put("user", userId);
			res.put("isTrigger", isTrigger);
		} catch (Exception e) {
			logger.info("Getting error" + e);
		}
		logger.info("item :" + item + "\n"  
//				+ "partial: " + res.get("partial") + "\n"
				+ "upc :" + upc + "\n" 
				+ "isTrigger :" + isTrigger + "\n"
				+ "store size:" + stores.length);  
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
		    logger.info("Post response from web app:" + sb.toString());  
		} else {
		    logger.warning(con.getResponseMessage());  
		}  
	}
	
	@POST
	@Path("getstores.do")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String scan(
			@FormParam("item")String item,
			@FormParam("zips")String zips,
			@FormParam("user")String userInput,
			@FormParam("isTrigger")String isTrigger,
			@FormParam("type")String storeType) throws Exception {
		JSONObject res = new JSONObject();
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
		StoreType type = storeType.equals("target") ? StoreType.TARGET : StoreType.WALMART;
		// Starting a scan asynchronously.
		new Thread(new ScanRunnable(zips, type)).start();
		
		return res.toString();
	}
	
	private class ScanRunnable implements Runnable {
		private final String zipsCallable;
		private final StoreType storeType;
		
		ScanRunnable(String zipsCallable, StoreType storeType) {
			this.zipsCallable = zipsCallable;
			this.storeType = storeType;
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
			logger.info(String.format("DEV Start geting the store for item: %s, zip size: %s",
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
						ListenableFuture<Map<String, JSONObject>> future = null;
						if (storeType == StoreType.WALMART) {
							future = service.submit(new GetWalmartStoresCallable(subZips));
						} else {
							future = service.submit(new GetTargetStoresCallable(subZips));
						}
						futureList.add(future);
				     }
					i++;
				} else {
					ListenableFuture<Map<String, JSONObject>> future = null;
					if (storeType == StoreType.WALMART) {
						future = service.submit(new GetWalmartStoresCallable(subZips));
					} else {
						future = service.submit(new GetTargetStoresCallable(subZips));
					}
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
	
	private class GetTargetStoresCallable implements Callable<Map<String, JSONObject>> {
		
		private final List<String> zips;
		
		private GetTargetStoresCallable(List<String> zips) {
			this.zips = new ArrayList<String>(zips);
		}

		@Override
		public Map<String, JSONObject> call() throws Exception {
			Map<String, JSONObject> m = new HashMap<String, JSONObject>();
			for (String zip : zips) {
				try {
					JSONObject data = getTargetStoreData(item, zip);
					if (upc == null) {
						upc = "";
					}
					JSONArray products = data.getJSONArray("products");
					if (products == null) {
						return new HashMap<String, JSONObject>();
					}
					JSONArray stores = products.getJSONObject(0).getJSONArray("locations");
					for (int i = 0; i < stores.length(); i++) {
						String status = stores.getJSONObject(i).getString("availability_status");
						if (status != null && status.indexOf("IN_STOCK") >= 0) {
							m.put(stores.getJSONObject(i).getString("location_id"), stores.getJSONObject(i));
						}
					}
				} catch (Exception e) {
					if (e.getMessage() != null && !e.getMessage().contains("A JSONObject text must begin with")) {
						logger.warning("error get data: " + e.getMessage());
					}
				}
				Thread.sleep(500);
			}
			return m;
		}
	}
	
	private class GetWalmartStoresCallable implements Callable<Map<String, JSONObject>> {
		
		private final List<String> zips;
		
		private GetWalmartStoresCallable(List<String> zips) {
			this.zips = new ArrayList<String>(zips);
		}

		@Override
		public Map<String, JSONObject> call() throws Exception {
			Map<String, JSONObject> m = new HashMap<String, JSONObject>();
			for (String zip : zips) {
				try {
					JSONObject data = getWalmartStoreData(item, getLocation(zip));
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
						logger.warning("error get data: " + e.getMessage());
					}
				}
				Thread.sleep(500);
			}
			return m;
		}
	}

}