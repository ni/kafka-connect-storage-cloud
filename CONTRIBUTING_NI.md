# Contributing to the NI-maintained fork of Kafka Connect Storage Cloud

- [Contributing to the NI-maintained fork of Kafka Connect Storage Cloud](#contributing-to-the-ni-maintained-fork-of-kafka-connect-storage-cloud)
  - [Overview](#overview)
  - [How do I...](#how-do-i)
    - [Build this locally?](#build-this-locally)
    - [Integrate a new release of kafka-connect-storage-cloud into the fork?](#integrate-a-new-release-of-kafka-connect-storage-cloud-into-the-fork)
    - [Run the integration tests locally?](#run-the-integration-tests-locally)
    - [Make a private change to ni/kafka-connect-storage-cloud?](#make-a-private-change-to-nikafka-connect-storage-cloud)
    - [Get a new build of Kafka Connect Storage Cloud in the Data Frame Service?](#get-a-new-build-of-kafka-connect-storage-cloud-in-the-data-frame-service)
    - [Make a change that could be accepted upstream?](#make-a-change-that-could-be-accepted-upstream)

## Overview

The default branch of this repository is `master`, which contains both
NI-specific changes and changes we intend to upstream. This branch is always
based on a stable tagged commit from upstream, which is documented at the top of
[README.md](README.md).

Changes that will be upstreamed will also live in separate topic branches
prefixed with `ni/pub/`. These branches should be based on the same tag that
upstream is based on, rather than on `master` itself. This approach has several
benefits:

- PRs can be opened on
  [confluentinc/kafka-connect-storage-cloud](https://github.com/confluentinc/kafka-connect-storage-cloud)
  using these branches instead of having to cherry pick commits from `master`.
- Prevents us from accidentally making changes that will be upstreamed dependent
  on private changes.
- Makes it easier to remove changes that were accepted upstream from our
  mainline.

## How do I...

### Build this locally?

These instructions are meant to be run from a Linux machine. We recommend
creating a VM from the
[latest Ubuntu LTS release](https://wiki.ubuntu.com/Releases).

1. Install `sdkman` using the instructions [here](https://sdkman.io/install).
   This tool makes it easy to install the Java build toolchain.
2. Install the OpenJDK 11 LTS release. We arbitrarily use Microsoft's fork:
   > `sdk install java 11.0.16.1-ms`
3. Install `gradle`:
   > `sdk install gradle 7.3.3`
4. Install `maven`. We install an older version, as `3.8` fails builds which
   reference `http` repos:
   > `sdk install maven 3.6.3`

To build this component, five other components must first be cloned and built
locally. This only needs to be repeated when we upgrade our fork
`kafka-connect-storage-cloud`.

| Component                                                                                    | Tag            | Build command                      |
| -------------------------------------------------------------------------------------------- | -------------- | ---------------------------------- |
| [kafka](https://github.com/confluentinc/kafka/)                                              | `v7.2.1-6-ccs` | `./gradlewAll publishToMavenLocal` |
| [common](https://github.com/confluentinc/common)                                             | `v7.2.1-22`    | `mvn install`                      |
| [rest-utils](https://github.com/confluentinc/rest-utils)                                     | `v7.2.1-30`    | `mvn install`                      |
| [schema-registry](https://github.com/confluentinc/schema-registry)                           | `v7.2.1`       | `mvn install`                      |
| [kafka-connect-storage-common](https://github.com/confluentinc/kafka-connect-storage-common) | `v11.0.15`     | `mvn install`                      |
| [kafka-connect-storage-cloud](https://github.com/ni/kafka-connect-storage-cloud)             | `v10.2.2-ni-0` | `mvn install -DskipITs`            |

1. Clone and build `confluentinc/kafka`:
   > `git clone https://github.com/confluentinc/kafka/ && cd kafka && git checkout v7.2.1-6-ccs && ./gradlewAll publishToMavenLocal`
2. Clone and build `confluentinc/common`:
   > `git clone https://github.com/confluentinc/common && cd common && git checkout v7.2.1-22 && mvn install`
3. Clone and build `confluentinc/rest-utils`:
   > `git clone https://github.com/confluentinc/rest-utils && cd rest-utils && git checkout v7.2.1-30 && mvn install`
4. Clone and build `confluentinc/schema-registry`:
   > `git clone https://github.com/confluentinc/schema-registry && cd schema-registry && git checkout v7.2.1 && mvn install`
5. Clone and build `kafka-connect-storage-common`:
   > `git clone https://github.com/confluentinc/kafka-connect-storage-common && cd kafka-connect-storage-common && git checkout v11.0.15 && mvn install`
6. Clone and build `kafka-connect-storage-cloud`:

   > `git clone https://github.com/ni/kafka-connect-storage-cloud && cd kafka-connect-storage-cloud && git checkout v10.1.1-ni-0 && mvn install -DskipITs`

   Note that the `-DskipITs` option will skip the integration tests. If you wish
   to run them, see
   [running the integration tests locally](#run-the-integration-tests-locally).

You are now able to open `kafka-connect-storage-cloud` in your IDE of choice. We
recommend [Intellij](https://www.jetbrains.com/idea/).

### Integrate a new release of kafka-connect-storage-cloud into the fork?

First, pull the latest tags from upstream:

```
git remote add upstream
git fetch upstream --tags
git push origin --tags
```

Backup the current `master` branch, so we can revert if necessary. Append the
most recent tag to the branch name:

```
git checkout master && git pull
git checkout -b master-archive-10.1.1-ni-<latest_version> # Most recent tag for last rebase
git push -u origin master-archive-10.1.1-ni-<latest_version>
```

Find the latest stable release tag by referencing the changelog
[here](https://docs.confluent.io/kafka-connectors/s3-sink/current/changelog.html).

Back on `master`, reset the branch to the desired upstream release tag:

```
git checkout master
git reset --hard v10.2
```

We now need to figure out which tags of `kafka-connect-storage-cloud`'s
dependencies must be checked out and built in order for the build to succeed.
This requires a bit of trial and error. For illustrative purposes, the
instructions below describe how we discovered the current tags that we build,
but you will need to substitute these versions out based on the versions in the
`pom.xml` that we are upgrading to.

1. From [pom.xml](./pom.xml), determine which version of
   `kafka-connect-storage-common` the sink depends on. At the time of writing,
   we depend on `v11.0.15`:
   ```
       <parent>
        <groupId>io.confluent</groupId>
        <artifactId>kafka-connect-storage-common-parent</artifactId>
        <version>11.0.15</version>
      </parent>
   ```
1. Check out the tag of `kafka-connect-storage-common` that we need to build.
   Inspect this component's `pom.xml` to to find the version of `common` that we
   need to build. At the time of writing, the `pom.xml` tells us that we need to
   build version `7.2.1` of `common`:
   ```
       <parent>
        <groupId>io.confluent</groupId>
        <artifactId>common</artifactId>
        <version>7.2.1</version>
      </parent>
   ```
1. The `common` version from `kafka-connect-storage-common`'s `pom.xml` gives us
   a rough of idea of which tags we need to check out and build for the ,
   `kafka`, `common`, `rest-utils`, and `schema-registry` dependencies. The
   process from this point requires a bit of trial and error to figure out which
   specific `v7.2.1-X` tag we need to check out for each component. The process
   is to:
   1. Check out the latest `v7.2.1-X-ccs` tag from
      [the kafka](https://github.com/confluentinc/kafka/) repo and attempt to
      build it with `./gradlewAll publishToMavenLocal`. If you run into build
      errors, try a tag with a different `X` value.
   1. Check out the latest `v7.2.1-X` tag from
      [the common](https://github.com/confluentinc/common) repo and attempt to
      build it with `mvn install`. If you run into build errors, check out a tag
      with a different `X` value. You may need to pick a different tag for
      `kafka` and restart the tag discovery process from there.
   1. Check out the latest `v7.2.1-X` tag from the
      [rest-utils](https://github.com/confluentinc/rest-utils) repo and attempt
      to build it with `mvn install`. If you run into build errors, check out a
      tag with a different `X` value. You may need to pick a different tag for
      `kafka` and restart the tag discovery process from there.
   1. Check out the `v7.2.1` tag from the
      [schema-registry](https://github.com/confluentinc/schema-registry) repo
      and attempt to build it with `mvn install`.
   1. Check out the `v11.0.15` tag from the
      [kafka-connect-storage-common](https://github.com/confluentinc/kafka-connect-storage-common)
      repo and attempt to build it with `mvn install`.
   1. Check out the `v10.2.2` tag from the
      [kafka-connect-storage-cloud](https://github.com/ni/kafka-connect-storage-cloud)
      repo and attempt to build it with `mvn install -DskipITs`.
1. Run the integration tests for `kafka-connect-storage-cloud` locally. See
   [running the integration tests locally](#run-the-integration-tests-locally).
1. Update the instructions and tags in the [table](#build-this-locally) to
   reflect the new tags.
1. Cherry-pick all of our NI-specific commits from the archive branch. The first
   commit should have the message:

   _Add CONTRIBUTING_NI and update README_.

   ```
   # git cherry-pick X^..Y where X is the first commit and Y is the latest on the archive branch you created above
   git cherry-pick 095cea2^..d919979
   ```

   Carefully resolve any merge conflicts. Run `git cherry-pick --skip` for any
   changes that were accepted upstream since the last version bump. Once the
   cherry pick is completed, force push `master` (`git push -f`). If you are not
   an admin, ask one to do this step for you.

Follow the instructions in
[Get a new build of Kafka Connect Storage Cloud in the Data Frame Service](#get-a-new-build-of-kafka-connect-storage-cloud-in-the-data-frame-service)
to create the initial tag for the new version of the fork and uptake it in the
Data Frame Service.

### Run the integration tests locally?

The `kafka-connect-storage-cloud` integration tests require a connection to
Amazon S3. We typically run these tests against the `stratus-dev` AWS account.

1. Create a `~/.aws/credentials` file, where the default profile is configured
   with `us-east-1` as the region, along with access keys capable of creating,
   reading, and deleting buckets and files on S3. Access keys can be retrieved
   from the `nidataframe-s3-credentials` secret on `stratus-dev`. Here is a
   sample file:

   ```
   [default]
   aws_access_key_id = foo
   aws_secret_access_key = bar
   region = us-east-1
   ```

### Make a private change to ni/kafka-connect-storage-cloud?

To make changes to the fork that are NI-specific, simply create a topic branch
based on `master`, make your changes, and then open a pull request merging your
topic branch back into `master`. After the changes are reviewed and accepted,
the branch should be **squash merged** and then deleted. See
[here](#get-a-new-build-of-kafka-connect-storage-cloud-for-the-data-frame-service)
for instructions on how to get a new build of this component uptaken in the Data
Frame Service.

### Get a new build of Kafka Connect Storage Cloud in the Data Frame Service?

1. Create a new `ni` tag. The convention is to append `-ni-X` to the upstream
   tag, where `X` is incremented each time you want to make a new build. For
   example, if you just rebased `master` off of the `v10.2` tag from upstream,
   the initial tag in our fork should be `v10.2.2-ni-0`.
   > `git tag v10.2.2-ni-0`
1. Push the tag:
   > `git push --tags`
1. Create a PR to update the
   [Kafka Connect Dockerfile in AzDO](https://dev.azure.com/ni/DevCentral/_git/Skyline?path=/DataFrameService/kafkaConnectImage/Dockerfile)
   with the new tags.
1. Follow the instructions in the
   [Kafka Connect Readme in AzDO](https://dev.azure.com/ni/DevCentral/_git/Skyline?path=/DataFrameService/kafkaConnectImage/README.md&version=GBmaster&line=64&lineEnd=65&lineStartColumn=1&lineEndColumn=1&lineStyle=plain&_a=contents)
   to uptake the new image in the Data Frame Service.

### Make a change that could be accepted upstream?

In an ideal world, we could make changes to
[confluentinc/kafka-connect-storage-cloud](https://github.com/confluentinc/kafka-connect-storage-cloud)
directly and then integrate them into our fork after they release a new stable
version. However, this could take weeks or months. To keep our development
cycles short, these changes need to also be merged into our fork in the
meantime.

Create a branch prefixed with `ni/pub` based on the same tag that
confluentinc/kafka-connect-storage-cloud's `master` is based on. Example:

```
# Finds the most recent tag reachable from master
git describe --tags --abbrev=0 master
git checkout -b ni/pub/cool-new-feature <latest-tag>
```

Then, make your changes on this branch and open a pull request merging it into
`master`. If your changes on an `ni/pub/*` branch result in merge conflicts with
another `ni/pub/*` branch, rebase your branch on the conflicting branch and
force push. After the changes are reviewed and accepted, the branch should be
**squash merged** and **not** deleted.
