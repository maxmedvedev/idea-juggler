package com.ideajuggler.core

import com.ideajuggler.config.ConfigRepository
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

class DirectoryManager(private val configRepository: ConfigRepository) {

    fun ensureProjectDirectories(projectId: String): ProjectDirectories {
        val root = getProjectRoot(projectId)

        val directories = ProjectDirectories(
            root = root,
            config = root.resolve("config"),
            system = root.resolve("system"),
            logs = root.resolve("logs"),
            plugins = root.resolve("plugins")
        )

        // Create all directories
        Files.createDirectories(directories.config)
        Files.createDirectories(directories.system)
        Files.createDirectories(directories.logs)
        Files.createDirectories(directories.plugins)

        return directories
    }

    fun cleanProject(projectId: String) {
        val projectRoot = getProjectRoot(projectId)
        if (!Files.exists(projectRoot)) return

        @OptIn(ExperimentalPathApi::class)
        projectRoot.deleteRecursively()
    }

    fun getProjectRoot(projectId: String): Path {
        return configRepository.baseDir.resolve("projects").resolve(projectId)
    }
}
