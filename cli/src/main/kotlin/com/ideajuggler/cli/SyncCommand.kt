package com.ideajuggler.cli

import com.ideajuggler.cli.framework.*
import com.ideajuggler.config.ConfigRepository
import com.ideajuggler.core.ProjectIdGenerator
import com.ideajuggler.core.ProjectLauncher
import com.ideajuggler.core.ProjectManager
import com.ideajuggler.util.PathUtils
import java.nio.file.Path
import kotlin.io.path.exists

class SyncCommand : Command(
    name = "sync",
    help = "Synchronize project settings with base settings"
) {
    private val projectIdentifierArg = StringArgument(
        name = "project-id-or-path",
        help = "Project ID or path"
    ).also { arguments.add(it) }

    private val vmOptionsFlag = FlagOption(
        shortName = null,
        longName = "vmoptions",
        help = "Sync VM options from base-vmoptions"
    ).also { options.add(it) }

    private val configFlag = FlagOption(
        shortName = null,
        longName = "config",
        help = "Sync config from base-config"
    ).also { options.add(it) }

    private val pluginsFlag = FlagOption(
        shortName = null,
        longName = "plugins",
        help = "Sync plugins from base-plugins"
    ).also { options.add(it) }

    private val allFlag = FlagOption(
        shortName = "a",
        longName = "all",
        help = "Sync all settings (vmoptions, config, plugins)"
    ).also { options.add(it) }

    override fun run() {
        val projectIdentifier = projectIdentifierArg.getValue()
        val syncVmOptions = vmOptionsFlag.getValue()
        val syncConfig = configFlag.getValue()
        val syncPlugins = pluginsFlag.getValue()
        val syncAll = allFlag.getValue()

        val configRepository = ConfigRepository.create()
        val projectManager = ProjectManager.getInstance(configRepository)
        val projectLauncher = ProjectLauncher.getInstance(configRepository)

        // Determine what to sync
        val noFlagsSpecified = !syncAll && !syncVmOptions && !syncConfig && !syncPlugins

        val shouldSyncVmOptions = if (noFlagsSpecified) {
            // Default: only sync if configured
            configRepository.load().baseVmOptionsPath != null
        } else {
            syncAll || syncVmOptions
        }

        val shouldSyncConfig = if (noFlagsSpecified) {
            // Default: sync (can use auto-detection)
            true
        } else {
            syncAll || syncConfig
        }

        val shouldSyncPlugins = if (noFlagsSpecified) {
            // Default: sync (can use auto-detection)
            true
        } else {
            syncAll || syncPlugins
        }

        // Resolve project ID
        val projectId = resolveProjectId(projectIdentifier, projectManager)
        val project = projectManager.get(projectId)

        if (project == null) {
            echo("Project not found: $projectIdentifier", err = true)
            echo("Use 'idea-juggler list' to see tracked projects", err = true)
            throw ExitException(1)
        }

        echo("Synchronizing project: ${project.name}")
        echo()

        try {
            // Show what will be synced and from where
            val directoryManager = com.ideajuggler.core.DirectoryManager.getInstance(configRepository)
            val baseVMOptionsTracker = com.ideajuggler.core.BaseVMOptionsTracker.getInstance(configRepository)

            if (shouldSyncVmOptions) {
                val vmPath = baseVMOptionsTracker.getBaseVmOptionsPath()
                if (vmPath != null) {
                    echo("  Syncing VM options from: $vmPath")
                } else {
                    echo("  Syncing VM options from: (not configured)", err = true)
                }
            }
            if (shouldSyncConfig) {
                val configPath = directoryManager.getBaseConfigPath()
                if (configPath != null) {
                    echo("  Syncing config from: $configPath")
                } else {
                    echo("  Syncing config from: (not found)", err = true)
                }
            }
            if (shouldSyncPlugins) {
                val pluginsPath = directoryManager.getBasePluginsPath()
                if (pluginsPath != null) {
                    echo("  Syncing plugins from: $pluginsPath")
                } else {
                    echo("  Syncing plugins from: (not found)", err = true)
                }
            }
            echo()

            projectLauncher.syncProject(
                projectId,
                shouldSyncVmOptions,
                shouldSyncConfig,
                shouldSyncPlugins
            )

            echo("Successfully synchronized project settings.")
        } catch (e: IllegalStateException) {
            echo()
            echo("Error: ${e.message}", err = true)
            throw ExitException(1)
        } catch (e: Exception) {
            echo()
            echo("Error syncing project: ${e.message}", err = true)
            throw ExitException(1)
        }
    }

    private fun resolveProjectId(identifier: String, projectManager: ProjectManager): String {
        // Try as ID first
        if (projectManager.get(identifier) != null) {
            return identifier
        }

        // Try as path
        val path = PathUtils.expandTilde(Path.of(identifier))
        if (path.exists()) {
            return ProjectIdGenerator.generate(path)
        }

        // Return as-is, will fail with proper error later
        return identifier
    }
}
