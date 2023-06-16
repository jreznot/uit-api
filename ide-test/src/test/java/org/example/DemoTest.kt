package org.example

import org.example.remoting.Connection
import org.junit.Test

class DemoTest {
    private val connection: Connection = Connection()

    @Test
    fun test() {
        val buildService = connection.getInstance(BuildService::class.java)

        connection.withWriteAction {
            val done = buildService.build(null, "some")
            val args = done.args
            println(args)
        }
    }
}
