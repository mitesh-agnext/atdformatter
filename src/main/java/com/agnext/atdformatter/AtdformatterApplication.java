package com.agnext.atdformatter;

import com.agnext.atdformatter.service.AttendanceFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
@Slf4j
public class AtdformatterApplication implements CommandLineRunner {

	@Autowired AttendanceFormatter attendanceFormatter;

	public static void main(String[] args) {
		SpringApplication.run(AtdformatterApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Arrays.asList(args).stream().forEach(arguement -> {
			log.info("Arguement is {}", arguement);
		});
		String fileName = args[0];
		if(StringUtils.isBlank(fileName) || !(StringUtils.equalsIgnoreCase(fileName,"data.xml"))){
			throw new RuntimeException(
							"Filename is not present, please validate file name \"data.xml\" is present in the folder");
		}
		attendanceFormatter.readAndParseDataFile(fileName);
	}
}

