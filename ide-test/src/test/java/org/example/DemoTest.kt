package org.example

import org.example.remoting.Connection
import org.example.remoting.sdk.ProjectManager
import org.example.shared.OnDispatcher
import org.junit.Test
import kotlin.test.assertEquals

class DemoTest {
    private val connection: Connection = Connection.create()

    @Test
    fun test() {
        assertEquals(true, connection.isAvailable)

        connection.withContext(OnDispatcher.DEFAULT) {
            val projects = service(ProjectManager::class).getOpenProjects()
            assertEquals(1, projects.size)
            assertEquals("intellij", projects[0].getName())

            val buildService = connection.service(BuildService::class)
            val x = withReadAction { buildService.build(projects[0], "") }
            val y = withReadAction { buildService.build(projects[0], "") }

            withWriteAction { buildService.build(null, x.args + y.args) }
        }
    }
}
