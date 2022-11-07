package io.confluent.connect.s3.continuum;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;

// TODO: we probably only need the source, since we're producing
public class S3ContinuumSink extends S3Continuum {
    private static final Logger log = LoggerFactory.getLogger(S3ContinuumSink.class);

    private String versionColumnName;
    private String updatedOnColumnName;

    public S3ContinuumSink(AbstractConfig config) {
        super(config);

        if (isActive()) {
            versionColumnName = config.getString(
                    S3ContinuumConfig.CONTINUUM_VERSION_COLUMN_NAME_CONFIG);
            updatedOnColumnName = config.getString(
                    S3ContinuumConfig.CONTINUUM_UPDATED_ON_COLUMN_NAME_CONFIG);
        }
    }

    public void continueOn(Collection<SinkRecord> records, String filename, int offset, int recordCount) {
        if (isActive()) {
            try {
                log.debug("Signalling sink continuum for {} records with filename {} and offset {}",
                        records.size(), filename, offset);
                for (SinkRecord record : records) {

                    log.trace("Signalling sink continuum for {}", record.value());

                    Struct recordValue = (Struct) record.value();
                    String key = record.key().toString();

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