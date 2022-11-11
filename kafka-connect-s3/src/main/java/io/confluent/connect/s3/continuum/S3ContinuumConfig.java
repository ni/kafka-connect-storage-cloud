package io.confluent.connect.s3.continuum;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Validator;
import org.apache.kafka.common.config.ConfigException;

public class S3ContinuumConfig {
    private static final String CONTINUUM_GROUP = "Continuum";

    public static final String CONTINUUM_TOPIC_CONFIG =
            "continuum.topic";
    private static final String CONTINUUM_TOPIC_DOC =
            "The topic to send events to once the record is processed.";
    private static final String CONTINUUM_TOPIC_DISPLAY =
            "Continuum Topic";

    public static final String CONTINUUM_BOOTSTRAP_SERVERS_CONFIG =
            "continuum.bootstrap.servers";
    private static final String CONTINUUM_BOOTSTRAP_SERVERS_DOC =
            "The initial Kafka brokers to established connection to for the continuum topic.";
    private static final String CONTINUUM_BOOTSTRAP_SERVERS_DISPLAY =
            "Continuum Bootstrap Servers";

    public static final String CONTINUUM_SCHEMA_REGISTRY_URL_CONFIG =
            "continuum.schema.registry.url";
    private static final String CONTINUUM_SCHEMA_REGISTRY_URL_DOC =
            "The schema registry service. Required if using Avro as the value converter";
    private static final String CONTINUUM_SCHEMA_REGISTRY_URL_DISPLAY =
            "Continuum Schema Registry";

    public static final String CONTINUUM_VALUE_CONVERTER_CONFIG =
            "continuum.value.converter";
    private static final String CONTINUUM_VALUE_CONVERTER_DOC =
            "The class to use to serialize the values written to the Kafka notification topic. " +
                    "Supported values include io.confluent.kafka.serializers.KafkaAvroSerializer.class for AVRO," +
                    " and org.apache.kafka.connect.json.JsonSerializer for JSON";
    private static final String CONTINUUM_VALUE_CONVERTER_DISPLAY =
            "Continuum Value Serializer";

    public static final String CONTINUUM_TOPIC_PARTITION_CONFIG =
            "continuum.topic.partition";
    private static final String CONTINUUM_TOPIC_PARTITION_DOC =
            "The partition to which the Continuum should publish to.";
    private static final String CONTINUUM_TOPIC_PARTITION_DISPLAY =
            "Continuum Topic Partition";

    public static S3ContinuumConfigValues parseConfigValues(AbstractConfig config) {
        S3ContinuumConfigValues values = new S3ContinuumConfigValues();

        values.topic = config
                .getString(S3ContinuumConfig.CONTINUUM_TOPIC_CONFIG)
                .trim();
        values.bootstrapServers = config
                .getString(S3ContinuumConfig.CONTINUUM_BOOTSTRAP_SERVERS_CONFIG);
        values.schemaRegistryURL = config
                .getString(S3ContinuumConfig.CONTINUUM_SCHEMA_REGISTRY_URL_CONFIG);
        values.partition = config
                .getInt(S3ContinuumConfig.CONTINUUM_TOPIC_PARTITION_CONFIG);
        values.valueConverter = config.getString(S3ContinuumConfig.CONTINUUM_VALUE_CONVERTER_CONFIG);

        return values;
    }

    public static ConfigDef continuumDefs(ConfigDef defs) {
        return defs
                .define(
                        CONTINUUM_BOOTSTRAP_SERVERS_CONFIG,
                        ConfigDef.Type.STRING,
                        "",
                        ConfigDef.Importance.LOW,
                        CONTINUUM_BOOTSTRAP_SERVERS_DOC,
                        CONTINUUM_GROUP,
                        1,
                        ConfigDef.Width.MEDIUM,
                        CONTINUUM_BOOTSTRAP_SERVERS_DISPLAY
                ).define(
                        CONTINUUM_TOPIC_CONFIG,
                        ConfigDef.Type.STRING,
                        "",
                        new Validator() {
                            @Override
                            public void ensureValid(final String name, final Object value) {
                                if (value == null) {
                                    return;
                                }

                                String trimmed = ((String) value).trim();

                                if (trimmed.length() > 249) {
                                    throw new ConfigException(name, value,
                                            "Continuum topic length must not exceed max topic name length, 249 chars");
                                }

                            }
                        },
                        ConfigDef.Importance.LOW,
                        CONTINUUM_TOPIC_DOC,
                        CONTINUUM_GROUP,
                        3,
                        ConfigDef.Width.MEDIUM,
                        CONTINUUM_TOPIC_DISPLAY
                ).define(
                        CONTINUUM_SCHEMA_REGISTRY_URL_CONFIG,
                        ConfigDef.Type.STRING,
                        "",
                        ConfigDef.Importance.LOW,
                        CONTINUUM_SCHEMA_REGISTRY_URL_DOC,
                        CONTINUUM_GROUP,
                        2,
                        ConfigDef.Width.MEDIUM,
                        CONTINUUM_SCHEMA_REGISTRY_URL_DISPLAY
                ).define(
                        CONTINUUM_TOPIC_PARTITION_CONFIG,
                        ConfigDef.Type.INT,
                        1,
                        ConfigDef.Importance.LOW,
                        CONTINUUM_TOPIC_PARTITION_DOC,
                        CONTINUUM_GROUP,
                        2,
                        ConfigDef.Width.MEDIUM,
                        CONTINUUM_TOPIC_PARTITION_DISPLAY
                ).define(
                        CONTINUUM_VALUE_CONVERTER_CONFIG,
                        ConfigDef.Type.STRING,
                        "",
                        ConfigDef.Importance.HIGH,
                        CONTINUUM_VALUE_CONVERTER_DOC,
                        CONTINUUM_GROUP,
                        2,
                        ConfigDef.Width.MEDIUM,
                        CONTINUUM_VALUE_CONVERTER_DISPLAY);
    }
}