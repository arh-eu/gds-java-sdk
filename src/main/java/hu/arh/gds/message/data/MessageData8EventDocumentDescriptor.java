package hu.arh.gds.message.data;

import org.msgpack.value.Value;

import java.util.List;
import java.util.Map;

public interface MessageData8EventDocumentDescriptor {
    String getTableName();
    List<FieldHolder> getFieldHolders();
    List<List<Value>> getRecords();
    List<List<Object>> getRecordsObject();
    List<Map<String, Value>> getRecordsMap();
    List<Map<String, Object>> getRecordsObjectMap();
    Map<Integer, List<String>> getReturningOptions();
}
