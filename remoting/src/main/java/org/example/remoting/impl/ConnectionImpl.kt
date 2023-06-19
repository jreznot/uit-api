package org.example.remoting.impl

import org.example.remoting.Connection
import org.example.remoting.Remote
import org.example.remoting.jmx.JmxCallException
import org.example.remoting.jmx.JmxCallHandler
import org.example.remoting.jmx.JmxHost
import org.example.remoting.jmx.JmxName
import org.example.shared.LockSemantics
import org.example.shared.OnDispatcher
import org.example.shared.Ref
import org.example.shared.impl.*
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
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

    override val isAvailable: Boolean
        get() {
            try {
                invoker.ping()
            } catch (ioe: JmxCallException) {
                return false
            }

            return true
        }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> service(clazz: KClass<T>): T {
        return appServices.computeIfAbsent(clazz.java, ::serviceBridge) as T
    }

    override fun <T : Any> service(clazz: KClass<T>, project: Ref?): T {
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> utility(clazz: KClass<T>): T {
        return utils.computeIfAbsent(clazz.java, ::utilityBridge) as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> new(clazz: KClass<T>, vararg args: Any?) : T {
        val remote = findRemoteMeta(clazz.java)
                ?: throw IllegalArgumentException("Class $clazz is not annotated with @Remote annotation")

        val (sessionId, dispatcher, semantics) = sessionHolder.get() ?: NO_SESSION
        val call = NewInstanceCall(
                sessionId,
                null,
                dispatcher,
                semantics,
                remote.value,
                convertArgsToPass(args)
        )
        val callResult = invoker.invoke(call)
        return convertResult(callResult, clazz.java) as T
    }

    private fun convertArgsToPass(args: Array<out Any?>?): Array<Any?> {
        if (args == null) return emptyArray()

        return args
                .map { if (it is RefWrapper) it.getRef() else it }
                .toTypedArray()
    }


    private fun convertResult(callResult: RemoteCallResult, targetClass: Class<*>): Any? {
        val value = callResult.value ?: return null

        if (value is Ref) {
            return refBridge(targetClass, value)
        }

        return null
    }

    private fun convertResult(callResult: RemoteCallResult, method: Method): Any? {
        val value = callResult.value ?: return null

        if (RemoteCall.isPassByValue(value)) {
            return value
        }

        if (value is Ref) {
            if (Ref::class.java.isAssignableFrom(method.returnType)) return value

            return refBridge(method.returnType, value)
        }

        if (value is RefList) {
            if (method.returnType == RefList::class.java) return value

            if (Collection::class.java.isAssignableFrom(method.returnType)) {
                val parameterizedType = method.genericReturnType as? ParameterizedType ?: return value.items
                val componentType = parameterizedType.actualTypeArguments.firstOrNull() as? Class<*>
                        ?: return value.items

                return value.items.map { refBridge(componentType, it) }
            }

            if (method.returnType.isArray) {
                val componentType = method.returnType.componentType
                val array = java.lang.reflect.Array.newInstance(componentType, value.items.size)
                for ((i, item) in value.items.withIndex()) {
                    java.lang.reflect.Array.set(array, i, refBridge(componentType, item))
                }
                return array
            }

            return value // todo better handle primitive lists
        }

        return null
    }

    private fun serviceBridge(clazz: Class<*>): Any {
        val remote = findRemoteMeta(clazz)
                ?: throw IllegalArgumentException("Class $clazz is not annotated with @Remote annotation")

        return Proxy.newProxyInstance(getClassLoader(), arrayOf(clazz)) { proxy: Any?, method: Method, args: Array<Any?>? ->
            when (method.name) {
                "equals" -> proxy == args?.firstOrNull()
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
                            convertArgsToPass(args),
                            null
                    )
                    val callResult = invoker.invoke(call)
                    convertResult(callResult, method)
                }
            }
        }
    }

    private fun utilityBridge(clazz: Class<*>): Any {
        val remote = findRemoteMeta(clazz)
                ?: throw IllegalArgumentException("Class $clazz is not annotated with @Remote annotation")

        return Proxy.newProxyInstance(getClassLoader(), arrayOf(clazz)) { proxy: Any?, method: Method, args: Array<Any?>? ->
            when (method.name) {
                "equals" -> proxy == args?.firstOrNull()
                "hashCode" -> clazz.hashCode()
                "toString" -> "Utility " + remote.value
                else -> {
                    val (sessionId, dispatcher, semantics) = sessionHolder.get() ?: NO_SESSION
                    val call = UtilityCall(
                            sessionId,
                            null,
                            dispatcher,
                            semantics,
                            remote.value,
                            method.name,
                            convertArgsToPass(args)
                    )
                    val callResult = invoker.invoke(call)
                    convertResult(callResult, method)
                }
            }
        }
    }

    private fun refBridge(clazz: Class<*>, ref: Ref): Any {
        val remote = findRemoteMeta(clazz)
                ?: throw IllegalArgumentException("Class $clazz is not annotated with @Remote annotation")

        return Proxy.newProxyInstance(getClassLoader(), arrayOf(clazz, RefWrapper::class.java)) { proxy: Any?, method: Method, args: Array<Any?>? ->
            when (method.name) {
                "equals" -> proxy == args?.firstOrNull()
                "hashCode" -> ref.identityHashCode
                "toString" -> ref.asString
                "getRef" -> ref
                else -> {
                    val (sessionId, dispatcher, semantics) = sessionHolder.get() ?: NO_SESSION
                    val call = RefCall(
                            sessionId,
                            null,
                            dispatcher,
                            semantics,
                            remote.value,
                            method.name,
                            convertArgsToPass(args),
                            ref
                    )
                    val callResult = invoker.invoke(call)
                    convertResult(callResult, method)
                }
            }
        }
    }

    private fun getClassLoader(): ClassLoader? {
        return javaClass.classLoader
    }

    override fun <T> withContext(dispatcher: OnDispatcher,
                                 semantics: LockSemantics,
                                 code: Connection.() -> T): T {
        val currentValue = sessionHolder.get()
        val runAsSession = Session(currentValue?.id ?: invoker.newSession(), dispatcher, semantics)
        sessionHolder.set(runAsSession)
        return try {
            this.code()
        } finally {
            if (currentValue != null) {
                sessionHolder.set(currentValue)
            } else {
                invoker.cleanup(runAsSession.id) // todo handle network errors quietly
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
    fun ping()

    fun invoke(call: RemoteCall): RemoteCallResult

    fun newSession(): Int

    fun cleanup(sessionId: Int)
}