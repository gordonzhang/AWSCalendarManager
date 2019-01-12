package edu.wpi.gordon.cs509.lambdas.models;

import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.annotations.Expose;

public class Calendar {
	@Expose
	public final String idCal;
	@Expose
	public String name;
	@Expose
	public Date startDate;
	@Expose
	public Date endDate;
	@Expose
	public Time startHour;
	@Expose
	public Time endHour;
	@Expose
	public int minsPerSess;
	
	public Calendar (String idCal, String name, String startDate, String endDate, String startHour, String endHour, int minsPerSess) throws ParseException {
		SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm:ss");

		this.idCal = idCal;
		this.name = name;
        this.startDate = new java.sql.Date(dateParser.parse(startDate).getTime());
		this.endDate = new java.sql.Date(dateParser.parse(endDate).getTime());
		this.startHour = new Time(timeParser.parse(startHour).getTime());
		this.endHour = new Time(timeParser.parse(endHour).getTime());
		this.minsPerSess = minsPerSess;
	}
	
	public Calendar (String idCal, String name, Date startDate, Date endDate, Time startHour, Time endHour, int minsPerSess) throws ParseException {
		this.idCal = idCal;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.startHour = startHour;
		this.endHour = endHour;
		this.minsPerSess = minsPerSess;
	}

}
