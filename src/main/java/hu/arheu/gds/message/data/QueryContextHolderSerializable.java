package hu.arheu.gds.message.data;

import java.io.Serializable;
import java.util.List;

public interface QueryContextHolderSerializable extends Serializable {
    String getScrollId();
    String getQuery();
    Long getDeliveredNumberOfHits();
    Long getQueryStartTime();
    ConsistencyType getConsistencyType();
    String getLastBucketId();
    String getClusterName();
    String getGDSNodeName();
    List<Object> getFieldValues();
    List<String> getPartitionNames();
}
