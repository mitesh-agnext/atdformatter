package com.agnext.atdformatter.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ExcelService {

	public void createExcelFile(List<AttendanceDetails> attendaceList) {
		log.info("Writing {} records in Excel ", attendaceList.size());
		try{
			String property = System.getProperty("user.dir");
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("attendanceformat.xlsx");
			String time = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
			String path = property+ FileSystems.getDefault().getSeparator() + "output-"+ time + "-" + RandomStringUtils.randomAlphanumeric(4)+".xlsx";
			FileOutputStream out = new FileOutputStream(path);
			XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(inputStream);
			XSSFSheet attendanceSheet = workbook.getSheetAt(1);
			writeData(attendanceSheet, attendaceList,workbook);
			workbook.write(out);
			log.info("Complete creating file, sending data by Email");
			inputStream.close();
			workbook.close();
			//new ByteArrayInputStream(out.toByteArray());
		}catch (IOException ioException){
			log.error("Exception caught : {}", ioException.getMessage());

		}

	}

	private void writeData(XSSFSheet attendanceSheet, List<AttendanceDetails> attendaceList, XSSFWorkbook workbook) {
		//clearPreviousdataIfAny(attendanceSheet);
		int rowCount = 2;
		for (AttendanceDetails attendanceDetails : attendaceList) {
			int columnCount =0;
			Row row = attendanceSheet.createRow(++rowCount);

			Cell cell = row.createCell(columnCount);
			cell.setCellStyle(attendanceSheet.getColumnStyle(columnCount));
			cell.setCellValue(StringUtils.isNotBlank(attendanceDetails.getEmpId())?attendanceDetails.getEmpId():"NO_EMPID");

			cell = row.createCell(++columnCount);
			cell.setCellStyle(attendanceSheet.getColumnStyle(columnCount));
			cell.setCellValue(StringUtils.isNotBlank(attendanceDetails.getName())?attendanceDetails.getName():"NO_EMP_NAME");

			cell = row.createCell(++columnCount);
			cell.setCellStyle(attendanceSheet.getColumnStyle(columnCount));
			cell.setCellValue("");

			for (DayDetails dayDetails : attendanceDetails.getDayDetailsList()) {
				String dayStatus = getStatusFromDayDetails(dayDetails);
				cell = row.createCell(++columnCount);
				cell.setCellStyle(attendanceSheet.getColumnStyle(columnCount));
				cell.setCellValue(dayStatus);

			}
		}

	}

	private String getStatusFromDayDetails(DayDetails dayDetails) {
		String morning = dayDetails.getFphase();
		String evening = dayDetails.getSphase();
		String status = "";
		if(StringUtils.isBlank(morning) && StringUtils.isNotBlank(evening)){
			status = evening;
		}else if (StringUtils.isNotBlank(morning) && StringUtils.isBlank(evening)){
			status = morning;
		}else if(StringUtils.isNotBlank(morning) && StringUtils.isNotBlank(evening)){
			if(StringUtils.equalsIgnoreCase(morning,evening)){
				status = morning;
			}else{
				//status = "1/2"+morning+"+1/2"+evening;
				status = "\u00BD"+" "+morning+" "+"+\u00BD" + " "+evening;
			}
		}
		return status;
	}

}
