package org.example.shared.impl;

import org.example.shared.LockSemantics;
import org.example.shared.OnDispatcher;
import org.example.shared.Ref;

import java.io.Serial;

public final class RefCall extends RemoteCall {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Ref ref;

    public RefCall(int sessionId,
                   String pluginId,
                   OnDispatcher dispatcher,
                   LockSemantics lockSemantics,
                   String className,
                   String methodName,
                   Object[] args,
                   Ref ref) {
        super(sessionId, pluginId, dispatcher, lockSemantics, className, methodName, args);

        this.ref = ref;
    }

    public Ref getRef() {
        return ref;
    }

    @Override
    public String toString() {
        return "RefCall{" +
                "className=" + getClassName() +
                "methodName=" + getMethodName() +
                "ref=" + ref +
                '}';
    }
}
