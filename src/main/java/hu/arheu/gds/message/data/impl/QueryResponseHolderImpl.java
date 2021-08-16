package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.data.FieldHolder;
import hu.arheu.gds.message.data.QueryContextHolder;
import hu.arheu.gds.message.data.QueryContextHolderSerializable;
import hu.arheu.gds.message.data.QueryResponseHolder;
import hu.arheu.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QueryResponseHolderImpl implements QueryResponseHolder {
    private static final int NUMBER_OF_PUBLIC_ELEMENTS = 7;
    private final Long numberOfHits;
    private final Long numberOfFilteredHits;
    private final Boolean morePage;
    private QueryContextHolderSerializable queryContextHolderSerializable;
    private final QueryContextHolder queryContextHolder;
    private final List<FieldHolder> fieldHolders;
    private final List<List<Value>> hits;
    private final Long numberOfTotalHits;

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
        checkContent(this);
    }

    private static void checkContent(QueryResponseHolder queryResponse) {
        ExceptionHelper.requireNonNullValue(queryResponse.getNumberOfHits(), queryResponse.getClass().getSimpleName(),
                "queryResponse");
        ExceptionHelper.requireNonNullValue(queryResponse.getNumberOfFilteredHits(),
                queryResponse.getClass().getSimpleName(),
                "numberOfFilteredHits");
        ExceptionHelper.requireNonNullValue(queryResponse.getMorePage(), queryResponse.getClass().getSimpleName(),
                "morePage");
        ExceptionHelper.requireNonNullValue(queryResponse.getQueryContextHolder(),
                queryResponse.getClass().getSimpleName(),
                "queryContextDescriptor");
        ExceptionHelper.requireNonNullValue(queryResponse.getFieldHolders(), queryResponse.getClass().getSimpleName(),
                "fieldDescriptors");
        ExceptionHelper.requireNonNullValue(queryResponse.getHits(), queryResponse.getClass().getSimpleName(),
                "hits");
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
    public QueryContextHolderSerializable getQueryContextHolderSerializable() {
        if (queryContextHolderSerializable == null) {
            queryContextHolderSerializable = Converters
                    .getQueryContextDescriptorSerializable(queryContextHolder);
        }
        return queryContextHolderSerializable;
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
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_ELEMENTS;
    }

    @Override
    public Long getNumberOfTotalHits() {
        return numberOfTotalHits;
    }

    @Override
    public void packContent(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.numberOfHits);
        WriterHelper.packValue(packer, this.numberOfFilteredHits);
        WriterHelper.packValue(packer, this.morePage);
        WriterHelper.packPackable(packer, this.queryContextHolder);
        WriterHelper.packPackables(packer, this.fieldHolders);
        WriterHelper.packValueListListValues(packer, this.hits);
        WriterHelper.packValue(packer, this.numberOfTotalHits);
    }

    public static QueryResponseHolderImpl unpackContent(MessageUnpacker unpacker) throws IOException, ReadException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "query ack data",
                QueryResponseHolderImpl.class.getSimpleName())) {

            int headerSize = ReaderHelper.unpackArrayHeader(unpacker, NUMBER_OF_PUBLIC_ELEMENTS - 1, "query ack data",
                    QueryResponseHolderImpl.class.getSimpleName());

            Long numberOfHitsTemp = ReaderHelper.unpackLongValue(unpacker, "number of hits",
                    QueryResponseHolderImpl.class.getSimpleName());

            Long numberOfFilteredHits = ReaderHelper.unpackLongValue(unpacker, "number of filtered hits",
                    QueryResponseHolderImpl.class.getSimpleName());

            Boolean morePage = ReaderHelper.unpackBooleanValue(unpacker, "more page",
                    QueryResponseHolderImpl.class.getSimpleName());

            QueryContextHolder queryContextHolderTemp = QueryContextHolderImpl.unpackContent(unpacker);

            List<FieldHolder> fieldHoldersTemp = null;

            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "field descriptors",
                    QueryResponseHolderImpl.class.getSimpleName())) {

                fieldHoldersTemp = new ArrayList<>();

                int fieldDescriptorsSize = ReaderHelper.unpackArrayHeader(unpacker, null,
                        "field descriptors",
                        QueryResponseHolderImpl.class.getSimpleName());

                for (int i = 0; i < fieldDescriptorsSize; i++) {
                    fieldHoldersTemp.add(FieldHolderImpl.unpackContent(unpacker));
                }

            } else {
                unpacker.unpackNil();
            }

            List<List<Value>> hitsTemp = ReaderHelper.unpackValueListListValues(unpacker,
                    null,
                    null,
                    "records",
                    "record",
                    "fieldvalue",
                    QueryResponseHolderImpl.class.getSimpleName());

            Long totalHits = null;
            if (headerSize >= NUMBER_OF_PUBLIC_ELEMENTS) {
                totalHits = ReaderHelper.unpackLongValue(unpacker, "numberOfTotalHits", QueryResponseHolderImpl.class.getSimpleName());
                for (int ii = NUMBER_OF_PUBLIC_ELEMENTS + 1; ii < headerSize; ++ii) {
                    unpacker.unpackValue();
                }
            }


            QueryResponseHolderImpl queryResponseTemp = new QueryResponseHolderImpl(numberOfHitsTemp,
                    numberOfFilteredHits,
                    morePage,
                    queryContextHolderTemp,
                    fieldHoldersTemp,
                    hitsTemp,
                    totalHits);
            checkContent(queryResponseTemp);
            return queryResponseTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryResponseHolderImpl that = (QueryResponseHolderImpl) o;
        return Objects.equals(numberOfHits, that.numberOfHits) && Objects.equals(numberOfFilteredHits, that.numberOfFilteredHits) && Objects.equals(morePage, that.morePage) && Objects.equals(queryContextHolderSerializable, that.queryContextHolderSerializable) && Objects.equals(queryContextHolder, that.queryContextHolder) && Objects.equals(fieldHolders, that.fieldHolders) && Objects.equals(hits, that.hits) && Objects.equals(numberOfTotalHits, that.numberOfTotalHits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfHits, numberOfFilteredHits, morePage, queryContextHolderSerializable, queryContextHolder, fieldHolders, hits, numberOfTotalHits);
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
