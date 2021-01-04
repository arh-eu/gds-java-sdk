package hu.arheu.gds.message.data;

import hu.arheu.gds.message.util.PublicElementCountable;

public interface MessageData12NextQueryPageDescriptor extends PublicElementCountable {
    QueryContextHolder getQueryContextDescriptor();
    QueryContextHolderSerializable getQueryContextDescriptorSerializable() throws Exception;
    Long getTimeout();
}
