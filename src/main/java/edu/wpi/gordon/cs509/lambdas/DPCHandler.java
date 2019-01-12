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

//Delete Personal Calendar
public class DPCHandler implements RequestStreamHandler {
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
					boolean calDeletingResult = deleteCalendarFromRDS(idCal);
					boolean tsDeletingResult = deleteTSFromRDS(idCal);
					if (calDeletingResult==true & tsDeletingResult==true) {
						responseBody.addProperty("result", "Success");
					} else {
						responseBody.addProperty("result", "Failure");
					}
					logger.log("Calendar to Delete: \n" + idCal + "\n");
					logger.log("Calendar deleting result:" + calDeletingResult + "\n");
					logger.log("TimeSlots deleting result:" + tsDeletingResult + "\n");
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
	
	private boolean deleteCalendarFromRDS(String idCal) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.deleteCalendar(idCal);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean deleteTSFromRDS(String idCal) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.deleteTimeSlots(idCal);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}