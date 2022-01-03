
package hu.arheu.gds.message.data;

import hu.arheu.gds.message.data.impl.AckStatus;
import hu.arheu.gds.message.util.GdsMessagePart;
import org.msgpack.value.Value;

import java.util.Map;


public interface EventDocumentResultHolder extends GdsMessagePart {

    /**
     * The status code associated with the current event result
     *
     * @return the Status Code
     */
    AckStatus getStatus();

    String getNotification();

    /**
     * Placeholder value, not yet used.
     *
     * @return {@code null} always
     */
    Map<String, Value> getReturnValues();

    @Override
    default int getNumberOfPublicElements() {
        return 3;
    }
}
