package com.projectjuggler.core

import com.projectjuggler.config.ProjectId
import com.projectjuggler.config.ProjectPath
import com.projectjuggler.util.HashUtils

object ProjectIdGenerator {
    fun generate(projectPath: ProjectPath): ProjectId {
        // Generate SHA-256 hash and take first 16 characters
        val hash = HashUtils.calculateStringHash(projectPath.toString())
        return ProjectId(hash.take(16))
    }
}
