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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.AbstractConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

public class S3Continuum {
  private static final Logger log = LoggerFactory.getLogger(S3Continuum.class);

  private Producer<String, Object> producer;
  private String topic;
  private int partition;
  private ObjectMapper mapper;
  private Schema valueSchema;

  private final String avroValueConverterClass
          = io.confluent.kafka.serializers.KafkaAvroSerializer.class.getName();
  private final String jsonValueConverterClass
          = org.apache.kafka.connect.json.JsonSerializer.class.getName();

  public S3Continuum(AbstractConfig config) {
    final S3ContinuumConfigValues continuumConfig = S3ContinuumConfig.parseConfigValues(config);

    if (continuumConfig.isConfigured()) {
      boolean usingAvro = continuumConfig.schemaRegistryURL != "";
      String valueConverter;
      if (usingAvro) {
        valueConverter = avroValueConverterClass;
      } else {
        valueConverter = jsonValueConverterClass;
      }

      Properties producerProperties = new Properties();
      producerProperties.put(BOOTSTRAP_SERVERS_CONFIG, continuumConfig.bootstrapServers);
      producerProperties.put(SCHEMA_REGISTRY_URL_CONFIG, continuumConfig.schemaRegistryURL);
      producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG,
              org.apache.kafka.common.serialization.StringSerializer.class);
      producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG,
              valueConverter);
      this.producer = new KafkaProducer<>(producerProperties);

      if (usingAvro) {
        String s3NotificationSchema =
                "{\"type\":\"record\","
                        + "\"name\":\"" + "new_file_ingested_schema" + "_continuum\","
                        + "\"namespace\":\"io.confluent.connect.s3.continuum\","
                        + "\"fields\":["
                        + "{\"name\":\"filename\",\"type\":\"string\"},"
                        + "{\"name\":\"offset\",\"type\":\"long\"},"
                        + "{\"name\":\"recordCount\",\"type\":\"long\"}"
                        + "]}";
        Schema.Parser parser = new Schema.Parser();
        this.valueSchema = parser.parse(s3NotificationSchema);
      } else {
        this.mapper = new ObjectMapper();
      }

      this.topic = continuumConfig.topic;
      this.partition = continuumConfig.partition;

      log.info("Created Continuum producer with topic {}", topic);
    } else {
      log.info("No Continuum producer created");
    }
  }

  public boolean isActive() {
    return this.producer != null;
  }

  public void produce(String key, String filename, long offset, long recordCount) {
    if (isActive()) {
      boolean usingAvro = this.valueSchema != null;
      if (usingAvro) {
        GenericRecord value = new GenericData.Record(this.valueSchema);
        value.put("filename", filename);
        value.put("offset", offset);
        value.put("recordCount", recordCount);

        this.producer.send(new ProducerRecord<>(this.topic, this.partition, key, value));
      } else {
        JsonNode value = this.mapper.valueToTree(
          new NewFileCommittedMessageBody(filename, offset, recordCount));

        this.producer.send(new ProducerRecord<>(this.topic, this.partition, key, value));
      }
    }
  }

  public void stop() {
    if (producer != null) {
      log.debug("Stopping S3Continuum... Continuum producer detected, closing producer.");
      try {
        this.producer.close(Duration.ofSeconds(10));
      } catch (Throwable t) {
        log.warn("Error while closing the continuum producer: ", t);
      } finally {
        this.producer = null;
      }
    }
  }
}
