package io.confluent.connect.s3.continuum;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Validator;
import org.apache.kafka.common.config.ConfigException;

import io.confluent.connect.s3.S3SinkConnectorConfig;

public class S3ContinuumConfig {
  private static final String CONTINUUM_GROUP = "Continuum";

  public static final String CONTINUUM_TOPIC_CONFIG =
      "continuum.topic";
  private static final String CONTINUUM_TOPIC_DOC =
      "The topic to send events to once the record is processed.";
  private static final String CONTINUUM_TOPIC_DISPLAY =
      "Continuum Topic";

  public static final String CONTINUUM_LABEL_CONFIG =
      "continuum.label";
  private static final String CONTINUUM_LABEL_DOC =
      "The label associated with the completion of a connector task.";
  private static final String CONTINUUM_LABEL_DISPLAY =
      "Continuum Label";

  public static final String CONTINUUM_BOOTSTRAP_SERVERS_CONFIG =
      "continuum.bootstrap.servers";
  private static final String CONTINUUM_BOOTSTRAP_SERVERS_DOC =
      "The initial Kafka brokers to established connection to for the continuum topic.";
  private static final String CONTINUUM_BOOTSTRAP_SERVERS_DISPLAY =
      "Continuum Bootstrap Servers";

  public static final String CONTINUUM_SCHEMA_REGISTRY_URL_CONFIG =
      "continuum.schema.registry.url";
  private static final String CONTINUUM_SCHEMA_REGISTRY_URL_DOC =
      "The schema registry service.";
  private static final String CONTINUUM_SCHEMA_REGISTRY_URL_DISPLAY =
      "Continuum Schema Registry";

  public static final String CONTINUUM_VERSION_COLUMN_NAME_CONFIG =
      "continuum.version.column.name";
  private static final String CONTINUUM_VERSION_COLUMN_NAME_DOC =
      "The column name that stores the record version.";
  private static final String CONTINUUM_VERSION_COLUMN_NAME_DISPLAY =
      "Continuum Version Column Name";

  public static final String CONTINUUM_UPDATED_ON_COLUMN_NAME_CONFIG =
      "continuum.updatedOn.column.name";
  private static final String CONTINUUM_UPDATED_ON_COLUMN_NAME_DOC =
      "The column name that stores the record last updated on datetime.";
  private static final String CONTINUUM_UPDATED_ON_COLUMN_NAME_DISPLAY =
      "Continuum Last Updated On Column Name";

  public static S3ContinuumConfigValues parseConfigValues(AbstractConfig config) {
    S3ContinuumConfigValues values = new S3ContinuumConfigValues();

    values.topic = config
        .getString(S3ContinuumConfig.CONTINUUM_TOPIC_CONFIG)
        .trim();
    values.label = config
        .getString(S3ContinuumConfig.CONTINUUM_LABEL_CONFIG)
        .trim();
    values.bootstrapServers = config
        .getString(S3ContinuumConfig.CONTINUUM_BOOTSTRAP_SERVERS_CONFIG);
    values.schemaRegistryURL = config
        .getString(S3ContinuumConfig.CONTINUUM_SCHEMA_REGISTRY_URL_CONFIG);
    values.versionColumnName = config
        .getString(S3ContinuumConfig.CONTINUUM_VERSION_COLUMN_NAME_CONFIG);
    values.updatedOnColumnName = config
        .getString(S3ContinuumConfig.CONTINUUM_UPDATED_ON_COLUMN_NAME_CONFIG);

    return values;
  }

  public static ConfigDef continuumDefs(ConfigDef defs) {
    return defs
      .define(
        CONTINUUM_BOOTSTRAP_SERVERS_CONFIG,
        ConfigDef.Type.STRING,
        "",
        ConfigDef.Importance.LOW,
        CONTINUUM_BOOTSTRAP_SERVERS_DOC,
        CONTINUUM_GROUP,
        1,
        ConfigDef.Width.MEDIUM,
        CONTINUUM_BOOTSTRAP_SERVERS_DISPLAY
      ).define(
        CONTINUUM_SCHEMA_REGISTRY_URL_CONFIG,
        ConfigDef.Type.STRING,
        "",
        ConfigDef.Importance.LOW,
        CONTINUUM_SCHEMA_REGISTRY_URL_DOC,
        CONTINUUM_GROUP,
        2,
        ConfigDef.Width.MEDIUM,
        CONTINUUM_SCHEMA_REGISTRY_URL_DISPLAY
      ).define(
        CONTINUUM_TOPIC_CONFIG,
        ConfigDef.Type.STRING,
        "",
        new Validator() {
          @Override
          public void ensureValid(final String name, final Object value) {
            if (value == null) {
              return;
            }

            String trimmed = ((String) value).trim();

            if (trimmed.length() > 249) {
              throw new ConfigException(name, value,
                  "Continuum topic length must not exceed max topic name length, 249 chars");
            }

//            if (S3SinkConnectorConfig.INVALID_CHARS.matcher(trimmed).find()) {
//              throw new ConfigException(name, value,
//                  "Continuum topic must not contain any character other than "
//                  + "ASCII alphanumerics, '.', '_' and '-'.");
//            }
          }
        },
        ConfigDef.Importance.LOW,
        CONTINUUM_TOPIC_DOC,
        CONTINUUM_GROUP,
        3,
        ConfigDef.Width.MEDIUM,
        CONTINUUM_TOPIC_DISPLAY
      ).define(
        CONTINUUM_LABEL_CONFIG,
        ConfigDef.Type.STRING,
        "S3Connector",
        ConfigDef.Importance.LOW,
        CONTINUUM_LABEL_DOC,
        CONTINUUM_GROUP,
        4,
        ConfigDef.Width.MEDIUM,
        CONTINUUM_LABEL_DISPLAY
      ).define(
        CONTINUUM_VERSION_COLUMN_NAME_CONFIG,
        ConfigDef.Type.STRING,
        "",
        ConfigDef.Importance.HIGH,
        CONTINUUM_VERSION_COLUMN_NAME_DOC,
        CONTINUUM_GROUP,
        5,
        ConfigDef.Width.MEDIUM,
        CONTINUUM_VERSION_COLUMN_NAME_DISPLAY
      ).define(
        CONTINUUM_UPDATED_ON_COLUMN_NAME_CONFIG,
        ConfigDef.Type.STRING,
        "",
        ConfigDef.Importance.HIGH,
        CONTINUUM_UPDATED_ON_COLUMN_NAME_DOC,
        CONTINUUM_GROUP,
        6,
        ConfigDef.Width.MEDIUM,
        CONTINUUM_UPDATED_ON_COLUMN_NAME_DISPLAY);
  }

}