package hu.arh.gds.message.data;

public interface MessageData10QueryRequestDescriptor {
    String getQuery();
    ConsistencyType getConsistencyType();
    Long getTimeout();
    Integer getPageSize();
    Integer getQueryType();
}
