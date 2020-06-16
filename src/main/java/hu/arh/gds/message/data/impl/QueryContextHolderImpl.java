package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.data.ConsistencyType;
import hu.arh.gds.message.data.GDSHolder;
import hu.arh.gds.message.data.QueryContextHolder;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.List;

public class QueryContextHolderImpl implements QueryContextHolder {
    private static final int NUMBER_OF_PUBLIC_ELEMENTS = 9;
    private final String scrollId;
    private final String query;
    private final Long deliveredNumberOfHits;
    private final Long queryStartTime;
    private final ConsistencyType consistencyType;
    private final String lastBucketId;
    private final GDSHolder gdsHolder;
    private final List<Value> fieldValues;
    private final List<String> partitionNames;

    public QueryContextHolderImpl(String scrollId,
                                  String query,
                                  Long deliveredNumberOfHits,
                                  Long queryStartTime,
                                  ConsistencyType consistencyType,
                                  String lastBucketId,
                                  GDSHolder gdsHolder,
                                  List<Value> fieldValues,
                                  List<String> partitionNames) {
        this.scrollId = scrollId;
        this.query = query;
        this.deliveredNumberOfHits = deliveredNumberOfHits;
        this.queryStartTime = queryStartTime;
        this.consistencyType = consistencyType;
        this.lastBucketId = lastBucketId;
        this.gdsHolder = gdsHolder;
        this.fieldValues = fieldValues;
        this.partitionNames = partitionNames;
        checkContent(this);
    }

    private static void checkContent(QueryContextHolder queryContext) {
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
        ExceptionHelper.requireNonNullValue(queryContext.getGDSHolder(), queryContext.getClass().getSimpleName(),
                "gdsDescriptor");
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
    public GDSHolder getGDSHolder() {
        return this.gdsHolder;
    }

    @Override
    public List<Value> getFieldValues() {
        return this.fieldValues;
    }

    @Override
    public List<String> getPartitionNames() {
        return this.partitionNames;
    }

    @Override
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_ELEMENTS;
    }

    @Override
    public void packContent(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, getScrollId());
        WriterHelper.packValue(packer, getQuery());
        WriterHelper.packValue(packer, getDeliveredNumberOfHits());
        WriterHelper.packValue(packer, getQueryStartTime());
        WriterHelper.packValue(packer, getConsistencyType().toString());
        WriterHelper.packValue(packer, getLastBucketId());
        WriterHelper.packPackable(packer, this.gdsHolder);
        WriterHelper.packValueValues(packer, this.fieldValues);
        WriterHelper.packStringValues(packer, this.partitionNames);
    }

    public static QueryContextHolder unpackContent(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "query context descriptor",
                QueryContextHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, NUMBER_OF_PUBLIC_ELEMENTS, "query context descriptor",
                    QueryContextHolderImpl.class.getSimpleName());

            String scrollIdTemp = ReaderHelper.unpackStringValue(unpacker, "scroll id",
                    QueryContextHolderImpl.class.getSimpleName());

            String queryTemp = ReaderHelper.unpackStringValue(unpacker, "query",
                    QueryContextHolderImpl.class.getSimpleName());

            Long deliveredNumberOfHitsTemp = ReaderHelper.unpackLongValue(unpacker, "delivered number of hits",
                    QueryContextHolderImpl.class.getSimpleName());

            Long queryStartTimeTemp = ReaderHelper.unpackLongValue(unpacker, "query start time",
                    QueryContextHolderImpl.class.getSimpleName());

            ConsistencyType consistencyTypeTemp = ReaderHelper.unpackEnumValueAsString(unpacker, ConsistencyType.class,
                    "consistency type",
                    QueryContextHolderImpl.class.getSimpleName());

            String lastBucketIdTemp = ReaderHelper.unpackStringValue(unpacker, "last bucket id",
                    QueryContextHolderImpl.class.getSimpleName());

            GDSHolder gdsHolderTemp = GDSHolderImpl.unpackContent(unpacker);

            List<Value> fieldValuesTemp = ReaderHelper.unpackValueValues(unpacker,
                    null,
                    "field values",
                    "field value",
                    QueryContextHolderImpl.class.getSimpleName());

            List<String> partitionNamesTemp = ReaderHelper.unpackStringValues(unpacker,
                    null,
                    "partition names",
                    "partition name",
                    QueryContextHolderImpl.class.getSimpleName());

            QueryContextHolder queryContextTemp = new QueryContextHolderImpl(scrollIdTemp,
                    queryTemp,
                    deliveredNumberOfHitsTemp,
                    queryStartTimeTemp,
                    consistencyTypeTemp,
                    lastBucketIdTemp,
                    gdsHolderTemp,
                    fieldValuesTemp,
                    partitionNamesTemp);

            checkContent(queryContextTemp);
            return queryContextTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryContextHolderImpl that = (QueryContextHolderImpl) o;
        if (scrollId != null ? !scrollId.equals(that.scrollId) : that.scrollId != null) return false;
        if (query != null ? !query.equals(that.query) : that.query != null) return false;
        if (deliveredNumberOfHits != null ? !deliveredNumberOfHits.equals(that.deliveredNumberOfHits) : that.deliveredNumberOfHits != null)
            return false;
        if (queryStartTime != null ? !queryStartTime.equals(that.queryStartTime) : that.queryStartTime != null)
            return false;
        if (consistencyType != that.consistencyType) return false;
        if (lastBucketId != null ? !lastBucketId.equals(that.lastBucketId) : that.lastBucketId != null) return false;
        if (gdsHolder != null ? !gdsHolder.equals(that.gdsHolder) : that.gdsHolder != null) return false;
        if (fieldValues != null ? !fieldValues.equals(that.fieldValues) : that.fieldValues != null) return false;
        return partitionNames != null ? partitionNames.equals(that.partitionNames) : that.partitionNames == null;
    }

    @Override
    public int hashCode() {
        int result = scrollId != null ? scrollId.hashCode() : 0;
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result + (deliveredNumberOfHits != null ? deliveredNumberOfHits.hashCode() : 0);
        result = 31 * result + (queryStartTime != null ? queryStartTime.hashCode() : 0);
        result = 31 * result + (consistencyType != null ? consistencyType.hashCode() : 0);
        result = 31 * result + (lastBucketId != null ? lastBucketId.hashCode() : 0);
        result = 31 * result + (gdsHolder != null ? gdsHolder.hashCode() : 0);
        result = 31 * result + (fieldValues != null ? fieldValues.hashCode() : 0);
        result = 31 * result + (partitionNames != null ? partitionNames.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QueryContextHolderImpl{" +
                "scrollId='" + scrollId + '\'' +
                ", query='" + query + '\'' +
                ", deliveredNumberOfHits=" + deliveredNumberOfHits +
                ", queryStartTime=" + queryStartTime +
                ", consistencyType=" + consistencyType +
                ", lastBucketId='" + lastBucketId + '\'' +
                ", gdsHolder=" + gdsHolder +
                ", fieldValues=" + fieldValues +
                ", partitionNames=" + partitionNames +
                '}';
    }
}
