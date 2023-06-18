package org.example.app.remote;

import org.example.shared.Ref;
import org.example.shared.impl.*;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.shared.impl.RemoteCall.isPassByValue;

public class Invoker implements InvokerMBean {
    public static final int NO_SESSION_ID = 0;

    // todo store initial pluginId for each reference
    private final Map<Integer, Session> sessions = new ConcurrentHashMap<>();
    private final AtomicInteger sessionIdSequence = new AtomicInteger(1);

    private final Map<Integer, WeakReference<Object>> adhocReferenceMap = new ConcurrentHashMap<>();
    private final AtomicInteger adhocRefSequence = new AtomicInteger(1);

    @Override
    public RemoteCallResult invoke(RemoteCall call) {
        Object[] args = transformArgs(call);

        CallTarget callTarget = getCallTarget(call);

        Object instance = findInstance(call, callTarget.clazz());

        Object result;
        try {
            result = callTarget.targetMethod().invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Exception ", e);
        }

        if (isPassByValue(result)) {
            // does not need a session, pass by value
            return new RemoteCallResult(result);
        }

        // todo support allow list of simple type results, return them as plain value
        if (call.getSessionId() == NO_SESSION_ID) {
            int id = adhocRefSequence.getAndIncrement();
            Ref ref = RefProducer.makeRef(id, result);
            adhocReferenceMap.put(id, new WeakReference<>(result));

            if (result instanceof Collection<?>) {
                List<Ref> items = new ArrayList<>(((Collection<?>) result).size());
                for (Object item : ((Collection<?>) result)) {
                    int itemId = adhocRefSequence.getAndIncrement();
                    items.add(RefProducer.makeRef(itemId, item));
                }
                return new RemoteCallResult(new RefList(id, result.getClass().getName(), items));
            }

            return new RemoteCallResult(ref);
        } else {
            Session session = sessions.get(call.getSessionId());
            Ref ref = session.putReference(result);

            if (result instanceof Collection<?>) {
                List<Ref> items = new ArrayList<>(((Collection<?>) result).size());
                for (Object item : ((Collection<?>) result)) {
                    items.add(session.putReference(item));
                }
                return new RemoteCallResult(new RefList(ref.id(), result.getClass().getName(), items));
            }

            return new RemoteCallResult(ref);
        }
    }

    private @NotNull CallTarget getCallTarget(RemoteCall call) {
        Class<?> clazz;
        try {
            clazz = getClass().getClassLoader().loadClass(call.getClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No such class" + call.getClassName(), e);
        }

        Method targetMethod;
        try {
            targetMethod = Arrays.stream(clazz.getMethods())
                    .filter(m -> m.getName().equals(call.getMethodName()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException("No method by name")); // todo take into account argument types
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No method " + call.getMethodName() + " in class " + call.getClassName(), e);
        }

        return new CallTarget(clazz, targetMethod);
    }

    private Object findInstance(RemoteCall call, Class<?> clazz) {
        if (call instanceof ServiceCall) {
            Object instance;
            try {
                Method instanceGetter = clazz.getDeclaredMethod("getInstance");
                instance = instanceGetter.invoke(null);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("No getInstance() in class" + call.getClassName(), e);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Unable to get service instance", e);
            }
            return instance;
        }

        if (call instanceof RefCall) {
            Ref ref = ((RefCall) call).getRef();
            Object reference = getReference(call.getSessionId(), ref.id());

            if (reference == null) throw new IllegalStateException("No such ref exists " + ref);

            return reference;
        }

        if (call instanceof UtilityCall) {
            return null; // static call
        }

        throw new UnsupportedOperationException("Unsupported call type " + call);
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

        WeakReference<Object> reference = adhocReferenceMap.get(id);
        Object weakTarget = reference.get();
        if (weakTarget == null) throw new IllegalStateException("Weak Ref expired " + id);
        if (weakTarget == RefProducer.NULL_REF) return null;

        return weakTarget;
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

record CallTarget(Class<?> clazz, Method targetMethod) {
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