package org.example.remoting;

import org.example.remoting.jmx.JmxCallHandler;
import org.example.remoting.jmx.JmxHost;
import org.example.remoting.jmx.JmxName;
import org.example.shared.RemoteCall;
import org.example.shared.RemoteCallResult;
import org.example.shared.RemoteRef;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Connection {
    private final Invoker invoker;
    private final Map<Class<?>, Object> bridges = new ConcurrentHashMap<>();

    public Connection(JmxHost host) {
        this.invoker = JmxCallHandler.jmx(Invoker.class, host);
    }

    public Connection() {
        this(new JmxHost(null, null, "localhost:7777"));
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        return (T) bridges.computeIfAbsent(clazz, this::buildBridge);
    }

    public <T> T bridge(RemoteRef ref, Class<T> clazz) {
        return null; // todo
    }

    private Object buildBridge(Class<?> clazz) {
        return Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{clazz},
                (proxy, method, args) -> {
                    if ("equals".equals(method.getName())) return false;
                    if ("hashCode".equals(method.getName())) return clazz.hashCode();
                    if ("toString".equals(method.getName())) return "§ " + clazz.getSimpleName();

                    var call = new RemoteCall("", method.getName(), Arrays.asList(args), true, false);

                    // todo if the expected result is @Remote and call result is RemoteRef

                    return invoker.invoke(call);
                });
    }
}

@JmxName("")
interface Invoker {
    RemoteCallResult invoke(RemoteCall call);
}
