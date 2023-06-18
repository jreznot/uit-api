package org.example.shared.impl;

import org.example.shared.LockSemantics;
import org.example.shared.OnDispatcher;

import java.io.Serial;

public final class NewInstanceCall extends RemoteCall {
    @Serial
    private static final long serialVersionUID = 1L;

    public NewInstanceCall(int sessionId,
                           String pluginId,
                           OnDispatcher dispatcher,
                           LockSemantics lockSemantics,
                           String className,
                           Object[] args) {
        super(sessionId, pluginId, dispatcher, lockSemantics, className, "new", args);
    }

    @Override
    public String toString() {
        return "NewInstanceCall{" +
                "className=" + getClassName() +
                '}';
    }
}
