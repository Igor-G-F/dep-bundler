# Dependency Bundler Plugin

Simple Gradle plugin for bundling dependencies,
while retaining the ability to manage the configuration for each dependency.

---

## Problem case

When applying a generic dependency block to modules, we want to abstract it. 
Such that it is reusable across the project and the many `build.gradle` files within the project.

- Below are the dependency structures we want in our modules.
- Notice how entire blocks of dependencies that are always together and similarly configured are repeated between the modules.
- At the same time we cannot put this configuration in the `subprojects {}` block as not all dependencies are repeated.

#### Desired `build.gradle` of a module A

```groovy
dependencies {
    // exposed dependencies
    api "com.fasterxml.jackson.core:jackson-core"
    api "com.fasterxml.jackson.core:jackson-databind"
    api "com.fasterxml.jackson.core:jackson-annotations"

    // persistence dependency with an exclusion
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:3.4.3") {
        exclude group: 'org.mongodb', module: 'mongodb-driver-sync'
        exclude group: 'org.mongodb', module: 'mongodb-driver-core'
    }
    // override of excluded dependencies
    implementation('org.mongodb:mongodb-driver-sync:5.1.4')
    implementation('org.mongodb:mongodb-driver-core:5.1.4')
}
```

#### Desired `build.gradle` of a module B

```groovy
dependencies {
    // exposed dependencies
    api "com.fasterxml.jackson.core:jackson-core"
    api "com.fasterxml.jackson.core:jackson-databind"
    api "com.fasterxml.jackson.core:jackson-annotations"

    // test dependencies
    testImplementation "org.junit.jupiter:junit-jupiter-api"
    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine"
    testRuntimeOnly "org.junit.platform:junit-platform-launcher"
}
```

## Dependency Bundler Solution

The plugin allows configuration of dependency blocks and then calling them via a new extensions method `applyBundle`.

#### Project root `settings.gradle`

```groovy
pluginManagement {
    repositories {
        mavenCentral() // Make sure plugins can be fetched from maven central
    }
}

rootProject.name = 'example'
include 'submodule-A'
include 'submodule-B'
```

#### Project root `build.gradle`

```groovy
plugins {
    id "io.github.igor-g-f.dep-bundler" "2.0.0" // apply plugin to project
}

subprojects {
    apply plugin: "io.github.igor-g-f.dep-bundler" // apply plugin to submodules

    // register multiple dependency blocks/bundles via extension method, supplying a Map<String, Closure>
    dependencyBundles.registerBundles([
            test: {
                testImplementation "org.junit.jupiter:junit-jupiter-api"
                testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine"
                testRuntimeOnly "org.junit.platform:junit-platform-launcher"
            },
            mongo: {
                // persistence dependency with an exclusion
                implementation("org.springframework.boot:spring-boot-starter-data-mongodb:3.4.3") {
                    exclude group: "org.mongodb", module: "mongodb-driver-sync"
                    exclude group: "org.mongodb", module: "mongodb-driver-core"
                }
                // override of excluded dependencies
                implementation("org.mongodb:mongodb-driver-sync:5.1.4")
                implementation("org.mongodb:mongodb-driver-core:5.1.4")
            }
    ])

    // register individual dependency blocks via extension method
    dependencyBundles.registerBundle "jacksonApi", {
        api "com.fasterxml.jackson.core:jackson-core"
        api "com.fasterxml.jackson.core:jackson-databind"
        api "com.fasterxml.jackson.core:jackson-annotations"
    }
    
    // registered dependency blocks/bundles are additive and stored in a map
    // therefore reusing a name will overwrite an existing config

    repositories {
        mavenCentral()
    }
}
```

#### `build.gradle` of a module A

```groovy
// No need to specify the plugin as it's loaded via `subprojects {}` block

dependencies {
    applyBundle "jackson"
    applyBundle "mongo"
}
```

#### `build.gradle` of a module B

```groovy
// No need to specify the plugin as it's loaded via `subprojects {}` block

dependencies {
    applyBundle "jackson"
}

applyBundle "test" // can be called outside of the `dependencies {}` block, but it's recommended to keep it inside for readability
```

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