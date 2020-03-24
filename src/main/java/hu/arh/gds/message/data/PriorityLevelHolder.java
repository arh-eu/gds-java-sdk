package hu.arh.gds.message.data;

import hu.arh.gds.message.util.Packable;
import hu.arh.gds.message.util.PublicElementCountable;
import java.util.Map;

public interface PriorityLevelHolder extends PublicElementCountable, Packable {
    Map<Integer, Boolean> getOperations();
}
