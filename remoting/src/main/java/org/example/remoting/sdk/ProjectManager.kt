package org.example.remoting.sdk

import org.example.remoting.Remote

@Remote("org.example.app.ProjectManager")
interface ProjectManager {
    fun getOpenProjects(): List<Project>
}

@Remote("org.example.app.Project")
interface Project {
    fun getName(): String
}