package io.confluent.connect.s3;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewFileWrittenMessageBody {
    @JsonProperty("filename")
    public String filename;

    @JsonProperty("offset")
    public Long offset;

    @JsonProperty("recordCount")
    public Long recordCount;

    public NewFileWrittenMessageBody(){

    }
}
