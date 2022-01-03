package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.FieldHolder;
import hu.arheu.gds.message.data.QueryContextHolder;
import hu.arheu.gds.message.data.QueryResponseHolder;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QueryResponseHolderImpl extends MessagePart implements QueryResponseHolder {

    private Long numberOfHits;
    private Long numberOfFilteredHits;
    private Boolean morePage;
    private QueryContextHolder queryContextHolder;
    private List<FieldHolder> fieldHolders;
    private List<List<Value>> hits;
    private Long numberOfTotalHits;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public QueryResponseHolderImpl() {
    }

    public QueryResponseHolderImpl(Long numberOfHits,
                                   Long numberOfFilteredHits,
                                   Boolean morePage,
                                   QueryContextHolder queryContextHolder,
                                   List<FieldHolder> fieldHolders,
                                   List<List<Value>> hits) {

        this(numberOfHits, numberOfFilteredHits, morePage, queryContextHolder, fieldHolders, hits, null);
    }

    public QueryResponseHolderImpl(Long numberOfHits,
                                   Long numberOfFilteredHits,
                                   Boolean morePage,
                                   QueryContextHolder queryContextHolder,
                                   List<FieldHolder> fieldHolders,
                                   List<List<Value>> hits,
                                   Long numberOfTotalHits) {

        this.numberOfHits = numberOfHits;
        this.numberOfFilteredHits = numberOfFilteredHits;
        this.morePage = morePage;
        this.queryContextHolder = queryContextHolder;
        this.fieldHolders = fieldHolders;
        this.hits = hits;
        this.numberOfTotalHits = numberOfTotalHits;

        checkContent();
    }

    @Override
    public void checkContent() {
        Validator.requireNonNullValue(getNumberOfHits(), getClass().getSimpleName(), "queryResponse");
        Validator.requireNonNullValue(getNumberOfFilteredHits(), getClass().getSimpleName(), "numberOfFilteredHits");
        Validator.requireNonNullValue(getMorePage(), getClass().getSimpleName(), "morePage");
        Validator.requireNonNullValue(getQueryContextHolder(), getClass().getSimpleName(), "queryContextDescriptor");
        Validator.requireNonNullValue(getFieldHolders(), getClass().getSimpleName(), "fieldDescriptors");
        Validator.requireNonNullValue(getHits(), getClass().getSimpleName(), "hits");
    }

    @Override
    protected Type getMessagePartType() {
        return Type.OTHER;
    }

    @Override
    public Long getNumberOfHits() {
        return this.numberOfHits;
    }

    @Override
    public Long getNumberOfFilteredHits() {
        return this.numberOfFilteredHits;
    }

    @Override
    public Boolean getMorePage() {
        return this.morePage;
    }

    @Override
    public QueryContextHolder getQueryContextHolder() {
        return this.queryContextHolder;
    }

    @Override
    public List<FieldHolder> getFieldHolders() {
        return this.fieldHolders;
    }

    @Override
    public List<List<Value>> getHits() {
        return this.hits;
    }

    @Override
    public Long getNumberOfTotalHits() {
        return this.numberOfTotalHits;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        int elements = numberOfTotalHits == null ? getNumberOfPublicElements() - 1 : getNumberOfPublicElements();

        WriterHelper.packArrayHeader(packer, elements);
        WriterHelper.packValue(packer, this.numberOfHits);
        WriterHelper.packValue(packer, this.numberOfFilteredHits);
        WriterHelper.packValue(packer, this.morePage);
        WriterHelper.packMessagePart(packer, this.queryContextHolder);
        WriterHelper.packMessagePartCollection(packer, this.fieldHolders);
        WriterHelper.packValueListListValues(packer, this.hits);

        if (numberOfTotalHits != null) {
            WriterHelper.packValue(packer, this.numberOfTotalHits);
        }
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "query ack data",
                QueryResponseHolderImpl.class.getSimpleName())) {

            int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, getNumberOfPublicElements() - 1, "query ack data",
                    QueryResponseHolderImpl.class.getSimpleName());

            numberOfHits = ReaderHelper.unpackLongValue(unpacker, "number of hits",
                    QueryResponseHolderImpl.class.getSimpleName());

            numberOfFilteredHits = ReaderHelper.unpackLongValue(unpacker, "number of filtered hits",
                    QueryResponseHolderImpl.class.getSimpleName());

            morePage = ReaderHelper.unpackBooleanValue(unpacker, "more page",
                    QueryResponseHolderImpl.class.getSimpleName());

            queryContextHolder = new QueryContextHolderImpl();
            queryContextHolder.unpackContentFrom(unpacker);

            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "field descriptors",
                    QueryResponseHolderImpl.class.getSimpleName())) {

                fieldHolders = new ArrayList<>();

                int fieldDescriptorsSize = ReaderHelper.unpackArrayHeader(unpacker, null,
                        "field descriptors",
                        QueryResponseHolderImpl.class.getSimpleName());

                for (int i = 0; i < fieldDescriptorsSize; i++) {
                    FieldHolderImpl fieldHolder = new FieldHolderImpl();
                    fieldHolder.unpackContentFrom(unpacker);
                    fieldHolders.add(fieldHolder);
                }

            } else {
                ReaderHelper.unpackNil(unpacker);
            }

            hits = ReaderHelper.unpackValueListListValues(unpacker,
                    null,
                    null,
                    "records",
                    "record",
                    "fieldvalue",
                    QueryResponseHolderImpl.class.getSimpleName());

            if (arrayHeaderSize >= getNumberOfPublicElements()) {
                numberOfTotalHits =
                        ReaderHelper.unpackLongValue(unpacker, "number of total hits", QueryResponseHolderImpl.class.getSimpleName());
            } else {
                numberOfTotalHits = null;
            }

            checkContent();
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryResponseHolderImpl that = (QueryResponseHolderImpl) o;
        return Objects.equals(numberOfHits, that.numberOfHits) &&
                Objects.equals(numberOfFilteredHits, that.numberOfFilteredHits) &&
                Objects.equals(morePage, that.morePage) &&
                Objects.equals(queryContextHolder, that.queryContextHolder) &&
                Objects.equals(fieldHolders, that.fieldHolders) &&
                Objects.equals(hits, that.hits) &&
                Objects.equals(numberOfTotalHits, that.numberOfTotalHits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfHits, numberOfFilteredHits, morePage, queryContextHolder, fieldHolders, hits, numberOfTotalHits);
    }

    @Override
    public String toString() {
        return "QueryResponseHolderImpl{" +
                "numberOfHits=" + numberOfHits +
                ", numberOfFilteredHits=" + numberOfFilteredHits +
                ", morePage=" + morePage +
                ", queryContextHolder=" + queryContextHolder +
                ", fieldHolders=" + fieldHolders +
                ", hits=" + hits +
                ", numberOfTotalHits=" + numberOfTotalHits +
                '}';
    }
}
