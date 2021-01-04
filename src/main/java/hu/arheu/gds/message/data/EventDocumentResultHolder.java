package hu.arheu.gds.message.data;

import hu.arheu.gds.message.data.impl.AckStatus;
import hu.arheu.gds.message.util.Packable;
import hu.arheu.gds.message.util.PublicElementCountable;
import org.msgpack.value.Value;

import java.util.Map;

public interface EventDocumentResultHolder extends PublicElementCountable, Packable {
    AckStatus getStatus();
    String getNotification();
    Map<String, Value> getReturnValues();
}
