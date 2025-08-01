/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.flink.sink;

import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.data.InternalRow;
import org.apache.paimon.flink.FlinkRowData;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.operation.WriteRestore;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.table.TableTestBase;
import org.apache.paimon.table.sink.SinkRecord;
import org.apache.paimon.utils.Pair;
import org.apache.paimon.utils.SerializationUtils;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.apache.flink.table.data.RowData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Test for {@link StoreCompactOperator}. */
public class StoreCompactOperatorTest extends TableTestBase {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testCompactExactlyOnce(boolean streamingMode) throws Exception {
        createTableDefault();

        CompactRememberStoreWrite compactRememberStoreWrite =
                new CompactRememberStoreWrite(streamingMode);
        StoreCompactOperator.Factory operatorFactory =
                new StoreCompactOperator.Factory(
                        getTableDefault(),
                        (table, commitUser, state, ioManager, memoryPool, metricGroup) ->
                                compactRememberStoreWrite,
                        "10086",
                        !streamingMode);

        TypeSerializer<Committable> serializer =
                new CommittableTypeInfo().createSerializer(new ExecutionConfig());
        OneInputStreamOperatorTestHarness<RowData, Committable> harness =
                new OneInputStreamOperatorTestHarness<>(operatorFactory);
        harness.setup(serializer);
        harness.initializeEmptyState();
        harness.open();

        harness.processElement(new StreamRecord<>(data(0)));
        harness.processElement(new StreamRecord<>(data(0)));
        harness.processElement(new StreamRecord<>(data(1)));
        harness.processElement(new StreamRecord<>(data(1)));
        harness.processElement(new StreamRecord<>(data(2)));

        StoreCompactOperator operator = (StoreCompactOperator) harness.getOperator();
        assertThat(operator.compactionWaitingSet())
                .containsExactlyInAnyOrder(
                        Pair.of(BinaryRow.EMPTY_ROW, 0),
                        Pair.of(BinaryRow.EMPTY_ROW, 1),
                        Pair.of(BinaryRow.EMPTY_ROW, 2));
        assertThat(compactRememberStoreWrite.compactTime).isEqualTo(0);
        operator.prepareCommit(true, 1);
        assertThat(operator.compactionWaitingSet()).isEmpty();
        assertThat(compactRememberStoreWrite.compactTime).isEqualTo(3);
    }

    private RowData data(int bucket) {
        GenericRow genericRow =
                GenericRow.of(
                        0L,
                        SerializationUtils.serializeBinaryRow(BinaryRow.EMPTY_ROW),
                        bucket,
                        new byte[] {0x00, 0x00, 0x00, 0x00});
        return new FlinkRowData(genericRow);
    }

    private static class CompactRememberStoreWrite implements StoreSinkWrite {

        private final boolean streamingMode;
        private int compactTime = 0;

        public CompactRememberStoreWrite(boolean streamingMode) {
            this.streamingMode = streamingMode;
        }

        @Override
        public void setWriteRestore(WriteRestore writeRestore) {}

        @Override
        public SinkRecord write(InternalRow rowData) {
            return null;
        }

        @Override
        public SinkRecord write(InternalRow rowData, int bucket) {
            return null;
        }

        @Override
        public SinkRecord toLogRecord(SinkRecord record) {
            return null;
        }

        @Override
        public void compact(BinaryRow partition, int bucket, boolean fullCompaction) {
            compactTime++;
        }

        @Override
        public void notifyNewFiles(
                long snapshotId, BinaryRow partition, int bucket, List<DataFileMeta> files) {}

        @Override
        public List<Committable> prepareCommit(boolean waitCompaction, long checkpointId) {
            return null;
        }

        @Override
        public void snapshotState() {}

        @Override
        public boolean streamingMode() {
            return streamingMode;
        }

        @Override
        public void close() {}

        @Override
        public void replace(FileStoreTable newTable) {}
    }
}
