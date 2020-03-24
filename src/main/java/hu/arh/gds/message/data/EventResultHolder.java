package hu.arh.gds.message.data;

import hu.arh.gds.message.util.Packable;
import hu.arh.gds.message.data.impl.AckStatus;

import java.util.List;

public interface EventResultHolder extends Packable {
    AckStatus getStatus();
    String getNotification();
    List<FieldHolder> getFieldHolders();
    List<EventSubResultHolder> getFieldValues();
}
