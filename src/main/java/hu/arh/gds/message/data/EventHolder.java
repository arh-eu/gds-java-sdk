package hu.arh.gds.message.data;

import hu.arh.gds.message.util.Packable;
import hu.arh.gds.message.util.PublicElementCountable;
import org.msgpack.value.Value;

import java.util.Map;

public interface EventHolder extends PublicElementCountable, Packable {
    String getTableName();
    Map<String, Value> getFields();
}
