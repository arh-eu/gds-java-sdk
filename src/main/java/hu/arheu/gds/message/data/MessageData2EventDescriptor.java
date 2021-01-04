package hu.arheu.gds.message.data;

import java.util.List;
import java.util.Map;

public interface MessageData2EventDescriptor {
    String getOperations();
    Map<String, byte[]> getBinaryContents();
    List<PriorityLevelHolder> getPriorityLevels();
}
