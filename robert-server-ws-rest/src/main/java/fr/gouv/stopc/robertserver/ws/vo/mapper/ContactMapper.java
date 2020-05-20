package fr.gouv.stopc.robertserver.ws.vo.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.internal.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import fr.gouv.stopc.robertserver.database.model.Contact;
import fr.gouv.stopc.robertserver.database.model.HelloMessageDetail;
import fr.gouv.stopc.robertserver.ws.proto.ProtoStorage.ContactProto;
import fr.gouv.stopc.robertserver.ws.proto.ProtoStorage.IdProto;
import fr.gouv.stopc.robertserver.ws.vo.DistinctiveHelloInfoWithinEpochForSameEBIDVo;
import fr.gouv.stopc.robertserver.ws.vo.GroupedHellosReportVo;

@Component
public class ContactMapper {


	public ContactMapper() {}

	public Optional<Contact> convertToEntity(GroupedHellosReportVo groupedHellosReportVo) {

		return Optional.ofNullable(groupedHellosReportVo).map(this::mapContact).orElse(Optional.empty());
	}

	public List<Contact> convertToEntity(List<GroupedHellosReportVo> groupedHellosReportVoList) {

		if (CollectionUtils.isEmpty(groupedHellosReportVoList)) {
			return Collections.emptyList();
		}

		List<Contact> contacts = new ArrayList<>();
		groupedHellosReportVoList.stream()
				.map(this::convertToEntity)
				.filter(item -> item.isPresent())
				.forEach(item -> contacts.add(item.get()));

		return contacts;

	}

	private HelloMessageDetail mapHelloMessageDetail(DistinctiveHelloInfoWithinEpochForSameEBIDVo dtoMessage) {
		return HelloMessageDetail.builder()
				.mac(Base64.decode(dtoMessage.getMac()))
				.rssiCalibrated(dtoMessage.getRssiCalibrated())
				.rssiRaw(dtoMessage.getRssiRaw())
				.timeCollectedOnDevice(dtoMessage.getTimeCollectedOnDevice())
				.timeFromHelloMessage(dtoMessage.getTimeFromHelloMessage())
				.build();
	}

	private Optional<Contact> mapContact(GroupedHellosReportVo contactVo) {
		List<HelloMessageDetail> messageDetails = contactVo.getIds()
				.stream()
				.map(this::mapHelloMessageDetail)
				.collect(Collectors.toList());

		return Optional.of(Contact.builder()
				.ecc(Base64.decode(contactVo.getEcc()))
				.ebid(Base64.decode(contactVo.getEbid()))
				.timeInsertion(System.currentTimeMillis())
				.messageDetails(messageDetails)
				.build());
	}

	/**
	 * Function mapping a list of <code>ContactProto</code> in Protobuf v3 format to
	 * a list of <code>Contact</code>.
	 * 
	 * @param contactsProtoList list of <code>ContactProto</code> to convert
	 * @return an empty list if contactProtoList is empty else a list of the same
	 *         size of <code>Contact</code>
	 */
	public List<Contact> convertContactsProtoToEntities(List<ContactProto> contactsProtoList) {
		if (CollectionUtils.isEmpty(contactsProtoList)) {
			return Collections.emptyList();
		}

		List<Contact> contacts = new ArrayList<>();
		contactsProtoList.stream().map(this::convertContactProtoToEntity).filter(item -> item.isPresent())
				.forEach(item -> contacts.add(item.get()));

		return contacts;
	}

	/**
	 * Function mapping a <code>ContactProto</code> in Protobuf v3 format to
	 * a <code>Contact</code>
	 * 
	 * @param contactProto <code>ContactProto</code> to convert
	 * @return an Optional <code>Contact</code> if contactProto is not null, else an empty optional
	 */
	public Optional<Contact> convertContactProtoToEntity(ContactProto contactProto) {
		return Optional.ofNullable(contactProto).map(this::mapContactProto).orElse(Optional.empty());
	}

	/**
	 * Function mapping a <code>ContactProto</code> in Protobuf v3 format to
	 * a <code>Contact</code>
	 * 
	 * @param contactProto <code>ContactProto</code> to convert
	 * @return an Optional <code>Contact</code>
	 */
	private Optional<Contact> mapContactProto(ContactProto contactProto) {
		List<HelloMessageDetail> messageDetails = contactProto.getIdsList().stream().map(this::mapIdProto)
				.collect(Collectors.toList());
		return Optional
				.of(Contact.builder().ecc(Base64.decode(contactProto.getEcc())).ebid(Base64.decode(contactProto.getEbid()))
						.timeInsertion(System.currentTimeMillis()).messageDetails(messageDetails).build());
	}

	/**
	 * Function mapping an <code>IdProto</code> in Protobuf v3 format to
	 * a <code>HelloMessageDetail</code>.
	 * 
	 * @param idproto <code>IdProto</code> to convert
	 * @return an Optional <code>HelloMessageDetail</code>
	 */
	private HelloMessageDetail mapIdProto(IdProto idproto) {
		return HelloMessageDetail.builder().mac(Base64.decode(idproto.getMac()))
				.rssiCalibrated(idproto.getRssiCalibrated()).rssiRaw(idproto.getRssiRaw())
				.timeCollectedOnDevice(idproto.getTimeCollectedOnDevice())
				.timeFromHelloMessage(idproto.getTimeFromHelloMessage()).build();
	}
}
