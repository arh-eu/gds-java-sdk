package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.Packable;
import hu.arheu.gds.message.util.PublicElementCountable;
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
