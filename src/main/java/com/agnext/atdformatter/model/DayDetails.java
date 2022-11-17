package com.agnext.atdformatter.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DayDetails {

	private String cNo;
	private String day;
	private String fphase;
	private String sphase;
	private String shiftType;

}
