package io.github.igorgf

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * <p> Plugin implementation.
 *
 * <p> The plugin loads preconfigured dependencies blocks via reference name.
 *
 * @since 2.0.0
 * @author Igor Flakiewicz
 */
class DependencyBundlerPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create('dependencyBundles', DependencyBundlesExtension)
        def bundles = project.extensions.getByType(DependencyBundlesExtension).bundles

        project.ext.applyBundle = { String bundleName ->

            if (!bundles.containsKey(bundleName)) {
                throw new GradleException("Dependency bundle '$bundleName' not found")
            }

            project.dependencies {
                bundles.get(bundleName).delegate = delegate
                bundles.get(bundleName).call()
            }
        }
    }

    static class DependencyBundlesExtension {
        final Map<String, Closure> bundles = [:]

        void registerBundle(String refName, Closure bundle) {
            bundles.put(refName, bundle)
        }

        void registerBundles(Map<String, Closure> bundleDefinitions) {
            bundles.putAll(bundleDefinitions)
        }
    }
}
