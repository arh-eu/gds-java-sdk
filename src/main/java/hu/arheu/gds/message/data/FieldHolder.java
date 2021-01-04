package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.Packable;
import hu.arheu.gds.message.util.PublicElementCountable;

public interface FieldHolder extends PublicElementCountable, Packable {
    FieldValueType getFieldType();
    String getMimeType();
    String getFieldName();
}
