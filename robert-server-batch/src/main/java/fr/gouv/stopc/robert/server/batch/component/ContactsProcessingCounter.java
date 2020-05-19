package fr.gouv.stopc.robert.server.batch.component;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Scope("singleton")
public class ContactsProcessingCounter {

	private static ContactsProcessingCounter instance;

	@Getter
	public int numberOfChunks = 0;

	@Getter
	public int numberOfProcessedContacts = 0;

	private ContactsProcessingCounter() {

	}

	public static ContactsProcessingCounter getInstance() {

		if (instance == null) {
			instance = new ContactsProcessingCounter();
		}

		return instance;
	}

	public void increaseNumberOfChunks() {

		this.numberOfChunks += 1;
	}

	public void increaseNumberOfProcessedContacts(int numberOfProcessedContacts) {

		this.numberOfProcessedContacts += numberOfProcessedContacts;
	}

}
