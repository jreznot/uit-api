package org.example

import org.example.remoting.Remote
import org.example.remoting.sdk.Project

@Remote("org.example.app.BuildService")
internal interface BuildService {
    fun build(projectRef: Project?, args: String?): BuildResult
}

@Remote("org.example.app.BuildResult")
internal interface BuildResult {
    fun getArgs(): String
}