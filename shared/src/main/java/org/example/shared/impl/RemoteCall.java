package org.example.shared.impl;

import org.example.shared.LockSemantics;
import org.example.shared.OnDispatcher;

import java.io.Serial;
import java.io.Serializable;

public abstract class RemoteCall implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int sessionId;
    private final String pluginId;

    private final OnDispatcher dispatcher;
    private final LockSemantics lockSemantics;
    private final String className;
    private final String methodName;
    private final Object[] args;

    public RemoteCall(int sessionId,
                      String pluginId,
                      OnDispatcher dispatcher,
                      LockSemantics lockSemantics,
                      String className,
                      String methodName,
                      Object[] args) {
        this.sessionId = sessionId;
        this.pluginId = pluginId;
        this.dispatcher = dispatcher;
        this.lockSemantics = lockSemantics;
        this.className = className;
        this.methodName = methodName;
        this.args = args;
    }

    public int getSessionId() {
        return sessionId;
    }

    public OnDispatcher getDispatcher() {
        return dispatcher;
    }

    public LockSemantics getLockSemantics() {
        return lockSemantics;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public String getPluginId() {
        return pluginId;
    }

    public static boolean isPassByValue(Object result) {
        // does not need a session, pass by value
        return result == null
                || result instanceof String
                || result instanceof Boolean
                || result instanceof Integer
                || result instanceof Long
                || result instanceof Byte
                || result instanceof Short
                || result instanceof Double
                || result instanceof Float;
    }
}