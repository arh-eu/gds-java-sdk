package hu.arheu.gds.message.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

public class MessageIdGenerator {
    private final String vrsIdPrefix;
    private final AtomicLong lastTime = new AtomicLong(System.currentTimeMillis() << 6);
    private final ZoneId zoneId = TimeZone.getTimeZone("UTC").toZoneId();
    private final DateTimeFormatter df;

    public MessageIdGenerator(String vrsId, String postfixDateFormat) throws Throwable {
        if (vrsId != null && vrsId.length() == 4) {
            this.vrsIdPrefix = vrsId;
            this.df = DateTimeFormatter.ofPattern(postfixDateFormat).withZone(this.zoneId);
        } else {
            throw new Exception("The generator prefix String value has to be four character length!");
        }
    }

    public String nextId() {
        long bigNow = System.currentTimeMillis() << 6;
        long myTime = this.lastTime.incrementAndGet();
        Instant i;
        ZonedDateTime z;
        if (myTime < bigNow && this.lastTime.compareAndSet(myTime, bigNow)) {
            i = Instant.ofEpochMilli(bigNow >> 6);
            z = ZonedDateTime.ofInstant(i, this.zoneId);
            return this.vrsIdPrefix + z.format(this.df) + '0';
        } else {
            i = Instant.ofEpochMilli(myTime >> 6);
            z = ZonedDateTime.ofInstant(i, this.zoneId);
            return this.vrsIdPrefix + z.format(this.df) + (char)((int)(48L + (myTime & 63L)));
        }
    }
}
