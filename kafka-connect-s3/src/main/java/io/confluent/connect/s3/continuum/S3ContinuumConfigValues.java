package io.confluent.connect.s3.continuum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class S3ContinuumConfigValues {
    private static final Logger log = LoggerFactory.getLogger(S3ContinuumConfigValues.class);

    public String topic;
    public String bootstrapServers;
    public String schemaRegistryURL;
    public int partition;
    public String valueConverter;

    public boolean isConfigured() {
        if (topic != "" || bootstrapServers != "") {
            ArrayList<String> missingValues = new ArrayList<String>();

            if (topic == "") {
                missingValues.add(S3ContinuumConfig.CONTINUUM_TOPIC_CONFIG);
            }
            if (bootstrapServers == "") {
                missingValues.add(S3ContinuumConfig.CONTINUUM_BOOTSTRAP_SERVERS_CONFIG);
            }
            if (schemaRegistryURL == "" && valueConverter != null && valueConverter.contains("avro")) {
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
}