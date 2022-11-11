package io.confluent.connect.s3.continuum;

import com.fasterxml.jackson.annotation.JsonProperty;

// Class used for serializing the JSON representation of the message written to the Continuum's Kafka topic
public class NewFileWrittenMessageBody {
    @JsonProperty("filename")
    public String filename;

    @JsonProperty("offset")
    public long offset;

    @JsonProperty("recordCount")
    public long recordCount;

    public NewFileWrittenMessageBody() {
    }

    public NewFileWrittenMessageBody(String filename, long offset, long recordCount) {
        this.filename = filename;
        this.offset = offset;
        this.recordCount = recordCount;
    }
}
