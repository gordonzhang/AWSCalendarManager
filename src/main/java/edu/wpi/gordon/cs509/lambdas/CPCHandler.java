package edu.wpi.gordon.cs509.lambdas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.wpi.gordon.cs509.lambdas.db.CalendarDAO;
import edu.wpi.gordon.cs509.lambdas.models.Calendar;
import edu.wpi.gordon.cs509.lambdas.models.TimeSlot;

import com.google.gson.JsonParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

//Create Personal Calendar
public class CPCHandler implements RequestStreamHandler {
	JsonParser parser = new JsonParser();

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		
		Calendar calendar;
		
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
					String calName = body.get("calName").getAsString();
					String startDate = body.get("startDate").getAsString();
					String endDate = body.get("endDate").getAsString();
					String startHour = body.get("startHour").getAsString();
					String endHour = body.get("endHour").getAsString();
					int minsPerSess = body.get("minsPerSess").getAsInt();
					String idCal = UUID.randomUUID().toString();
					calendar = new Calendar(idCal, calName, startDate, endDate, startHour, endHour, minsPerSess);
					boolean calAddingResult = addCalendarToRDS(calendar);
					
					ArrayList<TimeSlot> timeSlots = generateTimeSlots(idCal, startDate, endDate, startHour, endHour, minsPerSess);
					boolean tsAddingResult = addTimeSlotsToRDS(timeSlots);
					
					if (calAddingResult==true & tsAddingResult==true) {
						responseBody.addProperty("result", "Success");
					} else {
						responseBody.addProperty("result", "Failure");
					}

					logger.log("New calendar to add: \n" + calName + ";" + idCal + "\n");
					logger.log("Calendar Creating result:" + calAddingResult + "\n");
					logger.log("TimeSlots Creating result:" + tsAddingResult + "\n");
					logger.log("TimeSlots size: " + timeSlots.size() + "\n");
				}
			}
			responseJson.addProperty("statusCode", 200);
//			NOTICE: For API Gateway to accept the response, headers MUST NOT be string and body MUST be string. 
	        responseJson.addProperty("body", responseBody.toString());
	        
        } catch(JsonParseException | ParseException pex) {
            responseJson.addProperty("statusCode", 400);
            responseJson.addProperty("exception", pex.toString());
        }
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
	}
	
	private ArrayList<TimeSlot> generateTimeSlots(String idCal, String startDate, String endDate, String startHour, String endHour, int minsPerSess) throws ParseException {
		ArrayList<TimeSlot> TSList = new ArrayList<TimeSlot>();
		
		SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm:ss");

		Date startD = new Date(dateParser.parse(startDate).getTime());
		Date endD = new Date(dateParser.parse(endDate).getTime());
		Time startH = new Time(timeParser.parse(startHour).getTime());
		Time endH = new Time(timeParser.parse(endHour).getTime());
		
		Date thisD = startD;
		
		while ( !thisD.after(endD) ) {
			Time thisT = startH;
			while ( !addMins(thisT, minsPerSess).after(endH) ) {
				TimeSlot timeSlot = new TimeSlot(UUID.randomUUID().toString(), idCal, thisD.toString(), thisT.toString(), 0, 0);
				TSList.add(timeSlot);
				
				thisT = addMins(thisT, minsPerSess);
			}
			thisD = addDays(thisD, 1);
		}
		return TSList;
	}

	private boolean addCalendarToRDS(Calendar calendar) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.addCalendar(calendar);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean addTimeSlotsToRDS(ArrayList<TimeSlot> timeSlots) {
		CalendarDAO dao = new CalendarDAO();
		try {
			dao.addTimeSlots(timeSlots);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private java.sql.Date addDays(java.sql.Date thisDay, int nDays) {
        LocalDate localD = thisDay.toLocalDate();
        localD = localD.plusDays(nDays);
        java.sql.Date nextDay = java.sql.Date.valueOf(localD);
        return nextDay;
	}
	
	private java.sql.Time addMins(java.sql.Time thisTime, int nMins) {
        LocalTime localT = thisTime.toLocalTime();
        localT = localT.plusMinutes(nMins);
        java.sql.Time nextHour = java.sql.Time.valueOf(localT);
        return nextHour;
	}
	
}