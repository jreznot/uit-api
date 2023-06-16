package org.example.remoting

import org.example.remoting.jmx.JmxCallHandler
import org.example.remoting.jmx.JmxHost
import org.example.remoting.jmx.JmxName
import org.example.shared.*
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

// todo slf4j logging for calls
// todo separate interface and ConnectionImpl
class Connection @JvmOverloads constructor(host: JmxHost? = JmxHost(null, null, "localhost:7777")) {
    private val invoker: Invoker = JmxCallHandler.jmx(Invoker::class.java, host)
    private val sessionHolder = ThreadLocal<Session>()
    private val appServices: MutableMap<Class<*>, Any> = ConcurrentHashMap()
    private val projectServices: Map<ProjectRef, Map<Class<*>, Any>> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    fun <T> getInstance(clazz: Class<T>): T {
        return appServices.computeIfAbsent(clazz, ::serviceBridge) as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getInstance(clazz: Class<T>, projectRef: ProjectRef?): T {
        return appServices.computeIfAbsent(clazz, ::serviceBridge) as T
    }

    fun <T> bridge(ref: Ref?, clazz: Class<T>?): T? {
        return null // todo
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

    fun <T> withContext(dispatchers: OnDispatcher = OnDispatcher.DEFAULT,
                        semantics: LockSemantics = LockSemantics.NO_LOCK,
                        code: Supplier<T>): T {
        val currentValue = sessionHolder.get()
        sessionHolder.set(Session(currentValue?.id ?: invoker.newSession(), dispatchers, semantics))
        return try {
            code.get()
        } finally {
            if (currentValue != null) {
                sessionHolder.set(currentValue)
            } else {
                invoker.cleanup(invoker.newSession()) // todo handle network errors quietly
            }
        }
    }

    fun withContext(dispatcher: OnDispatcher = OnDispatcher.DEFAULT,
                    semantics: LockSemantics = LockSemantics.NO_LOCK,
                    code: Runnable) {
        withContext(dispatcher, semantics, toSupplier(code))
    }

    fun <T> withWriteAction(code: Supplier<T>): T {
        return withContext(OnDispatcher.EDT, LockSemantics.WRITE_ACTION, code)
    }

    fun withWriteAction(code: Runnable) {
        withContext(OnDispatcher.EDT, LockSemantics.WRITE_ACTION, toSupplier(code))
    }

    fun <T> withReadAction(dispatcher: OnDispatcher = OnDispatcher.DEFAULT,
                           code: Supplier<T>): T {
        return withContext(dispatcher, LockSemantics.READ_ACTION, code)
    }

    fun withReadAction(dispatcher: OnDispatcher = OnDispatcher.DEFAULT, code: Runnable) {
        withContext(dispatcher, LockSemantics.READ_ACTION, toSupplier(code))
    }
}

private fun toSupplier(code: Runnable): Supplier<Any?> {
    return Supplier {
        code.run()
        null
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
