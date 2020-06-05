package fr.gouv.stopc.robert.server.batch.service;

import fr.gouv.stopc.robert.server.batch.exception.RobertScoringException;
import fr.gouv.stopc.robert.server.batch.vo.ScoringResult;
import fr.gouv.stopc.robertserver.database.model.Contact;

public interface ScoringStrategyService {
    /**
     * Compute a risk score based on the nature of the contact
     * @param contact
     */
    ScoringResult execute(Contact contact) throws RobertScoringException;
}
