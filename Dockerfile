# Using maven 3.6 to work around 3.8 failing builds which reference http repos
FROM maven:3.6-openjdk-11 as base
ARG kafka_commit_hash=f9593f29356f472cfa821d41d886b0d5b4229050
ARG common_commit_hash=01985c6d1c482779aba3b6daa55707c224e06027
ARG rest_utils_commit_hash=1b2014bd46ed1194cdd5113ea3979ac1a12c0da0
ARG schema_registry_commit_hash=76ef8c91bec11002583ac315d515558731fd6503
ARG kafka_connect_commit_hash=d50bd5c24a981801744b755e5cd1cb2bed9281a3

# Before we can build kafka-connect-storage-cloud, we must first build its dependencies
# See the FAQ here: https://github.com/confluentinc/kafka-connect-storage-common/wiki/FAQ
# Note that the FAQ has incorrect build instructions for Kafka, and fails to mention that kafka-connect-storage-common
# is also a dependency.

# Clone and build kafka
RUN git clone https://github.com/confluentinc/kafka/ && cd kafka && git checkout v7.2.1-6-ccs
RUN cd kafka && ./gradlewAll publishToMavenLocal

# Clone and build common
RUN git clone https://github.com/confluentinc/common && cd common && git checkout v7.2.1-22
RUN cd common && mvn install

# Clone and build rest-utils
RUN git clone https://github.com/confluentinc/rest-utils && cd rest-utils && git checkout v7.2.1-30
RUN cd rest-utils && mvn install

# Clone and build schema-registry
RUN git clone https://github.com/confluentinc/schema-registry && cd schema-registry && git checkout v7.2.1
RUN cd schema-registry && mvn install

# Clone and build kafka-connect-storage-common
RUN git clone https://github.com/confluentinc/kafka-connect-storage-common && cd kafka-connect-storage-common && git checkout v11.0.12
RUN cd kafka-connect-storage-common && mvn install

# RUN mkdir /root/.aws
# ADD credentials /root/.aws/credentials

# Clone and build kafka-connect-storage-cloud
RUN git clone https://github.com/ni/kafka-connect-storage-cloud && cd kafka-connect-storage-cloud && git checkout users/pvallone/wip-on-v10.2
RUN cd kafka-connect-storage-cloud && mvn install -DskipITs

ENTRYPOINT ["tail", "-f", "/dev/null"]
# RUN /bin/sh
