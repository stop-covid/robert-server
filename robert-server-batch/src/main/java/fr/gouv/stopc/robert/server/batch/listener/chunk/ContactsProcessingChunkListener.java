package fr.gouv.stopc.robert.server.batch.listener.chunk;

import java.time.Duration;
import java.time.Instant;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.scope.context.ChunkContext;

import fr.gouv.stopc.robert.server.batch.component.ContactsProcessingCounter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContactsProcessingChunkListener implements ChunkListener {

	private Instant startTime;

	private static final ContactsProcessingCounter INSTANCE = ContactsProcessingCounter.getInstance();

	@Override
	public void beforeChunk(ChunkContext chunkContext) {

		this.startTime = Instant.now();
	}

	@Override
	public void afterChunkError(ChunkContext context) {

		//
	}

	@Override
	@AfterChunk
	public void afterChunk(ChunkContext chunkContext) {

		final Instant endTime = Instant.now();
		final long duration = Duration.between(this.startTime, endTime).getSeconds();

		final int readCount = chunkContext.getStepContext().getStepExecution().getReadCount();

		INSTANCE.increaseNumberOfChunks();
		INSTANCE.increaseNumberOfProcessedContacts(readCount);

		log.info("Number of processed contacts per chunk = {}", INSTANCE.getNumberOfProcessedContacts());

		log.info("Chunk execution duration = {}s", duration);
	}

}
