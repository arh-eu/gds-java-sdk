package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.Packable;
import hu.arheu.gds.message.util.PublicElementCountable;

import java.util.Map;

public interface PriorityLevelHolder extends PublicElementCountable, Packable {
    Map<Integer, Boolean> getOperations();
}
