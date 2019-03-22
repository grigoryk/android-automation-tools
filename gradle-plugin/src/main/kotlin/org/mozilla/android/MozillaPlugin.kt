/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.android

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.URI

class MozillaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Add custom repositories for application-services dependency resolution.
        // This should be temporary while https://github.com/mozilla/application-services
        // isn't automatically publishing to maven.mozilla.org yet.
        // See https://github.com/mozilla/application-services/issues/252.
        prependMavenRepository(project, "appservices", "https://dl.bintray.com/mozilla-appservices/application-services")
        // The main repository which eventually will serve everything.
        prependMavenRepository(project, "mozilla-maven", "https://maven.mozilla.org/maven2")
    }

    private fun prependMavenRepository(project: Project, name: String, url: String) {
        with(project) {
            val customURI = URI.create(url)

            // NB: if you change this repository, or add more repositories, please update the repository
            // injection section of the README.me.

            // If there's already a Maven repo with the right URL, or even the right name, roll with it.
            // The name gives the opportunity to customize, if it helps in the wild.
            val existing = project.repositories.find {
                (it is MavenArtifactRepository) && (it.url == customURI || it.name == name)
            }

            // Otherwise, inject the dependency.
            if (existing == null) {
                logger.info("Injecting repository for project '${project}': '${name}' Maven repository with url '${customURI.toASCIIString()}'")
                val customMavenRepo = project.repositories.maven {
                    it.name = name
                    it.url = customURI
                }

                project.repositories.removeAt(project.repositories.size - 1)
                project.repositories.addFirst(customMavenRepo)

                logger.info("Repository list for ${project} after injection:")
                project.repositories.toList().forEach {
                    logger.info("- ${it.getName()}")
                }
            }
        }
    }
}
