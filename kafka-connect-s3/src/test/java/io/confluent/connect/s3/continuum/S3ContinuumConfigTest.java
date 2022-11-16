/*
 * Copyright 2022 National Instruments Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.connect.s3.continuum;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class S3ContinuumConfigTest {
  private Map<String, String> localProps = new HashMap<>();
  private final String topic = "test-topic";
  private final String bootstrapServers = "localhost:9092";
  private final String schemaRegistryURL = "http://localhost:8081";
  private final String avroConverter = "io.confluent.connect.avro.AvroConverter";
  private final String jsonConverter = "org.apache.kafka.connect.json.JsonConverter";
  private final int partition = 0;

  private Map<String, String> createProps() {
    Map<String, String> props = new HashMap<>();
    props.put(S3ContinuumConfig.CONTINUUM_TOPIC_CONFIG, topic);
    props.put(S3ContinuumConfig.CONTINUUM_TOPIC_PARTITION_CONFIG, Integer.toString(partition));
    props.put(S3ContinuumConfig.CONTINUUM_BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    return props;
  }

  private void configureWithAvroConverter() {
    this.localProps.put(S3ContinuumConfig.CONTINUUM_SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);
    this.localProps.put(S3ContinuumConfig.CONTINUUM_VALUE_CONVERTER_CONFIG, avroConverter);
  }

  private void configureWithJsonConverter() {
    this.localProps.put(S3ContinuumConfig.CONTINUUM_VALUE_CONVERTER_CONFIG, jsonConverter);
  }

  @Before
  public void setUp() {
    this.localProps = createProps();
  }

  @After
  public void tearDown() throws Exception {
    localProps.clear();
  }

  @Test
  public void parseConfigValues_ValidAvroValues_Configured() {
    configureWithAvroConverter();

    S3ContinuumConfig config = new S3ContinuumConfig(this.localProps);
    S3ContinuumConfigValues values = S3ContinuumConfig.parseConfigValues(config);

    assertEquals(this.topic, values.topic);
    assertEquals(this.bootstrapServers, values.bootstrapServers);
    assertEquals(this.schemaRegistryURL, values.schemaRegistryURL);
    assertEquals(this.avroConverter, values.valueConverter);
    assertTrue(values.isConfigured());
  }

  @Test
  public void parseConfigValues_ValidJsonValues_Configured() {
    configureWithJsonConverter();

    S3ContinuumConfig config = new S3ContinuumConfig(this.localProps);
    S3ContinuumConfigValues values = S3ContinuumConfig.parseConfigValues(config);

    assertEquals(this.topic, values.topic);
    assertEquals(this.partition, values.partition);
    assertEquals(this.bootstrapServers, values.bootstrapServers);
    assertEquals(this.jsonConverter, values.valueConverter);
    assertTrue(values.isConfigured());
  }

  @Test
  public void parseConfigValues_MissingSchemaRegistryUrl_NotConfigured() {
    configureWithAvroConverter();
    localProps.remove(S3ContinuumConfig.CONTINUUM_SCHEMA_REGISTRY_URL_CONFIG);

    S3ContinuumConfig config = new S3ContinuumConfig(this.localProps);
    S3ContinuumConfigValues values = S3ContinuumConfig.parseConfigValues(config);

    assertFalse(values.isConfigured());
  }

  @Test
  public void parseConfigValues_MissingConverter_NotConfigured() {
    configureWithAvroConverter();
    localProps.remove(S3ContinuumConfig.CONTINUUM_VALUE_CONVERTER_CONFIG);

    S3ContinuumConfig config = new S3ContinuumConfig(this.localProps);
    S3ContinuumConfigValues values = S3ContinuumConfig.parseConfigValues(config);

    assertFalse(values.isConfigured());
  }

  @Test
  public void parseConfigValues_MissingBootstrapServers_NotConfigured() {
    configureWithAvroConverter();
    localProps.remove(S3ContinuumConfig.CONTINUUM_BOOTSTRAP_SERVERS_CONFIG);

    S3ContinuumConfig config = new S3ContinuumConfig(this.localProps);
    S3ContinuumConfigValues values = S3ContinuumConfig.parseConfigValues(config);

    assertFalse(values.isConfigured());
  }

  @Test
  public void parseConfigValues_MissingTopic_NotConfigured() {
    configureWithAvroConverter();
    localProps.remove(S3ContinuumConfig.CONTINUUM_TOPIC_CONFIG);

    S3ContinuumConfig config = new S3ContinuumConfig(this.localProps);
    S3ContinuumConfigValues values = S3ContinuumConfig.parseConfigValues(config);

    assertFalse(values.isConfigured());
  }

  @Test
  public void parseConfigValues_MissingPartition_Configured() {
    configureWithAvroConverter();
    localProps.remove(S3ContinuumConfig.CONTINUUM_TOPIC_PARTITION_CONFIG);

    S3ContinuumConfig config = new S3ContinuumConfig(this.localProps);
    S3ContinuumConfigValues values = S3ContinuumConfig.parseConfigValues(config);

    assertTrue(values.isConfigured());
  }
}
