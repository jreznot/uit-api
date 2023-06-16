package org.example.remoting

annotation class Service(
        val value: ServiceLevel,
        val plugin: String = ""
)

annotation class Remote(
        val value: String,
        val plugin: String = ""
)
