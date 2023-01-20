**Attention: This is a fork of
[Kafka Connect Connector for S3](https://github.com/confluentinc/kafka-connect-storage-cloud)
for use in the NI SystemLink™ platform. Significant changes are:**

This repo is based off of the `v10.2.2` tag from upstream.

- Whenever a new file is committed to S3, the sink publishes information about
  that file to a Kafka topic:

  - Filename
  - The number of rows in the file
  - The start and end offsets of the file

- The integration tests assume that they are connecting to an S3 bucket in the
  `us-east-1` region

---

# Kafka Connect Connector for S3

[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bhttps%3A%2F%2Fgithub.com%2Fconfluentinc%2Fkafka-connect-storage-cloud.svg?type=shield)](https://app.fossa.io/projects/git%2Bhttps%3A%2F%2Fgithub.com%2Fconfluentinc%2Fkafka-connect-storage-cloud?ref=badge_shield)

_kafka-connect-storage-cloud_ is the repository for Confluent's
[Kafka Connectors](http://kafka.apache.org/documentation.html#connect) designed
to be used to copy data from Kafka into Amazon S3.

## Kafka Connect Sink Connector for Amazon Simple Storage Service (S3)

Documentation for this connector can be found
[here](http://docs.confluent.io/current/connect/connect-storage-cloud/kafka-connect-s3/docs/index.html).

Blogpost for this connector can be found
[here](https://www.confluent.io/blog/apache-kafka-to-amazon-s3-exactly-once).

# Development

To build a development version you'll need a recent version of Kafka as well as
a set of upstream Confluent projects, which you'll have to build from their
appropriate snapshot branch. See
[the kafka-connect-storage-common FAQ](https://github.com/confluentinc/kafka-connect-storage-common/wiki/FAQ)
for guidance on this process.

You can build _kafka-connect-storage-cloud_ with Maven using the standard
lifecycle phases.

# Running Integration Tests

Integration tests are run as part of `mvn install`; however one needs to first
configure the environment variable`AWS_CREDENTIALS_PATH` to point to a json file
path with following structure:

```
{
    "aws_access_key_id": "<key>",
    "aws_secret_access_key": "<secret>"
}
```

# Contribute

- Source Code: https://github.com/confluentinc/kafka-connect-storage-cloud
- Issue Tracker:
  https://github.com/confluentinc/kafka-connect-storage-cloud/issues
- Learn how to work with the connector's source code by reading our
  [Development and Contribution guidelines](CONTRIBUTING.md).

# License

This project is licensed under the [Confluent Community License](LICENSE).

[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bhttps%3A%2F%2Fgithub.com%2Fconfluentinc%2Fkafka-connect-storage-cloud.svg?type=large)](https://app.fossa.io/projects/git%2Bhttps%3A%2F%2Fgithub.com%2Fconfluentinc%2Fkafka-connect-storage-cloud?ref=badge_large)
