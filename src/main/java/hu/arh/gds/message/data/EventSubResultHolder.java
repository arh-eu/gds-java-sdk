package hu.arh.gds.message.data;

import hu.arh.gds.message.data.impl.AckStatus;
import hu.arh.gds.message.util.Packable;
import org.msgpack.value.Value;

import java.util.List;

public interface EventSubResultHolder extends Packable {
    AckStatus getSubStatus();
    String getId();
    String getTableName();
    Boolean getCreated();
    Long getVersion();
    List<Value> getRecordValues();
}
