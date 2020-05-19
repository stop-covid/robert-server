package fr.gouv.stopc.robert.server.batch.listener.step;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import fr.gouv.stopc.robert.server.batch.component.ContactsProcessingCounter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContactsProcessingStepExecutionListener implements StepExecutionListener {

	private static final ContactsProcessingCounter INSTANCE = ContactsProcessingCounter.getInstance();

	@Override
	public void beforeStep(StepExecution stepExecution) {

		// Start of the step
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {

		final Instant endTime = Instant.now();
		final Date startTime = stepExecution.getStartTime();
		final long timeElapsed = Duration.between(startTime.toInstant(), endTime).getSeconds();

		log.info("Step execution duration = {}s", timeElapsed);

		log.info("Average execution duration for processing a chunk = {}s", timeElapsed / INSTANCE.getNumberOfChunks());

		if (INSTANCE.getNumberOfProcessedContacts() != 0) {
			log.info("Average execution duration for processing a contact = {}ms",
					 timeElapsed * 1000 / INSTANCE.getNumberOfProcessedContacts());
		}

		return null;
	}

}
