package edu.wpi.gordon.cs509.lambdas.models;

import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.annotations.Expose;


public class TimeSlot {
	@Expose
	public final String idTS;
	@Expose
	public final String idCal;
	@Expose
	public final Date date;
	@Expose
	public final Time time;
	@Expose
	public int hasMeeting;
	@Expose
	public int closed;
	
	public TimeSlot (String idTS, String idCal, String date, String time, int hasMeeting, int closed) throws ParseException {
		SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm:ss");

		this.idTS = idTS;
		this.idCal = idCal;
        this.date = new java.sql.Date(dateParser.parse(date).getTime());
		this.time = new java.sql.Time(timeParser.parse(time).getTime());
		this.hasMeeting = hasMeeting;
		this.closed = closed;
	}
	
	public TimeSlot (String idTS, String idCal, Date date, Time time, int hasMeeting, int closed) throws ParseException {
		this.idTS = idTS;
		this.idCal = idCal;
        this.date = date;
		this.time = time;
		this.hasMeeting = hasMeeting;
		this.closed = closed;
	}

}
