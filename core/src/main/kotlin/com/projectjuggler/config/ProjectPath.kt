package com.projectjuggler.config

import com.projectjuggler.core.ProjectIdGenerator
import kotlinx.serialization.Serializable
import kotlin.io.path.Path

@JvmInline
@Serializable
value class ProjectPath(
    val pathString: String
) {
    val id: ProjectId get() = ProjectIdGenerator.generate(this)
    val path get() = Path(pathString)
    val name: String get() = path.fileName.toString()
}