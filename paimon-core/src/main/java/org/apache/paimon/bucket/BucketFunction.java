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

package org.apache.paimon.bucket;

import org.apache.paimon.CoreOptions;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.types.RowType;

import java.io.Serializable;

/** Bucket function. */
public interface BucketFunction extends Serializable {

    int bucket(BinaryRow row, int numBuckets);

    static BucketFunction create(CoreOptions options, RowType bucketKeyType) {
        CoreOptions.BucketFunctionType type = options.bucketFunctionType();
        return create(type, bucketKeyType);
    }

    static BucketFunction create(
            CoreOptions.BucketFunctionType bucketFunctionType, RowType bucketKeyType) {
        switch (bucketFunctionType) {
            case DEFAULT:
                return new DefaultBucketFunction();
            case MOD:
                return new ModBucketFunction(bucketKeyType);
            default:
                throw new IllegalArgumentException(
                        "Unsupported bucket type: " + bucketFunctionType);
        }
    }
}
