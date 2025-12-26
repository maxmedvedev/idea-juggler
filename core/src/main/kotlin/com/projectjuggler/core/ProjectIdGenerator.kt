package com.projectjuggler.core

import com.projectjuggler.config.ProjectId
import com.projectjuggler.config.ProjectPath
import com.projectjuggler.util.HashUtils

object ProjectIdGenerator {
    fun generate(projectPath: ProjectPath): ProjectId {
        val hash = HashUtils.calculateStringHash(projectPath.toString())
        return ProjectId(projectPath.name + "-" + hash.take(16))
    }
}
