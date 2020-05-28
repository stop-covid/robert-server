package fr.gouv.stopc.robertserver.ws.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum AuthConstants {
    EBID(8, "EBID"),
    TIME(4, "TIME"),
    MAC(32, "MAC"),
    EPOCHID(4, "EPOCHID"),
    IDA(5, "IDA"),
    CHECKMAC(12, "CHECKMAC");
    private int size;
    private String type;
}
