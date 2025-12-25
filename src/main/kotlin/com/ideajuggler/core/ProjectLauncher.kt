package com.ideajuggler.core

import com.ideajuggler.config.ConfigRepository
import com.ideajuggler.config.RecentProjectsIndex
import java.nio.file.Path

class ProjectLauncher(
    configRepository: ConfigRepository
) {
    private val projectManager = ProjectManager.getInstance(configRepository)
    private val directoryManager = DirectoryManager.getInstance(configRepository)
    private val baseVMOptionsTracker = BaseVMOptionsTracker.getInstance(configRepository)
    private val intellijLauncher = IntelliJLauncher.getInstance(configRepository)
    private val recentProjectsIndex = RecentProjectsIndex.getInstance(configRepository)

    /**
     * Launch a project by path, handling base VM options changes and project registration
     */
    fun launchByPath(projectPath: Path, onBaseVmOptionsChanged: () -> Unit = {}) {
        val projectId = ProjectIdGenerator.generate(projectPath)

        // Check if base VM options changed
        if (baseVMOptionsTracker.hasChanged()) {
            onBaseVmOptionsChanged()
            regenerateAllProjects()
            baseVMOptionsTracker.updateHash()
        }

        // Register or update project metadata
        projectManager.registerOrUpdate(projectId, projectPath)

        // Record in recent projects
        recentProjectsIndex.recordOpen(projectId)

        // Launch IntelliJ
        intellijLauncher.launch(projectId, projectPath)
    }

    /**
     * Launch a project by ID and path (for when ID is already known)
     */
    fun launchById(projectId: String, projectPath: Path) {
        // Register or update project metadata
        projectManager.registerOrUpdate(projectId, projectPath)

        // Record in recent projects
        recentProjectsIndex.recordOpen(projectId)

        // Launch IntelliJ
        intellijLauncher.launch(projectId, projectPath)
    }

    private fun regenerateAllProjects() {
        val projects = projectManager.listAll()
        val baseVmOptionsPath = baseVMOptionsTracker.getBaseVmOptionsPath()

        projects.forEach { project ->
            val projectDirs = directoryManager.ensureProjectDirectories(project.id)
            VMOptionsGenerator.generate(
                baseVmOptionsPath,
                ProjectDirectories(
                    root = projectDirs.root,
                    config = projectDirs.config,
                    system = projectDirs.system,
                    logs = projectDirs.logs,
                    plugins = projectDirs.plugins
                )
            )
        }
    }

    companion object {
        fun getInstance(configRepository: ConfigRepository): ProjectLauncher = ProjectLauncher(configRepository)
    }
}
