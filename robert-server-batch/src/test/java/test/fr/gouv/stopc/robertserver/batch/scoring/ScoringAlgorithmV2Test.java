package test.fr.gouv.stopc.robertserver.batch.scoring;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import fr.gouv.stopc.robert.server.batch.model.ScoringResult;
import fr.gouv.stopc.robertserver.database.model.Contact;
import fr.gouv.stopc.robertserver.database.model.HelloMessageDetail;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ContextConfiguration(classes = RobertServerBatchApplication.class)
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@SpringBootTest(properties = "robert.scoring.algo-version=2")
public class ScoringAlgorithmV2Test {

    @Autowired
    private ScoringStrategyService serviceScoring;

    @Test
    public void test_C4_20_A() {
        String directory = "input_v2/C4_20_A";

        List<ScoringResult> expectedOutput = new ArrayList<>();
        expectedOutput.add(ScoringResult.builder().rssiScore(0.09630785220984316).duration(8).nbContacts(4).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.2690889726550369).duration(5).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.9188468573271861).duration(8).nbContacts(9).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.7844933697841359).duration(5).nbContacts(6).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(1.0).duration(7).nbContacts(4).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.2997326807625254).duration(5).nbContacts(6).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.6303555467982143).duration(8).nbContacts(9).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.9855837469984042).duration(3).nbContacts(4).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.9138366325650362).duration(11).nbContacts(8).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.04812870906470155).duration(5).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.33181742683112014).duration(5).nbContacts(6).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.00816488366617391).duration(5).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.2239559701483656).duration(5).nbContacts(4).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.03085252260947431).duration(8).nbContacts(5).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.35600179586952296).duration(8).nbContacts(9).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.08357163231878248).duration(8).nbContacts(5).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.05340180729330277).duration(5).nbContacts(6).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.06288613265637359).duration(8).nbContacts(8).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(5).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.004579375650454545).duration(5).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.005463604437033377).duration(5).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.013839784038168795).duration(8).nbContacts(4).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.03434945856404308).duration(8).nbContacts(5).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0034766556990293383).duration(5).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(5).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.021166037239308474).duration(5).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(5).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.15123076921114512).duration(5).nbContacts(6).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.07489472684820186).duration(5).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.003987421872574704).duration(8).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.10351257149981881).duration(8).nbContacts(6).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.11425948507049816).duration(5).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.01346236131837855).duration(9).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.5783631059573081).duration(14).nbContacts(7).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.6041103800846415).duration(0).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.1299230513316064).duration(8).nbContacts(5).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(7).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(5).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.2795079862859789).duration(3).nbContacts(4).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(3.57089552255591E-4).duration(8).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(5).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(5).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(5).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(5).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(5).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(1.0).duration(5).nbContacts(6).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(1.0).duration(8).nbContacts(9).build());

        launchTestsOnDirectoryAndExpectOutput(directory, expectedOutput, 0.07562234455563899);
    }

    private List<Contact> retrieveContacts(String dir) throws URISyntaxException, IOException, CsvException {
        List<Contact> contacts = new ArrayList<>();
        File[] files = new File(getClass().getClassLoader().getResource(dir).toURI()).listFiles();
        List<File> sortedFiles2 = Arrays.asList(files).stream().sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
        for (File input : sortedFiles2) {
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
        String directory = "input_v2/R1_AA";

        List<ScoringResult> expectedOutput = new ArrayList<>();

        expectedOutput.add(ScoringResult.builder().rssiScore(0.18828925374707017).duration(3).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.03527469926198253).duration(12).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.08875714805601603).duration(12).nbContacts(8).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.8628472789731441).duration(3).nbContacts(3).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.07728305636399849).duration(12).nbContacts(6).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.3094484841531).duration(3).nbContacts(4).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.2759959337276156).duration(12).nbContacts(8).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.065830213258381).duration(3).nbContacts(3).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.8627136335858346).duration(3).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.22794883273013583).duration(12).nbContacts(5).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0022868985596177145).duration(12).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.06765771238586098).duration(3).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.2331092423516272).duration(3).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.09511102638316932).duration(12).nbContacts(7).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.11003612037813724).duration(12).nbContacts(8).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.12966931134665252).duration(3).nbContacts(3).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(12).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(2).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.025627922648303533).duration(12).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.013510086059469451).duration(3).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.005032878893234789).duration(12).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.06361366597976946).duration(3).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.06112885641933592).duration(12).nbContacts(4).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.019138739751979165).duration(3).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.11231542520601334).duration(12).nbContacts(10).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.397698695411321).duration(3).nbContacts(3).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.01384682276973418).duration(3).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(12).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(2).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(12).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.02567946235608333).duration(12).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(3).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(9).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(11).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.4447715000302811).duration(3).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(12).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0018191639131148627).duration(3).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.004027990137875511).duration(10).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(1).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.025992726067021722).duration(3).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(12).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.7463816319843944).duration(3).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(12).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.020928691888089504).duration(12).nbContacts(1).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(3).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(9).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(3).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(8).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(12).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(3).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(4).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(11).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(0).nbContacts(0).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.20528318167146567).duration(1).nbContacts(2).build());
        expectedOutput.add(ScoringResult.builder().rssiScore(0.0).duration(1).nbContacts(0).build());

        launchTestsOnDirectoryAndExpectOutput(directory, expectedOutput, 0.03991493973736204);
    }

    private void launchTestsOnDirectoryAndExpectOutput(String directory,
                                                       List<ScoringResult> expectedOutput,
                                                       double expectedFinalRisk) {
        log.info("Launching test on directory {}", directory);

        try {
            List<Contact> contacts = retrieveContacts(directory);
            List<ScoringResult> risks = contacts.stream().map(contact -> {
                try {
                    return serviceScoring.execute(contact);
                } catch (RobertScoringException e) {
                    return null;
                }
            }).collect(Collectors.toList());
            assertTrue(Arrays.equals(expectedOutput.toArray(), risks.toArray()));
            assertEquals(expectedFinalRisk,
                    this.serviceScoring.aggregate(risks
                            .stream()
                            .map(a -> a.getRssiScore())
                            .collect(Collectors.toList())));
        } catch (URISyntaxException | IllegalStateException | IOException | CsvException e) {
            fail(e);
        }
    }
}
