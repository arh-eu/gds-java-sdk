
package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.GdsMessagePart;
import org.msgpack.value.Value;

import java.util.List;


public interface QueryContextHolder extends GdsMessagePart {

    String getScrollId();

    String getQuery();

    Long getDeliveredNumberOfHits();

    Long getQueryStartTime();

    ConsistencyType getConsistencyType();

    String getLastBucketId();

    GDSHolder getGDSHolder();

    List<Value> getFieldValues();

    List<String> getPartitionNames();

    @Override
    default int getNumberOfPublicElements() {
        return 9;
    }
}
