package test.fr.gouv.stopc.robertserver.batch.scoring;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import fr.gouv.stopc.robert.server.batch.RobertServerBatchApplication;
import fr.gouv.stopc.robert.server.batch.exception.RobertScoringException;
import fr.gouv.stopc.robert.server.batch.service.ScoringStrategyService;
import fr.gouv.stopc.robert.server.batch.vo.ScoringResult;
import fr.gouv.stopc.robertserver.database.model.Contact;
import fr.gouv.stopc.robertserver.database.model.HelloMessageDetail;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ContextConfiguration(classes = RobertServerBatchApplication.class)
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@SpringBootTest(properties = "robert.scoring.algo-version=2")
public class ScoringAlgorithmV2Test {

	@Autowired
	private ScoringStrategyService serviceScoring;

	@Test
	public void test_C4_20_A() {
		try {
			List<Contact> contacts = retrieveContacts("input_v2/C4_20_A");
			List<ScoringResult> risks = contacts.stream().map(contact -> {
				try {
					return serviceScoring.execute(contact);
				} catch (RobertScoringException e) {
					return null;
				}
			}).collect(Collectors.toList());

			// TODO vérifier la valeur des risks avec les résultats fournis par l'inria
			log.info("{}", risks);

		} catch (URISyntaxException | IllegalStateException | IOException | CsvException e) {
			fail(e);
		}
	}

	private List<Contact> retrieveContacts(String dir) throws URISyntaxException, IOException, CsvException{
		List<Contact> contacts = new ArrayList<>();
		for (File input : new File(getClass().getClassLoader().getResource(dir).toURI())
				.listFiles()) {
			// Read the current file corresponding to a contact
			CSVReader reader = new CSVReader(new FileReader(input));
			// Skip the header
			reader.skip(1);
			List<String[]> lines = reader.readAll();
			// Each line is a HelloMessageDetail
			List<HelloMessageDetail> hellos = lines.stream()
					.map(line -> HelloMessageDetail.builder().timeCollectedOnDevice(Long.parseLong(line[1].trim()))
							.timeFromHelloMessage(Integer.parseInt(line[2].trim()))
							.rssiCalibrated(Integer.parseInt(line[3].trim())).build())
					.collect(Collectors.toList());
			// Add the contact
			contacts.add(Contact.builder().messageDetails(hellos).build());
			reader.close();
		}
		
		return contacts;
	}
	
	@Test
	public void test_R1_AA() {
		try {
			List<Contact> contacts = retrieveContacts("input_v2/R1_AA");
			List<ScoringResult> risks = contacts.stream().map(contact -> {
				try {
					return serviceScoring.execute(contact);
				} catch (RobertScoringException e) {
					return null;
				}
			}).collect(Collectors.toList());

			// TODO vérifier la valeur des risks avec les résultats fournis par l'inria
			log.info("{}", risks);

		} catch (URISyntaxException | IllegalStateException | IOException | CsvException e) {
			fail(e);
		}
	}
}
