package fr.gouv.stopc.robertserver.crypto.callables;

import fr.gouv.stopc.robertserver.crypto.callables.TupleGenerator;
import fr.gouv.stopc.robertserver.crypto.model.EphemeralTuple;
import fr.gouv.stopc.robertserver.crypto.services.implementation.CryptoServiceImpl;
import fr.gouv.stopc.robert.server.common.utils.TimeUtils;

import java.util.*;

public class EBIDEncryptorsGeneratorTest {

    public static void main(String[] args) throws Exception {
        // 40 bits IDa
        byte[] IDa = new byte[40/8];


        // Server key should be 192-bits (3 x 64-bits) long because of the TripleDES key property
        final byte[] kServ = new byte[(3*64)/8];

        // Federation key should be 256-bits long.
        final byte[] kFed = new byte[256/8];

        // Hazarding IDa, kServ and kFed keys
        Random random = new Random();
        random.nextBytes(IDa);
        random.nextBytes(kServ);
        random.nextBytes(kFed);

        final int currentEpoch = TimeUtils.getNumberOfEpochsBetween(0L, TimeUtils.convertUnixMillistoNtpSeconds(new Date().getTime()));
        int numberOfEpochs = 4 * 24 * 4 ;

        long start = System.currentTimeMillis();
        final TupleGenerator tupleGenerator = new TupleGenerator(kServ, kFed, 50);
        final Collection<EphemeralTuple> ephemeralTuples = tupleGenerator.exec(IDa, currentEpoch, numberOfEpochs, (byte) 0x33);
        tupleGenerator.stop();

        ephemeralTuples.forEach(et -> {
            System.out.println("ECC     size : " + et.getEncryptedCountryCode().length * 8 + "-bits" + " " + Arrays.toString(et.getEncryptedCountryCode()));
            System.out.println("EBID    size : " + et.getEbid().length * 8 + "-bits"+ " " + Arrays.toString(et.getEbid()));
            System.out.println("EPOCH : " + et.getEpoch());
        });

        long end = System.currentTimeMillis();

        System.out.println("Time 1 :" + (end - start) + " ms");

    }
}
