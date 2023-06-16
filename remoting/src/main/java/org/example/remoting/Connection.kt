package org.example.remoting

import org.example.remoting.impl.ConnectionImpl
import org.example.remoting.jmx.JmxHost
import org.example.shared.LockSemantics
import org.example.shared.OnDispatcher
import org.example.shared.ProjectRef
import kotlin.reflect.KClass

annotation class Remote(
        val value: String,
        val plugin: String = ""
)

interface Connection {
    fun <T : Any> service(clazz: KClass<T>): T

    fun <T : Any> service(clazz: KClass<T>, projectRef: ProjectRef?): T

    fun <T : Any> utility(clazz: KClass<T>): T

    fun <T : Any> new(clazz: KClass<T>, vararg args: Any?)

    fun <T> withContext(dispatcher: OnDispatcher = OnDispatcher.DEFAULT,
                        semantics: LockSemantics = LockSemantics.NO_LOCK,
                        code: Connection.() -> T): T

    fun <T> withReadAction(dispatcher: OnDispatcher = OnDispatcher.DEFAULT,
                           code: Connection.() -> T): T

    fun <T> withWriteAction(code: Connection.() -> T): T

    companion object {
        @JvmStatic
        fun create(host: JmxHost? = JmxHost(null, null, "localhost:7777")): Connection {
            return ConnectionImpl(host)
        }
    }
}
