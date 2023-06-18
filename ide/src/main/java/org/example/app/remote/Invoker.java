package org.example.app.remote;

import org.example.shared.Ref;
import org.example.shared.impl.RemoteCall;
import org.example.shared.impl.RemoteCallResult;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Invoker implements InvokerMBean {
    public static final int NO_SESSION_ID = 0;

    // todo store initial pluginId for each reference
    private final Map<Integer, WeakReference<Object>> adhocReferenceMap = new ConcurrentHashMap<>();
    private final Map<Integer, Session> sessions = new ConcurrentHashMap<>();

    private final AtomicInteger sessionIdSequence = new AtomicInteger(1);
    private final AtomicInteger adhocRefSequence = new AtomicInteger(1);

    @Override
    public RemoteCallResult invoke(RemoteCall call) {
        Class<?> target;
        try {
            target = getClass().getClassLoader().loadClass(call.getClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No such class" + call.getClassName(), e);
        }

        Method targetMethod;
        try {
            targetMethod = target.getMethod(call.getMethodName());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No method " + call.getMethodName() + " in class" + call.getClassName(), e);
        }

        Object instance = findInstance(call, target);
        Object[] args = transformArgs(call);

        Object result;
        try {
            result = targetMethod.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Exception ", e);
        }

        // todo support allow list of simple type results, return them as plain value
        if (call.getSessionId() == NO_SESSION_ID) {
            int id = adhocRefSequence.getAndIncrement();
            Ref ref = RefProducer.makeRef(id, result);
            adhocReferenceMap.put(id, new WeakReference<>(result));
            return new RemoteCallResult(ref);
        } else {
            Session session = sessions.get(call.getSessionId());
            Ref ref = session.putReference(result);
            return new RemoteCallResult(ref);
        }
    }

    private Object findInstance(RemoteCall call, Class<?> target) {
        Object instance;
        try {
            Method instanceGetter = target.getDeclaredMethod("getInstance");
            instance = instanceGetter.invoke(null);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No getInstance() in class" + call.getClassName(), e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Unable to get service instance", e);
        }
        return instance;
    }

    private @NotNull Object[] transformArgs(RemoteCall call) {
        return Arrays.stream(call.getArgs())
                .map(x -> {
                    if (x instanceof Ref) {
                        Object reference = getReference(call.getSessionId(), ((Ref) x).id());
                        if (reference == RefProducer.NULL_REF) return null;

                        return reference;
                    }
                    return x;
                })
                .toList()
                .toArray();
    }

    private Object getReference(int sessionId, int id) {
        if (sessionId != NO_SESSION_ID) {
            // first lookup in session
            Session session = sessions.get(sessionId);
            if (session == null) {
                throw new IllegalStateException("No such session " + sessionId);
            }

            return session.findReference(id);
        }

        return adhocReferenceMap.get(id);
    }

    @Override
    public int newSession() {
        int id = sessionIdSequence.getAndIncrement();
        sessions.put(id, new Session());
        return id;
    }

    @Override
    public void cleanup(int sessionId) {
        sessions.remove(sessionId);
    }
}

final class Session {
    private final AtomicInteger refIdSequence = new AtomicInteger(1);
    private final Map<Integer, Object> variables = new ConcurrentHashMap<>();

    public Object findReference(int id) {
        if (!variables.containsKey(id)) throw new IllegalStateException("No such reference with id " + id);

        return variables.get(id);
    }

    public Ref putReference(Object value) {
        var id = refIdSequence.getAndIncrement();
        var refValue = value == null ? RefProducer.NULL_REF : value;

        variables.put(id, refValue);

        // todo unwrap lists

        return RefProducer.makeRef(id, refValue);
    }
}

final class RefProducer {
    public static final Ref NULL_REF = new Ref(-1, "", -1, "null");

    public static Ref makeRef(int id, Object value) {
        if (value instanceof Ref) return (Ref) value;
        if (value == null) return NULL_REF;

        return new Ref(
                id,
                value.getClass().getName(),
                System.identityHashCode(value),
                value.toString()
        );
    }
}