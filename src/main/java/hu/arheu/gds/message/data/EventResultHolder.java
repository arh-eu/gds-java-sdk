
package hu.arheu.gds.message.data;

import hu.arheu.gds.message.data.impl.AckStatus;
import hu.arheu.gds.message.util.GdsMessagePart;

import java.util.List;


public interface EventResultHolder extends GdsMessagePart {

    /**
     * The ACK status associated with the current event result
     *
     * @return the status code
     */
    AckStatus getStatus();

    String getNotification();

    List<FieldHolder> getFieldHolders();

    /**
     * @return the field values
     */
    List<EventSubResultHolder> getFieldValues();

    @Override
    default int getNumberOfPublicElements() {
        return 4;
    }
}
