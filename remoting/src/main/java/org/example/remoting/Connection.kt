package org.example.remoting;

import org.example.remoting.jmx.JmxCallHandler;
import org.example.remoting.jmx.JmxHost;
import org.example.remoting.jmx.JmxName;
import org.example.shared.*;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

// todo slf4j logging for calls
// todo separate interface and ConnectionImpl
public final class Connection {
    private static final Session NO_SESSION = new Session(0, OnDispatcher.EDT, LockSemantics.NO_LOCK);

    private final Invoker invoker;
    private final InheritableThreadLocal<Session> sessionHolder = new InheritableThreadLocal<>();

    private final Map<Class<?>, Object> appServices = new ConcurrentHashMap<>();
    private final Map<ProjectRef, Map<Class<?>, Object>> projectServices = new ConcurrentHashMap<>();

    public Connection(JmxHost host) {
        this.invoker = JmxCallHandler.jmx(Invoker.class, host);
    }

    public Connection() {
        this(new JmxHost(null, null, "localhost:7777"));
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        return (T) appServices.computeIfAbsent(clazz, this::serviceBridge);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz, ProjectRef projectRef) {
        return (T) appServices.computeIfAbsent(clazz, this::serviceBridge);
    }

    public <T> T bridge(RemoteRef ref, Class<T> clazz) {
        return null; // todo
    }

    private Object serviceBridge(Class<?> clazz) {
        return Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{clazz},
                (proxy, method, args) -> {
                    if ("equals".equals(method.getName())) return false;
                    if ("hashCode".equals(method.getName())) return clazz.hashCode();
                    if ("toString".equals(method.getName())) return "§ " + clazz.getSimpleName();

                    Session session = Objects.requireNonNullElse(this.sessionHolder.get(), NO_SESSION);
                    int sessionId = session.id();
                    var call = new RemoteCall(
                            sessionId,
                            session.dispatcher(),
                            null,
                            "",
                            method.getName(),
                            args,
                            true,
                            false
                    );

                    // todo if the expected result is @Remote and call result is RemoteRef

                    return invoker.invoke(call);
                });
    }

    public <T> T withContext(OnDispatcher dispatchers, LockSemantics semantics, Supplier<T> code) {
        Session currentValue = sessionHolder.get();
        sessionHolder.set(new Session(currentValue != null ? currentValue.id() : invoker.newSession(), dispatchers, semantics));
        try {
            return code.get();
        } finally {
            if (currentValue != null) {
                sessionHolder.set(currentValue);
            } else {
                invoker.cleanup(invoker.newSession()); // todo handle network errors quietly
            }
        }
    }

    public <T> T withContext(OnDispatcher dispatchers, Supplier<T> code) {
        return this.withContext(dispatchers, LockSemantics.NO_LOCK, code);
    }

    public void withContext(OnDispatcher dispatcher, Runnable code) {
        withContext(dispatcher, toSupplier(code));
    }

    public void withContext(Runnable code) {
        withContext(OnDispatcher.DEFAULT, LockSemantics.NO_LOCK, toSupplier(code));
    }

    public <T> T withWriteAction(Supplier<T> code) {
        return withContext(OnDispatcher.EDT, LockSemantics.WRITE_ACTION, code);
    }

    public void withWriteAction(Runnable code) {
        withContext(OnDispatcher.EDT, LockSemantics.WRITE_ACTION, toSupplier(code));
    }

    public <T> T withReadAction(OnDispatcher dispatcher, Supplier<T> code) {
        return withContext(dispatcher, LockSemantics.READ_ACTION, code);
    }

    public <T> T withReadAction(Supplier<T> code) {
        return withReadAction(OnDispatcher.DEFAULT, code);
    }

    public void withReadAction(Runnable code) {
        withReadAction(OnDispatcher.EDT, toSupplier(code));
    }

    private static Supplier<Object> toSupplier(Runnable code) {
        return () -> {
            code.run();
            return null;
        };
    }
}

record Session(
        int id,
        OnDispatcher dispatcher,
        LockSemantics semantics
) {
}

@JmxName("com.intellij:type=Invoker")
interface Invoker {
    RemoteCallResult invoke(RemoteCall call);

    int newSession();

    void cleanup(int sessionId);
}
