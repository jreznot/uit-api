package org.example.remoting.impl

import org.example.remoting.Connection
import org.example.remoting.Remote
import org.example.remoting.jmx.JmxCallHandler
import org.example.remoting.jmx.JmxHost
import org.example.remoting.jmx.JmxName
import org.example.shared.LockSemantics
import org.example.shared.OnDispatcher
import org.example.shared.Ref
import org.example.shared.impl.RemoteCall
import org.example.shared.impl.RemoteCallResult
import org.example.shared.impl.ServiceCall
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

// todo slf4j logging for calls
internal class ConnectionImpl(host: JmxHost?) : Connection {
    private val invoker: Invoker = JmxCallHandler.jmx(Invoker::class.java, host)
    private val sessionHolder = ThreadLocal<Session>()

    private val appServices: MutableMap<Class<*>, Any> = ConcurrentHashMap()
    private val projectServices: Map<Ref, Map<Class<*>, Any>> = ConcurrentHashMap()
    private val utils: MutableMap<Class<*>, Any> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> service(clazz: KClass<T>): T {
        return appServices.computeIfAbsent(clazz.java, ::serviceBridge) as T
    }

    override fun <T : Any> service(clazz: KClass<T>, project: Ref?): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any> utility(clazz: KClass<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any> new(clazz: KClass<T>, vararg args: Any?) {
        TODO("Not yet implemented")
    }

    private fun serviceBridge(clazz: Class<*>): Any {
        val remote = findRemoteMeta(clazz) ?: throw IllegalArgumentException("Class $clazz is not annotated with @Remote annotation")

        return Proxy.newProxyInstance(Connection::class.java.classLoader, arrayOf(clazz)) { _: Any?, method: Method, args: Array<Any?>? ->
            when (method.name) {
                "equals" -> false
                "hashCode" -> clazz.hashCode()
                "toString" -> "@Service(APP) " + remote.value
                else -> {
                    val (sessionId, dispatcher, semantics) = sessionHolder.get() ?: NO_SESSION
                    val call = ServiceCall(
                            sessionId,
                            null,
                            dispatcher,
                            semantics,
                            remote.value,
                            method.name,
                            args,
                            null
                    )
                    invoker.invoke(call)
                }
            }
        }
    }

    override fun <T> withContext(dispatcher: OnDispatcher,
                                 semantics: LockSemantics,
                                 code: Connection.() -> T): T {
        val currentValue = sessionHolder.get()
        sessionHolder.set(Session(currentValue?.id ?: invoker.newSession(), dispatcher, semantics))
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

private fun findRemoteMeta(clazz: Class<*>): Remote? {
    return clazz.annotations
            .filterIsInstance<Remote>()
            .firstOrNull()
}

internal data class Session(
        val id: Int,
        val dispatcher: OnDispatcher,
        val semantics: LockSemantics
)

private val NO_SESSION: Session = Session(0, OnDispatcher.DEFAULT, LockSemantics.NO_LOCK)

@JmxName("com.intellij:type=Invoker")
internal interface Invoker {
    fun invoke(call: RemoteCall): RemoteCallResult?

    fun newSession(): Int

    fun cleanup(sessionId: Int)
}