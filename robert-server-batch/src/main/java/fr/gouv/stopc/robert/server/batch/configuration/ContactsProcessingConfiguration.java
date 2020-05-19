package fr.gouv.stopc.robert.server.batch.configuration;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;

import fr.gouv.stopc.robert.crypto.grpc.server.client.service.ICryptoServerGrpcClient;
import fr.gouv.stopc.robert.server.batch.listener.chunk.ContactsProcessingChunkListener;
import fr.gouv.stopc.robert.server.batch.listener.step.ContactsProcessingStepExecutionListener;
import fr.gouv.stopc.robert.server.batch.processor.ContactProcessor;
import fr.gouv.stopc.robert.server.batch.reader.CustomMongoItemReader;
import fr.gouv.stopc.robert.server.batch.service.ScoringStrategyService;
import fr.gouv.stopc.robert.server.batch.writer.ContactsProcessingItemWriter;
import fr.gouv.stopc.robert.server.common.service.IServerConfigurationService;
import fr.gouv.stopc.robertserver.database.model.Contact;
import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.service.ContactService;
import fr.gouv.stopc.robertserver.database.service.IRegistrationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class ContactsProcessingConfiguration {

	private final IServerConfigurationService serverConfigurationService;

	private final IRegistrationService registrationService;

	private final ContactService contactService;

	private final ScoringStrategyService scoringStrategyService;

	private final ICryptoServerGrpcClient cryptoServerClient;

	public static final int CHUNK_SIZE = 1000;

	private static final int GRID_SIZE = 10;

	@Inject
	public ContactsProcessingConfiguration(final IServerConfigurationService serverConfigurationService,
										   final IRegistrationService registrationService,
										   final ContactService contactService,
										   final ICryptoServerGrpcClient cryptoServerClient,
										   final ScoringStrategyService scoringStrategyService) {

		this.serverConfigurationService = serverConfigurationService;
		this.registrationService = registrationService;
		this.contactService = contactService;
		this.cryptoServerClient = cryptoServerClient;
		this.scoringStrategyService = scoringStrategyService;
	}

	@Bean
	public Job readReport(JobBuilderFactory jobBuilderFactory, Step step) {

		return jobBuilderFactory.get("processContacts").flow(step).end().build();
	}

	@Bean
	public Step step(StepBuilderFactory stepBuilderFactory, MongoItemReader<Contact> mongoItemReader) {

		return stepBuilderFactory.get("partitionStep")
								 .partitioner("slaveStep", partitioner())
								 .partitionHandler(partitionHandler(stepBuilderFactory, mongoItemReader))
								 .build();
	}

	@Bean
	public Partitioner partitioner() {

		return new CustomPartitioner();
	}

	@Bean
	public PartitionHandler partitionHandler(StepBuilderFactory stepBuilderFactory,
			MongoItemReader<Contact> mongoItemReader) {

		TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
		handler.setGridSize(GRID_SIZE);
		handler.setTaskExecutor(taskExecutor());
		handler.setStep(slaveStep(stepBuilderFactory, mongoItemReader));

		try {
			handler.afterPropertiesSet();
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return handler;
	}

	@Bean
	public TaskExecutor taskExecutor() {

		return new SimpleAsyncTaskExecutor();
	}

	@Bean
	public Step slaveStep(StepBuilderFactory stepBuilderFactory, MongoItemReader<Contact> mongoItemReader) {

		return stepBuilderFactory.get("slaveStep")
								 .<Contact, Registration>chunk(CHUNK_SIZE)
								 .reader(mongoItemReader)
								 .processor(contactsProcessor())
								 .writer(writer())
								 .listener(stepExecutionListener())
								 .listener(chunkListener())
								 .build();
	}

	// TODO: Configurer un second step pour vider la table CONTACTS_TO_PROCESS.

	@Bean
	@StepScope
	public MongoItemReader<Contact> mongoItemReader(MongoTemplate mongoTemplate,
			@Value("#{stepExecutionContext[name]}") final String name,
			@Value("#{stepExecutionContext[start]}") final int start,
			@Value("#{stepExecutionContext[end]}") final int end) {

		log.info("{} currently reading from {} to {}", name, start, end);

		CustomMongoItemReader<Contact> reader = new CustomMongoItemReader<>();
		reader.setTemplate(mongoTemplate);
		reader.setSort(initSorts());
		reader.setTargetType(Contact.class);
		reader.setQuery("{}");
		reader.setPage(start);
		reader.setPageSize(end);

		return reader;
	}

	@Bean
	@StepScope
	public ItemWriter<Registration> writer() {

		return new ContactsProcessingItemWriter(this.registrationService);
	}

	@Bean
	@StepScope
	public ItemProcessor<Contact, Registration> contactsProcessor() {

		return new ContactProcessor(this.serverConfigurationService, this.registrationService, this.contactService,
									this.cryptoServerClient, this.scoringStrategyService);
	}

	@Bean
	@StepScope
	public StepExecutionListener stepExecutionListener() {

		return new ContactsProcessingStepExecutionListener();
	}

	@Bean
	@StepScope
	public ChunkListener chunkListener() {

		return new ContactsProcessingChunkListener();
	}

	private Map<String, Sort.Direction> initSorts() {

		Map<String, Sort.Direction> sorts = new HashMap<>();
		sorts.put("_id", Direction.DESC);

		return sorts;
	}

}
