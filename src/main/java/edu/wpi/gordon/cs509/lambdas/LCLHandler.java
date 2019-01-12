package edu.wpi.gordon.cs509.lambdas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.wpi.gordon.cs509.lambdas.db.CalendarDAO;
import edu.wpi.gordon.cs509.lambdas.models.Calendar;

import com.google.gson.JsonParseException;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
//import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

//Load Calendar List
public class LCLHandler implements RequestStreamHandler {
	JsonParser parser = new JsonParser();

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		
		List<Calendar> calList;
		JsonArray calJsonArray = new JsonArray();
		JsonObject responseJson = new JsonObject();
		JsonObject headerJson = new JsonObject();
        JsonObject responseBody = new JsonObject();
        
		headerJson.addProperty("Content-Type",  "application/json");
		headerJson.addProperty("Access-Control-Allow-Methods", "GET,POST");
		headerJson.addProperty("Access-Control-Allow-Origin",  "*");
        responseJson.add("headers", headerJson);
		responseJson.addProperty("isBase64Encoded", false);
        
        try {
			calList = loadAllCalendarsFromRDS();
			
			for (Calendar cal: calList) {
				String calJsonString = getCalendarJson(cal);
				JsonObject calJson = parser.parse(calJsonString).getAsJsonObject();
				calJsonArray.add(calJson);
			}
			
			responseBody.add("result", calJsonArray);
//			NOTICE: For API Gateway to accept the response, headers MUST NOT be string and body MUST be string.
	        responseJson.addProperty("body", responseBody.toString());
			logger.log("Response Body:\n" + responseBody.toString() + "\n");
			responseJson.addProperty("statusCode", 200);
	        
        } catch(JsonParseException pex) {
            responseJson.addProperty("exception", pex.toString());
            responseJson.addProperty("statusCode", 400);
        }
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
	}
	
	private List<Calendar> loadAllCalendarsFromRDS() {
		CalendarDAO dao = new CalendarDAO();
		List<Calendar> calList = null;
		try {
			calList = dao.loadAllCalendars();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return calList;
	}
	
	private String getCalendarJson(Calendar calendar) {
	    Gson gson = null;
	    gson = new GsonBuilder()
	    .excludeFieldsWithoutExposeAnnotation()
	    .create();
	    return gson.toJson(calendar);
	}

}