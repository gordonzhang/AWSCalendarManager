var loadCalendarListURL = "https://suthx1jfw5.execute-api.us-east-2.amazonaws.com/alpha/loadcalendarlist";
var createPersonalCalendarURL = "https://suthx1jfw5.execute-api.us-east-2.amazonaws.com/alpha/createpersonalcalendar";
var deletePersonalCalendarURL = "https://suthx1jfw5.execute-api.us-east-2.amazonaws.com/alpha/deletepersonalcalendar";
var loadTimeSlotsURL = "https://suthx1jfw5.execute-api.us-east-2.amazonaws.com/alpha/loadpersonalcalendar";
var addDeleteDayInCalendarURL = "https://suthx1jfw5.execute-api.us-east-2.amazonaws.com/alpha/adddaytocalendar";
var loadMeetingsURL = "https://suthx1jfw5.execute-api.us-east-2.amazonaws.com/alpha/loadmeetings";
var closeTimeSlotsURL = "https://suthx1jfw5.execute-api.us-east-2.amazonaws.com/alpha/closetimeslots";
var addCancelMeetingURL = "https://suthx1jfw5.execute-api.us-east-2.amazonaws.com/alpha/addcancelmeeting";

var calList;
var tsList;
var mtList;
var dateList;
var timeList;
var minsPerSess;

var activeCal = null;

function set_activeCal(newCal) {
	activeCal = newCal;
	if (activeCal) {
		$('#btnLPC').prop("disabled", false);
		$('#btnDPC').prop("disabled", false);
		$('#btnAddDay').prop("disabled", false);
		$('#btnCloseTimeSlots').prop("disabled", false);
	} else {
		$('#btnLPC').prop("disabled", true);
		$('#btnDPC').prop("disabled", true);
		$('#btnAddDay').prop("disabled", true);
		$('#btnCloseTimeSlots').prop("disabled", true);
		$('#btnDeleteDay').prop("disabled", true);
	}
}

function doOnLoad() {
	loadCalendarList();

	var daySel = $("#daySel")[0];
	//	Disable load and delete buttons when no calendar is selected.
	$('#calSel')[0].onchange = function () {
		var idCalSelected = $('#calSel')[0].value;
		if (idCalSelected) {
			$('#btnLPC').prop("disabled", false);
			$('#btnDPC').prop("disabled", false);
		} else {
			$('#btnLPC').prop("disabled", true);
			$('#btnDPC').prop("disabled", true);
		}
	};

	//	Disable new and cancel meeting buttons when no timeslot is selected.
	$('#tsSel')[0].onchange = function () {
		var idTsSelected = $('#tsSel')[0].value;
		if (idTsSelected) {
			$('#labelNewMeetingTime')[0].innerHTML = $("#daySel option:selected").text() + ", " + $("#tsSel option:selected").text().substring(0, 8);
			var meeting = findEntriesInJsonObjByKeyValue(mtList, 'idTS', idTsSelected)[0];
			if (meeting) {
				$('#btnNewMeeting').prop("disabled", true);
				$('#btnEditMeeting').prop("disabled", false);
				$('#btnCancelMeeting').prop("disabled", false);

				$('#inputTitleToEdit').val(meeting.title);
				$('#inputLocationToEdit').val(meeting.location);
				$('#inputParticipantToEdit').val(meeting.participant);
			} else {
				$('#btnNewMeeting').prop("disabled", false);
				$('#btnEditMeeting').prop("disabled", true);
				$('#btnCancelMeeting').prop("disabled", true);
			}
		} else {
			$('#btnNewMeeting').prop("disabled", true);
			$('#btnEditMeeting').prop("disabled", true);
			$('#btnCancelMeeting').prop("disabled", true);
		}
	};

	// When selection changes in day select
	$('#daySel')[0].onchange = function () {
		if ($('#daySel')[0].selectedOptions.length == 1) {
			$('#btnDeleteDay').prop("disabled", false);
		} else {
			$('#btnDeleteDay').prop("disabled", true);
		}
		displaySchedule();
	};

	// When day of week checkbox changes, set date to close select disabled or not.
	$('.dayOfWeekToClose').change(function () {
		if ($('.dayOfWeekToClose:checked').length > 0) {
			// any checkbox is checked
			$('#dateToCloseSel').prop("disabled", true);
			$("#dateToCloseSel").val('NA');
		} else if ($('.dayOfWeekToClose:checked').length == 0) {
			$('#dateToCloseSel').prop("disabled", false);
			if ($("#dateToCloseSel")[0].value == 'NA') {
				$("#dateToCloseSel").val('placeholder');
			}
		} else {
			$('#dateToCloseSel').prop("disabled", false);
		}
		setCloseTimeSlotsSubmitBtn();
	});

	// Close TimeSlots Submit button disabled if any of date/time options is default.
	$('.closeTSSel').change(function () {
		setCloseTimeSlotsSubmitBtn();
	});

	// Disable all day of week checkboxes if a date option is selected
	$('#dateToCloseSel')[0].onchange = function () {
		if ($('#dateToCloseSel')[0].value == 'Everyday') {
			$('.dayOfWeekToClose').prop('disabled', true);
		} else {
			$('.dayOfWeekToClose').prop('disabled', false);
		}
	};

}

function loadCalendarList() {
	var calSel = $('#calSel')[0];
	var daySel = $('#daySel')[0];
	var tsSel = $('#tsSel')[0];

	$('#calSel').children().remove();
	$('#daySel').children().remove();
	$('#tsSel').children().remove();

	var xhr = new XMLHttpRequest();
	xhr.open("GET", loadCalendarListURL, true);
	xhr.send();

	xhr.onloadend = function () {
		if (xhr.readyState == XMLHttpRequest.DONE) {
			console.log("Calendar list received: \n" + JSON.stringify(JSON.parse(xhr.responseText).result, null, 2));
			calList = JSON.parse(xhr.responseText).result;
			for (var i in calList) {
				var option = document.createElement("option");
				option.text = calList[i].name;
				option.value = calList[i].idCal;
				calSel.add(option);
			}
			if (activeCal) {
				calSel.value = activeCal.idCal;
			}
		} else {
			console.log("Unable to get calendar list.");
		}
	};
}

function createPersonalCalendar() {
	data = {};
	data.calName = $("#calNameNCM")[0].value;
	data.startDate = $("#startDateNCM")[0].value;
	data.endDate = $("#endDateNCM")[0].value;
	data.startHour = $("#startHourNCM")[0].value + ":00";
	data.endHour = $("#endHourNCM")[0].value + ":00";
	data.minsPerSess = $("#minsPerSessNCM")[0].value;
	var js = JSON.stringify(data);
	console.log("To create calendar: \n" + JSON.stringify(JSON.parse(js), null, 2));

	var xhr = new XMLHttpRequest();
	xhr.open("POST", createPersonalCalendarURL, true);
	xhr.send(js);
	xhr.onloadend = function () {
		if (xhr.readyState == XMLHttpRequest.DONE) {
			var result = JSON.parse(xhr.responseText).result;
			if (result == "Success") {
				$('#newCalModal').modal('hide');
				$('#calCreatingSuccessModal').modal('show');
			} else {
				$('#calCreatingFailureModal').modal('show');
			}
			loadCalendarList();
			set_activeCal(null);
		} else {
			console.log("Unable to create calendar.");
		}
	};
}

function loadPersonalCalendar(id) {
	var calSel = $("#calSel")[0];
	var idCal;

	if (id) {
		idCal = id;
	} else {
		idCal = calSel.value;
	}
	console.log("To load calendar with ID: " + idCal);
	set_activeCal(findEntriesInJsonObjByKeyValue(calList, "idCal", idCal)[0]);
	minsPerSess = activeCal.minsPerSess;
	$('#labelAvailableDays')[0].innerHTML = 'Days in ' + activeCal.name + ':';
	$('#labelAddDay')[0].innerHTML = 'Add Day to ' + activeCal.name;
	$('#labelNewMeeting')[0].innerHTML = 'New Meeting for ' + activeCal.name;
	$('#labelEditMeeting')[0].innerHTML = 'View/Edit Meeting for ' + activeCal.name;
	$('#labelCloseTimeSlots')[0].innerHTML = 'Close Sessions in ' + activeCal.name;

	var daySel = $("#daySel")[0];
	$("#daySel").children().remove();

	var dateToCloseSel = $("#dateToCloseSel")[0];
	$("#dateToCloseSel").children().remove();
	// Add special options to close timeSlot options.
	// default selection / placeholder
	var option = document.createElement("option");
	option.text = ' -- Select Date To Close -- ';
	option.value = 'placeholder';
	option.setAttribute("disabled", "disabled");
	option.setAttribute("selected", "selected");
	option.setAttribute("hidden", "hidden");
	dateToCloseSel.add(option);
	// 'Everyday' selection
	option = document.createElement("option");
	option.text = 'Everyday';
	option.value = 'Everyday';
	dateToCloseSel.add(option);
	// Not applicable when any of the day of week checkboxes are selected.
	option = document.createElement("option");
	option.text = 'Not applicable';
	option.value = 'NA';
	option.setAttribute("disabled", "disabled");
	option.setAttribute("hidden", "hidden");
	dateToCloseSel.add(option);


	var timeToCloseSel = $("#timeToCloseSel")[0];
	$("#timeToCloseSel").children().remove();
	// default selection / placeholder
	option = document.createElement("option");
	option.text = ' -- Select Time To Close -- ';
	option.value = 'placeholder';
	option.setAttribute("disabled", "disabled");
	option.setAttribute("selected", "selected");
	option.setAttribute("hidden", "hidden");
	timeToCloseSel.add(option);
	// 'Whole Day' selection
	option = document.createElement("option");
	option.text = 'Whole Day';
	option.value = 'Whole Day';
	timeToCloseSel.add(option);


	var xhr = new XMLHttpRequest();
	xhr.open("GET", loadTimeSlotsURL + "?idCal=" + idCal, true);
	xhr.send();
	xhr.onloadend = function () {
		if (xhr.readyState == XMLHttpRequest.DONE) {
			console.log("TimeSlots received: \n" + JSON.stringify(JSON.parse(xhr.responseText).result, null, 2));
			tsList = JSON.parse(xhr.responseText).result;
			dateList = getSortedDates(tsList);
			timeList = getTimes(tsList);

			var optgroups = createOptgroupsDates(dateList);
			for (var j in optgroups) {
				daySel.appendChild(optgroups[j]);
			}
			var optgroups2 = createOptgroupsDates(dateList);
			for (var k in optgroups2) {
				dateToCloseSel.appendChild(optgroups2[k]);
			}
			for (var l in timeList) {
				var option = document.createElement("option");
				option.text = timeList[l];
				option.value = timeList[l];
				timeToCloseSel.add(option);
			}

		} else {
			console.log("Unable to load time slots.");
		}
	};

	var tsSel = $("#tsSel")[0];
	$("#tsSel").children().remove();

	var xhrMt = new XMLHttpRequest();
	xhrMt.open("GET", loadMeetingsURL + "?idCal=" + idCal, true);
	xhrMt.send();
	xhrMt.onloadend = function () {
		if (xhrMt.readyState == XMLHttpRequest.DONE) {
			console.log("Meetings received: \n" + JSON.stringify(JSON.parse(xhrMt.responseText).result, null, 2));
			mtList = JSON.parse(xhrMt.responseText).result;
		} else {
			console.log("Unable to load meetings.");
		}
	};
}

function deletePersonalCalendar() {
	data = {};
	var calSel = $("#calSel")[0];
	var idCal = calSel.value;
	data.idCal = idCal;
	var js = JSON.stringify(data);
	console.log("To delete calendar: \n" + JSON.stringify(JSON.parse(js), null, 2));

	var xhr = new XMLHttpRequest();
	xhr.open("POST", deletePersonalCalendarURL, true);
	xhr.send(js);
	xhr.onloadend = function () {
		if (xhr.readyState == XMLHttpRequest.DONE) {
			var result = JSON.parse(xhr.responseText).result;
			if (result == "Success") {
				$('#calDeletingSuccessModal').modal('show');
			} else {
				$('#calDeletingFailureModal').modal('show');
			}
			loadCalendarList();
			set_activeCal(null);
		}
	};
}

function addDayToCalendar() {
	data = {};
	data.operation = "add";
	data.idCal = activeCal.idCal;
	data.date = $("#inputDateToAdd")[0].value;
	var js = JSON.stringify(data);
	console.log("To add date: \n" + JSON.stringify(JSON.parse(js), null, 2));

	var xhr = new XMLHttpRequest();
	xhr.open("POST", addDeleteDayInCalendarURL, true);
	xhr.send(js);
	xhr.onloadend = function () {
		if (xhr.readyState == XMLHttpRequest.DONE) {
			var result = JSON.parse(xhr.responseText).result;
			if (result == "Success") {
				$('#addDayModal').modal('hide');
				$('#dayAddingSuccessModal').modal('show');
			} else {
				$('#dayAddingFailureModal').modal('show');
			}
			resetBtnStatus();
			loadCalendarList();
			loadPersonalCalendar(activeCal.idCal);
		}
	};
}

function deleteDayFromCalendar() {
	data = {};
	data.operation = "delete";
	data.idCal = activeCal.idCal;
	var daySel = $("#daySel")[0];
	data.date = daySel.value;
	var js = JSON.stringify(data);
	console.log("To delete date: \n" + JSON.stringify(JSON.parse(js), null, 2));

	var xhr = new XMLHttpRequest();
	xhr.open("POST", addDeleteDayInCalendarURL, true);
	xhr.send(js);
	xhr.onloadend = function () {
		if (xhr.readyState == XMLHttpRequest.DONE) {
			var result = JSON.parse(xhr.responseText).result;
			if (result == "Success") {
				$('#addDayModal').modal('hide');
				$('#dayAddingSuccessModal').modal('show');
			} else {
				$('#dayAddingFailureModal').modal('show');
			}
			resetBtnStatus();
			loadCalendarList();
			loadPersonalCalendar(activeCal.idCal);
		}
	};
}

function displaySchedule() {
	var optionsSelected = $("#daySel")[0].selectedOptions;
	var datesSelected = [];
	var tsSelected = [];
	var mtSelected = [];
	for (var iOpt = 0; iOpt < optionsSelected.length; iOpt++) {
		var date = optionsSelected[iOpt].value;
		var timeSlotsOfThisDay = findEntriesInJsonObjByKeyValue(tsList, 'date', date);
		var meetingsOfThisDay = [];
		for (var iTS = 0; iTS < timeSlotsOfThisDay.length; iTS++) {
			var meetingOfThisTS = findEntriesInJsonObjByKeyValue(mtList, 'idTS', timeSlotsOfThisDay[iTS].idTS)[0];
			meetingsOfThisDay.push(meetingOfThisTS);
		}
		datesSelected.push(date);
		tsSelected.push(timeSlotsOfThisDay);
		mtSelected.push(meetingsOfThisDay);
	}
	// console.log(datesSelected);
	// console.log(tsSelected);
	// console.log(mtSelected);

	$('#labelScheduleList')[0].innerHTML = minsPerSess + ' mins/session';

	var tsSel = $("#tsSel")[0];
	$("#tsSel").children().remove();
	var optgroups = createOptgroupsTimeSlots(datesSelected, tsSelected, mtSelected);
	for (var iOG in optgroups) {
		tsSel.appendChild(optgroups[iOG]);
	}
}

function closeTimeSlots() {
	data = {};
	data.idCal = activeCal.idCal;
	data.date = $("#dateToCloseSel")[0].value;
	if ($("#timeToCloseSel")[0].value == 'Whole Day') {
		data.time = $("#timeToCloseSel")[0].value;
	} else {
		data.time = TransTo24HourBase($("#timeToCloseSel")[0].value);
	}

	var dowList = [];
	$('.dayOfWeekToClose').each(function () {
		if (this.checked) {
			dowList.push(this.value);
		}
	});
	data.dowList = dowList;
	var js = JSON.stringify(data);

	console.log("To close timeSlots: \n" + JSON.stringify(JSON.parse(js), null, 2));

	var xhr = new XMLHttpRequest();
	xhr.open("POST", closeTimeSlotsURL, true);
	xhr.send(js);
	xhr.onloadend = function () {
		if (xhr.readyState == XMLHttpRequest.DONE) {
			var result = JSON.parse(xhr.responseText).result;
			if (result == "Success") {
				$('#closeTimeSlotsModal').modal('hide');
				$('#dayAddingSuccessModal').modal('show');
			} else {
				$('#dayAddingFailureModal').modal('show');
			}
			resetBtnStatus();
			loadCalendarList();
			loadPersonalCalendar(activeCal.idCal);
		}
	};
}

function createMeeting() {
	data = {};
	data.operation = "add";
	data.idCal = activeCal.idCal;
	data.idTS = $('#tsSel')[0].value;
	data.title = $("#inputTitle")[0].value;
	data.location = $("#inputLocation")[0].value;
	data.participant = $("#inputParticipant")[0].value;
	var js = JSON.stringify(data);
	console.log("To create meeting: \n" + JSON.stringify(JSON.parse(js), null, 2));

	var xhr = new XMLHttpRequest();
	xhr.open("POST", addCancelMeetingURL, true);
	xhr.send(js);
	xhr.onloadend = function () {
		if (xhr.readyState == XMLHttpRequest.DONE) {
			var result = JSON.parse(xhr.responseText).result;
			if (result == "Success") {
				$('#newMeetingModal').modal('hide');
				$('#dayAddingSuccessModal').modal('show');
			} else {
				$('#dayAddingFailureModal').modal('show');
			}
			resetBtnStatus();
			loadCalendarList();
			loadPersonalCalendar(activeCal.idCal);
		}
	};
}

function cancelMeeting() {
	data = {};
	data.operation = "delete";
	data.idCal = activeCal.idCal;
	data.idTS = $('#tsSel')[0].value;
	data.title = $("#inputTitle")[0].value;
	data.location = $("#inputLocation")[0].value;
	data.participant = $("#inputParticipant")[0].value;
	var js = JSON.stringify(data);
	console.log("To create meeting: \n" + JSON.stringify(JSON.parse(js), null, 2));

	var xhr = new XMLHttpRequest();
	xhr.open("POST", addCancelMeetingURL, true);
	xhr.send(js);
	xhr.onloadend = function () {
		if (xhr.readyState == XMLHttpRequest.DONE) {
			var result = JSON.parse(xhr.responseText).result;
			if (result == "Success") {
				$('#addDayModal').modal('hide');
				$('#dayAddingSuccessModal').modal('show');
			} else {
				$('#dayAddingFailureModal').modal('show');
			}
			resetBtnStatus();
			loadCalendarList();
			loadPersonalCalendar(activeCal.idCal);
		}
	};
}



// Helper Function
//-------------------------------------------------------------------------------------------------
function findEntriesInJsonObjByKeyValue(objList, keyMatch, valMatch) {
	var jo = [];
	for (var i = 0; i < objList.length; i++) {
		if (objList[i][keyMatch] == valMatch) {
			jo.push(objList[i]);
		}
	}
	return jo;
}

function getSortedDates(timeslotList) {
	var dateSet = new Set();
	for (var i in timeslotList) {
		dateSet.add(timeslotList[i].date);
	}
	return Array.from(dateSet).sort();
}

function getTimes(timeslotList) {
	var timeSet = new Set();
	for (var i in timeslotList) {
		timeSet.add(timeslotList[i].time);
	}
	return Array.from(timeSet);
}

function createOptgroupsDates(dateList) {
	var optgroups = [];
	var optgroup;
	var previousMonth;

	for (var i in dateList) {
		var date = dateList[i];
		var currentMonth = date.substring(0, 7);

		var option = document.createElement("option");
		option.text = date;
		option.value = date;

		if (i == 0) {
			optgroup = document.createElement("optgroup");
			optgroup.setAttribute("label", currentMonth);
		}

		if (currentMonth != previousMonth && i != 0) {
			optgroups.push(optgroup);
			optgroup = document.createElement("optgroup");
			optgroup.setAttribute("label", currentMonth);
		}

		optgroup.appendChild(option);

		if (i == (dateList.length - 1)) {
			optgroups.push(optgroup);
		}
		previousMonth = currentMonth;
	}
	return optgroups;
}

function createOptgroupsTimeSlots(dateList, tsList, mtList) {
	var optgroups = [];
	var optgroup;
	var previousMonth;

	for (var iDate in dateList) {
		var date = dateList[iDate];
		var timeSlots = tsList[iDate];
		var meetings = mtList[iDate];

		optgroup = document.createElement("optgroup");
		optgroup.setAttribute("label", date);
		optgroup.setAttribute("label", date);

		for (var iTS in timeSlots) {
			var text = '';
			var timeSlot = timeSlots[iTS];
			var meeting = meetings[iTS];
			text += timeSlot.time.substring(0, 5);
			text += ' ';
			text += timeSlot.time.substring(timeSlot.time.length - 2);
			if (timeSlot.closed == 0) {
				text += ' O ';
			} else {
				text += ' C ';
			}
			if (meetings[iTS]) {
				meeting = meetings[iTS];
				text += meeting.title;
			}

			var option = document.createElement("option");
			option.text = text;
			option.value = timeSlot.idTS;
			optgroup.appendChild(option);
		}
		optgroups.push(optgroup);
	}
	return optgroups;
}

function setCloseTimeSlotsSubmitBtn() {
	var numDefaults = 0;
	$('.closeTSSel').each(function () {
		if (this.value != 'placeholder') {
			numDefaults += 1;
		}
	});
	if (numDefaults == 2) {
		$('#btnSubmitCloseTimeSlots').prop('disabled', false);
	} else {
		$('#btnSubmitCloseTimeSlots').prop('disabled', true);
	}
}

function TransTo24HourBase(timeString) {
	if (timeString.substring(timeString.length - 2) == 'AM') {
		return timeString.substring(0, 8);
	} else {
		var hr = parseInt(timeString.substring(0, 2)) + 12;
		var time = hr.toString() + timeString.substring(2, 8);
		return time;
	}
}

function resetBtnStatus () {
	$('#btnLPC').prop('disabled', true);
	$('#btnDPC').prop('disabled', true);

	$('#btnAddDay').prop('disabled', true);
	$('#btnDeleteDay').prop('disabled', true);
	$('#btnCloseTimeSlots').prop('disabled', true);

	$('#btnNewMeeting').prop('disabled', true);
	$('#btnEditMeeting').prop('disabled', true);
	$('#btnCancelMeeting').prop('disabled', true);
	
	$('#btnSubmitCloseTimeSlots').prop('disabled', true);
	$('#btnSubmitNewMeeting').prop('disabled', true);
}