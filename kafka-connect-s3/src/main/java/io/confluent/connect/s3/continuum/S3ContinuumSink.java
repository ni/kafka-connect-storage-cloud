package io.confluent.connect.s3.continuum;

import org.apache.kafka.common.config.AbstractConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3ContinuumSink extends S3Continuum {
    private static final Logger log = LoggerFactory.getLogger(S3ContinuumSink.class);

//    private String versionColumnName;
//    private String updatedOnColumnName;

    public S3ContinuumSink(AbstractConfig config) {
        super(config);

//        if (isActive()) {
//            versionColumnName = config.getString(
//                    S3ContinuumConfig.CONTINUUM_VERSION_COLUMN_NAME_CONFIG);
//            updatedOnColumnName = config.getString(
//                    S3ContinuumConfig.CONTINUUM_UPDATED_ON_COLUMN_NAME_CONFIG);
//        }
    }

    public void continueOn(String filename, long offset, long recordCount) {
        if (isActive()) {
            try {
                log.debug("Signalling sink continuum for filename {}, offset {}, recordCount {}",
                        filename, offset, recordCount);
                // todo: key
                produce(filename, filename, offset, recordCount);
            } catch (IllegalStateException e) {
                log.error("Fatal: Cannot produce Continuum record", e);
                stop();
            }
        }
    }
}