package hu.arh.gds.message.data;

import hu.arh.gds.message.util.Packable;
import hu.arh.gds.message.util.PublicElementCountable;
import org.msgpack.value.Value;

import java.util.List;

public interface QueryResponseHolder extends PublicElementCountable, Packable {
    Long getNumberOfHits();
    Long getNumberOfFilteredHits();
    Boolean getMorePage();
    QueryContextHolder getQueryContextHolder();
    QueryContextHolderSerializable getQueryContextHolderSerializable() throws Exception;
    List<FieldHolder> getFieldHolders();
    List<List<Value>> getHits();
}
