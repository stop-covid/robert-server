package fr.gouv.stopc.robertserver.ws.service;

import java.util.List;

import javax.validation.constraints.NotNull;

import fr.gouv.stopc.robertserver.ws.exception.RobertServerException;
import fr.gouv.stopc.robertserver.ws.proto.ProtoStorage.ContactProto;
import fr.gouv.stopc.robertserver.ws.vo.GroupedHellosReportVo;


public interface ContactDtoService {

	/**
	 * Function saving a list of <code>GroupedHellosReportVo</code>
	 * 
	 * @param contactVoList the list of contact to save
	 * @throws RobertServerException error during the save
	 */
	public void saveContacts(List<GroupedHellosReportVo> contactVoList) throws RobertServerException;

	/**
	 * Function saving a list of <code>ContactProto</code> coming from a Protobuf v3 binary string
	 * 
	 * @param contactProtoList the list of <code>ContactProto</code> to save
	 * @throws RobertServerException error during the save
	 */
	public void saveProtoContacts(@NotNull List<ContactProto> contactProtoList) throws RobertServerException;
}
