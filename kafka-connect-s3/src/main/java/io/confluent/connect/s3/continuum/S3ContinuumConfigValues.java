package io.confluent.connect.s3.continuum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class S3ContinuumConfigValues {
    private static final Logger log = LoggerFactory.getLogger(S3ContinuumConfigValues.class);

    public String topic;
    public int partition;
    public String bootstrapServers;
    public String schemaRegistryURL;
    public String valueConverter;

    public boolean isConfigured() {
        if (stringIsNullOrEmpty(topic) || stringIsNullOrEmpty(bootstrapServers)) {
            ArrayList<String> missingValues = new ArrayList<String>();

            if (stringIsNullOrEmpty(topic)) {
                missingValues.add(S3ContinuumConfig.CONTINUUM_TOPIC_CONFIG);
            }
            if (stringIsNullOrEmpty(bootstrapServers)) {
                missingValues.add(S3ContinuumConfig.CONTINUUM_BOOTSTRAP_SERVERS_CONFIG);
            }
            if (stringIsNullOrEmpty(valueConverter)) {
                missingValues.add(S3ContinuumConfig.CONTINUUM_VALUE_CONVERTER_CONFIG);
            }
            if (stringIsNullOrEmpty(schemaRegistryURL) && !stringIsNullOrEmpty(valueConverter) && valueConverter.toLowerCase().contains("avro")) {
                missingValues.add(S3ContinuumConfig.CONTINUUM_SCHEMA_REGISTRY_URL_CONFIG);
            }

            if (missingValues.size() > 0) {
                log.warn("Continuum properties are partially configured. The following "
                                + "properties need to be configured to work: {}",
                        String.join(",", missingValues));
                return false;
            }

            return true;
        }

        log.info("Continuum properties are not configured.");
        return false;
    }

    private boolean stringIsNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}