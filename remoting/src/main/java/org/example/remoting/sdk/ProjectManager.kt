package org.example.remoting.sdk

import org.example.remoting.Remote
import org.example.remoting.Service
import org.example.remoting.ServiceLevel
import org.example.shared.ProjectRef

@Service(ServiceLevel.APP)
@Remote("org.example.app.ProjectManager")
interface ProjectManager {
    val openProjects: List<ProjectRef>
}
