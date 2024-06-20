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
package org.apache.hadoop.hbase.util.bulkdatagenerator;

import java.io.IOException;
import java.util.Map;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;

import org.apache.hbase.thirdparty.com.google.common.base.Preconditions;

public final class Utility {

  /**
   * Schema for HBase table to be generated by generated and populated by
   * {@link BulkDataGeneratorTool}
   */
  public enum TableColumnNames {
    ORG_ID("orgId".getBytes()),
    TOOL_EVENT_ID("toolEventId".getBytes()),
    EVENT_ID("eventId".getBytes()),
    VEHICLE_ID("vehicleId".getBytes()),
    SPEED("speed".getBytes()),
    LATITUDE("latitude".getBytes()),
    LONGITUDE("longitude".getBytes()),
    LOCATION("location".getBytes()),
    TIMESTAMP("timestamp".getBytes());

    private final byte[] columnName;

    TableColumnNames(byte[] column) {
      this.columnName = column;
    }

    public byte[] getColumnName() {
      return this.columnName;
    }
  }

  public static final String COLUMN_FAMILY = "cf";

  public static final int SPLIT_PREFIX_LENGTH = 6;

  public static final int MAX_SPLIT_COUNT = (int) Math.pow(10, SPLIT_PREFIX_LENGTH);

  /**
   * Private Constructor
   */
  private Utility() {

  }

  public static void deleteTable(Admin admin, String tableName) throws IOException {
    admin.disableTable(TableName.valueOf(tableName));
    admin.deleteTable(TableName.valueOf(tableName));
  }

  /**
   * Creates a pre-splitted HBase Table having single column family ({@link #COLUMN_FAMILY}) and
   * sequential splits with {@link #SPLIT_PREFIX_LENGTH} length character prefix. Example: If a
   * table (TEST_TABLE_1) need to be generated with splitCount as 10, table would be created with
   * (10+1) regions with boundaries end-keys as (000000-000001, 000001-000002, 000002-000003, ....,
   * 0000010-)
   * @param admin        - Admin object associated with HBase connection
   * @param tableName    - Name of table to be created
   * @param splitCount   - Number of splits for the table (Number of regions will be splitCount + 1)
   * @param tableOptions - Additional HBase metadata properties to be set for the table
   */
  public static void createTable(Admin admin, String tableName, int splitCount,
    Map<String, String> tableOptions) throws IOException {
    Preconditions.checkArgument(splitCount > 0, "Split count must be greater than 0");
    TableDescriptorBuilder tableDescriptorBuilder =
      TableDescriptorBuilder.newBuilder(TableName.valueOf(tableName));
    tableOptions.forEach(tableDescriptorBuilder::setValue);
    TableDescriptor tableDescriptor = tableDescriptorBuilder
      .setColumnFamily(ColumnFamilyDescriptorBuilder.of(COLUMN_FAMILY)).build();
    // Pre-splitting table based on splitCount
    byte[][] splitKeys = new byte[splitCount][];
    for (int i = 0; i < splitCount; i++) {
      splitKeys[i] = String.format("%0" + Utility.SPLIT_PREFIX_LENGTH + "d", i + 1).getBytes();
    }
    admin.createTable(tableDescriptor, splitKeys);
  }
}