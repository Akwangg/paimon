---
title: "Procedures"
weight: 99
type: docs
aliases:
- /spark/procedures.html
---
<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Procedures

This section introduce all available spark procedures about paimon.

<table class="table table-bordered">
    <thead>
    <tr>
      <th class="text-left" style="width: 4%">Procedure Name</th>
      <th class="text-left" style="width: 20%">Explanation</th>
      <th class="text-left" style="width: 4%">Example</th>
    </tr>
    </thead>
    <tbody style="font-size: 12px; ">
    <tr>
      <td>compact</td>
      <td>
         To compact files. Argument:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>partitions: partition filter. the comma (",") represents "AND", the semicolon (";") represents "OR". If you want to compact one partition with date=01 and day=01, you need to write 'date=01,day=01'. Left empty for all partitions. (Can't be used together with "where")</li>
            <li>where: partition predicate. Left empty for all partitions. (Can't be used together with "partitions")</li>          
            <li>order_strategy: 'order' or 'zorder' or 'hilbert' or 'none'. Left empty for 'none'.</li>
            <li>order_columns: the columns need to be sort. Left empty if 'order_strategy' is 'none'.</li>
            <li>options: additional dynamic options of the table. It prioritizes higher than original `tableProp` and lower than `procedureArg`.</li>
            <li>partition_idle_time: this is used to do a full compaction for partition which had not received any new data for 'partition_idle_time'. And only these partitions will be compacted. This argument can not be used with order compact.</li>
            <li>compact_strategy: this determines how to pick files to be merged, the default is determined by the runtime execution mode. 'full' strategy only supports batch mode. All files will be selected for merging. 'minor' strategy: Pick the set of files that need to be merged based on specified conditions.</li>
      </td>
      <td>
         SET spark.sql.shuffle.partitions=10; --set the compact parallelism <br/><br/>
         CALL sys.compact(table => 'T', partitions => 'p=0;p=1',  order_strategy => 'zorder', order_by => 'a,b') <br/><br/>
         CALL sys.compact(table => 'T', where => 'p>0 and p<3', order_strategy => 'zorder', order_by => 'a,b') <br/><br/>
         CALL sys.compact(table => 'T', where => 'dt>10 and h<20', order_strategy => 'zorder', order_by => 'a,b', options => 'sink.parallelism=4')<br/><br/> 
         CALL sys.compact(table => 'T', partition_idle_time => '60s')<br/><br/>
         CALL sys.compact(table => 'T', compact_strategy => 'minor')<br/><br/>
      </td>
    </tr>
    <tr>
      <td>expire_snapshots</td>
      <td>
         To expire snapshots. Argument:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>retain_max: the maximum number of completed snapshots to retain.</li>
            <li>retain_min: the minimum number of completed snapshots to retain.</li>
            <li>older_than: timestamp before which snapshots will be removed.</li>
            <li>max_deletes: the maximum number of snapshots that can be deleted at once.</li>
            <li>options: the additional dynamic options of the table. It prioritizes higher than original `tableProp` and lower than `procedureArg`.</li>
      </td>
      <td>CALL sys.expire_snapshots(table => 'default.T', retain_max => 10, options => 'snapshot.expire.limit=1')</td>
    </tr>
    <tr>
      <td>expire_partitions</td>
      <td>
         To expire partitions. Argument:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>expiration_time: the expiration interval of a partition. A partition will be expired if it‘s lifetime is over this value. Partition time is extracted from the partition value.</li>
            <li>timestamp_formatter: the formatter to format timestamp from string.</li>
            <li>timestamp_pattern: the pattern to get a timestamp from partitions.</li>
            <li>expire_strategy: specifies the expiration strategy for partition expiration, possible values: 'values-time' or 'update-time' , 'values-time' as default.</li>
            <li>max_expires: The maximum of limited expired partitions, it is optional.</li>
            <li>options: the additional dynamic options of the table. It prioritizes higher than original `tableProp` and lower than `procedureArg`.</li>
      </td>
      <td>CALL sys.expire_partitions(table => 'default.T', expiration_time => '1 d', timestamp_formatter => 
'yyyy-MM-dd', timestamp_pattern => '$dt', expire_strategy => 'values-time', options => 'partition.expiration-max-num=2')</td>
    </tr>
    <tr>
      <td>create_tag</td>
      <td>
         To create a tag based on given snapshot. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>tag: name of the new tag. Cannot be empty.</li>
            <li>snapshot(Long):  id of the snapshot which the new tag is based on.</li>
            <li>time_retained: The maximum time retained for newly created tags.</li>
      </td>
      <td>
         -- based on snapshot 10 with 1d <br/>
         CALL sys.create_tag(table => 'default.T', tag => 'my_tag', snapshot => 10, time_retained => '1 d') <br/><br/>
         -- based on the latest snapshot <br/>
         CALL sys.create_tag(table => 'default.T', tag => 'my_tag')
      </td>
    </tr>
    <tr>
      <td>create_tag_from_timestamp</td>
      <td>
         To create a tag based on given timestamp. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>tag: name of the new tag.</li>
            <li>timestamp (Long): Find the first snapshot whose commit-time is greater than this timestamp.</li>
            <li>time_retained : The maximum time retained for newly created tags.</li>
      </td>
      <td>
         CALL sys.create_tag_from_timestamp(`table` => 'default.T', `tag` => 'my_tag', `timestamp` => 1724404318750, time_retained => '1 d')
      </td>
    </tr>
    <tr>
      <td>rename_tag</td>
      <td>
         Rename a tag with a new tag name. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>tag: name of the tag. Cannot be empty.</li>
            <li>target_tag: the new tag name to rename. Cannot be empty.</li>
      </td>
      <td>
         CALL sys.rename_tag(table => 'default.T', tag => 'tag1', target_tag => 'tag2')
      </td>
    </tr>
    <tr>
      <td>replace_tag</td>
      <td>
         Replace an existing tag with new tag info. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>tag: name of the existed tag. Cannot be empty.</li>
            <li>snapshot(Long):  id of the snapshot which the tag is based on, it is optional.</li>
            <li>time_retained: The maximum time retained for the existing tag, it is optional.</li>
      </td>
      <td>
         CALL sys.replace_tag(table => 'default.T', tag_name => 'tag1', snapshot => 10, time_retained => '1 d')
      </td>
    </tr>
    <tr>
      <td>delete_tag</td>
      <td>
         To delete a tag. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>tag: name of the tag to be deleted. If you specify multiple tags, delimiter is ','.</li>
      </td>
      <td>CALL sys.delete_tag(table => 'default.T', tag => 'my_tag')</td>
    </tr>
    <tr>
      <td>expire_tags</td>
      <td>
         To expire tags by time. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>older_than: tagCreateTime before which tags will be removed.</li>
      </td>
      <td>
         CALL sys.expire_tags(table => 'default.T', older_than => '2024-09-06 11:00:00')
      </td>
    </tr>
    <tr>
      <td>trigger_tag_automatic_creation</td>
      <td>
         Trigger the tag automatic creation. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
      </td>
      <td>
         CALL sys.trigger_tag_automatic_creation(table => 'default.T')
      </td>
    </tr>
    <tr>
      <td>rollback</td>
      <td>
         To rollback to a specific version of target table, note version/snapshot/tag must set one of them. Argument:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>version: id of the snapshot or name of tag that will roll back to, version would be Deprecated.</li>
            <li>snapshot: snapshot that will roll back to.</li>
            <li>tag: tag that will roll back to.</li>
      </td>
      <td>
          CALL sys.rollback(table => 'default.T', version => 'my_tag')<br/><br/>
          CALL sys.rollback(table => 'default.T', version => 10)<br/><br/>
          CALL sys.rollback(table => 'default.T', tag => 'tag1')
          CALL sys.rollback(table => 'default.T', snapshot => 2)
      </td>
    </tr>
    <tr>
      <td>rollback_to_timestamp</td>
      <td>
         To rollback to the snapshot which earlier or equal than timestamp. Argument:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>timestamp: roll back to the snapshot which earlier or equal than timestamp.</li>
      </td>
      <td>
          CALL sys.rollback_to_timestamp(table => 'default.T', timestamp => 1730292023000)<br/><br/>
      </td>
    </tr>
    <tr>
      <td>rollback_to_watermark</td>
      <td>
         To rollback to the snapshot which earlier or equal than watermark. Argument:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>watermark: roll back to the snapshot which earlier or equal than watermark.</li>
      </td>
      <td>
          CALL sys.rollback_to_watermark(table => 'default.T', watermark => 1730292023000)<br/><br/>
      </td>
    </tr>
    <tr>
      <td>purge_files</td>
      <td>
         To clear table with purge files. Argument:
            <li>table: the target table identifier. Cannot be empty.</li>
      </td>
      <td>
          CALL sys.purge_files(table => 'default.T')<br/>
      </td>
    </tr>
    <tr>
      <td>migrate_database</td>
      <td>
         Migrate all hive tables in database to paimon tables. Arguments:
            <li>source_type: the origin database's type to be migrated, such as hive. Cannot be empty.</li>
            <li>database: name of the origin database to be migrated. Cannot be empty.</li>
            <li>options: the table options of the paimon table to migrate.</li>
            <li>options_map: Options map for adding key-value options which is a map.</li>
            <li>parallelism: the parallelism for migrate process, default is core numbers of machine.</li>
      </td>
      <td>CALL sys.migrate_database(source_type => 'hive', database => 'db01', options => 'file.format=parquet', options_map => map('k1','v1'), parallelism => 6)</td>
    </tr>
    <tr>
      <td>migrate_table</td>
      <td>
         Migrate hive table to a paimon table. Arguments:
            <li>source_type: the origin table's type to be migrated, such as hive. Cannot be empty.</li>
            <li>table: name of the origin table to be migrated. Cannot be empty.</li>
            <li>options: the table options of the paimon table to migrate.</li>
            <li>target_table: name of the target paimon table to migrate. If not set would keep the same name with origin table</li>
            <li>delete_origin: If had set target_table, can set delete_origin to decide whether delete the origin table metadata from hms after migrate. Default is true</li>
            <li>options_map: Options map for adding key-value options which is a map.</li>
            <li>parallelism: the parallelism for migrate process, default is core numbers of machine.</li>
      </td>
      <td>CALL sys.migrate_table(source_type => 'hive', table => 'default.T', options => 'file.format=parquet', options_map => map('k1','v1'), parallelism => 6)</td>
    </tr>
    <tr>
      <td>remove_orphan_files</td>
      <td>
         To remove the orphan data files and metadata files. Arguments:
            <li>table: the target table identifier. Cannot be empty, you can use database_name.* to clean whole database.</li>
            <li>older_than: to avoid deleting newly written files, this procedure only deletes orphan files older than 1 day by default. This argument can modify the interval.</li>
            <li>dry_run: when true, view only orphan files, don't actually remove files. Default is false.</li>
            <li>parallelism: The maximum number of concurrent deleting files. By default is the number of processors available to the Java virtual machine.</li>
            <li>mode: The mode of remove orphan clean procedure (local or distributed) . By default is distributed.</li>
      </td>
      <td>
          CALL sys.remove_orphan_files(table => 'default.T', older_than => '2023-10-31 12:00:00')<br/><br/>
          CALL sys.remove_orphan_files(table => 'default.*', older_than => '2023-10-31 12:00:00')<br/><br/>
          CALL sys.remove_orphan_files(table => 'default.T', older_than => '2023-10-31 12:00:00', dry_run => true)<br/><br/>
          CALL sys.remove_orphan_files(table => 'default.T', older_than => '2023-10-31 12:00:00', dry_run => true, parallelism => '5')<br/><br/>
          CALL sys.remove_orphan_files(table => 'default.T', older_than => '2023-10-31 12:00:00', dry_run => true, parallelism => '5', mode => 'local')
      </td>
    </tr>
    <tr>
      <td>remove_unexisting_files</td>
      <td>
        Procedure to remove unexisting data files from manifest entries. See <a href="https://paimon.apache.org/docs/master/api/java/org/apache/paimon/flink/action/RemoveUnexistingFilesAction.html">Java docs</a> for detailed use cases. Arguments:
            <li>table: the target table identifier. Cannot be empty, you can use database_name.* to clean whole database.</li>
            <li>dry_run (optional): only check what files will be removed, but not really remove them. Default is false.</li>
            <li>parallelism (optional): number of parallelisms to check files in the manifests.</li>
         <br>
         Note that user is on his own risk using this procedure, which may cause data loss when used outside from the use cases listed in Java docs.
      </td>
      <td>
        -- remove unexisting data files in the table `mydb.myt`<br/>
        CALL sys.remove_unexisting_files(table => 'mydb.myt')<br/><br/>
        -- only check what files will be removed, but not really remove them (dry run)<br/>
        CALL sys.remove_unexisting_files(table => 'mydb.myt', dry_run = true)
      </td>
   </tr>
    <tr>
      <td>repair</td>
      <td>
         Synchronize information from the file system to Metastore. Argument:
            <li>database_or_table: empty or the target database name or the target table identifier, if you specify multiple tags, delimiter is ','</li>
      </td>
      <td>
          CALL sys.repair('test_db.T')<br/><br/>
          CALL sys.repair('test_db.T,test_db01,test_db.T2')
      </td>
    </tr>
    <tr>
      <td>create_branch</td>
      <td>
         To merge a branch to main branch. Arguments:
            <li>table: the target table identifier or branch identifier. Cannot be empty.</li>
            <li>branch: name of the branch to be merged.</li>
            <li>tag: name of the new tag. Cannot be empty.</li>
      </td>
      <td>
          CALL sys.create_branch(table => 'test_db.T', branch => 'test_branch')<br/><br/>
          CALL sys.create_branch(table => 'test_db.T', branch => 'test_branch', tag => 'my_tag')<br/><br/>
          CALL sys.create_branch(table => 'test_db.T$branch_existBranchName', branch => 'test_branch', tag => 'my_tag')<br/><br/>
      </td>
    </tr>
    <tr>
      <td>delete_branch</td>
      <td>
         To merge a branch to main branch. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>branch: name of the branch to be merged. If you specify multiple branches, delimiter is ','.</li>
      </td>
      <td>
          CALL sys.delete_branch(table => 'test_db.T', branch => 'test_branch')
      </td>
    </tr>
    <tr>
      <td>fast_forward</td>
      <td>
         To fast_forward a branch to main branch. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>branch: name of the branch to be merged.</li>
      </td>
      <td>
          CALL sys.fast_forward(table => 'test_db.T', branch => 'test_branch')
      </td>
    </tr>
   <tr>
      <td>reset_consumer</td>
      <td>
         To reset or delete consumer. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>consumerId: consumer to be reset or deleted.</li>
            <li>nextSnapshotId (Long): the new next snapshot id of the consumer.</li>
      </td>
      <td>
         -- reset the new next snapshot id in the consumer<br/>
         CALL sys.reset_consumer(table => 'default.T', consumerId => 'myid', nextSnapshotId => 10)<br/><br/>
         -- delete consumer<br/>
         CALL sys.reset_consumer(table => 'default.T', consumerId => 'myid')
      </td>
   </tr>
   <tr>
      <td>clear_consumers</td>
      <td>
         To clear consumers. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>includingConsumers: consumers to be cleared.</li>
            <li>excludingConsumers: consumers which not to be cleared.</li>
      </td>
      <td>
         -- clear all consumers in the table<br/>
         CALL sys.clear_consumers(table => 'default.T')<br/><br/>
         -- clear some consumers in the table (accept regular expression)<br/>
         CALL sys.clear_consumers(table => 'default.T', includingConsumers => 'myid.*')<br/><br/>
         -- clear all consumers except excludingConsumers in the table (accept regular expression)<br/>
         CALL sys.clear_consumers(table => 'default.T', includingConsumers => '', excludingConsumers => 'myid1.*')<br/><br/>
         -- clear all consumers with includingConsumers and excludingConsumers (accept regular expression)<br/>
         CALL sys.clear_consumers(table => 'default.T', includingConsumers => 'myid.*', excludingConsumers => 'myid1.*')
      </td>
   </tr>
    <tr>
      <td>mark_partition_done</td>
      <td>
         To mark partition to be done. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>partitions: partitions need to be mark done, If you specify multiple partitions, delimiter is ';'.</li>
      </td>
      <td>
         -- mark single partition done<br/>
         CALL sys.mark_partition_done(table => 'default.T', parititions => 'day=2024-07-01')<br/><br/>
         -- mark multiple partitions done<br/>
         CALL sys.mark_partition_done(table => 'default.T', parititions => 'day=2024-07-01;day=2024-07-02')
      </td>
   </tr>
   <tr>
      <td>compact_manifest</td>
      <td>
         To compact_manifest the manifests. Arguments:
            <li>table: the target table identifier. Cannot be empty.</li>
            <li>options: the additional dynamic options of the table. It prioritizes higher than original `tableProp` and lower than `procedureArg`.</li>
      </td>
      <td>
         CALL sys.compact_manifest(`table` => 'default.T')
      </td>
   </tr>
   <tr>
      <td>alter_view_dialect</td>
      <td>
         To alter view dialect. Arguments:
            <li>view: the target view identifier. Cannot be empty.</li>
            <li>action: define change action like: add, update, drop. Cannot be empty.</li>
            <li>engine: when engine which is not spark need define it.</li>
            <li>query: query for the dialect when action is add and update it couldn't be empty.</li>
      </td>
      <td>
         -- add dialect in the view<br/>
         CALL sys.alter_view_dialect('view_identifier', 'add', 'spark', 'query')<br/>
         CALL sys.alter_view_dialect(`view` => 'view_identifier', `action` => 'add', `query` => 'query')<br/><br/>
         -- update dialect in the view<br/>
         CALL sys.alter_view_dialect('view_identifier', 'update', 'spark', 'query')<br/>
         CALL sys.alter_view_dialect(`view` => 'view_identifier', `action` => 'update', `query` => 'query')<br/><br/>
         -- drop dialect in the view<br/>
         CALL sys.alter_view_dialect('view_identifier', 'drop', 'spark')<br/>
         CALL sys.alter_view_dialect(`view` => 'view_identifier', `action` => 'drop')<br/><br/>
      </td>
   </tr>
      <tr>
      <td>create_function</td>
      <td>
         CALL sys.create_function(<br/>
                'function_identifier',<br/>
                '[{"id": 0, "name":"length", "type":"INT"}, {"id": 1, "name":"width", "type":"INT"}]',<br/>
                '[{"id": 0, "name":"area", "type":"BIGINT"}]',<br/>
                true, 'comment', 'k1=v1,k2=v2')<br/>
      </td>
      <td>
         To create a function. Arguments:
            <li>function: the target function identifier. Cannot be empty.</li>
            <li>inputParams: inputParams of the function.</li>
            <li>returnParams: returnParams of the function.</li>
            <li>deterministic: Whether the function is deterministic.</li>
            <li>comment: The comment for the function.</li>
            <li>options: the additional dynamic options of the function.</li>
      </td>
      <td>
         CALL sys.create_function(`function` => 'function_identifier',<br/>
              `inputParams` => '[{"id": 0, "name":"length", "type":"INT"}, {"id": 1, "name":"width", "type":"INT"}]',<br/>
              `returnParams` => '[{"id": 0, "name":"area", "type":"BIGINT"}]',<br/>
              `deterministic` => true,<br/>
              `comment` => 'comment',<br/>
              `options` => 'k1=v1,k2=v2'<br/>
         )<br/>
      </td>
   </tr>
   <tr>
      <td>alter_function</td>
      <td>
         CALL sys.alter_function(<br/>
                'function_identifier',<br/>
                '{"action" : "addDefinition", "name" : "spark", "definition" : {"type" : "lambda", "definition" : "(Integer length, Integer width) -> { return (long) length * width; }", "language": "JAVA" } }')<br/>
      </td>
      <td>
         To alter a function. Arguments:
            <li>function: the target function identifier. Cannot be empty.</li>
            <li>change: change of the function.</li>
      </td>
      <td>
         CALL sys.alter_function(`function` => 'function_identifier',<br/>
              `change` => '{"action" : "addDefinition", "name" : "spark", "definition" : {"type" : "lambda", "definition" : "(Integer length, Integer width) -> { return (long) length * width; }", "language": "JAVA" } }'<br/>
         )<br/>
      </td>
   </tr>
   <tr>
      <td>drop_function</td>
      <td>
         CALL [catalog.]sys.drop_function('function_identifier')<br/>
      </td>
      <td>
         To drop a function. Arguments:
            <li>function: the target function identifier. Cannot be empty.</li>
      </td>
      <td>
         CALL sys.drop_function(`function` => 'function_identifier')<br/>
      </td>
   </tr>
   </tbody>
</table>
