
package hu.arheu.gds.message.data;

import org.msgpack.value.Value;

import java.util.List;
import java.util.Map;


public interface MessageData8EventDocument extends MessageData {

    String getTableName();

    List<FieldHolder> getFieldHolders();

    List<List<Value>> getRecords();

    Map<Integer, List<String>> getReturningOptions();

    //these are not serialized

    List<List<Object>> getRecordsObject();

    List<Map<String, Value>> getRecordsMap();

    List<Map<String, Object>> getRecordsObjectMap();

    @Override
    default MessageData8EventDocument asEventDocumentMessageData8() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isEventDocumentMessageData8() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.EVENT_DOCUMENT_8;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 4;
    }
}
