package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.Packable;
import hu.arheu.gds.message.util.PublicElementCountable;
import org.msgpack.value.Value;

import java.util.Map;

public interface EventHolder extends PublicElementCountable, Packable {
    String getTableName();
    Map<String, Value> getFields();
}
