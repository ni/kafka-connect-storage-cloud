package io.confluent.connect.s3.continuum;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.AbstractConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG; // TODO: different import here than we're used to
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

public class S3Continuum {
    private static final Logger log = LoggerFactory.getLogger(S3Continuum.class);

    private Producer<Object, Object> producer;
    private String topic;
    private String label; // TODO, what's this do?
    private Schema valueSchema; // TODO

    public S3Continuum(AbstractConfig config) {
        final S3ContinuumConfigValues continuumConfig = S3ContinuumConfig.parseConfigValues(config);

        if (continuumConfig.isConfigured()) {
            Properties props = new Properties();
            props.put(BOOTSTRAP_SERVERS_CONFIG, continuumConfig.bootstrapServers);
            props.put(SCHEMA_REGISTRY_URL_CONFIG, continuumConfig.schemaRegistryURL);
            props.put(KEY_SERIALIZER_CLASS_CONFIG,
                    org.apache.kafka.common.serialization.StringSerializer.class);
            props.put(VALUE_SERIALIZER_CLASS_CONFIG,
                    io.confluent.kafka.serializers.KafkaAvroSerializer.class);
            producer = new KafkaProducer<>(props);

            String s3NotificationSchema =
                    "{\"type\":\"record\","
                            + "\"name\":\"" + continuumConfig.topic + "_continuum\","
                            + "\"namespace\":\"io.confluent.connect.s3.continuum\","
                            + "\"fields\":["
                            + "{\"name\":\"label\",\"type\":\"string\"},"
                            + "{\"name\":\"filename\",\"type\":\"string\"},"
                            + "{\"name\":\"offset\",\"type\":\"int\"},"
                            + "{\"name\":\"recordCount\",\"type\":\"int\"},"
                            + "{\"name\":\"version\",\"type\":[\"null\",\"string\"],\"default\":null},"
                            + "{\"name\":\"updatedOn\",\"type\":[\"null\",{\"type\":\"long\","
                            + "\"logicalType\":\"timestamp-millis\"}],\"default\":null}"
                            + "]}";
            Schema.Parser parser = new Schema.Parser();
            valueSchema = parser.parse(s3NotificationSchema);

            topic = continuumConfig.topic;
            label = continuumConfig.label;

            log.info("Created Continuum producer with topic {}", topic);
        } else {
            log.info("No Continuum producer created");
        }
    }

    public boolean isActive() {
        return producer != null;
    }

    public void produce(String key, String filename, int offset, int recordCount, String version, Date updatedOn) {
        GenericRecord value = new GenericData.Record(valueSchema);
        value.put("label", label);
        value.put("filename", filename);
        value.put("offset", offset);
        value.put("recordCount", recordCount);
        value.put("version", version);
        value.put("updatedOn", updatedOn != null ? updatedOn.getTime() : null);

        producer.send(new ProducerRecord<Object, Object>(topic, key, value));
    }

    public void stop() {
        if (producer != null) {
            log.debug("Stopping S3Continuum... Continuum producer detected, closing producer.");
            try {
                producer.close();
            } catch (Throwable t) {
                log.warn("Error while closing the {} continuum producer: ", label, t);
            } finally {
                producer = null;
            }
        }
    }
}
