package com.agnext.atdformatter.service;

import com.agnext.atdformatter.model.AttendanceDetails;
import com.agnext.atdformatter.model.DayDetails;
import com.agnext.atdformatter.model.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@Slf4j
public class AttendanceFormatter {

	private final ResourceLoader resourceLoader;
	private final ExcelService excelService;

	public AttendanceFormatter(ResourceLoader resourceLoader, ExcelService excelService) {
		this.resourceLoader = resourceLoader;
		this.excelService = excelService;
	}

	public void readAndParseDataFile(String fileName) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			String baseDir = System.getProperty("user.dir");
			File file = new File(baseDir+ FileSystems.getDefault().getSeparator()+fileName);
			Document document = db.parse(file);
			document.getDocumentElement().normalize();
			log.info(document.getDocumentElement().getNodeName());
			NodeList group = document.getElementsByTagName("Group");
			Stream<Node> nodeStream = IntStream.range(0, group.getLength()).mapToObj(group::item);
			List<List<AttendanceDetails>> mergedAttendanceList = nodeStream.map(node -> {
				List<AttendanceDetails> attendanceDetailsList = null;
				if (node instanceof Element) {
					Element element = (Element) node;
					NodeList level2List = element.getElementsByTagName("Group");
					Stream<Node> level2Stream = IntStream.range(0, level2List.getLength()).mapToObj(level2List::item);
					attendanceDetailsList = level2Stream.map(iteration -> {
						AttendanceDetails.AttendanceDetailsBuilder builder = AttendanceDetails.builder();
						if (iteration instanceof Element) {
							Element l2Element = (Element) iteration;
							NodeList section = l2Element.getElementsByTagName("Section");
							Stream<Node> sectionStream =
											IntStream.range(0, section.getLength()).mapToObj(section::item);
							sectionStream.forEach(sectionItem -> {
								if (sectionItem instanceof Element) {
									Element sectionElement = (Element) sectionItem;
									String sectionNumber = sectionElement.getAttribute("SectionNumber");
									log.info("processing section number :{}", sectionNumber);
									if (StringUtils.equalsIgnoreCase(sectionNumber, "0")) {
										processSectionOne(sectionElement, builder);
									} else if (StringUtils.equalsIgnoreCase(sectionNumber, "1")) {
										processSectionTwo(sectionElement, builder);
									}
								}
							});
						}
						return builder.build();
					}).filter(Objects::nonNull).collect(Collectors.toList());
				}
				return attendanceDetailsList;
			}).filter(Objects::nonNull).collect(Collectors.toList());
			List<AttendanceDetails> attendaceList =
							mergedAttendanceList.stream().flatMap(Collection::stream).collect(Collectors.toList());
			log.info("Total Attendance List : {}",attendaceList.size());
			excelService.createExcelFile(attendaceList);

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	private void processSectionOne(Element element, AttendanceDetails.AttendanceDetailsBuilder builder) {
		NodeList fieldNodeList = element.getElementsByTagName("Field");
		Stream<Node> fieldStream = IntStream.range(0, fieldNodeList.getLength()).mapToObj(fieldNodeList::item);
		fieldStream.forEach(field -> {
			processFieldAttributes(field, builder);
		});

	}

	private void processFieldAttributes(Node field, AttendanceDetails.AttendanceDetailsBuilder builder) {
		if(field instanceof Element){
			Element fieldElement = (Element) field;
			String key = fieldElement.getAttribute("Name");
			String value = fieldElement.getFirstChild().getTextContent();
			log.info("the key is :{} and value is  : {}",key,value);
			switch (key){
				case "UserID1":
					builder.empId(value);
					break;
				case "Name1":
					builder.name(value);
					break;
				case "DSGName1":
					builder.designation(value);
					break;
				case "PRDays1":
					builder.presentDays(value);
					break;
				case "ABDays1":
					builder.absentDays(value);
					break;
				case "WODays1":
					builder.weekOffDays(value);
					break;
				case "PHDays1":
					builder.publicHolidays(value);
					break;
				case "PLDays1":
					builder.paidLeaveDays(value);
					break;
				case "ULDays1":
					builder.unpaidLeaveDays(value);
					break;
				case "TRDays1":
					builder.trDays(value);
					break;
				default:
					log.info("Executing default, the key is :{} and value is  : {}",key,value);
			}
		}
	}

	private void processSectionTwo(Element element, AttendanceDetails.AttendanceDetailsBuilder builder) {
		Integer numberOfDaysInMonth = getNumberOfDaysInMonth(element, builder);
		setDayDetails(element, builder,numberOfDaysInMonth);
	}

	private void setDayDetails(Element element, AttendanceDetails.AttendanceDetailsBuilder builder,
					Integer numberOfDaysInMonth) {
		NodeList cellsGroupNodeList = element.getElementsByTagName("Cells");
		Node item = cellsGroupNodeList.item(0);
		Element cellsGroupItem = (Element) item;
		NodeList cellNodeList = cellsGroupItem.getElementsByTagName("Cell");
		Stream<Node> cellGroupStream = IntStream.rangeClosed(0, numberOfDaysInMonth).mapToObj(cellNodeList::item);
		List<DayDetails> dayDetailsList = cellGroupStream.map(cellIndex -> {
			DayDetails.DayDetailsBuilder detailsBuilder = DayDetails.builder();
			if (cellIndex instanceof Element) {
				Element cellElement = (Element) cellIndex;
				String columnNumber = cellElement.getAttribute("ColumnNumber");
				NodeList cellValueList = cellElement.getElementsByTagName("CellValue");
				Stream<Node> cellValueStream =
								IntStream.range(0, cellValueList.getLength()).mapToObj(cellValueList::item);
				detailsBuilder.cNo(columnNumber);
				detailsBuilder.day(String.valueOf(Integer.parseInt(columnNumber) + 1));
				cellValueStream.forEach(cellValue -> {
					Element cellValueElement = (Element) cellValue;
					String index = cellValueElement.getAttribute("Index");
					NodeList formattedValue = cellValueElement.getElementsByTagName("FormattedValue");
					if (formattedValue.getLength() > 0) {
						String status = cellValueElement.getFirstChild().getTextContent();
						log.info("ColumnNumber : {}, Index is :{} and attendanceStatus is :{}", columnNumber, index,
						         status);
						if (StringUtils.equalsIgnoreCase(index, "0")) {
							detailsBuilder.shiftType(status);
						} else if (StringUtils.equalsIgnoreCase(index, "1")) {
							detailsBuilder.fphase(status);
						} else if (StringUtils.equalsIgnoreCase(index, "2")) {
							detailsBuilder.sphase(status);
						}
					}
				});
			}
			return detailsBuilder.build();
		}).filter(Objects::nonNull).collect(Collectors.toList());
		builder.dayDetailsList(dayDetailsList);
	}

	private Integer getNumberOfDaysInMonth(Element element, AttendanceDetails.AttendanceDetailsBuilder builder) {
		NodeList colGroup = element.getElementsByTagName("ColumnGroup");
		Element colItem = (Element) colGroup.item(1);
		NodeList columnTotal = colItem.getElementsByTagName("ColumnTotal");
		Integer numberOfDays = 31;
		if(columnTotal != null){
			log.info("total days :{}",columnTotal.getLength());
			builder.noOfDays(String.valueOf(columnTotal.getLength()));
			numberOfDays = columnTotal.getLength();
		}
		return numberOfDays;
	}


}
