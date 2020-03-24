package hu.arh.gds.message.data;

import hu.arh.gds.message.util.Packable;

import java.util.List;
import hu.arh.gds.message.util.PublicElementCountable;
import org.msgpack.value.Value;

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
