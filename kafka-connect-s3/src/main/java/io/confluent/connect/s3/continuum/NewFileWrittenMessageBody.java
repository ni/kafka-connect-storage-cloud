package io.confluent.connect.s3.continuum;

import com.fasterxml.jackson.annotation.JsonProperty;

// Class containing the JSON representation of the value written to the Continuum notification topic
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
