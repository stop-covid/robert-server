package test.fr.gouv.stopc.robertserver.batch.processor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class TimeArithmeticTest {
    private static int TOLERANCE = 180;
    private static int USHORT_MAX = 65535;


    @Test
    void testTSandTRInTheMiddleSucceeds() {
        for (int i = 0; i < 50; i++) {
            Random r = new Random();
            long ts = r.nextInt(USHORT_MAX - 2 * TOLERANCE) + TOLERANCE;
            long tr = r.nextInt(2 * TOLERANCE) - TOLERANCE + ts;

            assertTrue(toleranceCheckWithWrap(ts, tr, TOLERANCE));
        }
    }

    @Test
    void testTSAtBeginningAndTRAtEndSucceeds() {
        for (int i = 0; i < 50; i++) {
            Random r = new Random();
            long ts = r.nextInt(TOLERANCE);
            long overflow = TOLERANCE - ts;
            long tr = USHORT_MAX - r.nextInt((int)overflow);

            assertTrue(toleranceCheckWithWrap(ts, tr, TOLERANCE));
        }
    }

    @Test
    void testTSAtBeginningAndTRAtEndOrMiddleFails() {
        for (int i = 0; i < 50; i++) {
            Random r = new Random();
            long ts = r.nextInt(TOLERANCE);
            long overflow = TOLERANCE - ts;
            long tr = r.nextInt(USHORT_MAX - (int)overflow + 1 - TOLERANCE + (int)ts) + TOLERANCE + ts;

            assertFalse(toleranceCheckWithWrap(ts, tr, TOLERANCE));
        }
    }

    @Test
    void testTSAtBeginningAndTRInMiddleSucceeds() {
        for (int i = 0; i < 50; i++) {
            Random r = new Random();
            long ts = r.nextInt(TOLERANCE);
            long overflow = TOLERANCE - ts;
            long tr = USHORT_MAX - r.nextInt((int)overflow);

            assertTrue(toleranceCheckWithWrap(ts, tr, TOLERANCE));
        }
    }

    private boolean toleranceCheckWithWrap(long ts, long tr, int tolerance) {
        log.info("Checking ts={} with tr={} and tolerance={}", ts, tr, tolerance);

        // If value is not in min/max zones that may cause overflow
        if (Math.abs(ts - tr) <= tolerance) {
            return true;
        } else {
            // Overflow risk

            // ts is in min zone, value may overflow to max zone
            if (ts < TOLERANCE) {
                // tr is between 0 and ts
                if (tr < ts) {
                    return true;
                }
                else {
                    
                    if (tr > USHORT_MAX - (tolerance - ts)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
