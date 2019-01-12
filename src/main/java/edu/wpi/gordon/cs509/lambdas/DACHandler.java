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

//Delete / Add days in Calendar
public class DACHandler implements RequestStreamHandler {
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
					String operation = body.get("operation").getAsString();
					logger.log(operation);
					if (operation.equals("add")) {
						boolean dayAddingResult = addDayToRDS(idCal, date);
						if (dayAddingResult==true) {
							responseBody.addProperty("result", "Success");
						} else {
							responseBody.addProperty("result", "Failure");
						}
						logger.log("Day to add: \n" + date + "\n");
						logger.log("Day adding result:" + dayAddingResult + "\n");
					} else if (operation.equals("delete")) {
						boolean deleteAddingResult = deleteDayFromRDS(idCal, date);
						if (deleteAddingResult==true) {
							responseBody.addProperty("result", "Success");
						} else {
							responseBody.addProperty("result", "Failure");
						}
						logger.log("Day to delete: \n" + date + "\n");
						logger.log("Day deleting result:" + deleteAddingResult + "\n");
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
	
	private boolean addDayToRDS(String idCal, String dateToAdd) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.addDayToCalendar(idCal, dateToAdd);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean deleteDayFromRDS(String idCal, String dateToDelete) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.deleteDayFromCalendar(idCal, dateToDelete);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}