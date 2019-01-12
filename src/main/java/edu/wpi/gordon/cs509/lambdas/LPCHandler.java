package edu.wpi.gordon.cs509.lambdas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.wpi.gordon.cs509.lambdas.db.CalendarDAO;
import edu.wpi.gordon.cs509.lambdas.models.TimeSlot;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

//Load Personal Calendars
public class LPCHandler implements RequestStreamHandler {
	JsonParser parser = new JsonParser();

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		
		List<TimeSlot> timeSlots = null;
		JsonArray tsJsonArray = new JsonArray();

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
			logger.log("event:\n" + event.toString() + "\n");
			
			if(event.get("queryStringParameters") != null) {
				String reqJsonString = event.get("queryStringParameters").toString();
				if(reqJsonString != null) {
					JsonObject reqJson = parser.parse(reqJsonString).getAsJsonObject();
					String idCal = reqJson.get("idCal").getAsString();
					logger.log("To load timeSlots " + idCal + " from RDS.\n");
					timeSlots = loadTimeSlotsFromRDS(idCal);
					for (TimeSlot timeSlot: timeSlots) { 		 
						String tsJsonString = getTimeSlotJson(timeSlot);
						JsonObject tsJson = parser.parse(tsJsonString).getAsJsonObject();
						tsJsonArray.add(tsJson);
					}
				}
			}
			responseBody.add("result", tsJsonArray);
			responseJson.addProperty("body", responseBody.toString());
			logger.log(responseBody.toString());
			responseJson.addProperty("statusCode", 200);
//			NOTICE: For API Gateway to accept the response, headers MUST NOT be string and body MUST be string. 
	        responseJson.add("headers", headerJson);
	        responseJson.addProperty("body", responseBody.toString());
	        
        } catch(Exception pex) {
            responseJson.addProperty("statusCode", 400);
            responseJson.addProperty("exception", pex.toString());
        }
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
	}
	
	private List<TimeSlot> loadTimeSlotsFromRDS(String idCal) {
		CalendarDAO dao = new CalendarDAO();
		List<TimeSlot> tsList = null;
		try {
			tsList = dao.loadTimeSlots(idCal);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tsList;
	}
	
	private String getTimeSlotJson(TimeSlot timeSlot) {
	    Gson gson = null;
	    gson = new GsonBuilder()
	    .excludeFieldsWithoutExposeAnnotation()
	    .setDateFormat("yyyy-MM-dd")
	    .create();
	    return gson.toJson(timeSlot);
	}

}