package edu.wpi.gordon.cs509.lambdas.models;

import com.google.gson.annotations.Expose;


public class Meeting {
	@Expose
	public final String idMT;
	@Expose
	public String idTS;
	@Expose
	public final String idCal;
	@Expose
	public String title;;
	@Expose
	public String location;;
	@Expose
	public String participant;;
	
	public Meeting (String idMT, String idTS, String idCal, String title, String location, String participant) {
		this.idMT = idMT;
		this.idTS = idTS;
        this.idCal = idCal;
		this.title = title;
		this.location = location;
		this.participant = participant;
	}
	
}
