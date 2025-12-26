package com.projectjuggler.core

/**
 * Interface for outputting messages during project operations.
 * Implementations can send messages to CLI, plugin notifications, logs, etc.
 */
interface MessageOutput {
    fun echo(message: String)
}
