package io.confluent.connect.s3.continuum;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.connect.s3.NewFileWrittenMessageBody;
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

    private Producer<Object, Object> producer;
    private String topic;
    private int partition;
    private ObjectMapper mapper;
    private Schema valueSchema;

    public S3Continuum(AbstractConfig config) {
        final S3ContinuumConfigValues continuumConfig = S3ContinuumConfig.parseConfigValues(config);

        if (continuumConfig.isConfigured()) {
            Properties props = new Properties();
            props.put(BOOTSTRAP_SERVERS_CONFIG, continuumConfig.bootstrapServers);
            props.put(SCHEMA_REGISTRY_URL_CONFIG, continuumConfig.schemaRegistryURL);
            props.put(KEY_SERIALIZER_CLASS_CONFIG,
                    org.apache.kafka.common.serialization.StringSerializer.class);
            // TODO: use value converter from config
            props.put(VALUE_SERIALIZER_CLASS_CONFIG,
                    org.apache.kafka.connect.json.JsonSerializer.class.getName());
            producer = new KafkaProducer<>(props);

            if (continuumConfig.schemaRegistryURL != "") {
                String s3NotificationSchema =
                        "{\"type\":\"record\","
                                + "\"name\":\"" + continuumConfig.topic + "_continuum\","
                                + "\"namespace\":\"io.confluent.connect.s3.continuum\","
                                + "\"fields\":["
                                + "{\"name\":\"filename\",\"type\":\"string\"},"
                                + "{\"name\":\"offset\",\"type\":\"long\"},"
                                + "{\"name\":\"recordCount\",\"type\":\"long\"},"
                                + "{\"name\":\"version\",\"type\":[\"null\",\"string\"],\"default\":null},"
                                + "{\"name\":\"updatedOn\",\"type\":[\"null\",{\"type\":\"long\","
                                + "\"logicalType\":\"timestamp-millis\"}],\"default\":null}"
                                + "]}";
                Schema.Parser parser = new Schema.Parser();
                valueSchema = parser.parse(s3NotificationSchema);
            } else {
                mapper = new ObjectMapper();
            }

            topic = continuumConfig.topic;
            partition = continuumConfig.partition;

            log.info("Created Continuum producer with topic {}", topic);
        } else {
            log.info("No Continuum producer created");
        }
    }

    public boolean isActive() {
        return producer != null;
    }

    public void produce(String key, String filename, long offset, long recordCount) {
        if (isActive()) {
            boolean usingAvro = valueSchema != null;
            if (usingAvro) {
                GenericRecord value = new GenericData.Record(valueSchema);
                value.put("filename", filename);
                value.put("offset", offset);
                value.put("recordCount", recordCount);
                value.put("version", null); // todo
                producer.send(new ProducerRecord<>(topic, key, value));
            } else {
                NewFileWrittenMessageBody body = new NewFileWrittenMessageBody();
                body.filename = filename;
                body.offset = offset;
                body.recordCount = recordCount;
                JsonNode node = mapper.valueToTree(body);

                producer.send(new ProducerRecord<>(topic, partition, key, node));
            }
        }
    }

    public void stop() {
        if (producer != null) {
            log.debug("Stopping S3Continuum... Continuum producer detected, closing producer.");
            try {
                producer.close(Duration.ofSeconds(10));
            } catch (Throwable t) {
                log.warn("Error while closing the continuum producer: ", t);
            } finally {
                producer = null;
            }
        }
    }
}
