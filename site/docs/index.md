---
layout: home

---

# mules-redis - A Cache Implementation Backed by Redis [![Build Status](https://travis-ci.com/ChristopherDavenport/mules-redis.svg?branch=master)](https://travis-ci.com/ChristopherDavenport/mules-redis) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.chrisdavenport/mules-redis_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.chrisdavenport/mules-redis_2.12)

## Quick Start

To use mules-redis in an existing SBT project with Scala 2.11 or a later version, add the following dependencies to your
`build.sbt` depending on your needs:

```scala
libraryDependencies ++= Seq(
  "io.chrisdavenport" %% "mules-redis" % "<version>"
)
```