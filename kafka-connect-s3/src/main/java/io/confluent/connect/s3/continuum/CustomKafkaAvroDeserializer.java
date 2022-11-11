package io.confluent.connect.s3.continuum;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.Schema;

import java.util.List;

public class CustomKafkaAvroDeserializer extends KafkaAvroDeserializer {
//    public S3Continuum continuum;
    @Override
    public Object deserialize(String topic, byte[] bytes) {
        if (topic.equals(S3Continuum.topic)) {
            this.schemaRegistry = getMockClient(S3Continuum.valueSchema);
        }
        return super.deserialize(topic, bytes);
    }

    private static SchemaRegistryClient getMockClient(final Schema schema$) {
        return new MockSchemaRegistryClient() {
            @Override
            public synchronized ParsedSchema getSchemaById(int id) {
                return new ParsedSchema() {
                    @Override
                    public String rawSchema() {
                        return schema$.toString();
                    }

                    @Override
                    public String schemaType() {
                        return "AVRO";
                    }

                    @Override
                    public String name() {
                        return "foo_continuum";
                    }

                    @Override
                    public String canonicalString() {
                        return null;
                    }

                    @Override
                    public List<SchemaReference> references() {
                        return null;
                    }

                    @Override
                    public List<String> isBackwardCompatible(ParsedSchema previousSchema) {
                        return null;
                    }
                };
            }
//            @Override
//            public synchronized Schema getById(int id) {
//                return schema$;
//            }
        };
    }
}
