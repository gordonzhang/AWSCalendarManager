package edu.wpi.gordon.cs509.lambdas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import edu.wpi.gordon.cs509.lambdas.db.CalendarDAO;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

// Close Time Slots
public class CTSHandler implements RequestStreamHandler {
	JsonParser parser = new JsonParser();

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		
		JsonObject responseJson = new JsonObject();
		JsonObject headerJson = new JsonObject();
        JsonObject responseBody = new JsonObject();
        
		headerJson.addProperty("Content-Type",  "application/json");
		headerJson.addProperty("Access-Control-Allow-Methods", "GET,POST");
		headerJson.addProperty("Access-Control-Allow-Origin",  "*");
        responseJson.add("headers", headerJson);
		responseJson.addProperty("isBase64Encoded", false);

        try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			JsonObject event = parser.parse(reader).getAsJsonObject();
			logger.log("event: " + event + '\n');
			
			if(event.get("body") != null) {
				String bodyString = event.get("body").getAsString();
				if(bodyString != null) {
					JsonObject body = parser.parse(bodyString).getAsJsonObject();
					String idCal = body.get("idCal").getAsString();
					String date = body.get("date").getAsString();
					String time = body.get("time").getAsString();
					JsonElement dL = body.get("dowList");
					Type listType = new TypeToken<List<String>>() {}.getType();
					List<String> dowList = new Gson().fromJson(dL, listType);
					
					boolean tsClosingResult = false;
					if ( dowList.size()==0 ) {
						if ( date.equals("Everyday") ) {
							if ( time.equals("Whole Day") ) {
								tsClosingResult = closeTimeSlotsInRDS(idCal);
							} else {
								tsClosingResult = closeTimeSlotsInRDS(idCal, time);
							}
						} else {
							if ( time.equals("Whole Day") ) {
								tsClosingResult = closeAllTimeSlotsOnDateInRDS(idCal, date);
							} else {
								tsClosingResult = closeTimeSlotsInRDS(idCal, date, time);
							}
						}
					} else {
						// days of week has been chosen
						if ( time.equals("Whole Day") ) {
							tsClosingResult = closeTimeSlotsInRDS(idCal, dowList);
						} else {
							tsClosingResult = closeTimeSlotsInRDS(idCal, dowList, time);
						}
					}
					
					if (tsClosingResult==true) {
						responseBody.addProperty("result", "Success");
					} else {
						responseBody.addProperty("result", "Failure");
					}
					logger.log("Date of TimeSlots to close: \n" + date + "\n");
					logger.log("Day adding result:" + tsClosingResult + "\n");
				}
			}
			responseJson.addProperty("statusCode", 200);
//			NOTICE: For API Gateway to accept the response, headers MUST NOT be string and body MUST be string. 
	        responseJson.addProperty("body", responseBody.toString());
	        
        } catch(JsonParseException pex) {
            responseJson.addProperty("statusCode", 400);
            responseJson.addProperty("exception", pex.toString());
        }
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
	}
	
	// close timeslots on whole day of everyday.
	private boolean closeTimeSlotsInRDS(String idCal) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.closeTimeSlots(idCal);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// close timeslots at a time on everyday
	private boolean closeTimeSlotsInRDS(String idCal, String time) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.closeTimeSlots(idCal, time);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// close all timeslots on a day.
	private boolean closeAllTimeSlotsOnDateInRDS(String idCal, String date) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.closeAllTimeSlotsOnDate(idCal, date);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// Close timeslots on given date and time
	private boolean closeTimeSlotsInRDS(String idCal, String date, String time) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.closeTimeSlots(idCal, date, time);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// close timeslots on whole day on given days of week.
	private boolean closeTimeSlotsInRDS(String idCal, List<String> dowList) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.closeTimeSlots(idCal, dowList);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// close timeslots on given time on given days of week.
	private boolean closeTimeSlotsInRDS(String idCal, List<String> dowList, String time) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.closeTimeSlots(idCal, dowList, time);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	

	

}