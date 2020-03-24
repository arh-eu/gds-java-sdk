package hu.arh.gds.message.data;

import hu.arh.gds.message.util.Packable;
import hu.arh.gds.message.data.impl.AckStatus;
import hu.arh.gds.message.util.PublicElementCountable;
import org.msgpack.value.Value;

import java.util.Map;

public interface EventDocumentResultHolder extends PublicElementCountable, Packable {
    AckStatus getStatus();
    String getNotification();
    Map<String, Value> getReturnValues();
}
