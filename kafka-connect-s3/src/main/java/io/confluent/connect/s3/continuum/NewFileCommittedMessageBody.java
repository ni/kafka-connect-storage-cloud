/*
 * Copyright 2022 National Instruments Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.confluent.connect.s3.continuum;

import com.fasterxml.jackson.annotation.JsonProperty;

// Class used for serializing the JSON representation of the
// message written to the Continuum's Kafka topic
public class NewFileCommittedMessageBody {
  @JsonProperty("filename")
  public String filename;

  @JsonProperty("offset")
  public long offset;

  @JsonProperty("recordCount")
  public long recordCount;

  public NewFileCommittedMessageBody() {
  }

  public NewFileCommittedMessageBody(String filename, long offset, long recordCount) {
    this.filename = filename;
    this.offset = offset;
    this.recordCount = recordCount;
  }
}
