package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.GdsMessagePart;
import org.msgpack.value.Value;

import java.util.List;

public interface QueryResponseHolder extends GdsMessagePart {

    Long getNumberOfHits();

    Long getNumberOfFilteredHits();

    Boolean getMorePage();

    QueryContextHolder getQueryContextHolder();

    List<FieldHolder> getFieldHolders();

    List<List<Value>> getHits();

    Long getNumberOfTotalHits();

    @Override
    default int getNumberOfPublicElements() {
        return 7;
    }
}
