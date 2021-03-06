package fr.gouv.stopc.robertserver.database.service;

import java.util.List;

import fr.gouv.stopc.robertserver.database.model.Contact;


public interface ContactService {

	void saveContacts(List<Contact> contacts);

	void delete(Contact contact);

	void deleteAll();

	List<Contact> findAll();

}
