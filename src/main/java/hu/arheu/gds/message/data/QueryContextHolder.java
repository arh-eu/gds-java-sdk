package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.Packable;
import hu.arheu.gds.message.util.PublicElementCountable;
import org.msgpack.value.Value;

import java.util.List;

public interface QueryContextHolder extends PublicElementCountable, Packable {
    String getScrollId();
    String getQuery();
    Long getDeliveredNumberOfHits();
    Long getQueryStartTime();
    ConsistencyType getConsistencyType();
    String getLastBucketId();
    GDSHolder getGDSHolder();
    List<Value> getFieldValues();
    List<String> getPartitionNames();
}
