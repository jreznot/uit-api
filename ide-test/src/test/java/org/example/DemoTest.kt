package org.example

import org.example.remoting.Connection
import org.example.shared.OnDispatcher
import org.junit.Test

class DemoTest {
    private val connection: Connection = Connection.create()

    @Test
    fun test() {
        connection.withContext(OnDispatcher.DEFAULT) {
            val done = service(BuildService::class).build(null, "some")
            println(done.args)

            val buildService = connection.service(BuildService::class)
            val x = withReadAction { buildService.build(null, "") }
            val y = withReadAction { buildService.build(null, "") }

            withWriteAction { buildService.build(null, x.args + y.args) }
        }
    }
}
