
package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.GdsMessagePart;

import java.util.Map;


public interface PriorityLevelHolder extends GdsMessagePart {

    Map<Integer, Boolean> getOperations();

    @Override
    default int getNumberOfPublicElements() {
        return 1;
    }
}
