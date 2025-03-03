package io.github.igorgf

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * <p> Configuration properties for the plugin.
 *
 * @see DependencyBundlerPlugin
 * @since 1.0.0
 * @author Igor Flakiewicz
 */
interface DependencyBundlerExtension {
    /**
     * @return Location of the file with the bundle configurations.
     */
    Property<String> getBundleConfigFilePath()

    /**
     * @return Bundles to be added to the module.
     */
    ListProperty<String> getBundles()

    /**
     * @return Version catalog in which to look for dependencies configured in the bundles.
     */
    Property<String> getVersionCatalogName()
}