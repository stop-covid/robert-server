package fr.gouv.stopc.robertserver.ws.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.gouv.stopc.robertserver.database.model.Contact;
import fr.gouv.stopc.robertserver.database.service.ContactService;
import fr.gouv.stopc.robertserver.ws.exception.RobertServerException;
import fr.gouv.stopc.robertserver.ws.proto.ProtoStorage.ContactProto;
import fr.gouv.stopc.robertserver.ws.service.ContactDtoService;
import fr.gouv.stopc.robertserver.ws.utils.MessageConstants;
import fr.gouv.stopc.robertserver.ws.vo.GroupedHellosReportVo;
import fr.gouv.stopc.robertserver.ws.vo.mapper.ContactMapper;

@Service
public class ContactDtoServiceImpl implements ContactDtoService {

	private final ContactService contactService;

	private final ContactMapper contactMapper;

	/**
	 * Spring injection constructor
	 * 
	 * @param contactService the <code>ContactService</code> bean to inject
	 * @param contactMapper  the <code>ContactMapper</code> bean to inject
	 */
	public ContactDtoServiceImpl(ContactService contactService, ContactMapper contactMapper) {

		this.contactService = contactService;
		this.contactMapper = contactMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveContacts(List<GroupedHellosReportVo> contactVoList) throws RobertServerException {

		try {
			List<Contact> contacts = contactMapper.convertToEntity(contactVoList);

			contactService.saveContacts(contacts);
		} catch (Exception e) {

			throw new RobertServerException(MessageConstants.ERROR_OCCURED, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveProtoContacts(List<ContactProto> contactProtoList) throws RobertServerException {
		try {
			List<Contact> contacts = contactMapper.convertContactsProtoToEntities(contactProtoList);

			contactService.saveContacts(contacts);
		} catch (Exception e) {

			throw new RobertServerException(MessageConstants.ERROR_OCCURED, e);
		}
	}

}
