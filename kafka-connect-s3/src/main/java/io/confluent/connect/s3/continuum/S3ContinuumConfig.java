/*
 * Copyright 2022 National Instruments Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.confluent.connect.s3.continuum;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import java.util.Map;

public class S3ContinuumConfig extends AbstractConfig {
  private static final String CONTINUUM_GROUP = "Continuum";

  public static final String CONTINUUM_TOPIC_CONFIG =
          "continuum.topic";
  private static final String CONTINUUM_TOPIC_DOC =
          "The topic to send events to when new files are written.";
  private static final String CONTINUUM_TOPIC_DISPLAY =
          "Continuum Topic";

  public static final String CONTINUUM_TOPIC_PARTITION_CONFIG =
          "continuum.topic.partition";
  private static final String CONTINUUM_TOPIC_PARTITION_DOC =
          "The partition of the topic to which the Continuum should publish to.";
  private static final String CONTINUUM_TOPIC_PARTITION_DISPLAY =
          "Continuum Topic Partition";

  public static final String CONTINUUM_BOOTSTRAP_SERVERS_CONFIG =
          "continuum.bootstrap.servers";
  private static final String CONTINUUM_BOOTSTRAP_SERVERS_DOC =
          "The Kafka brokers to connect to.";
  private static final String CONTINUUM_BOOTSTRAP_SERVERS_DISPLAY =
          "Continuum Bootstrap Servers";

  public static final String CONTINUUM_AVRO_SCHEMA_REGISTRY_URL_CONFIG =
          "continuum.avro.schema.registry.url";
  private static final String CONTINUUM_AVRO_SCHEMA_REGISTRY_URL_DOC =
          "The schema registry service URL. Setting this will cause the Continuum to publish "
                  + "messages with values serialized using Avro. JSON will be used "
                  + "if this is not set.";
  private static final String CONTINUUM_AVRO_SCHEMA_REGISTRY_URL_DISPLAY =
          "Continuum Avro Schema Registry";

  public S3ContinuumConfig(Map<?, ?> props) {
    super(continuumDefs(new ConfigDef()), props);
  }

  public static S3ContinuumConfigValues parseConfigValues(AbstractConfig config) {
    S3ContinuumConfigValues values = new S3ContinuumConfigValues();

    values.topic = config
            .getString(S3ContinuumConfig.CONTINUUM_TOPIC_CONFIG)
            .trim();
    values.partition = config
            .getInt(S3ContinuumConfig.CONTINUUM_TOPIC_PARTITION_CONFIG);
    values.bootstrapServers = config
            .getString(S3ContinuumConfig.CONTINUUM_BOOTSTRAP_SERVERS_CONFIG);
    values.schemaRegistryURL = config
            .getString(S3ContinuumConfig.CONTINUUM_AVRO_SCHEMA_REGISTRY_URL_CONFIG);

    return values;
  }

  public static ConfigDef continuumDefs(ConfigDef defs) {
    return defs
            .define(
                    CONTINUUM_TOPIC_CONFIG,
                    ConfigDef.Type.STRING,
                    "",
                    (name, value) -> {
                      if (value == null) {
                        return;
                      }

                      String trimmed = ((String) value).trim();

                      if (trimmed.length() > 249) {
                        throw new ConfigException(name, value,
                                "Continuum topic length must not exceed max topic name length"
                                + " , 249 chars");
                      }

                    },
                    ConfigDef.Importance.HIGH,
                    CONTINUUM_TOPIC_DOC,
                    CONTINUUM_GROUP,
                    1,
                    ConfigDef.Width.MEDIUM,
                    CONTINUUM_TOPIC_DISPLAY)
            .define(
                    CONTINUUM_TOPIC_PARTITION_CONFIG,
                    ConfigDef.Type.INT,
                    0,
                    ConfigDef.Importance.LOW,
                    CONTINUUM_TOPIC_PARTITION_DOC,
                    CONTINUUM_GROUP,
                    2,
                    ConfigDef.Width.MEDIUM,
                    CONTINUUM_TOPIC_PARTITION_DISPLAY)
            .define(
                    CONTINUUM_BOOTSTRAP_SERVERS_CONFIG,
                    ConfigDef.Type.STRING,
                    "",
                    ConfigDef.Importance.HIGH,
                    CONTINUUM_BOOTSTRAP_SERVERS_DOC,
                    CONTINUUM_GROUP,
                    3,
                    ConfigDef.Width.MEDIUM,
                    CONTINUUM_BOOTSTRAP_SERVERS_DISPLAY
            ).define(
                    CONTINUUM_AVRO_SCHEMA_REGISTRY_URL_CONFIG,
                    ConfigDef.Type.STRING,
                    "",
                    ConfigDef.Importance.LOW,
                    CONTINUUM_AVRO_SCHEMA_REGISTRY_URL_DOC,
                    CONTINUUM_GROUP,
                    4,
                    ConfigDef.Width.MEDIUM,
                    CONTINUUM_AVRO_SCHEMA_REGISTRY_URL_DISPLAY
            );
  }
}