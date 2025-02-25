# Dependency Bundler Plugin

Simple Gradle plugin for bundling dependencies,
while retaining the ability to manage the configuration for each dependency.

---

## Problem case

When applying a generic dependency block to modules, we want to abstract it.

#### desired build.gradle

```groovy
dependencies {
    testImplementation "org.junit.jupiter:junit-jupiter-api"
    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine"
    testRuntimeOnly "org.junit.platform:junit-platform-launcher"
}
```

We create a bundle through the .toml file.

#### libs.versions.toml

```toml
[libraries]
junit-jupiter-api = { module = "org.junit.jupiter:junit-jupiter-api" }
junit-jupiter-engine = { module = "org.junit.jupiter:junit-jupiter-engine" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher" }

[bundles]
junit = ["junit-jupiter-api", "junit-jupiter-engine", "junit-platform-launcher"]
```

And we apply our bundle.

#### incorrect build.gradle

```groovy
dependencies {
    testImplementation libs.bundles.junit
}
```

See how we can only apply the bundle with one configuration method, 
therefore for each configuration method a separate bundle would be required.

This unintuitively bundles the dependencies based on configuration not on use case.

We'd need to create two separate bundles here to abstract this simple dependency block.

---

## Solution

The plugin solves this by letting you set up bundles that specify the configurations for the dependencies.

#### libs.versions.toml

```toml
[libraries]
junit-jupiter-api = { module = "org.junit.jupiter:junit-jupiter-api" }
junit-jupiter-engine = { module = "org.junit.jupiter:junit-jupiter-engine" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher" }
```

It's done by configuring them in a separate file.

#### dependency-bundles.toml

```toml
[junit]
testImplementation = ['junit-jupiter-api']
testRuntimeOnly = ['junit-jupiter-engine', 'junit-platform-launcher']
```

And we apply our bundle by configuring a single property in our `build.gradle`

#### build.gradle

```groovy
dependencyBundles.bundles = ['junit']
```

---

## Basic Configuration

#### 1. Clone the plugin and build with: `./gradlew clean publishToMavenLocal`

#### 2. Add the `dependency-bundles.toml` to your project root and configure your bundles

#### 2. Configure your `build.gradle` to apply teh dependency bundles

```groovy
plugins {
    id 'com.igorgf.dep-bundler'
}

dependencyBundles.bundles = ['yourBundleName', 'yourOtherBundleName']
```

---

## Options

### Defining bundles

- Each section defines a bundle. The section name is the bundle name. It must be unique.
- Each configuration within each bundle is defined once, as a key.
- Dependencies are defined in an array assigned to the configuration key.
- If there is only one dependency for a configuration, then you can omit the `[]`.
- Do not prefix the Version Catalog name when declaring a dependency.

#### Correct Example

```toml
[yourBundleName]
compileOnly = ['dep1', 'dep2']
annotationProcessor = ['dep2']

[yourOtherBundleName]
compileOnly = ['dep3', 'dep4']
implementation = ['dep5']
testImplementation = 'dep6'
```

#### Incorrect Example

- Duplicate bundle name `yourBundleName`.
- Inside `yourBundleName`, `libs.dep2` should've been declared as `dep2`.
- Duplicate configuration keys within the bundle `yourOtherBundleName`.

```toml
[yourBundleName]
compileOnly = ['dep1', 'dep2']
annotationProcessor = ['libs.dep2']

[yourBundleName]
compileOnly = ['depx', 'depy']

[yourOtherBundleName]
implementation = ['dep3', 'dep4']
implementation = ['dep5']
```

### Version Catalogs

The plugin supports and encourages the use of Version Catalogs. 

You define the dependencies inside the bundle config without specifying the Version Catalog name.

#### Configure Catalog

The default catalog is `libs`. Override through `build.gradle`, 

```groovy
dependencyBundles.versionCatalogName = 'mylibs'
```

#### Declare Without Version Catalog

Using version catalogs is optional, both bundles defined in the `dependency-bundles.toml` below are valid.

Where `dep1` comes from a version catalog, but `dep2` and `dep3` are defined explicitly.

```toml
[yourBundleName]
compileOnly = ['dep1', { module = "com.somecomp:dep2" }, { module = "com.somecomp:dep3", version = "1.0.0" }]
```

### Bundles Config Location and Name

By default, the plugin expects the `dependency-bundles.toml` in the project root.

You can change the file location or name by configuring the following in your `build.gradle`,

```groovy
dependencyBundles.bundleConfigFilePath = 'mylibs'
```