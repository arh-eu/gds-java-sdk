
package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.GdsMessagePart;
import org.msgpack.value.Value;

import java.util.Map;


public interface EventHolder extends GdsMessagePart {

    /**
     * The name of the table if fields are not present.
     *
     * @return the table name
     */
    String getTableName();

    /**
     * The field for the current event
     *
     * @return the field
     */
    Map<String, Value> getFields();

    @Override
    default int getNumberOfPublicElements() {
        return 2;
    }
}
