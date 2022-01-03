
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.ConsistencyType;
import hu.arheu.gds.message.data.GDSHolder;
import hu.arheu.gds.message.data.QueryContextHolder;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.util.ReaderHelper;
import hu.arheu.gds.message.util.Validator;
import hu.arheu.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.Externalizable;
import java.util.List;
import java.util.Objects;


public class QueryContextHolderImpl extends MessagePart implements QueryContextHolder {

    private String scrollId;
    private String query;
    private Long deliveredNumberOfHits;
    private Long queryStartTime;
    private ConsistencyType consistencyType;
    private String lastBucketId;
    private GDSHolder gdsHolder;
    private List<Value> fieldValues;
    private List<String> partitionNames;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public QueryContextHolderImpl() {
    }

    public QueryContextHolderImpl(String scrollId,
                                  String query,
                                  Long deliveredNumberOfHits,
                                  Long queryStartTime,
                                  ConsistencyType consistencyType,
                                  String lastBucketId,
                                  GDSHolder gdsHolder,
                                  List<Value> fieldValues,
                                  List<String> partitionNames) throws ValidationException {

        this.scrollId = scrollId;
        this.query = query;
        this.deliveredNumberOfHits = deliveredNumberOfHits;
        this.queryStartTime = queryStartTime;
        this.consistencyType = consistencyType;
        this.lastBucketId = lastBucketId;
        this.gdsHolder = gdsHolder;
        this.fieldValues = fieldValues;
        this.partitionNames = partitionNames;

        checkContent();
    }

    @Override
    public void checkContent() throws ValidationException {

        Validator.requireNonNullValue(getScrollId(), getClass().getSimpleName(),
                "scrollId");
        Validator.requireNonNullValue(getQuery(), getClass().getSimpleName(),
                "query");
        Validator.requireNonNullValue(getDeliveredNumberOfHits(),
                getClass().getSimpleName(),
                "deliveredNumberOfHits");
        Validator.requireNonNullValue(getQueryStartTime(), getClass().getSimpleName(),
                "queryStartTime");
        Validator.requireNonNullValue(getConsistencyType(), getClass().getSimpleName(),
                "consistencyType");
        Validator.requireNonNullValue(getLastBucketId(), getClass().getSimpleName(),
                "lastBucketId");
        Validator.requireNonNullValue(getGDSHolder(), getClass().getSimpleName(),
                "gdsDescriptor");
        Validator.requireNonNullValue(getFieldValues(), getClass().getSimpleName(),
                "fieldValues");
        Validator.requireNonNullValue(getPartitionNames(), getClass().getSimpleName(),
                "partitionNames");
    }

    @Override
    protected Type getMessagePartType() {
        return Type.OTHER;
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
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, getScrollId());
        WriterHelper.packValue(packer, getQuery());
        WriterHelper.packValue(packer, getDeliveredNumberOfHits());
        WriterHelper.packValue(packer, getQueryStartTime());
        WriterHelper.packValue(packer, getConsistencyType().toString());
        WriterHelper.packValue(packer, getLastBucketId());
        WriterHelper.packMessagePart(packer, this.gdsHolder);
        WriterHelper.packValueCollection(packer, this.fieldValues);
        WriterHelper.packStringCollection(packer, this.partitionNames);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "query context descriptor",
                QueryContextHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, getNumberOfPublicElements(), "query context descriptor",
                    QueryContextHolderImpl.class.getSimpleName());

            scrollId = ReaderHelper.unpackStringValue(unpacker, "scroll id",
                    QueryContextHolderImpl.class.getSimpleName());

            query = ReaderHelper.unpackStringValue(unpacker, "query",
                    QueryContextHolderImpl.class.getSimpleName());

            deliveredNumberOfHits = ReaderHelper.unpackLongValue(unpacker, "delivered number of hits",
                    QueryContextHolderImpl.class.getSimpleName());

            queryStartTime = ReaderHelper.unpackLongValue(unpacker, "query start time",
                    QueryContextHolderImpl.class.getSimpleName());

            consistencyType = ReaderHelper.unpackEnumValueAsString(unpacker, ConsistencyType.class,
                    "consistency type",
                    QueryContextHolderImpl.class.getSimpleName());

            lastBucketId = ReaderHelper.unpackStringValue(unpacker, "last bucket id",
                    QueryContextHolderImpl.class.getSimpleName());

            gdsHolder = new GDSHolderImpl();
            gdsHolder.unpackContentFrom(unpacker);

            fieldValues = ReaderHelper.unpackValueValues(unpacker,
                    null,
                    "field values",
                    "field value",
                    QueryContextHolderImpl.class.getSimpleName());

            partitionNames = ReaderHelper.unpackStringValues(unpacker,
                    null,
                    "partition names",
                    "partition name",
                    QueryContextHolderImpl.class.getSimpleName());

            checkContent();
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryContextHolderImpl that = (QueryContextHolderImpl) o;
        return Objects.equals(scrollId, that.scrollId)
                && Objects.equals(query, that.query)
                && Objects.equals(deliveredNumberOfHits, that.deliveredNumberOfHits)
                && Objects.equals(queryStartTime, that.queryStartTime)
                && consistencyType == that.consistencyType
                && Objects.equals(lastBucketId, that.lastBucketId)
                && Objects.equals(gdsHolder, that.gdsHolder)
                && Objects.equals(fieldValues, that.fieldValues)
                && Objects.equals(partitionNames, that.partitionNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scrollId, query, deliveredNumberOfHits, queryStartTime,
                consistencyType, lastBucketId, gdsHolder, fieldValues, partitionNames);
    }
}
