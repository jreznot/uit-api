package org.example.remoting.sdk

import org.example.remoting.Remote
import org.example.shared.Ref

@Remote("org.example.app.ProjectManager")
interface ProjectManager {
    fun getOpenProjects(): List<Ref>
}
