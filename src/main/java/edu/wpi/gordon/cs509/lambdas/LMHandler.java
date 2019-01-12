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
import edu.wpi.gordon.cs509.lambdas.models.Meeting;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

//Load Meetings
public class LMHandler implements RequestStreamHandler {
	JsonParser parser = new JsonParser();

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		
		List<Meeting> meetings = null;
		JsonArray mtJsonArray = new JsonArray();

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
					logger.log("To load mettings of " + idCal + " from RDS.\n");
					meetings = loadMeetingsFromRDS(idCal);
					for (Meeting meeting: meetings) { 		 
						String mtJsonString = getMeetingJson(meeting);
						JsonObject mtJson = parser.parse(mtJsonString).getAsJsonObject();
						mtJsonArray.add(mtJson);
					}
				}
			}
			responseBody.add("result", mtJsonArray);
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
	
	private List<Meeting> loadMeetingsFromRDS(String idCal) {
		CalendarDAO dao = new CalendarDAO();
		List<Meeting> mtList = null;
		try {
			mtList = dao.loadMeetings(idCal);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mtList;
	}
	
	private String getMeetingJson(Meeting meeting) {
	    Gson gson = null;
	    gson = new GsonBuilder()
	    .excludeFieldsWithoutExposeAnnotation()
	    .create();
	    return gson.toJson(meeting);
	}

}