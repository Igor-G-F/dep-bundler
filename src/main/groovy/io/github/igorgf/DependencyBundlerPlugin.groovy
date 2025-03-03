package io.github.igorgf

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.slf4j.LoggerFactory
import org.tomlj.Toml
import org.tomlj.TomlArray
import org.tomlj.TomlParseResult
import org.tomlj.TomlTable

/**
 * <p> Plugin implementation.
 *
 * <p> The plugin loads a .toml file that defines groupings and configuration options of dependencies, and then applies those dependencies.
 *
 * @since 1.0.0
 * @author Igor Flakiewicz
 */
class DependencyBundlerPlugin implements Plugin<Project> {

    def logger = LoggerFactory.getLogger(DependencyBundlerPlugin)

    @Override
    void apply(Project project) {
        def config = project.extensions.create('dependencyBundles', DependencyBundlerExtension)
        config.bundleConfigFilePath.convention('../dependency-bundles.toml')
        config.bundles.convention([])
        config.versionCatalogName.convention('libs')

        project.task('applyDependencyBundles') {

            project.afterEvaluate {
                configureDependencies(project, config)
            }
        }
    }

    private void configureDependencies(
            Project project,
            DependencyBundlerExtension pluginConfig
    ) {
        def bundles = loadBundlesFromFile(project, pluginConfig.getBundleConfigFilePath().get())

        def versionCatalogs = project.extensions.getByType(VersionCatalogsExtension)
        def libs = versionCatalogs.named(pluginConfig.getVersionCatalogName().get())
        def requiredBundles = pluginConfig.getBundles().get()
        logger.info('Applying dependency bundles: {}', requiredBundles)

        pluginConfig.getBundles().get().each { requiredBundle ->
            def bundle = bundles.get(requiredBundle)
            if (bundle == null) {
                throw new IllegalArgumentException('No bundle config found for bundle: ' + requiredBundle)
            }

            bundle.each { config, dependencies ->
                dependencies.each { dependency ->
                    def libraryOptional = libs.findLibrary(dependency as String)
                    if (libraryOptional.isPresent()) {
                        project.dependencies.add(config, libraryOptional.get().get())
                    } else {
                        project.dependencies.add(config, dependency)
                    }
                }
            }
        }
    }

    private static Map<String, Map<String, List<Object>>> loadBundlesFromFile(
            Project project,
            String path
    ) {
        def tomlFile = project.file(path)
        if (!tomlFile.exists()) {
            throw new FileNotFoundException("Could not find " + path)
        }

        TomlParseResult dependencyBundles = Toml.parse(tomlFile.toPath())
        def bundleMap = new HashMap()
        dependencyBundles.keySet().each { sectionName ->
            TomlTable sectionTable = dependencyBundles.getTable(sectionName)
            def configMap = new HashMap()

            sectionTable.keySet().each { configType ->
                def value = sectionTable.get(configType)

                if (value instanceof TomlArray) {
                    configMap[configType] = value.toList()
                } else if (value instanceof String) {
                    configMap[configType] = Arrays.asList(value)
                } else {
                    throw new IllegalArgumentException("Dependencies must be strings or arrays of strings.")
                }
            }

            bundleMap[sectionName] = configMap
        }
        return bundleMap
    }


}
