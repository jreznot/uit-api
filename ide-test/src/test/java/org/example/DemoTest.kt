package org.example

import org.example.remoting.Connection
import org.junit.Test

class DemoTest {
    private val connection: Connection = Connection.create()

    @Test
    fun test() {
        val buildService = connection.getInstance(BuildService::class)

        connection.withWriteAction {
            val done = buildService.build(null, "some")
            val args = done.args
            println(args)
        }
    }
}
