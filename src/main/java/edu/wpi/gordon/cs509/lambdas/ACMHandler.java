package edu.wpi.gordon.cs509.lambdas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.wpi.gordon.cs509.lambdas.db.CalendarDAO;

import com.google.gson.JsonParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

//Delete and Add days in Calendar
public class ACMHandler implements RequestStreamHandler {
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
					String idTS = body.get("idTS").getAsString();
					String idCal = body.get("idCal").getAsString();
					String title = body.get("title").getAsString();
					String location = body.get("location").getAsString();
					String participant = body.get("participant").getAsString();
					String operation = body.get("operation").getAsString();
					logger.log("Operation: " + operation);
					if (operation.equals("add")) {
						boolean MeetingCreatingResult = createMeetingInRDS(idTS, idCal, title, location, participant);
						if (MeetingCreatingResult==true) {
							responseBody.addProperty("result", "Success");
						} else {
							responseBody.addProperty("result", "Failure");
						}
						logger.log("Meeting to create: " + title + location + participant +"\n");
						logger.log("Meeting creating result: " + MeetingCreatingResult + "\n");
					} else if (operation.equals("delete")) {
						boolean MeetingDeletingResult = deleteMeetingFromRDS(idTS);
						if (MeetingDeletingResult==true) {
							responseBody.addProperty("result", "Success");
						} else {
							responseBody.addProperty("result", "Failure");
						}
						logger.log("Meeting to delete: " + title + location + participant +"\n" + idTS + "\n");
						logger.log("Meeting deleting result: " + MeetingDeletingResult + "\n");
					}
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
	
	private boolean createMeetingInRDS(String idTS, String idCal, String title, String location, String participant) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.createMeeting(idTS, idCal, title, location, participant);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean deleteMeetingFromRDS(String idTS) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.deleteMeetingFromCalendar(idTS);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}