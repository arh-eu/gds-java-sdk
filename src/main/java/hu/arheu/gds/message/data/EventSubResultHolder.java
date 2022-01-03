package hu.arheu.gds.message.data;

import hu.arheu.gds.message.data.impl.AckStatus;
import hu.arheu.gds.message.util.GdsMessagePart;
import org.msgpack.value.Value;

import java.util.List;

public interface EventSubResultHolder extends GdsMessagePart {

    AckStatus getSubStatus();

    String getId();

    String getTableName();

    Boolean getCreated();

    String getVersion();

    List<Value> getRecordValues();

    @Override
    default int getNumberOfPublicElements() {
        return 6;
    }
}
