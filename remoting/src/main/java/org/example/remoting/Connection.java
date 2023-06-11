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
    private static final Session NO_SESSION = new Session(0, OnDispatcher.EDT);

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

    private <T> T withSession(OnDispatcher dispatchers, Supplier<T> code) {
        int sessionId = invoker.newSession();
        Session currentValue = sessionHolder.get();
        sessionHolder.set(new Session(sessionId, dispatchers));
        try {
            return code.get();
        } finally {
            sessionHolder.set(currentValue);
            invoker.cleanup(sessionId); // todo handle network errors quietly
        }
    }

    public <T> T withSession(Supplier<T> code) {
        return withSession(OnDispatcher.EDT, code);
    }

    public void withSession(Runnable code) {
        withSession(() -> {
            code.run();
            return null;
        });
    }

    public <T> T withContext(OnDispatcher dispatchers, Supplier<T> code) {
        return withSession(dispatchers, code);
    }

    public void withContext(OnDispatcher dispatchers, Runnable code) {
        withContext(dispatchers, () -> {
            code.run();
            return null;
        });
    }
}

record Session(int id, OnDispatcher dispatcher) {
}

@JmxName("com.intellij:type=Invoker")
interface Invoker {
    RemoteCallResult invoke(RemoteCall call);

    int newSession();

    void cleanup(int sessionId);
}
