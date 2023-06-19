package org.example

import org.example.remoting.Connection
import org.example.remoting.Remote
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
            val app = utility(ApplicationManager::class).getApplication()

            val editors = service(FileEditorManager::class).getEditors()
            assertEquals(2, editors.size)

            val newFrame = new(JFrame::class, "Demo $app")
            newFrame.setSize(640, 480)
            newFrame.setVisible(true)

            val projects = service(ProjectManager::class).getOpenProjects()
            assertEquals(1, projects.size)
            assertEquals("intellij", projects[0].getName())

            val buildService = connection.service(BuildService::class)
            val x = withReadAction { buildService.build(projects[0], "") }
            val y = withReadAction { buildService.build(projects[0], "") }

            withWriteAction { buildService.build(null, x.getArgs() + y.getArgs()) }
        }
    }
}

@Remote("javax.swing.JFrame")
interface JFrame {
    fun setVisible(visible: Boolean)

    fun setSize(width: Int, height: Int)
}