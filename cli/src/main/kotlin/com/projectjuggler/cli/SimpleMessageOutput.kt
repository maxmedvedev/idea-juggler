package com.projectjuggler.cli

import com.projectjuggler.core.MessageOutput

class SimpleMessageOutput : MessageOutput {
    override fun echo(message: String) {
        println(message)
    }
}
