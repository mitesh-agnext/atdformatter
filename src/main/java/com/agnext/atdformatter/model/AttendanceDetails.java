package com.agnext.atdformatter.model;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class AttendanceDetails {

	private String name;
	private String empId;
	private String designation;
	private String presentDays;
	private String absentDays;
	private String weekOffDays;
	private String publicHolidays;
	private String paidLeaveDays;
	private String unpaidLeaveDays;
	private String trDays;
	private String  month;
	private String  date;
	private String noOfDays;
	private List<DayDetails> dayDetailsList;

}
