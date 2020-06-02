package fr.gouv.stopc.robert.cockpit.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import fr.gouv.stopc.robert.cockpit.dto.StopCovidKpi;
import fr.gouv.stopc.robert.cockpit.exception.UnknownKpiFormatException;
import fr.gouv.stopc.robert.cockpit.service.IStopCovidKpiGenerationService;
import lombok.extern.slf4j.Slf4j;

/**
 * Endpoint definition for Kpi generation
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cockpit")
public class RobertCockpitKpiController {

	/**
	 * Generation service
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private IStopCovidKpiGenerationService generator;

	private List<String> supportedFormats = Arrays.asList("json", "csv");

	/**
	 * Spring injection constructor
	 * 
	 * @param generators the map of all services available for Kpi generation
	 * @since 0.0.1-SNAPSHOT
	 */
	public RobertCockpitKpiController(IStopCovidKpiGenerationService generator) {
		this.generator = generator;
	}

	/**
	 * Generates the Kpis for StopCovid on a period
	 * 
	 * @param fromDate start date of the period
	 * @param toDate   end date of the period
	 * @param format   output format
	 * @param response injected <code>HttpServletResponse</code>. The content is
	 *                 written in it
	 * @throws UnknownKpiFormatException the wanted output format is not handled by
	 *                                   robert-cockpit
	 * @since 0.0.1-SNAPSHOT
	 */
	@GetMapping("/kpi")
	@RolesAllowed("${robert.cockpit.authorized-roles}")
	public void getKpi(
			@RequestParam(name = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(name = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam("format") String format, HttpServletResponse response) throws UnknownKpiFormatException {

		if (!supportedFormats.contains(format)) {
			throw new UnknownKpiFormatException("Unsupported output format " + format);
		}

		List<StopCovidKpi> kpis = generator.computeKpis(fromDate, toDate);

		try {
			if ("csv".equals(StringUtils.lowerCase(format))) {
				// set file name and content type
				String filename = "stopcovid-kpi.csv";

				response.setContentType("text/csv");
				response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

				// create a csv writer
				StatefulBeanToCsv<StopCovidKpi> writer = new StatefulBeanToCsvBuilder<StopCovidKpi>(
						response.getWriter()).withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
								.withSeparator(CSVWriter.DEFAULT_SEPARATOR).withOrderedResults(true).build();
				writer.write(kpis);
			} else if ("json".equals(StringUtils.lowerCase(format))) {
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.setCharacterEncoding("UTF-8");
				// Map the object into json & write it into the response
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(response.getWriter(), kpis);
			}
		} catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			log.error("Failed to generate the CSV content", e);
		}
	}

}
