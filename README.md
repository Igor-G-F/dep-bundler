# Dependency Bundler Plugin

Simple Gradle plugin for bundling dependencies,
while retaining the ability to manage the configuration for each dependency.

---

## Problem case

When applying a generic dependency block to modules, we want to abstract it. Such that it is reusable across the project and the many `build.gradle` files within the project.

#### desired build.gradle

```groovy
dependencies {
    testImplementation "org.junit.jupiter:junit-jupiter-api"
    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine"
    testRuntimeOnly "org.junit.platform:junit-platform-launcher"
}
```

[//]: # (TODO: Add Documentation)

---

## Plugin Publishing

Following environment variables must be configured to publish the plugin.

```bash
SIGNING_KEY=Your PGP signing key
SIGNING_PASSWORD=Your PGP signing password
ORG_GRADLE_PROJECT_mavenCentralUsername=Your maven central user key
ORG_GRADLE_PROJECT_mavenCentralPassword=Your maven central user password key
```

To publish run:

```bash
./gradlew publishAllPublicationsToMavenCentralRepository
```