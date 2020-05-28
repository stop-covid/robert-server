package fr.gouv.stopc.robert.server.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RobertProtocolConstant {
    EPOCH("EPOCH", 4),
    HOURSEPOCH("HOURSEPOCH", 24),
    DAYSEPOCH("DAYSEPOCH", 4);

    private String type;
    private int value;
}
