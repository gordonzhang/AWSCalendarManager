package edu.wpi.gordon.cs509.lambdas.db;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;


import edu.wpi.gordon.cs509.lambdas.models.Calendar;
import edu.wpi.gordon.cs509.lambdas.models.Meeting;
import edu.wpi.gordon.cs509.lambdas.models.TimeSlot;


public class CalendarDAO {
	
	java.sql.Connection conn;
	
	SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm:ss");

    public CalendarDAO() {
    	try  {
    		conn = DatabaseUtil.connect();
    	} catch (Exception e) {
    		conn = null;
    	}
    }
    
    public List<Calendar> loadAllCalendars() throws Exception {
    	List<Calendar> calendarList = new ArrayList<Calendar>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Calendars;");
            ResultSet resultSet = ps.executeQuery();
            
            while (resultSet.next()) {
            	Calendar cal = generateCalendar(resultSet);
            	calendarList.add(cal);
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
        	e.printStackTrace();
            throw new Exception("Failed in getting calendars: " + e.getMessage());
        }
        return calendarList;
    }
    
    public Calendar loadCalendar(String idCal) throws Exception {
    	Calendar cal = null;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Calendars WHERE idCal = ?;");
            ps.setString(1, idCal);
            ResultSet resultSet = ps.executeQuery();
            
            while (resultSet.next()) {
            	cal = generateCalendar(resultSet);
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
        	e.printStackTrace();
            throw new Exception("Failed in getting calendar: " + e.getMessage());
        }
        return cal;
    }
    
    public boolean addCalendar(Calendar calendar) throws Exception {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Calendars WHERE name = ?;");
            ps.setString(1, calendar.name);
            ResultSet resultSet = ps.executeQuery();
            
            // already present?
            while (resultSet.next()) {
//                Calendar cal = generateCalendar(resultSet);
                resultSet.close();
                return false;
            }

            ps = conn.prepareStatement("INSERT INTO Calendars (idCal,name,startDate,endDate,startHour,endHour,minsPerSess) VALUES (?,?,?,?,?,?,?);");
            ps.setString(1, calendar.idCal);
            ps.setString(2, calendar.name);
            ps.setDate(3, calendar.startDate);
            ps.setDate(4, calendar.endDate);
            ps.setTime(5, calendar.startHour);
            ps.setTime(6, calendar.endHour);
            ps.setInt(7, calendar.minsPerSess);
            ps.execute();
            return true;

        } catch (Exception e) {
            throw new Exception("Failed to insert constant: " + e.getMessage());
        }
    }
    
    public boolean deleteCalendar(String idCal) throws Exception {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Calendars WHERE idCal = ?;");
            ps.setString(1, idCal);
            int numAffected = ps.executeUpdate();
            ps.close();
            
            return (numAffected == 1);

        } catch (Exception e) {
            throw new Exception("Failed to delete calendar: " + e.getMessage());
        }
    }
    
	public boolean addDayToCalendar(String idCal, String dateToAdd) throws Exception {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TimeSlots WHERE (idCal = ? AND date = ?);");
            ps.setString(1, idCal);
            ps.setString(2, dateToAdd);
            ResultSet resultSet = ps.executeQuery();
            // already present?
            while (resultSet.next()) {
//                Calendar cal = generateCalendar(resultSet);
                resultSet.close();
                return false;
            }
            // Get calendar's info
            Calendar cal = loadCalendar(idCal);
            ArrayList<TimeSlot> timeSlots = generateTimeSlots(idCal, dateToAdd, dateToAdd, timeParser.format(cal.startHour), timeParser.format(cal.endHour), cal.minsPerSess);
            return addTimeSlots(timeSlots);

        } catch (Exception e) {
            throw new Exception("Failed to insert constant: " + e.getMessage());
        }
	}
	
	public boolean deleteDayFromCalendar(String idCal, String dateToDelete) throws Exception {
        try {
        	List<TimeSlot> tsListOfMeetingsToDelete = loadTimeSlotsOnDate(idCal, dateToDelete);
        	int numMTAffected = 0;
        	for (TimeSlot timeslot: tsListOfMeetingsToDelete) {
                PreparedStatement psMt = conn.prepareStatement("DELETE FROM Meetings WHERE (idCal = ? AND idTS = ?);");
                psMt.setString(1, idCal);
                psMt.setString(2, timeslot.idTS);
                numMTAffected += psMt.executeUpdate();
                psMt.close();
        	}
            
            PreparedStatement ps = conn.prepareStatement("DELETE FROM TimeSlots WHERE (idCal = ? AND date = ?);");
            ps.setString(1, idCal);
            ps.setString(2, dateToDelete);
            int numTSAffected = ps.executeUpdate();
            ps.close();

            
            return (numTSAffected > 0);
        } catch (Exception e) {
            throw new Exception("Failed to delete calendar: " + e.getMessage());
        }
	}
	
    public List<TimeSlot> loadTimeSlots(String idCal) throws Exception {
    	List<TimeSlot> tsList = new ArrayList<TimeSlot>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TimeSlots WHERE idCal = ?;");
            ps.setString(1, idCal);

            ResultSet resultSet = ps.executeQuery();
            
            while (resultSet.next()) {
            	TimeSlot ts = generateTimeSlot(resultSet);
            	tsList.add(ts);
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
        	e.printStackTrace();
            throw new Exception("Failed in getting TimeSlots: " + e.getMessage());
        }
        return tsList;
    }
    
    public List<TimeSlot> loadTimeSlotsOnDate(String idCal, String date) throws Exception {
    	List<TimeSlot> tsList = new ArrayList<TimeSlot>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TimeSlots WHERE (idCal = ? AND date = ?);");
            ps.setString(1, idCal);
            ps.setString(2, date);

            ResultSet resultSet = ps.executeQuery();
            
            while (resultSet.next()) {
            	TimeSlot ts = generateTimeSlot(resultSet);
            	tsList.add(ts);
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
        	e.printStackTrace();
            throw new Exception("Failed in getting TimeSlots: " + e.getMessage());
        }
        return tsList;
    }
    
    public List<Meeting> loadMeetings(String idCal) throws Exception {
    	List<Meeting> mtList = new ArrayList<Meeting>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Meetings WHERE idCal = ?;");
            ps.setString(1, idCal);

            ResultSet resultSet = ps.executeQuery();
            
            while (resultSet.next()) {
            	Meeting meeting = generateMeeting(resultSet);
            	mtList.add(meeting);
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
        	e.printStackTrace();
            throw new Exception("Failed in getting Meetings: " + e.getMessage());
        }
        return mtList;
    }
    
    public boolean addTimeSlots(ArrayList<TimeSlot> timeSlots) throws Exception {
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO TimeSlots (idTS,idCal,date,time,hasMeeting,closed) VALUES (?,?,?,?,?,?);");
            for (TimeSlot timeSlot: timeSlots) {
            	ps.setString(1, timeSlot.idTS);
                ps.setString(2, timeSlot.idCal);
                ps.setDate(3, timeSlot.date);
                ps.setTime(4, timeSlot.time);
                ps.setInt(5, timeSlot.hasMeeting);
                ps.setInt(6, timeSlot.closed);
                ps.addBatch();
            }
            ps.executeBatch();
//            conn.commit();
            return true;
        } catch (Exception e) {
            throw new Exception("Failed to insert constant: " + e.getMessage());
        }
    }

    public boolean deleteTimeSlots(String idCal) throws Exception {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM TimeSlots WHERE idCal = ?;");
            ps.setString(1, idCal);
            int numAffected = ps.executeUpdate();
            ps.close();
            
            return (numAffected > 0);
        } catch (Exception e) {
            throw new Exception("Failed to delete calendar: " + e.getMessage());
        }
    }
    
    
//    Helper Functions
//==============================================================================================================
    
    private Calendar generateCalendar(ResultSet resultSet) throws Exception {
    	String id = resultSet.getString("idCal");
        String name  = resultSet.getString("name");
        Date startDate = resultSet.getDate("startDate");
        Date endDate = resultSet.getDate("endDate");
        Time startHour = resultSet.getTime("startHour");
        Time endHour = resultSet.getTime("endHour");
        int minsPerSess = resultSet.getInt("minsPerSess");
        
        return new Calendar(id, name, startDate, endDate, startHour, endHour, minsPerSess);
    }
    
    private TimeSlot generateTimeSlot(ResultSet resultSet) throws Exception {
    	String idTS = resultSet.getString("idTS");
        String idCal  = resultSet.getString("idCal");
        Date date = resultSet.getDate("date");
        Time time = resultSet.getTime("time");
        int hasMeeting = resultSet.getInt("hasMeeting");
        int closed = resultSet.getInt("closed");
        
        return new TimeSlot(idTS, idCal, date, time, hasMeeting, closed);
    }
    
//    String idMT, String idTS, String idCal, String name, String location, String participant
    private Meeting generateMeeting(ResultSet resultSet) throws Exception {
    	String idMT = resultSet.getString("idMT");
        String idTS  = resultSet.getString("idTS");
        String idCal = resultSet.getString("idCal");
        String title = resultSet.getString("title");
        String location = resultSet.getString("location");
        String participant = resultSet.getString("participant");
        
        return new Meeting(idMT, idTS, idCal, title, location, participant);
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