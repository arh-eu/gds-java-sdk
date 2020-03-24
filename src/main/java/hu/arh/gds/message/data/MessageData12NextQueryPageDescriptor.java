package hu.arh.gds.message.data;

import hu.arh.gds.message.util.PublicElementCountable;

public interface MessageData12NextQueryPageDescriptor extends PublicElementCountable {
    QueryContextHolder getQueryContextDescriptor();
    QueryContextHolderSerializable getQueryContextDescriptorSerializable() throws Exception;
    Long getTimeout();
}
