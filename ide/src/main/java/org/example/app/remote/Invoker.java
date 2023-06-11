package org.example.app.remote;

import org.example.shared.RemoteCall;
import org.example.shared.RemoteCallResult;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Invoker implements InvokerMBean {
    private final Map<Integer, WeakReference<Object>> weakReferenceMap = new ConcurrentHashMap<>();
    private final Map<Integer, Map<Integer, Object>> sessions = new ConcurrentHashMap<>();

    private final AtomicInteger sessionIdSequence = new AtomicInteger(1);
    private final AtomicInteger refIdSequence = new AtomicInteger(1);

    @Override
    public RemoteCallResult invoke(RemoteCall call) {
        // todo unmarshall all remote refs in args
        return new RemoteCallResult(null);
    }

    @Override
    public int newSession() {
        return sessionIdSequence.getAndIncrement();
    }

    @Override
    public void cleanup(int sessionId) {
        sessions.remove(sessionId);
    }
}
