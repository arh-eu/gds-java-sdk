package hu.arh.gds.message.data;

import hu.arh.gds.message.util.Packable;
import hu.arh.gds.message.util.PublicElementCountable;

public interface FieldHolder extends PublicElementCountable, Packable {
    FieldValueType getFieldType();
    String getMimeType();
    String getFieldName();
}
