package fr.gouv.stopc.robert.cockpit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import fr.gouv.stopc.robert.cockpit.client.RobertServerClient;
import fr.gouv.stopc.robert.cockpit.client.SubmissionCodeServerClient;
import fr.gouv.stopc.robert.cockpit.dto.StopCovidKpi;
import fr.gouv.stopc.robert.cockpit.service.IEndPointKpiService;
import fr.gouv.stopc.robert.cockpit.service.IStopCovidKpiGenerationService;

/**
 * 
 * @author plant-stopcovid
 *
 */
@SpringBootTest
class RobertCockpitApplicationTests {

	@Autowired
	private IStopCovidKpiGenerationService service;

	@MockBean
	private RobertServerClient robertServerClient;

	@MockBean
	private SubmissionCodeServerClient submissionCodeServerClient;

	@MockBean
	private IEndPointKpiService endointKpiService;

	@Autowired
	private StopCovidKpiFactory kpiFactory;

	@Test
	public void givenDayRange_whenComputeKpis_thenReturnStopCovidKpi() {
		// given
		LocalDate fromDate = LocalDate.now();
		fromDate.withDayOfMonth(1);
		LocalDate toDate = fromDate;
		// when
		Mockito.when(submissionCodeServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateScsDayRangeKpi(fromDate));
		Mockito.when(robertServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateRsDayRangeKpi(fromDate));
		Mockito.when(endointKpiService.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateDayRangeSupervisionKpi(fromDate));

		List<StopCovidKpi> kpis = service.computeKpis(fromDate, toDate);
		// then
		assertEquals(1, kpis.size());
	}

	@Test
	public void givenWeekRange_whenComputeKpis_thenReturnStopCovidKpi() {
		// given
		LocalDate fromDate = LocalDate.now();
		fromDate.withDayOfMonth(1);
		LocalDate toDate = fromDate.plusWeeks(1L);
		// when
		Mockito.when(submissionCodeServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateScsWeekRangeKpi(fromDate, toDate));
		Mockito.when(robertServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateRsWeekRangeKpi(fromDate, toDate));
		Mockito.when(endointKpiService.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateWeekRangeSupervisionKpi(fromDate, toDate));

		List<StopCovidKpi> kpis = service.computeKpis(fromDate, toDate);
		// then
		assertEquals(7, kpis.size());
	}

	@Test
	public void givenMonthRange_whenComputeKpis_thenReturnStopCovidKpi() {
		// given
		LocalDate fromDate = LocalDate.now();
		fromDate.withDayOfMonth(1);
		LocalDate toDate = fromDate.plusMonths(1L);
		// when
		Mockito.when(submissionCodeServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateScsMonthRangeKpi(fromDate, toDate));
		Mockito.when(robertServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateRsMonthRangeKpi(fromDate, toDate));
		Mockito.when(endointKpiService.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateMonthRangeSupervisionKpi(fromDate, toDate));
		List<StopCovidKpi> kpis = service.computeKpis(fromDate, toDate);
		// then
		assertEquals(ChronoUnit.DAYS.between(fromDate, toDate), kpis.size());
	}

	@Test
	public void givenWeekRangeMissingDays_whenComputeKpis_thenReturnStopCovidKpi() {
		// given
		LocalDate fromDate = LocalDate.now();
		fromDate.withDayOfMonth(1);
		LocalDate toDate = fromDate.plusWeeks(1L);
		// when
		Mockito.when(submissionCodeServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateScsWeekRangeMissingDaysKpi(fromDate, toDate));
		Mockito.when(robertServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateRsWeekRangeMissingDaysKpi(fromDate, toDate));
		Mockito.when(endointKpiService.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateSupervisionWeekRangeMissingDaysKpi(fromDate, toDate));

		List<StopCovidKpi> kpis = service.computeKpis(fromDate, toDate);
		// then
		assertEquals(5, kpis.size());
	}

	@Test
	public void givenDayRangeMissingScsKpi_whenComputeKpis_thenReturnStopCovidKpi() throws Exception {
		// given
		LocalDate fromDate = LocalDate.now();
		fromDate.withDayOfMonth(1);
		LocalDate toDate = fromDate;
		// when
		Mockito.when(submissionCodeServerClient.getKpi(fromDate, toDate)).thenReturn(new ArrayList<>());
		Mockito.when(robertServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateRsDayRangeKpi(fromDate));
		Mockito.when(endointKpiService.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateDayRangeSupervisionKpi(fromDate));

		List<StopCovidKpi> kpis = service.computeKpis(fromDate, toDate);

		// then
		int nbValidKpi = checkStopCovidKpiNullFields(kpis,
				Arrays.asList("nbLongCodesUsed", "nbShortCodesUsed", "nbExpiredCodes"));
		assertEquals(1, kpis.size());
		assertEquals(1, nbValidKpi);
	}

	@Test
	public void givenDayRangeMissingRsKpi_whenComputeKpis_thenReturnStopCovidKpi() throws Exception {
		// given
		LocalDate fromDate = LocalDate.now();
		fromDate.withDayOfMonth(1);
		LocalDate toDate = fromDate;
		// when
		Mockito.when(submissionCodeServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateScsDayRangeKpi(fromDate));
		Mockito.when(robertServerClient.getKpi(fromDate, toDate)).thenReturn(new ArrayList<>());
		Mockito.when(endointKpiService.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateDayRangeSupervisionKpi(fromDate));

		List<StopCovidKpi> kpis = service.computeKpis(fromDate, toDate);

		// then
		int nbValidKpi = checkStopCovidKpiNullFields(kpis, Arrays.asList("nbAlertedUsers", "nbExposedUsers"));
		assertEquals(1, kpis.size());
		assertEquals(1, nbValidKpi);
	}

	@Test
	public void givenDayRangeMissingSupervisionKpi_whenComputeKpis_thenReturnStopCovidKpi() throws Exception {

		// given
		LocalDate fromDate = LocalDate.now();
		fromDate.withDayOfMonth(1);
		LocalDate toDate = fromDate;
		// when
		Mockito.when(submissionCodeServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateScsDayRangeKpi(fromDate));
		Mockito.when(robertServerClient.getKpi(fromDate, toDate))
				.thenReturn(kpiFactory.generateRsDayRangeKpi(fromDate));
		Mockito.when(endointKpiService.getKpi(fromDate, toDate)).thenReturn(new ArrayList<>());

		List<StopCovidKpi> kpis = service.computeKpis(fromDate, toDate);

		// then
		int nbValidKpi = checkStopCovidKpiNullFields(kpis, Arrays.asList("nbRegisteredUsers", "nbUsersHavingLeftViaApp",
				"nbUsersHavingDeletedHistory", "nbContaminatedUsers", "nbActiveUsers"));
		assertEquals(1, kpis.size());
		assertEquals(1, nbValidKpi);
	}

	@Test
	public void givenDayRangeMissingAllKpi_whenComputeKpis_thenReturnNoKpi() {
		// given
		LocalDate fromDate = LocalDate.now();
		fromDate.withDayOfMonth(1);
		LocalDate toDate = fromDate;
		// when
		Mockito.when(submissionCodeServerClient.getKpi(fromDate, toDate)).thenReturn(new ArrayList<>());
		Mockito.when(robertServerClient.getKpi(fromDate, toDate)).thenReturn(new ArrayList<>());
		Mockito.when(endointKpiService.getKpi(fromDate, toDate)).thenReturn(new ArrayList<>());

		List<StopCovidKpi> kpis = service.computeKpis(fromDate, toDate);
		// then
		assertTrue(kpis.isEmpty());
	}

	/**
	 * 
	 * @param kpiToCheckList
	 * @param fieldNames
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private int checkStopCovidKpiNullFields(List<StopCovidKpi> kpiToCheckList, List<String> fieldNames)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		PropertyDescriptor[] propDescArr = Introspector.getBeanInfo(StopCovidKpi.class, Object.class)
				.getPropertyDescriptors();
		int result = 0;
		for (StopCovidKpi kpi : kpiToCheckList) {
			int nbNullFields = Arrays.stream(propDescArr).filter(prop -> fieldNames.contains(prop.getName()))
					.map(ThrowingFunction.unchecked(x -> x.getReadMethod().invoke(kpi)))
					.filter(fieldVal -> fieldVal == null).collect(Collectors.toList()).size();
			if (nbNullFields == fieldNames.size()) {
				result++;
			}
		}
		return result;
	}

}
