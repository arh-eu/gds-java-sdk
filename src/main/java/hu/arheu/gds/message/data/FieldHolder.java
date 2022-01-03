
package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.GdsMessagePart;


public interface FieldHolder extends GdsMessagePart {

    FieldValueType getFieldType();

    String getMimeType();

    String getFieldName();

    @Override
    default int getNumberOfPublicElements() {
        return 3;
    }
}
