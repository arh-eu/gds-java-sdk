package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.data.ConsistencyType;
import hu.arheu.gds.message.data.QueryContextHolderSerializable;
import hu.arheu.gds.message.util.ExceptionHelper;

import java.util.List;

public class QueryContextHolderSerializableImpl implements QueryContextHolderSerializable {
    private final String scrollId;
    private final String query;
    private final Long deliveredNumberOfHits;
    private final Long queryStartTime;
    private final ConsistencyType consistencyType;
    private final String lastBucketId;
    private final String clusterName;
    private final String GDSNodeName;
    private final List<Object> fieldValues;
    private final List<String> partitionNames;

    public QueryContextHolderSerializableImpl(String scrollId,
                                  String query,
                                  Long deliveredNumberOfHits,
                                  Long queryStartTime,
                                  ConsistencyType consistencyType,
                                  String lastBucketId,
                                  String clusterName,
                                  String GDSNodeName,
                                  List<Object> fieldValues,
                                  List<String> partitionNames) {
        this.scrollId = scrollId;
        this.query = query;
        this.deliveredNumberOfHits = deliveredNumberOfHits;
        this.queryStartTime = queryStartTime;
        this.consistencyType = consistencyType;
        this.lastBucketId = lastBucketId;
        this.clusterName = clusterName;
        this.GDSNodeName = GDSNodeName;
        this.fieldValues = fieldValues;
        this.partitionNames = partitionNames;
        checkContent(this);
    }

    private static void checkContent(QueryContextHolderSerializable queryContext) {
        ExceptionHelper.requireNonNullValue(queryContext.getScrollId(), queryContext.getClass().getSimpleName(),
                "scrollId");
        ExceptionHelper.requireNonNullValue(queryContext.getQuery(), queryContext.getClass().getSimpleName(),
                "query");
        ExceptionHelper.requireNonNullValue(queryContext.getDeliveredNumberOfHits(),
                queryContext.getClass().getSimpleName(),
                "deliveredNumberOfHits");
        ExceptionHelper.requireNonNullValue(queryContext.getQueryStartTime(), queryContext.getClass().getSimpleName(),
                "queryStartTime");
        ExceptionHelper.requireNonNullValue(queryContext.getConsistencyType(), queryContext.getClass().getSimpleName(),
                "consistencyType");
        ExceptionHelper.requireNonNullValue(queryContext.getLastBucketId(), queryContext.getClass().getSimpleName(),
                "lastBucketId");
        ExceptionHelper.requireNonNullValue(queryContext.getClusterName(), queryContext.getClass().getSimpleName(),
                "clusterName");
        ExceptionHelper.requireNonNullValue(queryContext.getGDSNodeName(), queryContext.getClass().getSimpleName(),
                "gdsNodeName");
        ExceptionHelper.requireNonNullValue(queryContext.getFieldValues(), queryContext.getClass().getSimpleName(),
                "fieldValues");
        ExceptionHelper.requireNonNullValue(queryContext.getPartitionNames(), queryContext.getClass().getSimpleName(),
                "partitionNames");
    }

    @Override
    public String getScrollId() {
        return this.scrollId;
    }

    @Override
    public String getQuery() {
        return this.query;
    }

    @Override
    public Long getDeliveredNumberOfHits() {
        return this.deliveredNumberOfHits;
    }

    @Override
    public Long getQueryStartTime() {
        return this.queryStartTime;
    }

    @Override
    public ConsistencyType getConsistencyType() {
        return this.consistencyType;
    }

    @Override
    public String getLastBucketId() {
        return this.lastBucketId;
    }

    @Override
    public String getClusterName() {
        return this.clusterName;
    }

    @Override
    public String getGDSNodeName() {
        return this.GDSNodeName;
    }

    @Override
    public List<Object> getFieldValues() {
        return this.fieldValues;
    }

    @Override
    public List<String> getPartitionNames() {
        return this.partitionNames;
    }

    @Override
    public String toString() {
        return "QueryContextHolderSerializableImpl{" +
                "scrollId='" + scrollId + '\'' +
                ", query='" + query + '\'' +
                ", deliveredNumberOfHits=" + deliveredNumberOfHits +
                ", queryStartTime=" + queryStartTime +
                ", consistencyType=" + consistencyType +
                ", lastBucketId='" + lastBucketId + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", GDSNodeName='" + GDSNodeName + '\'' +
                ", fieldValues=" + fieldValues +
                ", partitionNames=" + partitionNames +
                '}';
    }
}
