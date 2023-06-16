package org.example.app.remote;

import org.example.shared.impl.RemoteCall;
import org.example.shared.impl.RemoteCallResult;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Invoker implements InvokerMBean {
    private final Map<Integer, WeakReference<Object>> adhocReferenceMap = new ConcurrentHashMap<>();
    private final Map<Integer, Session> sessions = new ConcurrentHashMap<>();

    private final AtomicInteger sessionIdSequence = new AtomicInteger(1);

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

final class Session {
    private final AtomicInteger refIdSequence = new AtomicInteger(1);
    private Map<Integer, Object> variables = new ConcurrentHashMap<>();
}