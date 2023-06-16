package org.example.shared.impl;

import org.example.shared.LockSemantics;
import org.example.shared.OnDispatcher;

import java.io.Serial;

public final class UtilityCall extends RemoteCall {
    @Serial
    private static final long serialVersionUID = 1L;

    public UtilityCall(int sessionId,
                       String pluginId,
                       OnDispatcher dispatcher,
                       LockSemantics lockSemantics,
                       String className,
                       String methodName,
                       Object[] args) {
        super(sessionId, pluginId, dispatcher, lockSemantics, className, methodName, args);
    }

    @Override
    public String toString() {
        return "UtilityCall{" +
                "className=" + getClassName() +
                "methodName=" + getMethodName() +
                '}';
    }
}
