package hu.arheu.gds.message.data;

import hu.arheu.gds.message.data.impl.AckStatus;
import hu.arheu.gds.message.util.Packable;

import java.util.List;

public interface EventResultHolder extends Packable {
    AckStatus getStatus();
    String getNotification();
    List<FieldHolder> getFieldHolders();
    List<EventSubResultHolder> getFieldValues();
}
