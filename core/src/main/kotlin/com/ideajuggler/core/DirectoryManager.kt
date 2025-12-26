package com.ideajuggler.core

import com.ideajuggler.config.ConfigRepository
import com.ideajuggler.platform.ConfigLocator
import com.ideajuggler.platform.PluginLocator
import com.ideajuggler.util.DirectoryCopier
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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

        // Copy base config on first open (BEFORE plugins)
        copyBaseConfigIfNeeded(directories.config)

        // Copy base plugins on first open (AFTER config, will overwrite plugins subdir)
        copyBasePluginsIfNeeded(directories.plugins)

        return directories
    }

    /**
     * Copy plugins from base location if configured and this is first open.
     */
    private fun copyBasePluginsIfNeeded(pluginsDir: Path) {
        val basePluginsPath = getBasePluginsPath() ?: return
        DirectoryCopier.copyIfFirstOpen(basePluginsPath, pluginsDir)
    }

    /**
     * Get the base plugins directory from config or auto-detect.
     */
    fun getBasePluginsPath(): Path? {
        val config = configRepository.load()

        // Use configured path if available
        if (config.basePluginsPath != null) {
            return Paths.get(config.basePluginsPath)
        }

        // Otherwise, try to auto-detect
        return PluginLocator.findDefaultPluginsDirectory()
    }

    /**
     * Copy config files from base location if configured and this is first open.
     */
    private fun copyBaseConfigIfNeeded(configDir: Path) {
        val baseConfigPath = getBaseConfigPath() ?: return
        DirectoryCopier.copyIfFirstOpen(baseConfigPath, configDir)
    }

    /**
     * Get the base config directory from config or auto-detect.
     */
    fun getBaseConfigPath(): Path? {
        val config = configRepository.load()

        // Use configured path if available
        if (config.baseConfigPath != null) {
            return Paths.get(config.baseConfigPath)
        }

        // Otherwise, try to auto-detect
        return ConfigLocator.findDefaultConfigDirectory()
    }

    /**
     * Force sync config from base location, overwriting existing config
     */
    fun syncConfigFromBase(projectId: String) {
        val baseConfigPath = getBaseConfigPath() ?: throw IllegalStateException(
            "Base config path not found. Either configure it using 'idea-juggler config --base-config <path>' or ensure IntelliJ is installed with default paths."
        )
        val projectDirs = ensureProjectDirectories(projectId)
        DirectoryCopier.copy(baseConfigPath, projectDirs.config)
    }

    /**
     * Force sync plugins from base location, overwriting existing plugins
     */
    fun syncPluginsFromBase(projectId: String) {
        val basePluginsPath = getBasePluginsPath() ?: throw IllegalStateException(
            "Base plugins path not found. Either configure it using 'idea-juggler config --base-plugins <path>' or ensure IntelliJ is installed with default paths."
        )
        val projectDirs = ensureProjectDirectories(projectId)
        DirectoryCopier.copy(basePluginsPath, projectDirs.plugins)
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

    companion object {
        fun getInstance(configRepository: ConfigRepository) = DirectoryManager(configRepository)
    }
}
