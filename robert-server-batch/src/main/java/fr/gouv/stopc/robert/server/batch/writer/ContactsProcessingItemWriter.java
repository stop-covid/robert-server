package fr.gouv.stopc.robert.server.batch.writer;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.util.CollectionUtils;

import fr.gouv.stopc.robertserver.database.model.Registration;
import fr.gouv.stopc.robertserver.database.service.IRegistrationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ContactsProcessingItemWriter implements ItemWriter<Registration> {

	private final IRegistrationService registrationService;

	@SuppressWarnings("unchecked")
	@Override
	public void write(List<? extends Registration> items) throws Exception {

		if (!CollectionUtils.isEmpty(items)) {
			log.info("~~~~~ Record update started ~~~~~");

			this.registrationService.saveRegistrations((List<Registration>) items);
		}
	}

}
