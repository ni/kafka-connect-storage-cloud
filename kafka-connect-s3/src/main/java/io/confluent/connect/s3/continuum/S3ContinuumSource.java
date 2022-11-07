package io.confluent.connect.s3.continuum;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;

public class S3ContinuumSource extends S3Continuum {
    private static final Logger log = LoggerFactory.getLogger(S3ContinuumSource.class);

    //  private String incrementingColumnName;
    private String versionColumnName;
    private String updatedOnColumnName;

    public S3ContinuumSource(AbstractConfig config) {
        super(config);

        if (isActive()) {
//      incrementingColumnName = config.getString(
//          S3SinkConnectorConfig.INCREMENTING_COLUMN_NAME_CONFIG);
            versionColumnName = config.getString(
                    S3ContinuumConfig.CONTINUUM_VERSION_COLUMN_NAME_CONFIG);
            updatedOnColumnName = config.getString(
                    S3ContinuumConfig.CONTINUUM_UPDATED_ON_COLUMN_NAME_CONFIG);
        }
    }

    public void continueOn(Collection<SourceRecord> records, String filename, int offset, int recordCount) {
        if (isActive()) {
            try {
                log.debug("Signalling source continuum for {} records with filename {} and offset {}",
                        records.size(), filename, offset);
                for (SourceRecord record : records) {
                    log.trace("Signalling source continuum for {}", record.value());

                    Struct recordValue = (Struct) record.value();
//          String key = recordValue.get(incrementingColumnName).toString();
                    String key = "foo"; // todo

                    String version = null;
                    if (versionColumnName.length() > 0) {
                        version = recordValue.get(versionColumnName).toString();
                    }

                    Date updatedOn = null;
                    if (updatedOnColumnName.length() > 0) {
                        updatedOn = (Date) recordValue.get(updatedOnColumnName);
                    }

                    produce(key, filename, offset, recordCount, version, updatedOn);
                }
            } catch (IllegalStateException e) {
                log.error("Fatal: Cannot produce Continuum record", e);
                stop();
            }
        }
    }
}