package org.example.remoting

import org.example.remoting.jmx.JmxCallHandler
import org.example.remoting.jmx.JmxHost
import org.example.remoting.jmx.JmxName
import org.example.shared.*
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface Connection {
    fun <T : Any> getInstance(clazz: KClass<T>): T

    fun <T : Any> getInstance(clazz: KClass<T>, projectRef: ProjectRef?): T

    fun <T : Any> getUtil(clazz: KClass<T>): T

    fun <T> withContext(dispatchers: OnDispatcher = OnDispatcher.DEFAULT,
                        semantics: LockSemantics = LockSemantics.NO_LOCK,
                        code: Connection.() -> T): T

    fun <T> withReadAction(dispatcher: OnDispatcher = OnDispatcher.DEFAULT,
                           code: Connection.() -> T): T

    fun <T> withWriteAction(code: Connection.() -> T): T

    companion object {
        fun create(host: JmxHost? = JmxHost(null, null, "localhost:7777")): Connection {
            return ConnectionImpl(host)
        }
    }
}

// todo slf4j logging for calls
class ConnectionImpl(host: JmxHost?) : Connection {
    private val invoker: Invoker = JmxCallHandler.jmx(Invoker::class.java, host)
    private val sessionHolder = ThreadLocal<Session>()

    private val appServices: MutableMap<Class<*>, Any> = ConcurrentHashMap()
    private val utils: MutableMap<Class<*>, Any> = ConcurrentHashMap()
    private val projectServices: Map<ProjectRef, Map<Class<*>, Any>> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getInstance(clazz: KClass<T>): T {
        return appServices.computeIfAbsent(clazz.java, ::serviceBridge) as T
    }

    override fun <T : Any> getInstance(clazz: KClass<T>, projectRef: ProjectRef?): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any> getUtil(clazz: KClass<T>): T {
        TODO("Not yet implemented")
    }

    private fun serviceBridge(clazz: Class<*>): Any {
        return Proxy.newProxyInstance(Connection::class.java.classLoader, arrayOf(clazz)) { _: Any?, method: Method, args: Array<Any?>? ->
            when (method.name) {
                "equals" -> false
                "hashCode" -> clazz.hashCode()
                "toString" -> "# " + clazz.simpleName
                else -> {
                    val (sessionId, dispatcher, semantics) = Objects.requireNonNullElse(sessionHolder.get(), NO_SESSION)
                    val call = RemoteCall(
                            sessionId,
                            dispatcher,
                            semantics,
                            null,
                            "",
                            method.name,
                            args,
                            true,
                            false
                    )
                    invoker.invoke(call)
                }
            }
        }
    }

    override fun <T> withContext(dispatchers: OnDispatcher,
                                 semantics: LockSemantics,
                                 code: Connection.() -> T): T {
        val currentValue = sessionHolder.get()
        sessionHolder.set(Session(currentValue?.id ?: invoker.newSession(), dispatchers, semantics))
        return try {
            this.code()
        } finally {
            if (currentValue != null) {
                sessionHolder.set(currentValue)
            } else {
                invoker.cleanup(invoker.newSession()) // todo handle network errors quietly
            }
        }
    }

    override fun <T> withWriteAction(code: Connection.() -> T): T {
        return withContext(OnDispatcher.EDT, LockSemantics.WRITE_ACTION, code)
    }

    override fun <T> withReadAction(dispatcher: OnDispatcher, code: Connection.() -> T): T {
        return withContext(dispatcher, LockSemantics.READ_ACTION, code)
    }
}

internal data class Session(val id: Int, val dispatcher: OnDispatcher?, val semantics: LockSemantics?)

private val NO_SESSION: Session = Session(0, OnDispatcher.DEFAULT, LockSemantics.NO_LOCK)

@JmxName("com.intellij:type=Invoker")
internal interface Invoker {
    operator fun invoke(call: RemoteCall?): RemoteCallResult?
    fun newSession(): Int
    fun cleanup(sessionId: Int)
}